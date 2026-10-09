<template>
  <el-dialog v-model="visible" :title="title" width="460px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="学号是导入 / 导出对照表的键，变更会写审计。" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="当前学号">
        <span class="mono">{{ student.studentNo || '—' }}</span>
      </el-form-item>
      <el-form-item label="新学号" prop="studentNo">
        <el-input v-model="form.studentNo" placeholder="请输入新学号" maxlength="32" />
      </el-form-item>
      <el-form-item label="变更原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="2" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { updateStudentNo } from '@/api/edu/student';
import type { StudentVO } from '@/api/edu/student/types';

defineOptions({ name: 'EduStudentNoDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const student = ref<StudentVO>({} as StudentVO);

const form = reactive({ studentNo: '', reason: '' });

const title = computed(() => `变更学号 · ${student.value.studentName ?? '—'}`);

const rules: FormRules = {
  studentNo: [{ required: true, message: '请输入新学号', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写变更原因', trigger: 'blur' }]
};

const open = (row: StudentVO) => {
  visible.value = true;
  student.value = row;
  form.studentNo = '';
  form.reason = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await updateStudentNo(student.value.studentId, form.studentNo, form.reason);
    ElMessage.success('学号已变更');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
