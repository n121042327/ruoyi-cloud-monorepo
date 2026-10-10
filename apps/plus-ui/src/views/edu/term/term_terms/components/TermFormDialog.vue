<template>
  <el-dialog v-model="visible" :title="title" width="480px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="学期名称" prop="termName">
        <el-input v-model="form.termName" placeholder="如 第一学期" maxlength="50" />
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" class="w-full" />
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" class="w-full" />
      </el-form-item>
      <el-form-item label="设为当前" prop="isCurrent">
        <el-switch v-model="isCurrent" />
        <div class="hint">同一学校同一时间只允许一个当前学年学期</div>
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
import { saveTerm } from '@/api/edu/term';
import type { TermForm, TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduTermFormDialog' });

const props = withDefaults(defineProps<{ academicYearId?: string; schoolId?: string }>(), {
  academicYearId: '',
  schoolId: ''
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const editing = ref(false);
const formRef = ref<FormInstance>();
const isCurrent = ref(false);

const form = reactive<TermForm>({
  termId: undefined,
  academicYearId: '',
  termName: '',
  startDate: '',
  endDate: ''
});

const title = computed(() => (editing.value ? '编辑学期' : '新增学期'));

const rules: FormRules = {
  termName: [{ required: true, message: '请输入学期名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (value && form.startDate && value <= form.startDate) {
          callback(new Error('结束日期必须晚于开始日期'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ]
};

const open = (row?: TermVO) => {
  visible.value = true;
  editing.value = Boolean(row?.termId);
  form.termId = row?.termId;
  form.academicYearId = row?.academicYearId ?? props.academicYearId;
  form.schoolId = props.schoolId;
  form.termName = row?.termName ?? '';
  form.startDate = row?.startDate ?? '';
  form.endDate = row?.endDate ?? '';
  isCurrent.value = Boolean(row?.current);
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await saveTerm({ ...form, isCurrent: isCurrent.value ? '1' : '0' });
    ElMessage.success(editing.value ? '学期已保存' : '学期已创建');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
