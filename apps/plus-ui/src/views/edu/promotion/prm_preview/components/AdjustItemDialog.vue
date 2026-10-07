<template>
  <el-dialog v-model="visible" title="调整升班去向" width="560px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <h3 class="form-section-title" data-layout-group="学生信息">学生信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学号">
            <el-input v-model="form.studentNo" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名">
            <el-input v-model="form.studentName" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <h3 class="form-section-title" data-layout-group="去向信息">去向信息</h3>
      <el-form-item label="结果类型" prop="resultType">
        <el-select v-model="form.resultType" class="w-full">
          <el-option v-for="item in RESULT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标班级 / 去向" prop="targetClassId">
        <el-input v-model="form.targetClassId" placeholder="升班 / 留级 / 毕业时填写目标班级或去向" clearable />
      </el-form-item>
      <el-form-item label="调整说明" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：家长申请随班调整到高二 (2) 班" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { updatePromotionItem } from '@/api/edu/promotion';
import type { PromotionItemAdjustForm, PromotionItemVO } from '@/api/edu/promotion/types';

const RESULT_TYPE_OPTIONS = [
  { label: '升班', value: 'promote' },
  { label: '留级', value: 'repeat' },
  { label: '毕业', value: 'graduate' },
  { label: '结业', value: 'complete' }
];

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const taskId = ref('');

const defaultForm = (): PromotionItemAdjustForm => ({
  itemId: '',
  studentNo: '',
  studentName: '',
  resultType: 'promote',
  targetClassId: '',
  remark: ''
});

const form = reactive<PromotionItemAdjustForm>(defaultForm());

const rules: FormRules = {
  resultType: [{ required: true, message: '请选择结果类型', trigger: 'change' }]
};

/** 打开逐条调整弹窗（PAGE-PRM-ADJUST，REQ-PRM-021） */
const open = (currentTaskId: string, row: PromotionItemVO) => {
  visible.value = true;
  taskId.value = currentTaskId;
  Object.assign(form, defaultForm(), {
    itemId: row.itemId,
    studentNo: row.studentNo,
    studentName: row.studentName,
    resultType: row.resultType || 'promote',
    targetClassId: row.targetClassId || '',
    remark: row.remark || ''
  });
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await updatePromotionItem(taskId.value, { ...form });
    ElMessage.success('已更新该学生的升班去向');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
