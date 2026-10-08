<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有归档管理的查看权限" />
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
        title="日志保留不少于 3 年，超过在线保留窗口后按时间归档；归档后仍可按时间范围检索，归档动作本身写日志（REQ-AUD-032 / 033 / 034）。"
      />

      <el-table v-loading="loading" border :data="archiveList">
        <el-table-column label="归档批次" prop="batchNo" width="180" data-layout-group="归档信息" />
        <el-table-column label="归档范围" prop="archiveRange" min-width="220" data-layout-group="归档信息" />
        <el-table-column label="行数" prop="rowCount" width="100" align="center" data-layout-group="归档信息" />
        <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="归档信息" />
        <el-table-column label="操作人" prop="operator" width="120" data-layout-group="归档信息" />
        <el-table-column label="归档时间" prop="archiveTime" width="170" data-layout-group="归档信息" />

        <template #empty>
          <el-empty description="暂无归档批次" />
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
import { listArchiveBatch } from '@/api/edu/audit';
import type { OperationLogQuery, OperationLogVO } from '@/api/edu/audit/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAuditArchive' });

const loading = ref(false);
const total = ref(0);
const archiveList = ref<OperationLogVO[]>([]);
const queryParams = reactive<OperationLogQuery>({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['audit.log:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listArchiveBatch({ ...queryParams });
    archiveList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

onMounted(getList);
</script>
