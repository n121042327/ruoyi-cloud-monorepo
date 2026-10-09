<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有死信任务的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['data.async_task:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
        </el-row>
      </template>

      <el-alert class="mb-3" type="warning" :closable="false" title="超过最大重试次数的任务进入死信；重放动作写入审计。" />

      <el-table v-loading="loading" border :data="deadList">
        <el-table-column label="任务编号" prop="taskNo" width="190" data-layout-group="死信信息" />
        <el-table-column label="类型" prop="taskType" width="120" data-layout-group="死信信息" />
        <el-table-column label="进入死信时间" prop="deadTime" width="170" data-layout-group="死信信息" />
        <el-table-column label="重试次数" prop="retryCount" width="100" align="center" data-layout-group="死信信息" />
        <el-table-column label="最后错误" prop="lastError" min-width="220" :show-overflow-tooltip="true" data-layout-group="死信信息" />
        <el-table-column label="原批次号" prop="batchNo" width="180" data-layout-group="死信信息" />
        <el-table-column fixed="right" label="操作" width="120" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['data.async_task:update']" link type="primary" @click="handleReplay(scope.row)">重放</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无死信任务" />
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listDeadLetterTask, replayDeadLetterTask } from '@/api/edu/importExport';
import type { AsyncTaskVO } from '@/api/edu/importExport/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAsyncDeadLetter' });

const loading = ref(false);
const total = ref(0);
const deadList = ref<AsyncTaskVO[]>([]);
const queryParams = reactive({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['data.async_task:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listDeadLetterTask({ ...queryParams });
    deadList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

/** 重放：二次确认（危险操作），重放动作本身写审计 */
const handleReplay = async (row: AsyncTaskVO) => {
  await ElMessageBox.confirm(`确认重放死信任务 ${row.taskNo}？重放动作会写入审计。`, '重放确认', {
    confirmButtonText: '确认重放',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await replayDeadLetterTask(row.taskNo);
  ElMessage.success('已提交重放');
  await getList();
};

onMounted(getList);
</script>
