<template>
  <el-card shadow="hover" class="mt-2">
    <template #header>
      <div class="flex items-center gap-2">
        <span>选科变更记录</span>
        <el-tag type="info" size="small">可查不可改</el-tag>
      </div>
    </template>
    <el-timeline v-loading="loading">
      <el-timeline-item
        v-for="item in list"
        :key="item.historyId"
        :timestamp="item.effectiveDate || ''"
        placement="top"
        :type="item.changeType === '审批通过' ? 'success' : 'primary'"
      >
        <div class="text-sm">{{ item.changeType || '变更' }}：{{ item.beforeCombination || '—' }} → {{ item.afterCombination || '—' }}</div>
        <div class="text-xs text-gray-500 mt-1">操作人：{{ item.operator || '—' }} · 原因：{{ item.reason || '—' }}</div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-if="!loading && !list.length" description="暂无选科变更记录" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { listStreamHistory } from '@/api/edu/stream';
import type { StreamHistoryVO } from '@/api/edu/stream/types';

const loading = ref(false);
const list = ref<StreamHistoryVO[]>([]);

/** 加载选科变更记录（时间线区块 PAGE-STR-HISTORY） */
const load = async (query?: { studentId?: string; termId?: string }) => {
  loading.value = true;
  try {
    const res = await listStreamHistory(query);
    list.value = res.rows ?? res.data ?? [];
  } finally {
    loading.value = false;
  }
};

defineExpose({ load });
onMounted(() => load());
</script>
