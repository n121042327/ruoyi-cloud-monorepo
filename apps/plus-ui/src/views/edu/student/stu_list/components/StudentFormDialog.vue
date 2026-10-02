<template>
  <el-dialog v-model="visible" :title="title" width="720px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 基础信息：学号（只读发号）→ 全国学籍号 → 姓名 → 性别 -->
      <h3 class="form-section-title" data-layout-group="基础信息">基础信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学号" prop="studentNo">
            <el-input :model-value="form.studentId ? form.studentNo : '保存后自动生成'" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="全国学籍号" prop="nationalStudentNo">
            <el-input v-model="form.nationalStudentNo" placeholder="G / L 开头，可留空" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="studentName">
            <el-input v-model="form.studentName" placeholder="请输入姓名" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别" class="w-full">
              <el-option v-for="item in GENDER_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 教育信息：入学年份 → 学段 → 年级 → 班级（先父级后子级） -->
      <h3 class="form-section-title" data-layout-group="教育信息">教育信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="入学年份" prop="enrollYear">
            <el-input v-model="form.enrollYear" placeholder="如 2026" maxlength="4" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学段" prop="stageCode">
            <el-select v-model="form.stageCode" placeholder="请选择学段" class="w-full">
              <el-option v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年级" prop="gradeId">
            <el-select v-model="form.gradeId" placeholder="请选择年级" class="w-full" @change="handleGradeChange">
              <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班级" prop="classId">
            <el-select v-model="form.classId" placeholder="请选择班级" class="w-full" clearable>
              <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-alert
        class="mt-2"
        type="info"
        :closable="false"
        title="证件与联系方式、监护人两组字段在阶段 6 的下一批交付（对应原型 PAGE-STU-CREATE 的第 2、3 步）。"
      />
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
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { addStudent, updateStudent } from '@/api/edu/student';
import type { StudentForm } from '@/api/edu/student/types';
import { GENDER_OPTIONS, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

interface Props {
  /** 当前学校上下文：决定年级与班级下拉的范围 */
  schoolId?: string;
}

const props = withDefaults(defineProps<Props>(), { schoolId: '' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新增学生');
const submitting = ref(false);
const formRef = ref<FormInstance>();
const gradeOptions = ref<GradeVO[]>([]);
const classOptions = ref<ClassVO[]>([]);

const defaultForm = (): StudentForm => ({
  studentId: undefined,
  nationalStudentNo: '',
  studentName: '',
  gender: '',
  enrollYear: '',
  stageCode: '',
  gradeId: '',
  classId: ''
});

const form = reactive<StudentForm & { studentNo?: string }>(defaultForm());

const rules: FormRules = {
  studentName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  enrollYear: [
    { required: true, message: '请输入入学年份', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '入学年份为 4 位数字', trigger: 'blur' }
  ],
  stageCode: [{ required: true, message: '请选择学段', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
};

const loadGradeOptions = async () => {
  try {
    const res = await listGrade({ schoolId: props.schoolId });
    gradeOptions.value = res.data ?? [];
  } catch {
    gradeOptions.value = [];
  }
};

const loadClassOptions = async () => {
  if (!form.gradeId) {
    classOptions.value = [];
    return;
  }
  try {
    const res = await listClass({ schoolId: props.schoolId, gradeId: form.gradeId });
    classOptions.value = res.data ?? [];
  } catch {
    classOptions.value = [];
  }
};

const handleGradeChange = async () => {
  form.classId = '';
  await loadClassOptions();
};

/** 打开弹窗：不传 row 为新增，传 row 为编辑 */
const open = async (row?: Partial<StudentForm> & { studentNo?: string }) => {
  visible.value = true;
  title.value = row?.studentId ? '编辑学生' : '新增学生';
  Object.assign(form, defaultForm(), row ?? {});
  await loadGradeOptions();
  if (form.gradeId) {
    await loadClassOptions();
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    const payload: StudentForm = { ...form };
    if (payload.studentId) {
      await updateStudent(payload);
    } else {
      delete payload.studentId;
      await addStudent(payload);
    }
    ElMessage.success(payload.studentId ? '修改成功' : '新增成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
