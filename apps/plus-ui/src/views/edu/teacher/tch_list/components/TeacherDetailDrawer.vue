<template>
  <el-drawer v-model="visible" size="720px" :title="title" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <h2 class="text-base font-medium">教师详情 · {{ detail.teacherName || '—' }}</h2>
        <el-tag type="primary" size="small">{{ detail.teacherNo || '—' }}</el-tag>
        <el-tag :type="detail.employmentStatus === EmploymentStatusEnum.ON_DUTY ? 'success' : 'info'" size="small">
          {{ EMPLOYMENT_STATUS_LABEL[detail.employmentStatus ?? ''] ?? detail.employmentStatus ?? '—' }}
        </el-tag>
      </div>
    </template>

    <el-alert
      class="mb-3"
      type="info"
      :closable="false"
      title="教师详情是只读视图：在职状态只有「离职 / 撤销离职登记」一个写入入口，教育角色只有「教育角色」一个写入入口。"
    />

    <h4 class="form-section-title" data-layout-group="基础信息">基础信息</h4>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="工号">{{ detail.teacherNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ detail.teacherName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ detail.gender || '—' }}</el-descriptions-item>
      <el-descriptions-item label="所属学校">{{ detail.schoolName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ detail.phone || '—' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ detail.email || '—' }}</el-descriptions-item>
      <el-descriptions-item label="入职日期">{{ detail.hireDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="在职状态">
        {{ EMPLOYMENT_STATUS_LABEL[detail.employmentStatus ?? ''] ?? detail.employmentStatus ?? '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="离职日期">{{ detail.leaveDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <h4 class="form-section-title mt-4" data-layout-group="任职信息">任职信息</h4>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="教育角色">{{ detail.eduRoles || '未分配' }}</el-descriptions-item>
      <el-descriptions-item label="任教学科">{{ detail.subjectNames || '—' }}</el-descriptions-item>
      <el-descriptions-item label="任课班级数">{{ detail.teachingClassCount ?? 0 }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
    </el-descriptions>

    <h4 class="form-section-title mt-4" data-layout-group="任教清单">任教清单</h4>
    <el-table v-loading="assignLoading" :data="assignments" border size="small">
      <el-table-column label="学年学期" prop="termName" width="150" show-overflow-tooltip />
      <el-table-column label="学科" prop="subjectName" width="110" />
      <el-table-column label="班级" prop="className" min-width="140" show-overflow-tooltip />
      <el-table-column label="类型" prop="classType" width="100" align="center" />
      <el-table-column label="状态" prop="status" width="90" align="center" />
      <template #empty>
        <el-empty description="暂无任教关系" :image-size="60" />
      </template>
    </el-table>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getTeacher, listTeachingAssignment } from '@/api/edu/teacher';
import type { TeacherVO, TeachingAssignmentVO } from '@/api/edu/teacher/types';
import { EMPLOYMENT_STATUS_LABEL, EmploymentStatusEnum } from '@/enums/edu/TeacherEnum';

defineOptions({ name: 'EduTeacherDetailDrawer' });

const visible = ref(false);
const assignLoading = ref(false);
const detail = ref<TeacherVO>({} as TeacherVO);
const assignments = ref<TeachingAssignmentVO[]>([]);

const title = computed(() => `教师详情 · ${detail.value.teacherName ?? '—'}`);

const open = async (row: TeacherVO) => {
  visible.value = true;
  detail.value = row;
  assignments.value = [];
  const [detailRes, assignRes] = await Promise.all([
    getTeacher(row.teacherId).catch(() => null),
    listTeachingAssignment({ teacherId: row.teacherId, pageNum: 1, pageSize: 50 }).catch(() => null)
  ]);
  if (detailRes?.data) {
    detail.value = detailRes.data;
  }
  assignLoading.value = true;
  try {
    assignments.value = assignRes?.rows ?? [];
  } finally {
    assignLoading.value = false;
  }
};

defineExpose({ open });
</script>
