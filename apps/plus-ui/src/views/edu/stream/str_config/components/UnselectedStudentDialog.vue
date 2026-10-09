<template>
  <el-dialog v-model="visible" title="未选科学生" width="640px" append-to-body>
    <el-table v-loading="loading" :data="list" max-height="380">
      <el-table-column label="学号" prop="studentNo" width="140" data-layout-group="学生信息" />
      <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
      <el-table-column label="年级" prop="gradeName" width="120" data-layout-group="学生信息" />
      <el-table-column label="班级" prop="className" min-width="150" data-layout-group="学生信息" />
      <template #empty>
        <el-empty description="没有未选科学生" />
      </template>
    </el-table>
    <div class="hint mt-2">按年级 / 班级分组展示，可直接用于催办；数据范围仍受本校限制。</div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { listUnselectedStudent } from '@/api/edu/stream';
import type { UnselectedStudentVO } from '@/api/edu/stream/types';

const visible = ref(false);
const loading = ref(false);
const list = ref<UnselectedStudentVO[]>([]);

/** 打开并加载未选科学生（按学年学期） */
const open = async (termId?: string) => {
  visible.value = true;
  list.value = [];
  loading.value = true;
  try {
    const res = await listUnselectedStudent({ termId });
    list.value = res.rows ?? res.data ?? [];
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
