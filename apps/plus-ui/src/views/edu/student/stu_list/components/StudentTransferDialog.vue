<template>
  <el-dialog v-model="visible" :title="`调班 · ${student.studentName ?? ''}`" width="680px" append-to-body>
    <el-alert type="info" :closable="false" title="调班只改班级关系，不改学籍状态与学号；学生班级归属的唯一写入入口在班级管理（DP-01）。" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="mt-3">
      <!-- 班级关系：目标班级 → 生效日期 -->
      <h3 class="form-section-title" data-layout-group="班级关系">班级关系</h3>
      <el-form-item label="目标班级" prop="targetClassId">
        <el-select v-model="form.targetClassId" placeholder="请选择目标班级" class="w-full">
          <el-option
            v-for="item in classOptions"
            :key="item.classId"
            :label="classLabel(item)"
            :value="item.classId"
            :disabled="item.status === 'disabled'"
          />
        </el-select>
        <div class="hint">已停用的班级不可选；容量超限只提示不阻塞（BR-CLASS-005）。</div>
      </el-form-item>
      <el-form-item label="生效日期" prop="effectiveDate">
        <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" class="w-full" />
        <div class="hint">默认取当前学年学期开始日；生效日期之前的归属按历史关系查询（与班级模块调班口径一致）。</div>
      </el-form-item>

      <!-- 调班说明 -->
      <h3 class="form-section-title" data-layout-group="调班说明">调班说明</h3>
      <el-form-item label="调班原因" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如：随班调整 / 家长申请" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">确认调班</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { listClass, transferClass } from '@/api/edu/class';
import type { ClassTransferForm, ClassVO } from '@/api/edu/class/types';
import { getCurrentTerm } from '@/api/edu/term';
import type { StudentVO } from '@/api/edu/student/types';

interface Props {
  /** 当前学校上下文：决定目标班级下拉范围 */
  schoolId?: string;
}

const props = withDefaults(defineProps<Props>(), { schoolId: '' });
const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const student = ref<Partial<StudentVO>>({});
const classOptions = ref<ClassVO[]>([]);

const form = reactive<ClassTransferForm>({ studentId: '', targetClassId: '', effectiveDate: '', remark: '' });

const rules: FormRules = {
  targetClassId: [{ required: true, message: '请选择目标班级', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
};

/** 选项文案带在读人数 / 容量，便于判断容量超限（只提示不阻塞） */
const classLabel = (item: ClassVO) => {
  const extra = item.enrolledCount != null && item.capacity != null ? `（在读 ${item.enrolledCount} / 容量 ${item.capacity}）` : '';
  return `${item.className}${extra}`;
};

/** 生效日期默认取当前学年学期开始日，取不到时留空由用户选择 */
const loadDefaultEffectiveDate = async () => {
  try {
    const res = await getCurrentTerm();
    form.effectiveDate = res.data?.startDate ?? '';
  } catch {
    form.effectiveDate = '';
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

/** 打开弹窗：row 为学生列表行 */
const open = async (row: StudentVO | Partial<StudentVO>) => {
  visible.value = true;
  student.value = row;
  form.studentId = (row.studentId as string) ?? '';
  form.targetClassId = '';
  form.remark = '';
  await Promise.all([loadClassOptions(), loadDefaultEffectiveDate()]);
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await transferClass({ ...form });
    ElMessage.success('已提交调班');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
