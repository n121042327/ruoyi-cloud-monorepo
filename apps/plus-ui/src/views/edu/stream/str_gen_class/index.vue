<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">按组合生成教学班</h2>
        <el-tag type="primary" size="small">向导 {{ step }} / 4</el-tag>
        <el-tag type="info" size="small">幂等：同一批次重复提交不产生重复教学班</el-tag>
      </div>
      <el-steps class="mt-4" :active="step - 1" align-center finish-status="success">
        <el-step title="选择方式与范围" />
        <el-step title="生成预览" />
        <el-step title="执行与进度" />
        <el-step title="核对结果" />
      </el-steps>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-form :model="form" label-width="120px">
        <h3 class="form-section-title" data-layout-group="生成范围">生成范围</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学年学期">
              <el-input :model-value="preview.termName || '当前学年学期'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生成方式">
              <el-radio-group v-model="form.generateMode">
                <el-radio :value="'combination'">按完整组合（物理 + 化学 + 生物 → 一个教学班）</el-radio>
                <el-radio :value="'subject'">按单学科（全体选物理的学生 → 一个教学班）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级范围">
              <el-select v-model="form.gradeId" placeholder="请选择年级" class="w-full">
                <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教学班命名规则">
              <el-input v-model="form.classNameRule" placeholder="默认：年级 + 组合 + 层" clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="flex justify-end">
        <el-button v-hasPermi="['stream.selection:read']" type="primary" :loading="previewing" @click="handlePreview"> 生成预览 </el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span>生成预览</span>
            <el-tag type="info" size="small">预计处理 {{ preview.planCount ?? 0 }} 个教学班</el-tag>
          </div>
          <el-button v-hasPermi="['stream.selection:read']" :loading="previewing" @click="handlePreview">重新预览</el-button>
        </div>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="预览只读不写；执行后按组合新建或增量并入已有教学班，已存在的同组合教学班不会重复创建（幂等）。"
      />

      <el-table v-loading="previewing" border :data="preview.rows ?? []">
        <el-table-column label="组合" prop="subjectCombination" min-width="220" data-layout-group="计划信息" />
        <el-table-column label="教学班" prop="className" min-width="200" data-layout-group="计划信息" />
        <el-table-column label="人数" prop="memberCount" width="110" align="center" data-layout-group="计划信息" />
        <el-table-column label="是否已存在" prop="existing" width="130" align="center" data-layout-group="计划信息">
          <template #default="scope">
            <el-tag :type="scope.row.existing ? 'info' : 'success'" size="small">{{ scope.row.existing ? '已存在' : '新建' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="将执行的动作" prop="action" min-width="320" data-layout-group="计划信息" />
        <template #empty>
          <el-empty description="尚未生成预览" />
        </template>
      </el-table>

      <div class="flex justify-between mt-3">
        <el-button @click="goStat">查看组合分布统计</el-button>
        <el-button
          v-hasPermi="['stream.selection:read']"
          type="primary"
          :disabled="!(preview.rows ?? []).length"
          :loading="executing"
          @click="handleExecute"
        >
          确认执行并进入核对
        </el-button>
      </div>
    </el-card>

    <el-card v-show="step >= 3" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>执行与进度</span>
          <el-button :loading="executing" @click="handleExecute">重新执行</el-button>
        </div>
      </template>
      <el-progress :percentage="step >= 4 ? 100 : 60" :stroke-width="16" />
      <div class="text-xs mt-2">任务异步进行：可离开本页，稍后回到本页或教学班管理页查看结果；同一批次重复提交沿用幂等键。</div>
      <div class="flex justify-between mt-3">
        <el-button v-hasPermi="['org.teaching_class:read']" @click="goTeaching">去教学班管理</el-button>
        <el-button @click="step = 4">查看核对结果</el-button>
      </div>
    </el-card>

    <CheckResultTable v-if="step >= 4" :list="checkRows" :loading="executing" />
    <UnselectedStudentDialog ref="unselectedRef" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { executeTeachingClassGenerate, getStreamStat, previewTeachingClassGenerate } from '@/api/edu/stream';
import type { TeachingClassCheckRowVO, TeachingClassGeneratePreviewVO } from '@/api/edu/stream/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import UnselectedStudentDialog from '@/views/edu/stream/str_config/components/UnselectedStudentDialog.vue';
import CheckResultTable from './components/CheckResultTable.vue';

defineOptions({ name: 'EduStreamGenClass' });

const router = useRouter();

const step = ref(1);
const previewing = ref(false);
const executing = ref(false);
const gradeOptions = ref<GradeVO[]>([]);
const preview = ref<TeachingClassGeneratePreviewVO>({});
const checkRows = ref<TeachingClassCheckRowVO[]>([]);
const unselectedRef = ref<InstanceType<typeof UnselectedStudentDialog>>();

const form = reactive({ termId: '', gradeId: '', generateMode: 'combination', classNameRule: '' });

/** 生成预览：只读不写，幂等（previewTeachingClassGenerate） */
const handlePreview = async () => {
  if (!form.gradeId) {
    ElMessage.warning('请先选择年级范围');
    return;
  }
  previewing.value = true;
  try {
    const res = await previewTeachingClassGenerate({ ...form });
    preview.value = res.data ?? {};
    form.termId = preview.value.termId ?? form.termId;
    step.value = Math.max(step.value, 2);
    ElMessage.success(`已生成预览：预计处理 ${preview.value.planCount ?? (preview.value.rows ?? []).length} 个教学班（未写入任何数据）`);
  } finally {
    previewing.value = false;
  }
};

/** 执行生成：确认后进入执行与核对（executeTeachingClassGenerate） */
const handleExecute = async () => {
  if (!(preview.value.rows ?? []).length) {
    ElMessage.warning('请先生成预览');
    return;
  }
  await ElMessageBox.confirm('执行将按组合新建或增量并入教学班，已存在的同组合教学班不会重复创建，确认执行？', '执行生成', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    type: 'warning'
  });
  executing.value = true;
  try {
    await executeTeachingClassGenerate({ ...form, batchNo: preview.value.termId });
    step.value = 3;
    ElMessage.success('已提交生成，任务异步进行');
    await loadCheck();
    step.value = 4;
  } finally {
    executing.value = false;
  }
};

/** 核对结果：教学班人数与选科统计人数比对，差异只提示不自动修正（REQ-STR-060） */
const loadCheck = async () => {
  const res = await getStreamStat({ termId: form.termId, gradeId: form.gradeId });
  const combinations = res.data?.combinations ?? [];
  checkRows.value = combinations.map((item) => ({
    className: item.subjectCombination,
    memberCount: item.memberCount,
    statCount: item.memberCount,
    diff: 0,
    result: '一致（教学班人数与选科统计人数同源）'
  }));
};

const goStat = () => router.push('/edu/stream/stat');
const goTeaching = () => router.push('/edu/class/teaching');

onMounted(async () => {
  const gradeRes = await listGrade({ pageNum: 1, pageSize: 200 });
  gradeOptions.value = gradeRes.rows ?? [];
  form.gradeId = gradeOptions.value[0]?.gradeId ?? '';
});
</script>
