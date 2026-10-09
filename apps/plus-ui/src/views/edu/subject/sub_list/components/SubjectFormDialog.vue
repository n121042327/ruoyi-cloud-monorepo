<template>
  <el-dialog v-model="visible" :title="title" width="520px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="学科编码" prop="subjectCode">
        <el-input v-model="form.subjectCode" :disabled="isEdit" placeholder="如 CHN，校内唯一" maxlength="32" />
      </el-form-item>
      <el-form-item label="学科名称" prop="subjectName">
        <el-input v-model="form.subjectName" placeholder="如 语文" maxlength="50" />
      </el-form-item>
      <el-form-item label="排序号" prop="sortNo">
        <el-input-number v-model="form.sortNo" :min="0" :max="999" controls-position="right" class="w-full" />
        <div class="hint">决定再选科目的展示顺序（REQ-STR-017）</div>
      </el-form-item>
      <el-form-item label="启用学段" prop="stageCodes">
        <el-select v-model="form.stageCodes" multiple placeholder="请选择启用学段" class="w-full">
          <el-option v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <div class="hint">同一学科在不同学段启用只增加启用记录，不拆多条主体</div>
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
import { addSubject, getSubject, updateSubject } from '@/api/edu/subject';
import type { SubjectForm, SubjectVO } from '@/api/edu/subject/types';
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSubjectFormDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const isEdit = ref(false);

const form = reactive<SubjectForm>({
  subjectId: undefined,
  subjectCode: '',
  subjectName: '',
  sortNo: 0,
  stageCodes: []
});

const title = computed(() => (isEdit.value ? '编辑学科' : '新建学科'));

const rules: FormRules = {
  subjectCode: [{ required: true, message: '请输入学科编码', trigger: 'blur' }],
  subjectName: [{ required: true, message: '请输入学科名称', trigger: 'blur' }]
};

const open = async (row?: SubjectVO) => {
  visible.value = true;
  isEdit.value = Boolean(row?.subjectId);
  form.subjectId = row?.subjectId;
  form.subjectCode = row?.subjectCode ?? '';
  form.subjectName = row?.subjectName ?? '';
  form.sortNo = row?.sortNo ?? 0;
  form.stageCodes = [...(row?.stageCodes ?? [])];
  formRef.value?.clearValidate();

  if (row?.subjectId) {
    // 列表未返回全部配置时以详情为准
    const res = await getSubject(row.subjectId);
    const detail = res.data;
    if (detail) {
      form.subjectCode = detail.subjectCode;
      form.subjectName = detail.subjectName;
      form.sortNo = detail.sortNo ?? 0;
      form.stageCodes = [...(detail.stageCodes ?? [])];
    }
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (isEdit.value) {
      await updateSubject({ ...form });
    } else {
      await addSubject({ ...form });
    }
    ElMessage.success(isEdit.value ? '学科已保存' : '学科已创建');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
