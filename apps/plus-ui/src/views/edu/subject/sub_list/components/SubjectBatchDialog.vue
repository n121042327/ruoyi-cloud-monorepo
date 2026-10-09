<template>
  <el-dialog v-model="visible" title="按学段批量初始化" width="560px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="按选定学段补齐默认学科清单；已存在的学科不会重复创建。" />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
      <el-form-item label="初始化学段" prop="initStageCodes">
        <el-select v-model="form.initStageCodes" multiple placeholder="请选择要初始化的学段" class="w-full">
          <el-option v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="将初始化的学科（预览）">
        <div class="text-xs">语文 / 数学 / 外语 / 物理 / 化学 / 生物 / 思想政治 / 历史 / 地理</div>
        <div class="hint">默认清单与学段相关：小学不含物理 / 化学 / 生物，初中不含思想政治的部分组合</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">开始初始化</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { batchInitSubject } from '@/api/edu/subject';
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSubjectBatchDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({ initStageCodes: [] as string[] });

const rules: FormRules = {
  initStageCodes: [{ required: true, message: '至少选择一个学段', trigger: 'change' }]
};

const open = () => {
  visible.value = true;
  form.initStageCodes = [];
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await batchInitSubject(form.initStageCodes);
    ElMessage.success('初始化完成');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
