<template>
  <el-dialog v-model="visible" :title="title" width="460px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="工号是导入 / 导出对照表的键，变更会写审计。" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="当前工号">
        <span class="mono">{{ teacher.teacherNo || '—' }}</span>
      </el-form-item>
      <el-form-item label="新工号" prop="teacherNo">
        <el-input v-model="form.teacherNo" placeholder="请输入新工号" maxlength="32" />
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
import { updateTeacherNo } from '@/api/edu/teacher';
import type { TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduTeacherNoDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const teacher = ref<TeacherVO>({} as TeacherVO);

const form = reactive({ teacherNo: '', reason: '' });

const title = computed(() => `变更工号 · ${teacher.value.teacherName ?? '—'}`);

const rules: FormRules = {
  teacherNo: [{ required: true, message: '请输入新工号', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写变更原因', trigger: 'blur' }]
};

const open = (row: TeacherVO) => {
  visible.value = true;
  teacher.value = row;
  form.teacherNo = '';
  form.reason = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await updateTeacherNo(teacher.value.teacherId, form.teacherNo, form.reason);
    ElMessage.success('工号已变更');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
