<template>
  <el-dialog v-model="visible" :title="title" width="720px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 基础信息：工号 → 姓名 → 性别 -->
      <h3 class="form-section-title" data-layout-group="基础信息">基础信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="工号" prop="teacherNo">
            <el-input v-model="form.teacherNo" placeholder="同一租户内唯一" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="teacherName">
            <el-input v-model="form.teacherName" placeholder="请输入姓名" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择" class="w-full">
              <el-option v-for="item in GENDER_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属学校" prop="schoolId">
            <el-select v-model="form.schoolId" placeholder="请选择学校" class="w-full" :disabled="!!form.teacherId">
              <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 联系方式：手机号 → 邮箱 → 入职日期 -->
      <h3 class="form-section-title" data-layout-group="联系方式">联系方式</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="手机号" prop="teacherPhone">
            <el-input v-model="form.teacherPhone" placeholder="可留空" maxlength="11" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="可留空" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入职日期" prop="entryDate">
            <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="hint">保存成功后自动创建教师登录账号；教育角色与任教关系在教师详情的对应入口维护。</div>
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
import type { SchoolVO } from '@/api/edu/school/types';
import { addTeacher, updateTeacher } from '@/api/edu/teacher';
import type { TeacherForm, TeacherVO } from '@/api/edu/teacher/types';
import { GENDER_OPTIONS } from '@/enums/edu/StudentEnum';

const props = withDefaults(defineProps<{ schoolOptions?: SchoolVO[] }>(), { schoolOptions: () => [] });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新增教师');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): TeacherForm => ({
  teacherId: undefined,
  teacherNo: '',
  teacherName: '',
  gender: '',
  schoolId: '',
  teacherPhone: '',
  email: '',
  entryDate: '',
  remark: ''
});

const form = reactive<TeacherForm>(defaultForm());

const rules: FormRules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  schoolId: [{ required: true, message: '请选择所属学校', trigger: 'change' }]
};

const open = (row?: TeacherVO) => {
  visible.value = true;
  title.value = row?.teacherId ? '编辑教师' : '新增教师';
  Object.assign(form, defaultForm(), row ?? {});
  if (!row?.teacherId && !form.schoolId && props.schoolOptions.length) {
    form.schoolId = props.schoolOptions.find((item) => item.current)?.schoolId ?? props.schoolOptions[0].schoolId;
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (form.teacherId) {
      await updateTeacher({ ...form });
    } else {
      const payload = { ...form };
      delete payload.teacherId;
      await addTeacher(payload);
    }
    ElMessage.success(form.teacherId ? '修改成功' : '新增成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
