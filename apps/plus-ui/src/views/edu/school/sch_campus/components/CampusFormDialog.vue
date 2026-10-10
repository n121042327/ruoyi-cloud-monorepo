<template>
  <el-dialog v-model="visible" :title="title" width="520px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="校区名称" prop="campusName">
        <el-input v-model="form.campusName" placeholder="如 东校区" maxlength="50" />
      </el-form-item>
      <el-form-item label="校区编码" prop="campusCode">
        <el-input v-model="form.campusCode" placeholder="校内唯一" maxlength="32" />
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="form.address" placeholder="如 某某路 1 号" maxlength="200" />
      </el-form-item>
      <el-form-item label="负责人" prop="leaderName">
        <el-input v-model="form.leaderName" placeholder="选填" maxlength="50" />
      </el-form-item>
      <el-form-item label="负责人电话" prop="leaderPhone">
        <el-input v-model="form.leaderPhone" placeholder="选填" maxlength="20" />
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
import { addCampus } from '@/api/edu/school';
import type { CampusForm } from '@/api/edu/school/types';

defineOptions({ name: 'EduCampusFormDialog' });

const props = withDefaults(defineProps<{ schoolId?: string }>(), { schoolId: '' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive<CampusForm>({
  campusCode: '',
  campusName: '',
  address: '',
  leaderName: '',
  leaderPhone: ''
});

const title = computed(() => `新增校区${props.schoolId ? '' : '（未指定学校）'}`);

const rules: FormRules = {
  campusName: [{ required: true, message: '请输入校区名称', trigger: 'blur' }],
  campusCode: [{ required: true, message: '请输入校区编码', trigger: 'blur' }]
};

const open = () => {
  visible.value = true;
  form.campusCode = '';
  form.campusName = '';
  form.address = '';
  form.leaderName = '';
  form.leaderPhone = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (!props.schoolId) {
    ElMessage.warning('缺少学校上下文');
    return;
  }
  submitting.value = true;
  try {
    await addCampus(props.schoolId, { ...form, schoolId: props.schoolId });
    ElMessage.success('校区已创建');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
