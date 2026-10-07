<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有安全事件的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['audit.log:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
        </el-row>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="登录失败、账号锁定、激活码查看与重置、学号变更、权限变更都记入安全事件（REQ-AUD-007）；登录失败按学号限流，连续 5 次锁定 10 分钟（REQ-STU-091）。"
      />

      <el-table v-loading="loading" border :data="eventList">
        <el-table-column label="时间" prop="operateTime" width="170" data-layout-group="事件信息" />
        <el-table-column label="事件类型" prop="eventType" width="140" data-layout-group="事件信息" />
        <el-table-column label="账号" prop="account" width="140" data-layout-group="事件信息" />
        <el-table-column label="相关人" prop="operator" width="120" data-layout-group="事件信息" />
        <el-table-column label="结果" prop="result" width="100" align="center" data-layout-group="事件信息">
          <template #default="scope">
            <el-tag :type="scope.row.result === 'failed' ? 'danger' : 'success'" size="small">
              {{ scope.row.result === 'failed' ? '失败' : '成功' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源 IP" prop="sourceIp" width="140" data-layout-group="事件信息" />
        <el-table-column label="说明" prop="detail" min-width="220" :show-overflow-tooltip="true" data-layout-group="事件信息" />

        <template #empty>
          <el-empty description="暂无安全事件" />
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
import { listSecurityEvent } from '@/api/edu/audit';
import type { OperationLogQuery, OperationLogVO } from '@/api/edu/audit/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAuditSecurityEvent' });

const loading = ref(false);
const total = ref(0);
const eventList = ref<OperationLogVO[]>([]);
const queryParams = reactive<OperationLogQuery>({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['audit.log:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listSecurityEvent({ ...queryParams });
    eventList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

onMounted(getList);
</script>
