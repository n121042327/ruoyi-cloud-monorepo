<template>
  <el-dialog v-model="visible" :title="title" width="460px" append-to-body>
    <el-alert class="mb-3" type="warning" :closable="false" title="学校编码是导入 / 导出对照表的键，变更会写审计。" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="当前编码">
        <span class="mono">{{ school.schoolCode || '—' }}</span>
      </el-form-item>
      <el-form-item label="新编码" prop="schoolCode">
        <el-input v-model="form.schoolCode" placeholder="请输入新编码" maxlength="32" />
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
import { updateSchoolCode } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';

defineOptions({ name: 'EduSchoolCodeDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const school = ref<SchoolVO>({} as SchoolVO);

const form = reactive({ schoolCode: '', reason: '' });

const title = computed(() => `变更学校编码 · ${school.value.schoolName ?? '—'}`);

const rules: FormRules = {
  schoolCode: [{ required: true, message: '请输入新编码', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写变更原因', trigger: 'blur' }]
};

const open = (row: SchoolVO) => {
  visible.value = true;
  school.value = row;
  form.schoolCode = '';
  form.reason = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await updateSchoolCode(school.value.schoolId, form.schoolCode, form.reason);
    ElMessage.success('编码已变更');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
