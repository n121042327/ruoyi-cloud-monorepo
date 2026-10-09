<template>
  <el-card shadow="hover" class="mt-2">
    <template #header>
      <div class="flex items-center gap-2">
        <span>核对结果（教学班人数 vs 选科统计人数）</span>
        <el-tag :type="hasDiff ? 'warning' : 'success'" size="small">{{ hasDiff ? '存在差异' : '全部一致' }}</el-tag>
      </div>
    </template>
    <el-table v-loading="loading" border :data="list">
      <el-table-column label="教学班" prop="className" min-width="200" data-layout-group="核对信息" />
      <el-table-column label="教学班人数" prop="memberCount" width="150" align="center" data-layout-group="核对信息" />
      <el-table-column label="选科统计人数" prop="statCount" width="150" align="center" data-layout-group="核对信息" />
      <el-table-column label="差异" prop="diff" width="120" align="center" data-layout-group="核对信息" />
      <el-table-column label="核对结果" prop="result" min-width="240" data-layout-group="核对信息">
        <template #default="scope">
          <el-tag :type="scope.row.diff ? 'warning' : 'success'" size="small">{{
            scope.row.result || (scope.row.diff ? '存在差异' : '一致')
          }}</el-tag>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无核对结果" />
      </template>
    </el-table>
    <div class="hint mt-2">差异只提示不自动修正：审批后新增的选科需要再次增量生成。</div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TeachingClassCheckRowVO } from '@/api/edu/stream/types';

const props = defineProps<{ list?: TeachingClassCheckRowVO[]; loading?: boolean }>();

const hasDiff = computed(() => (props.list ?? []).some((item) => !!item.diff));
</script>
