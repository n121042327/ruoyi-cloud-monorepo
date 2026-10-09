<template>
  <div class="p-2" v-loading="loading">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/promotion-preview.html 的 .page-head -->
    <div class="page-head">
      <h1>升班预览与调整</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">预览与调整</h2>
        <el-tag type="primary" size="small">向导 2 / 4</el-tag>
        <el-tag type="info" size="small">预览不写入任何学生数据</el-tag>
      </div>
      <div class="mt-2 text-xs">
        任务：{{ task?.taskNo || '—' }} · 源学年学期：{{ task?.sourceTermName || '—' }} → 目标学年学期：{{ task?.targetTermName || '—' }}
      </div>
      <el-steps class="mt-4" :active="1" align-center finish-status="success">
        <el-step title="选择学年学期" />
        <el-step title="预览与调整" />
        <el-step title="校验" />
        <el-step title="执行与结果" />
      </el-steps>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span>源班级（在读人数）· 可整体指定目标班级</span>
            <el-tag type="info" size="small">已调整 {{ adjustedCount }} 人</el-tag>
          </div>
          <div class="flex gap-2">
            <el-button v-hasPermi="['promotion.batch:update']" :loading="loading" @click="handlePreview">重新生成预览</el-button>
            <el-button v-hasPermi="['data.export:export']" @click="handleExportPreview">导出预览</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!sourceClasses.length" description="尚未生成预览明细，请先点「重新生成预览」" />
      <el-row v-else :gutter="12">
        <el-col v-for="group in sourceClasses" :key="group.sourceClassId" :span="8" class="mb-2">
          <el-card shadow="never">
            <div class="text-sm font-medium">{{ group.sourceClassName }}</div>
            <div class="text-xs text-gray-500 mt-1">在读 {{ group.count }} 人 · 已调整 {{ group.adjusted }} 人</div>
            <div class="flex gap-2 mt-2">
              <el-select v-model="group.targetClassId" placeholder="选择目标班级" class="grow">
                <el-option label="留空（升班时按规则自动匹配）" value="" />
                <el-option v-for="item in targetClassOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button v-hasPermi="['promotion.batch:update']" @click="applyToClass(group)">应用到本班</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span>升班明细{{ sourceFilter ? `（仅看 ${sourceClassName(sourceFilter)}）` : '' }}</span>
          <el-button v-if="sourceFilter" link type="primary" @click="sourceFilter = ''">查看全部明细</el-button>
        </div>
      </template>

      <el-table v-loading="itemLoading" border :data="filteredItems">
        <el-table-column label="学号" prop="studentNo" width="150" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
        <el-table-column label="源班级" prop="sourceClassName" min-width="180" data-layout-group="学生信息" />
        <el-table-column label="结果类型" prop="resultType" width="110" align="center" data-layout-group="去向信息">
          <template #default="scope">{{ resultTypeLabel(scope.row.resultType) }}</template>
        </el-table-column>
        <el-table-column label="目标班级 / 去向" prop="targetClassName" min-width="200" data-layout-group="去向信息" />
        <el-table-column label="明细状态" prop="status" width="120" align="center" data-layout-group="去向信息">
          <template #default="scope">{{ itemStatusLabel(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="110" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="handleAdjust(scope.row)">调整</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有预览明细" />
        </template>
      </el-table>
    </el-card>

    <el-card shadow="hover" class="mt-2">
      <div class="flex justify-between">
        <el-button @click="goCreate">上一步：选择学年学期</el-button>
        <div class="flex gap-2">
          <el-button @click="goList">返回任务列表（只读）</el-button>
          <el-button v-hasPermi="['promotion.batch:update']" type="primary" :loading="validating" @click="handleValidate">
            下一步：升班校验
          </el-button>
        </div>
      </div>
    </el-card>

    <AdjustItemDialog ref="adjustRef" @success="loadItems" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { exportPromotionPreview, getPromotionTask, validatePromotionTask } from '@/api/edu/promotion';
import type { PromotionItemVO } from '@/api/edu/promotion/types';
import { usePromotionPreview } from './composables/usePromotionPreview';
import AdjustItemDialog from './components/AdjustItemDialog.vue';

defineOptions({ name: 'EduPromotionPreview' });

const route = useRoute();
const router = useRouter();
const taskId = String(route.query.taskId ?? '');
const validating = ref(false);
const adjustRef = ref<InstanceType<typeof AdjustItemDialog>>();

const {
  loading,
  itemLoading,
  task,
  sourceFilter,
  filteredItems,
  sourceClasses,
  adjustedCount,
  loadItems,
  loadTask,
  handlePreview,
  applyToClass,
  resultTypeLabel,
  itemStatusLabel
} = usePromotionPreview(taskId);

/**
 * 目标班级候选：来自目标学年学期的班级。
 * 上游未提供「按目标学期取班级」的专用接口，先用 getPromotionTask 返回的候选兜底；
 * 若为空，列表留空表示升班时按规则自动匹配（REQ-PRM-009 的齐备性由后端校验）。
 */
const targetClassOptions = ref<{ label: string; value: string }[]>([]);

const sourceClassName = (id: string) => sourceClasses.value.find((item) => item.sourceClassId === id)?.sourceClassName ?? id;

const goCreate = () => router.push('/edu/promotion/create');
const goList = () => router.push('/edu/promotion/list');
const goValidate = () => router.push({ path: '/edu/promotion/validate', query: { taskId } });

const handleAdjust = (row: PromotionItemVO) => {
  adjustRef.value?.open(taskId, row);
};

const handleExportPreview = async () => {
  await exportPromotionPreview(taskId);
  ElMessage.success('已生成预览明细，开始下载');
};

/** 校验通过才允许执行（REQ-PRM-027） */
const handleValidate = async () => {
  validating.value = true;
  try {
    await validatePromotionTask(taskId);
    ElMessage.success('已提交升班校验，进入校验结果页');
    goValidate();
  } finally {
    validating.value = false;
  }
};

onMounted(() => {
  if (!taskId) {
    ElMessage.warning('缺少任务上下文，请从升班任务列表进入');
    return;
  }
  loadTask(async () => (await getPromotionTask(taskId)).data);
});

defineExpose({ loadItems });
</script>
