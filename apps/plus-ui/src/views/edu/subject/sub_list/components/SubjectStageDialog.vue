<template>
  <el-dialog v-model="visible" :title="title" width="480px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="启用学段（多选）" prop="stageCodes">
        <el-select v-model="form.stageCodes" multiple placeholder="请选择启用学段" class="w-full">
          <el-option v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-alert class="mb-0" type="info" :closable="false" title="取消某个学段后，该学段下已有的任教关系与选科不受影响，但不能再新建。" />
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
import { saveSubjectStage } from '@/api/edu/subject';
import type { SubjectVO } from '@/api/edu/subject/types';
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSubjectStageDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  subjectId: '',
  subjectName: '',
  stageCodes: [] as string[]
});

const title = computed(() => `学段启用配置 · ${form.subjectName || '—'}`);

const rules: FormRules = {
  stageCodes: [{ required: true, message: '至少选择一个学段', trigger: 'change' }]
};

const open = (row: SubjectVO) => {
  visible.value = true;
  form.subjectId = row.subjectId;
  form.subjectName = row.subjectName;
  form.stageCodes = [...(row.stageCodes ?? [])];
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await saveSubjectStage(form.subjectId, form.stageCodes);
    ElMessage.success('学段配置已保存');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
