<template>
  <el-dialog v-model="visible" :title="title" width="480px" append-to-body>
    <el-alert v-if="isChange" class="mb-3" type="warning" :closable="false" title="变更班主任会写入班级任职历史；同一学年学期只保留一条生效记录。" />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="班级">
        <span>{{ form.className || '—' }}</span>
      </el-form-item>
      <el-form-item label="现任班主任">
        <span>{{ form.currentHeadTeacherName || '未指定' }}</span>
      </el-form-item>
      <el-form-item label="新班主任" prop="headTeacherId">
        <el-select v-model="form.headTeacherId" placeholder="请选择教师" filterable class="w-full">
          <el-option
            v-for="item in teacherOptions"
            :key="item.teacherId"
            :label="item.teacherNo ? `${item.teacherName}（${item.teacherNo}）` : item.teacherName"
            :value="item.teacherId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="生效日期" prop="effectiveDate">
        <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="默认当天" class="w-full" />
      </el-form-item>
      <el-form-item label="变更原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：原班主任调岗" />
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
import { assignClassHeadTeacher } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import type { TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduClassHeadTeacherDialog' });

interface Props {
  teacherOptions?: TeacherVO[];
}

const props = withDefaults(defineProps<Props>(), {
  teacherOptions: () => []
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  classId: '',
  className: '',
  currentHeadTeacherName: '',
  headTeacherId: '',
  effectiveDate: '',
  reason: ''
});

const isChange = computed(() => Boolean(form.currentHeadTeacherName));

const title = computed(() => {
  const suffix = form.className ? ` · ${form.className}` : '';
  return (isChange.value ? '变更班主任' : '指定班主任') + suffix;
});

const rules: FormRules = {
  headTeacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }]
};

const open = (row: ClassVO) => {
  visible.value = true;
  form.classId = row.classId ?? '';
  form.className = row.className ?? '';
  form.currentHeadTeacherName = row.headTeacherName ?? '';
  form.headTeacherId = '';
  form.effectiveDate = '';
  form.reason = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await assignClassHeadTeacher(form.classId, {
      headTeacherId: form.headTeacherId,
      ...(form.reason ? { reason: form.reason } : {})
    });
    ElMessage.success(isChange.value ? '班主任已变更' : '班主任已指定');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
