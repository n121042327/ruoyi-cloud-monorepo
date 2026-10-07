<template>
  <el-dialog v-model="visible" :title="`学籍异动 · ${student.studentName ?? ''}`" width="760px" append-to-body>
    <el-alert
      type="info"
      :closable="false"
      title="可执行的异动由当前状态决定（listEnrollmentStatusOption）；终态（已转出 / 毕业 / 结业 / 肄业 / 开除 / 退学 / 死亡）没有任何异动出口。"
    />
    <el-alert
      v-if="isPromotionMode"
      type="warning"
      :closable="false"
      class="mt-2"
      title="升班与学籍异动口径：与「学籍异动」同一份字段、同一个接口；差别在阶段限制（义务教育不得开除）、审批要求（退学 / 开除 / 死亡）与生效后的数据范围缓存刷新。"
    />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="mt-3">
      <!-- 异动信息：异动类型 → 生效日期 -->
      <h3 class="form-section-title" data-layout-group="异动信息">异动信息</h3>
      <el-form-item label="异动类型" prop="changeType">
        <el-select v-model="form.changeType" placeholder="请选择异动类型" class="w-full" @change="handleTypeChange">
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" :disabled="item.disabled" />
        </el-select>
        <div class="hint">选项随当前状态变化；非法流转后端直接拒绝并说明原因（BR-PROMO-010）。</div>
      </el-form-item>
      <el-form-item label="生效日期" prop="effectiveDate">
        <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" class="w-full" />
        <div class="hint">生效后立即更新相关学生与新班级的权限缓存（DP-06）。</div>
      </el-form-item>
      <el-alert
        v-if="selectedOption?.needApproval"
        type="warning"
        :closable="false"
        title="「退学 / 开除 / 死亡」等异动需要校级管理员审批；义务教育阶段禁止开除（REQ-PRM-043 / 044）。"
      />

      <!-- 复学 / 报到安排：条件必填 -->
      <h3 class="form-section-title" data-layout-group="复学 / 报到安排">复学 / 报到安排</h3>
      <el-form-item label="复学 / 报到后的班级" prop="classId">
        <el-select v-model="form.classId" placeholder="不需要（休学 / 出国 / 转出等）" clearable :disabled="!needClass" class="w-full">
          <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
        </el-select>
        <div class="hint">复学 / 报到 / 寻回必须指定班级（REQ-PRM-041）。</div>
      </el-form-item>

      <!-- 异动说明 -->
      <h3 class="form-section-title" data-layout-group="异动说明">异动说明</h3>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="如：家长申请休学一学期，附医院证明" />
        <div class="hint">原状态、新状态、生效日期、原因、操作人全部写审计且不可删除（BR-PROMO-012）。</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">提交异动</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { changeEnrollmentStatus, listEnrollmentStatusOption } from '@/api/edu/student';
import type { EnrollmentChangeForm, EnrollmentStatusOptionVO, StudentVO } from '@/api/edu/student/types';

interface Props {
  /** 当前学校上下文：决定「复学 / 报到后的班级」下拉范围 */
  schoolId?: string;
  /**
   * 入口口径：student = 学生管理的「学籍异动」；promotion = 升班模块的「异动登记（升班口径）」。
   * 两者字段与接口完全相同（PAGE-PRM-CHANGE 与 PAGE-STU-STATUS 同一写入口），差异只在提示与选项来源。
   */
  mode?: 'student' | 'promotion';
}

const props = withDefaults(defineProps<Props>(), { schoolId: '', mode: 'student' });
const emit = defineEmits<{ success: [] }>();

const isPromotionMode = computed(() => props.mode === 'promotion');

/**
 * 升班口径的异动类型：按原型 PAGE-PRM-CHANGE 的固定清单展示；
 * 学生口径则用 listEnrollmentStatusOption 按当前状态动态返回。
 */
const PROMOTION_OPTIONS: EnrollmentStatusOptionVO[] = [
  { value: 'suspend', label: '休学' },
  { value: 'resume', label: '复学', needClass: true },
  { value: 'abroad', label: '出国（保留学籍）' },
  { value: 'missing', label: '登记失踪' },
  { value: 'transfer_out', label: '转出（含跨校转学）' },
  { value: 'withdraw', label: '退学（校级管理员审批）', needApproval: true },
  { value: 'deceased', label: '死亡登记（校级管理员 + 证明材料）', needApproval: true }
];

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const student = ref<Partial<StudentVO>>({});
const options = ref<EnrollmentStatusOptionVO[]>([]);
const classOptions = ref<ClassVO[]>([]);

const form = reactive<EnrollmentChangeForm>({ changeType: '', effectiveDate: '', classId: '', reason: '' });

const selectedOption = computed(() => options.value.find((item) => item.value === form.changeType));
/** 复学 / 报到类异动必须指定班级 */
const needClass = computed(() => selectedOption.value?.needClass === true);

const rules: FormRules = {
  changeType: [{ required: true, message: '请选择异动类型', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  reason: [
    { required: true, message: '请填写异动原因（至少 5 个字）', trigger: 'blur' },
    { min: 5, message: '异动原因至少 5 个字', trigger: 'blur' }
  ],
  classId: [
    {
      validator: (_rule, value, callback) => {
        if (needClass.value && !value) {
          callback(new Error('复学 / 报到必须指定班级'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ]
};

const loadOptions = async (studentId: string) => {
  if (isPromotionMode.value) {
    options.value = PROMOTION_OPTIONS;
    return;
  }
  try {
    const res = await listEnrollmentStatusOption(studentId);
    options.value = res.data ?? [];
  } catch {
    options.value = [];
  }
};

const loadClassOptions = async () => {
  try {
    const res = await listClass({ schoolId: props.schoolId });
    classOptions.value = res.data ?? [];
  } catch {
    classOptions.value = [];
  }
};

const handleTypeChange = () => {
  if (!needClass.value) {
    form.classId = '';
  }
};

/** 打开弹窗：row 为学生列表行 */
const open = async (row: StudentVO | Partial<StudentVO>) => {
  visible.value = true;
  student.value = row;
  form.changeType = '';
  form.effectiveDate = '';
  form.classId = '';
  form.reason = '';
  await Promise.all([loadOptions(row.studentId as string), loadClassOptions()]);
};

const submitForm = async () => {
  await formRef.value?.validate();
  const studentId = student.value.studentId;
  if (!studentId) {
    return;
  }
  submitting.value = true;
  try {
    await changeEnrollmentStatus(studentId, { ...form, classId: needClass.value ? form.classId : undefined });
    ElMessage.success('已提交学籍异动');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
