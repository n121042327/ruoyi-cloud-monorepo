<template>
  <el-card shadow="hover" class="mt-2">
    <template #header>
      <div class="flex items-center gap-2">
        <span>学科选择人数</span>
        <el-tag type="info" size="small">占比分母按选科角色区分</el-tag>
      </div>
    </template>
    <el-table v-loading="loading" border :data="list">
      <el-table-column label="学科" prop="subjectName" width="160" data-layout-group="学科信息" />
      <el-table-column label="选科角色" prop="streamRole" width="120" align="center" data-layout-group="学科信息" />
      <el-table-column label="选择人数" prop="memberCount" width="120" align="center" data-layout-group="统计信息" />
      <el-table-column label="占比" prop="ratio" width="120" align="center" data-layout-group="统计信息" />
      <el-table-column label="占比分母" prop="ratioBase" min-width="240" data-layout-group="统计信息" />
      <template #empty>
        <el-empty description="暂无学科统计" />
      </template>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import type { StreamStatSubjectVO } from '@/api/edu/stream/types';

defineProps<{ list?: StreamStatSubjectVO[]; loading?: boolean }>();
</script>
