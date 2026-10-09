<template>
  <el-dialog v-model="visible" :title="title" width="520px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="激活码用于学生首次登录；导出与打印都会写审计。" />

    <el-descriptions :column="1" border>
      <el-descriptions-item label="学号">{{ student.studentNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ student.studentName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="激活码">
        <span class="mono">{{ code || '—' }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button :loading="printing" @click="handlePrint">打印激活单</el-button>
      <el-button :loading="exporting" @click="handleExport">导出激活码</el-button>
      <el-button type="primary" :loading="activating" @click="handleActivate">标记已激活</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { activateStudent, exportActivationCode, getActivationCode, printActivationSlip } from '@/api/edu/student';
import type { StudentVO } from '@/api/edu/student/types';

defineOptions({ name: 'EduStudentActivationDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const loading = ref(false);
const printing = ref(false);
const exporting = ref(false);
const activating = ref(false);
const code = ref('');
const student = ref<StudentVO>({} as StudentVO);

const title = computed(() => `激活码 · ${student.value.studentName ?? '—'}`);

const open = async (row: StudentVO) => {
  visible.value = true;
  student.value = row;
  code.value = '';
  loading.value = true;
  try {
    const res = await getActivationCode(row.studentId);
    const data = res.data as unknown as string | { activationCode?: string } | undefined;
    code.value = typeof data === 'string' ? data : (data?.activationCode ?? '');
  } finally {
    loading.value = false;
  }
};

const handlePrint = async () => {
  printing.value = true;
  try {
    await printActivationSlip([student.value.studentId]);
    ElMessage.success('已提交打印任务');
  } finally {
    printing.value = false;
  }
};

const handleExport = async () => {
  exporting.value = true;
  try {
    await exportActivationCode([student.value.studentId]);
    ElMessage.success('已提交导出任务，请到异步任务中心下载');
  } finally {
    exporting.value = false;
  }
};

const handleActivate = async () => {
  await ElMessageBox.confirm(`确认将「${student.value.studentName ?? '该学生'}」标记为已激活？`, '标记已激活', { type: 'warning' });
  activating.value = true;
  try {
    await activateStudent(student.value.studentId);
    ElMessage.success('已标记为激活');
    visible.value = false;
    emit('success');
  } finally {
    activating.value = false;
  }
};

defineExpose({ open });
</script>
