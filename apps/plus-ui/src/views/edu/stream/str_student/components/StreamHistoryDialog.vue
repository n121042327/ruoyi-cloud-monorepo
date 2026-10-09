<template>
  <el-dialog v-model="visible" title="选科历史" width="760px" append-to-body>
    <el-table v-loading="loading" :data="list" max-height="400">
      <el-table-column label="变更类型" prop="changeType" width="130" data-layout-group="变更信息" />
      <el-table-column label="变更前组合" prop="beforeCombination" min-width="170" data-layout-group="变更信息" />
      <el-table-column label="变更后组合" prop="afterCombination" min-width="170" data-layout-group="变更信息" />
      <el-table-column label="生效日期" prop="effectiveDate" width="130" data-layout-group="变更信息" />
      <el-table-column label="操作人" prop="operator" width="110" data-layout-group="变更信息" />
      <el-table-column label="原因" prop="reason" min-width="160" data-layout-group="变更信息" />
      <template #empty>
        <el-empty description="暂无选科历史" />
      </template>
    </el-table>
    <div class="hint mt-2">选科历史可查不可改；截止后的变更以「变更申请 → 审批通过」两条记录留痕。</div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { listStreamHistory } from '@/api/edu/stream';
import type { StreamHistoryVO } from '@/api/edu/stream/types';

const visible = ref(false);
const loading = ref(false);
const list = ref<StreamHistoryVO[]>([]);

const open = async (query?: { studentId?: string; termId?: string }) => {
  visible.value = true;
  list.value = [];
  loading.value = true;
  try {
    const res = await listStreamHistory(query);
    list.value = res.rows ?? res.data ?? [];
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
