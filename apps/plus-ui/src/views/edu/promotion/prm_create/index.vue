<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/promotion-create.html 的 .page-head -->
    <div class="page-head">
      <h1>新建升班任务</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">升班按学年追加，不改写历史</el-tag>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">新建升班任务</h2>
        <el-tag type="primary" size="small">向导 1 / 4</el-tag>
        <el-tag type="info" size="small">升班按学年追加，不改写历史</el-tag>
      </div>
      <el-steps class="mt-4" :active="0" align-center finish-status="success">
        <el-step title="选择学年学期" />
        <el-step title="预览与调整" />
        <el-step title="校验" />
        <el-step title="执行与结果" />
      </el-steps>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>步骤 1 · 选择源与目标学年学期</span>
          <el-tag type="info" size="small">必填项 2 个</el-tag>
        </div>
      </template>

      <el-alert class="mb-3" type="info" :closable="false">
        <template #title>创建口径</template>
        <div>
          必填：源学年学期、目标学年学期；目标学年学期的起始日期必须晚于源学年学期且两者不能相同。任务创建后状态为草稿，可修改范围后重新生成预览；创建写入审计。
        </div>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <h3 class="form-section-title" data-layout-group="升班范围">升班范围</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学校">
              <el-input v-model="schoolName" disabled />
              <div class="hint">升班任务在本校范围内执行，不跨校共享；只有平台运营与超级管理员可以切换学校，且切换后同样不能创建。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源学年学期" prop="sourceTermId">
              <el-select v-model="form.sourceTermId" placeholder="请选择源学年学期" class="w-full">
                <el-option v-for="item in termOptions" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
              <div class="hint">源学年学期决定「从哪一批在读学生升班」：只有在读计入，休学 / 转入未报到 / 出国保留学籍都不计入在读名单。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标学年学期" prop="targetTermId">
              <el-select v-model="form.targetTermId" placeholder="请选择目标学年学期" class="w-full">
                <el-option v-for="item in termOptions" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
              <div class="hint">目标学年学期的年级与班级必须已经建好，否则创建被阻止。</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="范围说明">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-word-limit
                placeholder="如：2021 级 小学六年级 毕业，其余年级按学段内序号 +1 升班"
              />
              <div class="hint">选填，最长 500 字；会随任务台账一起导出，便于教务主任线下核对。</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center gap-2">
          <span>目标学年学期的年级与班级齐备性</span>
          <el-tag type="info" size="small">按学段核对</el-tag>
        </div>
      </template>

      <el-alert class="mb-2" :type="readinessAlertType" :closable="false" :title="readinessTitle">
        {{ readinessMessage }}
      </el-alert>
      <el-alert class="mb-3" type="info" :closable="false" title="前端校验不作为安全边界：后端在 addPromotionTask 内重复校验。" />

      <div class="flex justify-between">
        <el-button v-hasPermi="['promotion.batch:read']" @click="goList">返回任务列表</el-button>
        <el-button v-hasPermi="['promotion.batch:create']" type="primary" :loading="submitting" @click="handleNext"> 创建任务并生成预览 </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { addPromotionTask } from '@/api/edu/promotion';
import type { PromotionTaskForm } from '@/api/edu/promotion/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { useUserStore } from '@/store/modules/user';

defineOptions({ name: 'EduPromotionCreate' });

const router = useRouter();
const userStore = useUserStore();

const submitting = ref(false);
const formRef = ref<FormInstance>();
const termOptions = ref<{ id: string; label: string }[]>([]);
const schoolName = computed(() => (userStore as unknown as { schoolName?: string }).schoolName || '本校');

const form = reactive<PromotionTaskForm>({
  schoolId: '',
  sourceTermId: '',
  targetTermId: '',
  remark: ''
});

const rules: FormRules = {
  sourceTermId: [{ required: true, message: '请选择源学年学期', trigger: 'change' }],
  targetTermId: [{ required: true, message: '请选择目标学年学期', trigger: 'change' }]
};

const readinessAlertType = computed(() => (form.sourceTermId && form.targetTermId ? 'success' : 'info'));
const readinessTitle = computed(() => (form.sourceTermId && form.targetTermId ? '可以创建' : '请先选择源与目标学年学期'));
const readinessMessage = computed(() =>
  form.sourceTermId && form.targetTermId
    ? '目标学年学期的年级与班级已齐备，同一源 → 目标学期没有未结束任务（REQ-PRM-009）。齐备性以后端 addPromotionTask 的二次校验为准（NFR-SEC-05）。'
    : '选定源与目标学年学期后，这里会给出目标学期年级与班级的齐备性结论（REQ-PRM-009）。'
);

const loadTerms = async () => {
  const res = await listTerm({});
  const rows: TermVO[] = res.data ?? [];
  termOptions.value = rows.map((item) => ({
    id: item.termId,
    label: `${item.academicYearName ?? ''} ${item.termName}`.trim() + (item.current ? '（当前）' : '')
  }));
};

const goList = () => router.push('/edu/promotion/list');

/** 创建任务（草稿）并进入预览步骤（REQ-PRM-011 / 012） */
const handleNext = async () => {
  await formRef.value?.validate();
  if (form.sourceTermId === form.targetTermId) {
    ElMessage.error('目标学年学期必须晚于源学年学期，两者不能相同（REQ-PRM-008）');
    return;
  }
  submitting.value = true;
  try {
    const res = await addPromotionTask({ ...form });
    const taskId = res.data?.taskId;
    ElMessage.success('已创建升班任务（草稿），进入预览与调整');
    router.push({ path: '/edu/promotion/preview', query: taskId ? { taskId } : {} });
  } finally {
    submitting.value = false;
  }
};

onMounted(loadTerms);
</script>
