<template>
  <el-drawer v-model="visible" title="教学班详情" size="640px">
    <template v-if="detail">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="教学班名称">{{ detail.className || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学年学期">{{ detail.termName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ detail.gradeName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="组合 / 学科">{{ detail.subjectCombination || '—' }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ detail.memberCount ?? 0 }} 人</el-descriptions-item>
        <el-descriptions-item label="任课教师（只读）">{{ detail.teacherName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status || '正常' }}</el-descriptions-item>
      </el-descriptions>

      <div class="mt-4 mb-2 text-sm font-medium">成员清单</div>
      <el-table v-loading="rosterLoading" border :data="roster" max-height="360">
        <el-table-column label="学号" prop="studentNo" width="140" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
        <el-table-column label="学籍状态" prop="enrollmentStatus" width="120" align="center" data-layout-group="学生信息" />
        <el-table-column label="行政班" prop="currentClassName" min-width="150" data-layout-group="班级信息" />
        <template #empty>
          <el-empty description="该教学班暂无成员" />
        </template>
      </el-table>
    </template>
    <el-empty v-else description="未加载到教学班详情" />

    <template #footer>
      <div class="flex justify-end gap-2">
        <el-button @click="goGenerate">去增量生成并核对</el-button>
        <el-button @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getTeachingClass, listTeachingClassRoster } from '@/api/edu/class';
import type { ClassRosterVO, TeachingClassVO } from '@/api/edu/class/types';

const router = useRouter();
const visible = ref(false);
const rosterLoading = ref(false);
const detail = ref<TeachingClassVO>();
const roster = ref<ClassRosterVO[]>([]);

const open = async (row: TeachingClassVO) => {
  visible.value = true;
  detail.value = row;
  roster.value = [];
  rosterLoading.value = true;
  try {
    const res = await getTeachingClass(row.classId);
    detail.value = res.data ?? row;
    const rosterRes = await listTeachingClassRoster(row.classId);
    roster.value = rosterRes.rows ?? [];
  } finally {
    rosterLoading.value = false;
  }
};

const goGenerate = () => router.push('/edu/stream/generate-class');

defineExpose({ open });
</script>
