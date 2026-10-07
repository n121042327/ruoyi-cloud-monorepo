<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有运营访问记录的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['audit.log:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['audit.log:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
        </el-row>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="平台运营对本租户数据的任何访问都会形成访问记录；租户可自助查询与导出，导出内容只含本租户数据（REQ-AUD-013 / 015 / 016）。"
      />

      <el-table v-loading="loading" border :data="accessList">
        <el-table-column label="访问时间" prop="operateTime" width="170" data-layout-group="访问信息" />
        <el-table-column label="运营账号" prop="operator" width="130" data-layout-group="访问信息" />
        <el-table-column label="对象类型" prop="objectType" width="130" data-layout-group="对象信息" />
        <el-table-column label="对象标识" prop="objectId" min-width="180" :show-overflow-tooltip="true" data-layout-group="对象信息" />
        <el-table-column label="访问动作" prop="actionType" width="110" align="center" data-layout-group="访问信息" />
        <el-table-column label="用途说明" prop="purpose" min-width="200" data-layout-group="访问信息" />
        <el-table-column label="来源 IP" prop="sourceIp" width="140" data-layout-group="访问信息" />

        <template #empty>
          <el-empty description="本租户暂无运营访问记录" />
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
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { listOperatorAccess } from '@/api/edu/audit';
import type { OperationLogQuery, OperationLogVO } from '@/api/edu/audit/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAuditOpsAccess' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loading = ref(false);
const total = ref(0);
const accessList = ref<OperationLogVO[]>([]);

const queryParams = reactive<OperationLogQuery>({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['audit.log:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listOperatorAccess({ ...queryParams });
    accessList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

/** 导出只含本租户数据，导出行为本身写审计（REQ-AUD-016 / 029） */
const handleExport = () => {
  proxy?.download('edu/audit/operator-access/export', { ...queryParams }, `operator_access_${new Date().getTime()}.xlsx`);
};

onMounted(getList);
</script>
