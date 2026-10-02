<template>
  <el-dialog v-model="visible" :title="title" width="860px" append-to-body>
    <!-- 三步向导：学籍信息 → 证件与联系 → 监护人（对应原型 PAGE-STU-CREATE） -->
    <el-steps :active="activeStep" align-center finish-status="success" class="mb-4">
      <el-step title="学籍信息" />
      <el-step title="证件与联系" />
      <el-step title="监护人" />
    </el-steps>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 第 1 步 -->
      <template v-if="activeStep === 0">
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
      </template>

      <!-- 第 2 步 -->
      <template v-else-if="activeStep === 1">
        <!-- 证件信息：证件类型 → 证件号码 → 出生日期 -->
        <h3 class="form-section-title" data-layout-group="证件信息">证件信息</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="证件类型" prop="idType">
              <el-select v-model="form.idType" placeholder="未填写" clearable class="w-full">
                <el-option v-for="item in ID_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件号码" prop="idCardNo">
              <el-input v-model="form.idCardNo" placeholder="填写时校验格式与唯一性" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择出生日期" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 联系方式：学生手机号 → 联系地址 -->
        <h3 class="form-section-title" data-layout-group="联系方式">联系方式</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学生手机号" prop="studentPhone">
              <el-input v-model="form.studentPhone" placeholder="可留空" maxlength="11" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系地址" prop="address">
              <el-input v-model="form.address" placeholder="可留空" clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 第 3 步 -->
      <template v-else>
        <h3 class="form-section-title" data-layout-group="监护人">监护人（上限 3）</h3>
        <el-table :data="guardians" border>
          <el-table-column label="监护人姓名" min-width="140">
            <template #default="scope">
              <el-input v-model="scope.row.guardianName" placeholder="请输入姓名" />
            </template>
          </el-table-column>
          <el-table-column label="与学生关系" width="150">
            <template #default="scope">
              <el-select v-model="scope.row.relation" placeholder="请选择">
                <el-option v-for="item in GUARDIAN_RELATION_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="监护人电话" width="160">
            <template #default="scope">
              <el-input v-model="scope.row.guardianPhone" placeholder="可留空" maxlength="11" />
            </template>
          </el-table-column>
          <el-table-column label="主要联系人" width="110" align="center">
            <template #default="scope">
              <el-radio v-model="primaryIndex" :value="scope.$index" @change="handlePrimaryChange(scope.$index)">&nbsp;</el-radio>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160">
            <template #default="scope">
              <el-input v-model="scope.row.remark" placeholder="可留空" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center" data-layout-group="操作">
            <template #default="scope">
              <el-button link type="danger" @click="removeGuardian(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button class="mt-2" :disabled="guardians.length >= 3" icon="Plus" @click="addGuardian">添加监护人</el-button>
        <div class="hint mt-1">同一家长可关联多个孩子；监护人手机号是敏感字段，列表默认掩码展示（GAP-015）。</div>
      </template>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button v-if="activeStep > 0" @click="activeStep -= 1">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="handleNext">下一步</el-button>
        <el-button v-else type="primary" :loading="submitting" @click="submitForm">保存</el-button>
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
import { addStudent, saveStudentGuardian, updateStudent } from '@/api/edu/student';
import type { GuardianForm, StudentForm } from '@/api/edu/student/types';
import { GENDER_OPTIONS, GUARDIAN_RELATION_OPTIONS, ID_TYPE_OPTIONS, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

interface Props {
  /** 当前学校上下文：决定年级与班级下拉的范围 */
  schoolId?: string;
}

const props = withDefaults(defineProps<Props>(), { schoolId: '' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新增学生');
const submitting = ref(false);
const activeStep = ref(0);
const formRef = ref<FormInstance>();
const gradeOptions = ref<GradeVO[]>([]);
const classOptions = ref<ClassVO[]>([]);
const guardians = ref<GuardianForm[]>([]);
const primaryIndex = ref(-1);

const defaultForm = (): StudentForm => ({
  studentId: undefined,
  nationalStudentNo: '',
  studentName: '',
  gender: '',
  enrollYear: '',
  stageCode: '',
  gradeId: '',
  classId: '',
  idType: '',
  idCardNo: '',
  birthDate: '',
  studentPhone: '',
  address: ''
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

const addGuardian = () => {
  if (guardians.value.length >= 3) {
    ElMessage.warning('监护人数量上限为 3');
    return;
  }
  guardians.value.push({ guardianName: '', relation: '', guardianPhone: '', isPrimary: false, remark: '' });
};

const removeGuardian = (index: number) => {
  guardians.value.splice(index, 1);
  if (primaryIndex.value >= guardians.value.length) {
    primaryIndex.value = guardians.value.length - 1;
  }
};

/** 主要联系人在同一学生下唯一 */
const handlePrimaryChange = (index: number) => {
  guardians.value.forEach((item, i) => {
    item.isPrimary = i === index;
  });
};

const handleNext = async () => {
  if (activeStep.value === 0) {
    // 第 1 步含全部必填字段，先校验再进入下一步
    await formRef.value?.validate();
  }
  activeStep.value += 1;
};

/** 打开弹窗：不传 row 为新增，传 row 为编辑 */
const open = async (row?: Partial<StudentForm> & { studentNo?: string }) => {
  visible.value = true;
  activeStep.value = 0;
  title.value = row?.studentId ? '编辑学生' : '新增学生';
  Object.assign(form, defaultForm(), row ?? {});
  guardians.value = [];
  primaryIndex.value = -1;
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
    let studentId = payload.studentId;
    if (studentId) {
      await updateStudent(payload);
    } else {
      delete payload.studentId;
      const created = await addStudent(payload);
      studentId = created.data?.studentId;
    }
    // 监护人落 edu_student_guardian；班主任是唯一写入口（DP-01）
    for (const guardian of guardians.value) {
      if (!guardian.guardianName || !guardian.relation) {
        continue;
      }
      if (studentId) {
        await saveStudentGuardian(studentId, guardian);
      }
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
