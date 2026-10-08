<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">执行与进度</h2>
        <el-tag type="primary" size="small">向导 4 / 4</el-tag>
        <el-tag type="info" size="small">执行异步进行，可离开页面，稍后回结果页查看</el-tag>
      </div>
      <div class="mt-2 text-xs">任务：{{ task?.taskNo || '—' }} · 目标学年学期：{{ task?.targetTermName || '—' }}</div>
      <el-steps class="mt-4" :active="3" align-center finish-status="success">
        <el-step title="选择学年学期" />
        <el-step title="预览与调整" />
        <el-step title="校验" />
        <el-step title="执行与结果" />
      </el-steps>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>执行进度</span>
          <el-button :loading="loading" @click="loadAll">刷新进度</el-button>
        </div>
      </template>

      <el-progress :percentage="percent" :status="progressStatus" :stroke-width="16" />
      <el-row :gutter="16" class="mt-4">
        <el-col :span="6"><el-statistic title="学生总数" :value="task?.totalCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="成功" :value="task?.successCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="失败" :value="task?.failedCount ?? 0" /></el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">任务状态</div>
          <el-tag class="mt-1" :type="statusType" size="small">{{ statusLabel }}</el-tag>
        </el-col>
      </el-row>

      <el-alert v-if="isRunning" class="mt-3" type="info" :closable="false" title="执行中：可离开本页；完成后到结果页查看失败项与重试入口。" />
      <el-alert
        v-else-if="task?.status === 'partial_failed'"
        class="mt-3"
        type="warning"
        :closable="false"
        title="部分失败：到结果页点「重试失败项」，只重新处理失败项，已成功记录不重复执行（REQ-PRM-032）。"
      />
      <el-alert v-else-if="task?.status === 'failed'" class="mt-3" type="error" :closable="false" title="执行失败：到结果页查看失败原因并重试。" />
      <el-alert
        v-else-if="task?.status === 'succeeded'"
        class="mt-3"
        type="success"
        :closable="false"
        title="执行完成：按学年追加，不改写历史（BR-PROMO-001）。"
      />
    </el-card>

    <el-card shadow="hover">
      <div class="flex justify-between">
        <el-button @click="goList">返回任务列表</el-button>
        <el-button v-hasPermi="['promotion.batch:read']" type="primary" @click="goResult">查看结果与重试</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getPromotionTask } from '@/api/edu/promotion';
import type { PromotionTaskVO } from '@/api/edu/promotion/types';
import { PROMOTION_STATUS_LABELS, PROMOTION_STATUS_TYPES } from '@/views/edu/promotion/prm_list/composables/usePromotionTaskList';

defineOptions({ name: 'EduPromotionExecute' });

const route = useRoute();
const router = useRouter();
const taskId = String(route.query.taskId ?? '');

const loading = ref(false);
const task = ref<PromotionTaskVO>();

const statusLabel = computed(() => (task.value?.status ? (PROMOTION_STATUS_LABELS[task.value.status] ?? task.value.status) : '—'));
const statusType = computed(() => (task.value?.status ? (PROMOTION_STATUS_TYPES[task.value.status] ?? 'info') : 'info'));
const isRunning = computed(() => ['validating', 'running'].includes(task.value?.status ?? ''));
const percent = computed(() => {
  const total = task.value?.totalCount ?? 0;
  if (!total) return task.value?.status === 'succeeded' ? 100 : 0;
  const done = (task.value?.successCount ?? 0) + (task.value?.failedCount ?? 0);
  return Math.min(100, Math.round((done / total) * 100));
});
const progressStatus = computed(() => {
  if (task.value?.status === 'failed') return 'exception';
  if (task.value?.status === 'succeeded') return 'success';
  if (task.value?.status === 'partial_failed') return 'warning';
  return undefined;
});

const loadAll = async () => {
  loading.value = true;
  try {
    const res = await getPromotionTask(taskId);
    task.value = res.data;
  } finally {
    loading.value = false;
  }
};

const goList = () => router.push('/edu/promotion/list');
const goResult = () => router.push({ path: '/edu/promotion/result', query: { taskId } });

onMounted(() => {
  if (!taskId) {
    ElMessage.warning('缺少任务上下文，请从升班任务列表进入');
    return;
  }
  loadAll();
});
</script>
