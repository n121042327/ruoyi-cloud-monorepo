<template>
  <el-dialog v-model="visible" :title="title" width="480px" append-to-body>
    <el-alert class="mb-3" type="warning" :closable="false" title="登记后该教师不能再新增任教关系；已有任教关系与历史数据保留，撤销登记后可恢复。" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="登记类型" prop="employmentStatus">
        <el-radio-group v-model="form.employmentStatus">
          <el-radio value="离职">离职</el-radio>
          <el-radio value="调离">调离</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="生效日期" prop="leaveDate">
        <el-date-picker v-model="form.leaveDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" class="w-full" />
      </el-form-item>
      <el-form-item label="登记原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：调往集团其他学校" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">提交登记</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { leaveTeacher } from '@/api/edu/teacher';
import type { TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduTeacherLeaveDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const teacher = ref<TeacherVO>({} as TeacherVO);

const form = reactive({ employmentStatus: '离职', leaveDate: '', reason: '' });

const title = computed(() => `离职 / 调离登记 · ${teacher.value.teacherName ?? '—'}`);

const rules: FormRules = {
  employmentStatus: [{ required: true, message: '请选择登记类型', trigger: 'change' }],
  leaveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写登记原因', trigger: 'blur' }]
};

const open = (row: TeacherVO) => {
  visible.value = true;
  teacher.value = row;
  form.employmentStatus = '离职';
  form.leaveDate = '';
  form.reason = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await leaveTeacher(teacher.value.teacherId, { ...form });
    ElMessage.success('登记完成');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
