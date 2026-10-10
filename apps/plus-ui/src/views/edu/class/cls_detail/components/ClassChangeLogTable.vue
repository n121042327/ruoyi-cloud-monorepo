<template>
  <el-table v-loading="loading" :data="changes">
    <el-table-column label="操作时间" prop="operateTime" width="180" data-layout-group="变更信息" />
    <el-table-column label="操作人" prop="operator" width="120" data-layout-group="变更信息" />
    <el-table-column label="操作类型" prop="actionType" width="110" align="center" data-layout-group="变更信息" />
    <el-table-column label="结果" prop="result" width="90" align="center" data-layout-group="变更信息" />
    <el-table-column label="变更明细" min-width="260" show-overflow-tooltip data-layout-group="变更信息">
      <template #default="scope">{{ changeSummary(scope.row) }}</template>
    </el-table-column>
    <template #empty>
      <el-empty description="该班级暂无变更记录" />
    </template>
  </el-table>
</template>

<script setup lang="ts">
/**
 * 班级详情的「变更记录」表。
 *
 * 变更记录来自审计模块（只读、不可删除，BR-AUDIT-003）；单独成组件的原因同 TeachingAssignmentTable。
 */
defineOptions({ name: 'ClassChangeLogTable' });

defineProps<{
  loading: boolean;
  changes: Array<{
    operateTime?: string;
    operator?: string;
    actionType?: string;
    result?: string;
    detail?: string;
    changes?: Array<{ fieldName?: string; fieldLabel?: string; beforeValue?: string; afterValue?: string }>;
  }>;
}>();

/** 变更明细摘要：优先展开字段级变更，否则回退到 detail / 动作类型 */
const changeSummary = (row: {
  detail?: string;
  actionType?: string;
  changes?: Array<{ fieldName?: string; fieldLabel?: string; beforeValue?: string; afterValue?: string }>;
}) => {
  if (row.changes?.length) {
    return row.changes.map((c) => `${c.fieldLabel || c.fieldName}：${c.beforeValue ?? '空'} → ${c.afterValue ?? '空'}`).join('；');
  }
  return row.detail || row.actionType || '—';
};
</script>
