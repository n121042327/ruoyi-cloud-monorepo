<template>
  <el-dialog v-model="visible" width="520px" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <span>停用教学班 · {{ row?.className || '' }}</span>
        <el-tag type="danger" size="small">危险动作</el-tag>
      </div>
    </template>

    <el-alert class="mb-3" type="warning" :closable="false">
      <template #title>影响范围</template>
      <div>停用后该教学班不再出现在选科结果与任课关系的新建可选列表中，<b>历史成员与成绩类数据保留</b>； 重新启用需重新生成，停用动作写入审计。</div>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <h3 class="form-section-title" data-layout-group="停用信息">停用信息</h3>
      <el-form-item label="停用原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="如：组合调整，本教学班并入新的物化生 A 层"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" plain :loading="submitting" @click="submitForm">确认停用</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { disableTeachingClass } from '@/api/edu/class';
import type { TeachingClassVO } from '@/api/edu/class/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const row = ref<TeachingClassVO>();
const form = reactive<{ reason: string }>({ reason: '' });

const rules: FormRules = {
  reason: [
    { required: true, message: '请填写停用原因（至少 5 个字）', trigger: 'blur' },
    { min: 5, message: '停用原因至少 5 个字', trigger: 'blur' }
  ]
};

const open = (target: TeachingClassVO) => {
  visible.value = true;
  row.value = target;
  form.reason = '';
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (!row.value) return;
  submitting.value = true;
  try {
    await disableTeachingClass(row.value.classId, form.reason);
    ElMessage.success('已停用该教学班，历史成员数据保留');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
