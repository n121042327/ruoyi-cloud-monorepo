<template>
  <el-dialog v-model="visible" title="选科变更申请" width="560px" append-to-body>
    <el-alert class="mb-3" type="warning" :closable="false">
      <template #title>变更口径</template>
      <div>开放期内变更直接生效；截止后提交会作为变更申请进入校级管理员审批， 审批通过前保持原选科不变。</div>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <h3 class="form-section-title" data-layout-group="学生信息">学生信息</h3>
      <el-form-item label="学生">
        <el-input :model-value="studentLabel" disabled />
      </el-form-item>

      <h3 class="form-section-title" data-layout-group="新选科">新选科</h3>
      <el-form-item label="新的首选" prop="primarySubjectCode">
        <el-radio-group v-model="form.primarySubjectCode">
          <el-radio :value="'physics'">物理</el-radio>
          <el-radio :value="'history'">历史</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="新的再选（4 选 2）" prop="secondarySubjectCodes">
        <el-checkbox-group v-model="form.secondarySubjectCodes" :max="2">
          <el-checkbox :value="'chemistry'">化学</el-checkbox>
          <el-checkbox :value="'biology'">生物</el-checkbox>
          <el-checkbox :value="'politics'">思想政治</el-checkbox>
          <el-checkbox :value="'geography'">地理</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="变更原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="如：升学规划调整，申请改选历史 + 政治 + 地理"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { addStreamChangeRequest } from '@/api/edu/stream';
import type { StreamChangeForm, StreamSelectionVO } from '@/api/edu/stream/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const row = ref<StreamSelectionVO>();

const form = reactive<StreamChangeForm>({
  studentId: '',
  primarySubjectCode: 'physics',
  secondarySubjectCodes: [],
  reason: ''
});

const studentLabel = computed(() => (row.value ? `${row.value.studentName ?? ''}（${row.value.studentNo ?? ''}）` : ''));

const rules: FormRules = {
  primarySubjectCode: [{ required: true, message: '请选择新的首选科目', trigger: 'change' }],
  reason: [
    { required: true, message: '请填写变更原因（至少 5 个字）', trigger: 'blur' },
    { min: 5, message: '变更原因至少 5 个字', trigger: 'blur' }
  ]
};

/** 打开变更申请弹窗（PAGE-STR-CHANGE） */
const open = (target: StreamSelectionVO) => {
  visible.value = true;
  row.value = target;
  form.studentId = target.studentId;
  form.primarySubjectCode = target.primarySubjectCode ?? 'physics';
  form.secondarySubjectCodes = [...(target.secondarySubjectCodes ?? [])];
  form.reason = '';
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (form.secondarySubjectCodes.length !== 2) {
    ElMessage.warning('再选科目必须选满 2 门且不能重复');
    return;
  }
  submitting.value = true;
  try {
    await addStreamChangeRequest({ ...form });
    ElMessage.success('已提交选科变更申请，待校级管理员审批（审批通过前保持原选科不变）');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
