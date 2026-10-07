<template>
  <el-dialog v-model="visible" title="复制上一学年任教关系" width="560px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false">
      <template #title>复制口径</template>
      <div>
        按「源学年学期 → 目标学年学期」整批复制任教关系；目标学期已有同一「班级 + 学科」的任教关系时跳过该条，
        不覆盖已有配置；复制结果写审计，可在异步任务中心查看明细。
      </div>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <h3 class="form-section-title" data-layout-group="复制范围">复制范围</h3>
      <el-form-item label="源学年学期" prop="sourceTermId">
        <el-select v-model="form.sourceTermId" placeholder="请选择源学年学期" class="w-full">
          <el-option
            v-for="item in termOptions"
            :key="item.termId"
            :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
            :value="item.termId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="目标学年学期" prop="targetTermId">
        <el-select v-model="form.targetTermId" placeholder="请选择目标学年学期" class="w-full">
          <el-option
            v-for="item in termOptions"
            :key="item.termId"
            :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
            :value="item.termId"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button v-hasPermi="['person.teaching_assignment:create']" type="primary" :loading="submitting" @click="submitForm"> 确认复制 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { copyTeachingAssignment } from '@/api/edu/teacher';
import type { TeachingAssignmentCopyForm } from '@/api/edu/teacher/types';
import type { TermVO } from '@/api/edu/term/types';

withDefaults(defineProps<{ termOptions?: TermVO[] }>(), { termOptions: () => [] });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const form = reactive<TeachingAssignmentCopyForm>({ sourceTermId: '', targetTermId: '' });

const rules: FormRules = {
  sourceTermId: [{ required: true, message: '请选择源学年学期', trigger: 'change' }],
  targetTermId: [{ required: true, message: '请选择目标学年学期', trigger: 'change' }]
};

const open = () => {
  visible.value = true;
  form.sourceTermId = '';
  form.targetTermId = '';
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (form.sourceTermId === form.targetTermId) {
    ElMessage.error('源学年学期与目标学年学期不能相同');
    return;
  }
  submitting.value = true;
  try {
    await copyTeachingAssignment({ ...form });
    ElMessage.success('已提交复制：目标学期已有同一班级 + 学科的任教关系会跳过，不覆盖已有配置');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
