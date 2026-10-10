<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/async-task-list.html 的 .page-head -->
    <div class="page-head">
      <h1>异步任务</h1>
      <span class="scope-hint">数据范围：本校 · 默认只看本人任务</span>
      <el-tag type="primary">记录保留 90 天</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有异步任务的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['data.async_task:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
        </el-row>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="默认只显示本人发起的任务；任务结果的查询与下载都会重新解析数据范围，不复用发起时的判定。"
      />

      <el-table v-loading="loading" border :data="taskList">
        <el-table-column label="任务编号" prop="taskNo" width="190" data-layout-group="任务信息" />
        <el-table-column label="类型" prop="taskType" width="120" data-layout-group="任务信息" />
        <el-table-column label="状态" prop="taskStatus" width="120" align="center" data-layout-group="任务信息">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.taskStatus)" size="small">{{ statusText(scope.row.taskStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" prop="progressPercent" width="120" data-layout-group="任务信息">
          <template #default="scope">
            <el-progress :percentage="Number(scope.row.progressPercent ?? 0)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="发起人" prop="ownerName" width="120" data-layout-group="任务信息" />
        <el-table-column label="发起时间" prop="createTime" width="170" data-layout-group="任务信息" />
        <el-table-column label="耗时" width="100" align="center" data-layout-group="任务信息">
          <template #default="scope">{{ durationText(scope.row.durationSeconds) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="270" data-layout-group="操作">
          <template #default="scope">
            <el-button
              v-if="scope.row.taskStatus === 'queued'"
              v-hasPermi="['data.async_task:update']"
              link
              type="primary"
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
            <el-button
              v-if="scope.row.taskStatus === 'failed' || scope.row.taskStatus === 'partial_failed'"
              v-hasPermi="['data.async_task:update']"
              link
              type="primary"
              @click="handleRetry(scope.row)"
            >
              重试
            </el-button>
            <el-button v-if="scope.row.resultFileId" link type="primary" @click="handleDownload(scope.row)">下载结果</el-button>
            <el-button link type="primary" @click="rowsRef?.open(scope.row.taskNo)">行明细</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无异步任务" />
        </template>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <TaskRowsDialog ref="rowsRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { cancelAsyncTask, downloadTaskResult, listAsyncTask, retryAsyncTask } from '@/api/edu/importExport';
import type { AsyncTaskVO } from '@/api/edu/importExport/types';
import TaskRowsDialog from './components/TaskRowsDialog.vue';
import { checkPermi } from '@/utils/permission';
import { downloadByFileRef } from '@/utils/eduFileRef';

defineOptions({ name: 'EduAsyncTaskList' });
const rowsRef = ref<InstanceType<typeof TaskRowsDialog>>();

const loading = ref(false);
const total = ref(0);
const taskList = ref<AsyncTaskVO[]>([]);
const queryParams = reactive({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['data.async_task:read']));

const statusText = (status?: string) =>
  ({ queued: '排队中', running: '执行中', succeeded: '已完成', partial_failed: '部分失败', failed: '失败' })[status ?? ''] ?? '执行中';
const statusTag = (status?: string) => (status === 'succeeded' ? 'success' : status === 'failed' ? 'danger' : 'warning');
/** 耗时展示：后端给的是秒数（EduAsyncTaskVo.durationSeconds） */
const durationText = (seconds?: number) => (seconds == null ? '—' : seconds < 60 ? `${seconds}s` : `${Math.floor(seconds / 60)}m${seconds % 60}s`);

const getList = async () => {
  loading.value = true;
  try {
    const res = await listAsyncTask({ ...queryParams });
    taskList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (row: AsyncTaskVO) => {
  await ElMessageBox.confirm(`确认取消排队中的任务 ${row.taskNo}？`, '取消任务', {
    confirmButtonText: '确认取消',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await cancelAsyncTask(row.taskNo);
  ElMessage.success('已取消任务');
  await getList();
};

const handleRetry = async (row: AsyncTaskVO) => {
  await ElMessageBox.confirm(`确认重试任务 ${row.taskNo}？重试沿用原批次号与幂等键，不会产生重复数据。`, '重试任务', {
    confirmButtonText: '确认重试',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await retryAsyncTask(row.taskNo);
  ElMessage.success('已提交重试');
  await getList();
};

const handleDownload = async (row: AsyncTaskVO) => {
  if (!row.resultFileId) return;
  const res = await downloadTaskResult(row.taskNo, row.resultFileId);
  downloadByFileRef(res.data, `task_result_${row.taskNo}.xlsx`);
};

onMounted(getList);
</script>
