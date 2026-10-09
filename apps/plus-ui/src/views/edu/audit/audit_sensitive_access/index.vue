<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有敏感数据访问记录的查看权限" />
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
        title="掩码展示不记录日志；只有揭示全量或申请明文导出时才记录。日志本身不含敏感字段明文，只记字段名与掩码后的值。"
      />

      <el-table v-loading="loading" border :data="accessList">
        <el-table-column label="访问时间" prop="operateTime" width="170" data-layout-group="访问信息" />
        <el-table-column label="查看人" prop="operator" width="120" data-layout-group="访问信息" />
        <el-table-column label="角色" prop="operatorRole" width="120" data-layout-group="访问信息" />
        <el-table-column label="对象" prop="objectId" min-width="160" :show-overflow-tooltip="true" data-layout-group="对象信息" />
        <el-table-column label="敏感字段" prop="fieldName" width="140" data-layout-group="对象信息" />
        <el-table-column label="访问方式" prop="accessType" width="120" align="center" data-layout-group="访问信息" />
        <el-table-column label="用途说明" prop="purpose" min-width="200" data-layout-group="访问信息" />

        <template #empty>
          <el-empty description="暂无敏感数据访问记录" />
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
import { listSensitiveAccess } from '@/api/edu/audit';
import type { OperationLogQuery, OperationLogVO } from '@/api/edu/audit/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAuditSensitiveAccess' });

const loading = ref(false);
const total = ref(0);
const accessList = ref<OperationLogVO[]>([]);

const queryParams = reactive<OperationLogQuery>({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['audit.log:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listSensitiveAccess({ ...queryParams });
    accessList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

onMounted(getList);
</script>
