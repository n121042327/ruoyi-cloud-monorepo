<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/student-cross-transfer.html 的 .page-head -->
    <div class="page-head">
      <h1>跨校转学（转出校）</h1>
      <span class="scope-hint">数据范围：本校（转出校） · 转学涉及两个学校租户</span>
      <el-tag type="primary">学号跨校保持不变</el-tag>
    </div>
    <el-card shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <span>跨校转学（转出校）</span>
          <div class="flex items-center gap-2">
            <el-tag type="info">学号跨校保持不变</el-tag>
            <el-tag>向导 {{ activeStep }} / 4</el-tag>
          </div>
        </div>
      </template>
      <el-steps :active="activeStep - 1" align-center finish-status="success">
        <el-step title="选择学生" />
        <el-step title="选择转入校与目标班级" />
        <el-step title="确认与提交" />
        <el-step title="结果" />
      </el-steps>
      <el-alert
        class="mt-3"
        type="info"
        :closable="false"
        title="跨校转学在同一平台内跨学校租户执行：转出校发起 → 释放行政班关系并把原在校记录置为已转出 → 转入校接收并新建「转入未报到」记录 → 学生报到后转为在读。"
      />
    </el-card>

    <!-- 步骤 1：选择学生 -->
    <el-card v-if="activeStep === 1" shadow="never" class="mt-3" data-layout-group="选择学生">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 1 · 选择学生</span>
          <el-tag type="info">只列在本校在读且无未完成转学单的学生</el-tag>
        </div>
      </template>
      <el-table v-loading="loading" border :data="studentList" highlight-current-row @current-change="handleSelectStudent">
        <el-table-column width="50" align="center">
          <template #default="scope">
            <el-radio v-model="selectedStudentId" :value="scope.row.studentId">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="学号" prop="studentNo" width="130" />
        <el-table-column label="姓名" prop="studentName" width="110" />
        <el-table-column label="年级" prop="gradeName" width="140" />
        <el-table-column label="班级" prop="className" width="140" />
        <el-table-column label="学籍状态" prop="enrollmentStatus" width="110" align="center">
          <template #default="scope">
            <el-tag type="success" size="small">{{ ENROLLMENT_STATUS_LABEL[scope.row.enrollmentStatus] ?? scope.row.enrollmentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可执行状态" min-width="200">
          <template #default>在读且无未完成转学单，可发起转学</template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有可发起转学的学生" />
        </template>
      </el-table>
    </el-card>

    <!-- 步骤 2：选择转入校与目标班级 -->
    <el-card v-else-if="activeStep === 2" shadow="never" class="mt-3" data-layout-group="选择转入校与目标班级">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 2 · 选择转入校与目标班级</span>
          <el-tag type="info">只暴露必要字段</el-tag>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="转入校" prop="toSchoolId">
          <el-select v-model="form.toSchoolId" placeholder="请选择" class="w-full" @change="handleSchoolChange">
            <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标年级" prop="toGradeId">
          <el-select v-model="form.toGradeId" placeholder="请选择" class="w-full" @change="handleGradeChange">
            <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标班级" prop="toClassId">
          <el-select v-model="form.toClassId" placeholder="暂不指定（报到时候再分班）" clearable class="w-full">
            <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
          </el-select>
          <div class="hint">接收前，学生不会被计入转入校的任何在读数与花名册。</div>
        </el-form-item>
        <el-form-item label="申请日期" prop="effectiveDate">
          <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 步骤 3：确认与提交 -->
    <el-card v-else-if="activeStep === 3" shadow="never" class="mt-3" data-layout-group="确认与提交">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 3 · 确认与提交</span>
          <el-tag type="info">转学单只读字段预览</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生">{{ selectedStudent?.studentName }} · {{ selectedStudent?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="转入校">{{ schoolName }}</el-descriptions-item>
        <el-descriptions-item label="目标年级 / 班级"> {{ gradeName }} / {{ className || '暂不分班' }} </el-descriptions-item>
        <el-descriptions-item label="学号">保持不变</el-descriptions-item>
      </el-descriptions>
      <el-alert
        class="mt-3"
        type="warning"
        :closable="false"
        title="提交后：本校原在校记录置为「已转出」、行政班关系释放；转入校建立「转入未报到」记录；转入校可撤销接收。"
      />
    </el-card>

    <!-- 步骤 4：结果 -->
    <el-card v-else shadow="never" class="mt-3" data-layout-group="结果">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 4 · 结果</span>
          <el-tag type="warning">待转入校接收</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="转学单号">{{ result.transferNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">待接收</el-descriptions-item>
        <el-descriptions-item label="学号">不变</el-descriptions-item>
        <el-descriptions-item label="两侧审计">已写入</el-descriptions-item>
      </el-descriptions>
      <div class="hint mt-2">转入校接收后学生到校报到，状态转为「在读」；转学两侧的操作都写入审计，且可被任一侧租户导出。</div>
    </el-card>

    <el-card shadow="never" class="mt-3">
      <div class="flex justify-between items-center">
        <el-button :disabled="activeStep === 1" @click="activeStep -= 1">上一步</el-button>
        <div class="flex items-center gap-2">
          <el-button v-if="activeStep === 4" @click="handleCancelTransfer">撤销申请</el-button>
          <el-button v-if="activeStep < 3" type="primary" @click="handleNext">下一步</el-button>
          <el-button v-else-if="activeStep === 3" type="primary" :loading="submitting" @click="handleSubmit">提交转学申请</el-button>
          <el-button v-else @click="handleBackToList">返回学生列表</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { addTransfer, cancelTransfer } from '@/api/edu/promotion';
import type { TransferForm, TransferOrderVO } from '@/api/edu/promotion/types';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import { listStudent } from '@/api/edu/student';
import type { StudentVO } from '@/api/edu/student/types';
import { ENROLLMENT_STATUS_LABEL } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduStudentCrossTransfer' });

const router = useRouter();
const route = useRoute();

const activeStep = ref(1);
const loading = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const studentList = ref<StudentVO[]>([]);
const schoolOptions = ref<SchoolVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);
const classOptions = ref<ClassVO[]>([]);
const selectedStudentId = ref('');
const result = ref<Partial<TransferOrderVO>>({});

const form = reactive<TransferForm>({ studentId: '', toSchoolId: '', toGradeId: '', toClassId: '', effectiveDate: '', remark: '' });

const selectedStudent = computed(() => studentList.value.find((item) => item.studentId === selectedStudentId.value));
const schoolName = computed(() => schoolOptions.value.find((item) => item.schoolId === form.toSchoolId)?.schoolName ?? '—');
const gradeName = computed(() => gradeOptions.value.find((item) => item.gradeId === form.toGradeId)?.gradeName ?? '—');
const className = computed(() => classOptions.value.find((item) => item.classId === form.toClassId)?.className ?? '');

const rules: FormRules = {
  toSchoolId: [{ required: true, message: '请选择转入校', trigger: 'change' }],
  toGradeId: [{ required: true, message: '请选择目标年级', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择申请日期', trigger: 'change' }]
};

const loadStudents = async () => {
  loading.value = true;
  try {
    const res = await listStudent({ pageNum: 1, pageSize: 100, enrollmentStatus: 'enrolled' });
    studentList.value = res.rows ?? [];
  } finally {
    loading.value = false;
  }
};

const loadSchoolOptions = async () => {
  try {
    const res = await listSchool();
    schoolOptions.value = res.data ?? [];
  } catch {
    schoolOptions.value = [];
  }
};

const handleSelectStudent = (row?: StudentVO) => {
  if (row) {
    selectedStudentId.value = row.studentId;
  }
};

const handleSchoolChange = async () => {
  form.toGradeId = '';
  form.toClassId = '';
  classOptions.value = [];
  try {
    const res = await listGrade({ schoolId: form.toSchoolId });
    gradeOptions.value = res.data ?? [];
  } catch {
    gradeOptions.value = [];
  }
};

const handleGradeChange = async () => {
  form.toClassId = '';
  try {
    const res = await listClass({ schoolId: form.toSchoolId, gradeId: form.toGradeId });
    classOptions.value = res.data ?? [];
  } catch {
    classOptions.value = [];
  }
};

const handleNext = async () => {
  if (activeStep.value === 1) {
    if (!selectedStudentId.value) {
      ElMessage.warning('请先选择要转学的学生');
      return;
    }
    form.studentId = selectedStudentId.value;
    activeStep.value = 2;
    return;
  }
  if (activeStep.value === 2) {
    await formRef.value?.validate();
    activeStep.value = 3;
    return;
  }
  activeStep.value += 1;
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    const res = await addTransfer({ ...form });
    result.value = res.data ?? {};
    activeStep.value = 4;
    ElMessage.success('已提交转学申请');
  } finally {
    submitting.value = false;
  }
};

/** 撤销申请：转学单逻辑删除并留痕；转入校尚未接收时不产生转出记录（REQ-PRM-054） */
const handleCancelTransfer = () => {
  const transferId = result.value.transferId;
  if (!transferId) {
    return;
  }
  ElMessageBox.confirm('确认撤销本次转学申请？撤销后转学单记录逻辑删除并留痕。', '撤销确认', {
    confirmButtonText: '确认撤销',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await cancelTransfer(transferId);
      ElMessage.success('已撤销转学申请');
      handleBackToList();
    })
    .catch(() => undefined);
};

const handleBackToList = () => {
  router.push('/edu/student/list');
};

onMounted(async () => {
  await Promise.all([loadStudents(), loadSchoolOptions()]);
  // 从学生列表带学号进入时预选该学生（原型：冲突行点击「跨校转学」直接预填）
  const studentId = route.query.studentId;
  if (typeof studentId === 'string' && studentId) {
    selectedStudentId.value = studentId;
    form.studentId = studentId;
  }
});
</script>
