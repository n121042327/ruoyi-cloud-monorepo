<template>
  <el-dialog v-model="visible" width="520px" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <span
          >取消升班任务 · <span class="mono">{{ task?.taskNo || '' }}</span></span
        >
        <el-tag type="warning" size="small">危险动作</el-tag>
      </div>
    </template>

    <el-alert class="mb-3" type="warning" :closable="false">
      <template #title>影响范围</template>
      <div>
        取消后<b>已完成的学生保留升班结果</b>，不做整批回滚；可在结果页「继续执行剩余项」， 也可对已完成项<b>逐条回滚</b>（<span class="mono"
          >REQ-PRM-036</span
        >
        / 已确认 4）。取消写入审计且不可删除（<span class="mono">REQ-PRM-058</span>）。
      </div>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <h3 class="form-section-title" data-layout-group="取消信息">取消信息</h3>
      <el-form-item label="取消原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="如：目标班级名单还在调整，本批先取消，下周重新执行"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">返回</el-button>
        <el-button type="warning" plain :loading="submitting" @click="submitForm">确认取消任务</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { cancelPromotionTask } from '@/api/edu/promotion';
import type { PromotionTaskVO } from '@/api/edu/promotion/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const task = ref<PromotionTaskVO>();
const form = reactive<{ reason: string }>({ reason: '' });

const rules: FormRules = {
  reason: [
    { required: true, message: '请填写取消原因（至少 5 个字）', trigger: 'blur' },
    { min: 5, message: '取消原因至少 5 个字', trigger: 'blur' }
  ]
};

const open = (row: PromotionTaskVO) => {
  visible.value = true;
  task.value = row;
  form.reason = '';
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (!task.value) return;
  submitting.value = true;
  try {
    await cancelPromotionTask(task.value.taskId, form.reason);
    ElMessage.success('已提交取消：任务状态将变为「已取消」，已完成的学生保留');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
