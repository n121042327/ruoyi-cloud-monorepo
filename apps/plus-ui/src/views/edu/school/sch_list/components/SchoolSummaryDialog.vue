<template>
  <el-dialog v-model="visible" :title="title" width="560px" append-to-body>
    <el-descriptions v-loading="loading" :column="2" border>
      <el-descriptions-item label="学校">{{ data.schoolName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="初始化状态">
        <el-tag :type="data.initialized ? 'success' : 'info'" size="small">{{ data.initialized ? '已初始化' : '未初始化' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="开设学段">{{ (data.stageCodes ?? []).map((c) => STAGE_LABEL[c] ?? c).join(' / ') || '—' }}</el-descriptions-item>
      <el-descriptions-item label="校区数">{{ data.campusCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="学年数">{{ data.academicYearCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="学期数">{{ data.termCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="年级数">{{ data.gradeCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="班级数">{{ data.classCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="在读学生数">{{ data.studentCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="教师数">{{ data.teacherCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="学科数">{{ data.subjectCount ?? 0 }}</el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getSchoolSummary } from '@/api/edu/school';
import type { SchoolSummaryVO, SchoolVO } from '@/api/edu/school/types';
import { STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSchoolSummaryDialog' });

const STAGE_LABEL = STAGE_CODE_LABEL;

const visible = ref(false);
const loading = ref(false);
const data = ref<SchoolSummaryVO>({} as SchoolSummaryVO);
const school = ref<SchoolVO>({} as SchoolVO);

const title = computed(() => `学校概要 · ${school.value.schoolName ?? '—'}`);

const open = async (row: SchoolVO) => {
  visible.value = true;
  school.value = row;
  loading.value = true;
  try {
    const res = await getSchoolSummary(row.schoolId);
    data.value = (res.data ?? {}) as SchoolSummaryVO;
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
