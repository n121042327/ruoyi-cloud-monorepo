<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">执行结果与重试</h2>
        <el-tag :type="statusType" size="small">{{ statusLabel }}</el-tag>
        <el-tag type="info" size="small">按学年追加，不改写历史（BR-PROMO-001）</el-tag>
      </div>
      <div class="mt-2 text-xs">
        任务：{{ task?.taskNo || '—' }} · 源学年学期：{{ task?.sourceTermName || '—' }} → 目标学年学期：{{ task?.targetTermName || '—' }}
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-row :gutter="16">
        <el-col :span="6"><el-statistic title="学生总数" :value="task?.totalCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="成功" :value="task?.successCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="失败" :value="task?.failedCount ?? 0" /></el-col>
        <el-col :span="6">
          <div class="flex gap-2 mt-4">
            <el-button v-hasPermi="['promotion.batch:update']" :disabled="!canRetry" type="primary" :loading="retrying" @click="handleRetry">
              重试失败项
            </el-button>
            <el-button v-hasPermi="['data.export:export']" @click="handleExport">结果导出</el-button>
          </div>
        </el-col>
      </el-row>
      <el-alert v-if="task?.failReason" class="mt-3" type="warning" :closable="false" title="失败原因" :description="task.failReason" />
      <el-alert
        class="mt-3"
        type="info"
        :closable="false"
        title="失败逐条记录原因，重试只处理失败项，已成功的记录不重复执行（REQ-PRM-032 / BR-PROMO-003）。"
      />
    </el-card>

    <el-card shadow="hover">
      <el-table v-loading="itemLoading" border :data="items">
        <el-table-column label="学号" prop="studentNo" width="150" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
        <el-table-column label="源班级" prop="sourceClassName" min-width="170" data-layout-group="学生信息" />
        <el-table-column label="结果类型" prop="resultType" width="110" align="center" data-layout-group="结果信息">
          <template #default="scope">{{ resultTypeLabel(scope.row.resultType) }}</template>
        </el-table-column>
        <el-table-column label="目标班级 / 去向" prop="targetClassName" min-width="180" data-layout-group="结果信息" />
        <el-table-column label="失败原因" prop="errorMsg" min-width="210" data-layout-group="结果信息" />
        <el-table-column fixed="right" label="操作" width="110" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="handleRetryRow(scope.row)"> 重试本项 </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有结果明细" />
        </template>
      </el-table>

      <div class="flex justify-between mt-3">
        <el-button v-hasPermi="['promotion.batch:update']" @click="goExecute">查看执行进度</el-button>
        <el-button @click="goList">返回任务列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { exportPromotionResult, getPromotionTask, listPromotionItem, retryPromotionTask } from '@/api/edu/promotion';
import type { PromotionItemVO, PromotionTaskVO } from '@/api/edu/promotion/types';
import { RESULT_TYPE_LABELS } from '@/views/edu/promotion/prm_preview/composables/usePromotionPreview';
import { PROMOTION_STATUS_LABELS, PROMOTION_STATUS_TYPES } from '@/views/edu/promotion/prm_list/composables/usePromotionTaskList';

defineOptions({ name: 'EduPromotionResult' });

const route = useRoute();
const router = useRouter();
const taskId = String(route.query.taskId ?? '');

const loading = ref(false);
const itemLoading = ref(false);
const retrying = ref(false);
const items = ref<PromotionItemVO[]>([]);
const task = ref<PromotionTaskVO>();

const statusLabel = computed(() => (task.value?.status ? (PROMOTION_STATUS_LABELS[task.value.status] ?? task.value.status) : '—'));
const statusType = computed(() => (task.value?.status ? (PROMOTION_STATUS_TYPES[task.value.status] ?? 'info') : 'info'));
const canRetry = computed(() => ['partial_failed', 'failed', 'cancelled', 'running'].includes(task.value?.status ?? ''));
const resultTypeLabel = (value?: string) => (value ? (RESULT_TYPE_LABELS[value] ?? value) : '—');

const loadAll = async () => {
  loading.value = true;
  itemLoading.value = true;
  try {
    const res = await getPromotionTask(taskId);
    task.value = res.data;
    const itemRes = await listPromotionItem(taskId);
    items.value = itemRes.data ?? [];
  } finally {
    loading.value = false;
    itemLoading.value = false;
  }
};

const goList = () => router.push('/edu/promotion/list');
const goExecute = () => router.push({ path: '/edu/promotion/execute', query: { taskId } });

/** 重试失败项 / 继续执行剩余项（REQ-PRM-032 / 036） */
const handleRetry = async () => {
  await ElMessageBox.confirm('重试只重新处理失败项，已成功的记录不重复执行，确认继续？', '重试失败项', {
    confirmButtonText: '确认重试',
    cancelButtonText: '取消',
    type: 'warning'
  });
  retrying.value = true;
  try {
    await retryPromotionTask(taskId, '结果页发起重试');
    ElMessage.success('已提交重试，请稍后刷新查看结果');
    await loadAll();
  } finally {
    retrying.value = false;
  }
};

const handleRetryRow = async (row: PromotionItemVO) => {
  await retryPromotionTask(taskId, `结果页重试单个明细：${row.studentNo ?? ''}`);
  ElMessage.success('已提交该项重试');
  await loadAll();
};

/** 结果导出：成功清单 / 失败清单 / 留级清单 / 毕业清单（REQ-PRM-034） */
const handleExport = async () => {
  await exportPromotionResult(taskId);
  ElMessage.success('已生成结果报告（成功 / 失败 / 留级 / 毕业清单），开始下载');
};

onMounted(() => {
  if (!taskId) {
    ElMessage.warning('缺少任务上下文，请从升班任务列表进入');
    return;
  }
  loadAll();
});
</script>
