<template>
  <el-dialog v-model="visible" :title="title" width="480px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
      <el-form-item label="是否参与 3+1+2 选科" prop="streamEnabled">
        <el-radio-group v-model="form.streamEnabled">
          <el-radio value="1">参与</el-radio>
          <el-radio value="0">不参与</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.streamEnabled === '1'" label="选科角色" prop="streamRole">
        <el-radio-group v-model="form.streamRole">
          <el-radio value="primary">首选</el-radio>
          <el-radio value="secondary">再选</el-radio>
        </el-radio-group>
        <div class="hint">首选固定为物理 / 历史两门；其余参与选科的科目都是再选</div>
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
import { saveSubjectStreamRole } from '@/api/edu/subject';
import type { SubjectVO } from '@/api/edu/subject/types';

defineOptions({ name: 'EduSubjectStreamDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  subjectId: '',
  subjectName: '',
  streamEnabled: '0',
  streamRole: 'none'
});

const title = computed(() => `选科角色配置 · ${form.subjectName || '—'}`);

const rules: FormRules = {
  streamRole: [
    {
      validator: (_rule, value, callback) => {
        if (form.streamEnabled === '1' && (!value || value === 'none')) {
          callback(new Error('参与选科时必须指定首选或再选'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ]
};

const open = (row: SubjectVO) => {
  visible.value = true;
  form.subjectId = row.subjectId;
  form.subjectName = row.subjectName;
  form.streamEnabled = row.streamEnabled === '1' ? '1' : '0';
  form.streamRole = row.streamRole && row.streamRole !== 'none' ? row.streamRole : 'primary';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await saveSubjectStreamRole(form.subjectId, {
      streamEnabled: form.streamEnabled,
      streamRole: form.streamEnabled === '1' ? form.streamRole : 'none'
    });
    ElMessage.success('选科角色已保存');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
