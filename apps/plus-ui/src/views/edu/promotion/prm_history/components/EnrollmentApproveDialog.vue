<template>
  <el-dialog v-model="visible" :title="title" width="520px" append-to-body>
    <el-alert class="mb-3" type="warning" :closable="false" title="退学 / 开除 / 死亡三类异动需要校级管理员审批；审批动作写入审计且不可撤销。" />

    <el-descriptions :column="1" border class="mb-3">
      <el-descriptions-item label="学生">{{ row.studentName || '—' }}（{{ row.studentNo || '—' }}）</el-descriptions-item>
      <el-descriptions-item label="异动类型">{{ row.changeType || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态变化">{{ row.beforeStatus || '—' }} → {{ row.afterStatus || '—' }}</el-descriptions-item>
      <el-descriptions-item label="生效日期">{{ row.effectiveDate || '—' }}</el-descriptions-item>
      <el-descriptions-item label="登记原因">{{ row.reason || '—' }}</el-descriptions-item>
    </el-descriptions>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="审批结论" prop="approved">
        <el-radio-group v-model="form.approved">
          <el-radio :value="true">通过</el-radio>
          <el-radio :value="false">驳回</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审批意见" prop="approveOpinion">
        <el-input
          v-model="form.approveOpinion"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          :placeholder="form.approved ? '选填' : '驳回时必须说明原因'"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">提交审批</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { approveEnrollmentChange } from '@/api/edu/promotion';
import type { EnrollmentChangeVO } from '@/api/edu/promotion/types';

defineOptions({ name: 'EduEnrollmentApproveDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const row = ref<EnrollmentChangeVO>({} as EnrollmentChangeVO);

const form = reactive({ approved: true, approveOpinion: '' });

const title = computed(() => `异动审批 · ${row.value.studentName ?? '—'}`);

const rules: FormRules = {
  approveOpinion: [
    {
      validator: (_rule, value, callback) => {
        if (!form.approved && (!value || value.trim().length < 2)) {
          callback(new Error('驳回时必须填写审批意见（至少 2 个字）'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ]
};

const open = (item: EnrollmentChangeVO) => {
  visible.value = true;
  row.value = item;
  form.approved = true;
  form.approveOpinion = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await approveEnrollmentChange(row.value.changeId, {
      approved: form.approved,
      ...(form.approveOpinion ? { approveOpinion: form.approveOpinion } : {})
    });
    ElMessage.success(form.approved ? '已通过审批' : '已驳回');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
