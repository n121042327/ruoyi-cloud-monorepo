<template>
  <el-drawer v-model="visible" size="640px" :title="`操作日志详情 · ${detail.actionType || ''}`" append-to-body>
    <div v-loading="loading">
      <el-alert type="info" :closable="false" title="日志只允许追加、不可修改与删除；列表与详情都不提供编辑入口（REQ-AUD-025 / 030）。" />

      <!-- 操作信息：操作人 → 角色快照 → 操作类型 → 执行结果 → 来源 IP → 请求标识 -->
      <h4 class="form-section-title" data-layout-group="操作信息">操作信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ detail.operator || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作人角色快照">{{ detail.operatorRole || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detail.actionType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="执行结果">{{ detail.result === 'failed' ? '失败' : '成功' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operateTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源 IP">{{ detail.sourceIp || '—' }}</el-descriptions-item>
        <el-descriptions-item label="请求标识">{{ detail.requestId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ detail.batchNo || '—' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 对象信息：对象类型 → 对象标识 → 租户 / 学校 -->
      <h4 class="form-section-title" data-layout-group="对象信息">对象信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="对象类型">{{ detail.objectType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="对象标识">{{ detail.objectId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="租户 / 学校">{{ detail.tenantId || '—' }} / {{ detail.schoolId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="用途说明">{{ detail.purpose || '—' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 变更明细：只列发生变化的字段，含变更前值与变更后值（REQ-AUD-003） -->
      <h4 class="form-section-title" data-layout-group="变更明细">变更明细</h4>
      <el-table :data="detail.changes ?? []" border>
        <el-table-column label="字段" prop="fieldLabel" width="150">
          <template #default="scope">{{ scope.row.fieldLabel || scope.row.fieldName }}</template>
        </el-table-column>
        <el-table-column label="变更前" prop="beforeValue" min-width="150">
          <template #default="scope">{{ scope.row.beforeValue ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="变更后" prop="afterValue" min-width="150">
          <template #default="scope">{{ scope.row.afterValue ?? '—' }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="本条日志没有字段级变更" :image-size="60" />
        </template>
      </el-table>

      <div class="hint mt-2">日志本身不含敏感字段明文，只记录字段名与掩码后的值（REQ-AUD-010）。</div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { getOperationLog } from '@/api/edu/audit';
import type { OperationLogVO } from '@/api/edu/audit/types';

const visible = ref(false);
const loading = ref(false);
const detail = ref<Partial<OperationLogVO>>({});

/** 打开抽屉：先用列表行填充，再取详情拿变更明细 */
const open = async (row: OperationLogVO) => {
  visible.value = true;
  detail.value = { ...row };
  loading.value = true;
  try {
    const res = await getOperationLog(row.logId);
    detail.value = { ...row, ...(res.data ?? {}) };
  } catch {
    detail.value = { ...row };
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
