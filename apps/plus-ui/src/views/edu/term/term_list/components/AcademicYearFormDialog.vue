<template>
  <el-dialog v-model="visible" :title="title" width="560px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 学年信息：学年 → 开始日期 → 结束日期 → 学期数（PRD 6.1 PAGE-TERM-CREATE） -->
      <h3 class="form-section-title" data-layout-group="学年信息">学年信息</h3>
      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="学年" prop="academicYearCode">
            <el-input v-model="form.academicYearCode" placeholder="格式 YYYY-YYYY，如 2026-2027" clearable />
            <div class="hint">格式 YYYY-YYYY（连续两个自然年，结束年 = 起始年 + 1）；学年编码在同一学校内唯一。</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="!form.academicYearId">
          <el-form-item label="学期数" prop="termCount">
            <el-select v-model="form.termCount" class="w-full">
              <el-option :value="2" label="2 个学期（默认）" />
              <el-option :value="1" label="1 个学期" />
            </el-select>
            <div class="hint">创建学年时一并生成学期；学期可在「学期管理」里调整。</div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-alert
        class="mb-2"
        type="info"
        :closable="false"
        title="学年不能与已有学年重叠：新学年开始日必须等于上一学年结束日 + 1 天。新建学年写入审计日志。"
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
import { addAcademicYear, updateAcademicYear } from '@/api/edu/term';
import type { AcademicYearForm, AcademicYearVO } from '@/api/edu/term/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新建学年');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): AcademicYearForm => ({
  academicYearId: undefined,
  schoolId: '',
  academicYearCode: '',
  startDate: '',
  endDate: '',
  termCount: 2
});

const form = reactive<AcademicYearForm>(defaultForm());

const codePattern = /^\d{4}-\d{4}$/;

const rules: FormRules = {
  academicYearCode: [
    { required: true, message: '请填写学年', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback: (error?: Error) => void) => {
        if (!value || !codePattern.test(value)) {
          callback(new Error('学年编码格式必须是 YYYY-YYYY，如 2026-2027'));
          return;
        }
        const [startYear, endYear] = value.split('-').map(Number);
        if (endYear !== startYear + 1) {
          callback(new Error('结束年必须是起始年 + 1，且为连续两个自然年'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
};

/** 打开弹窗；传入 row 时为编辑（学年编码不可改） */
const open = (row?: AcademicYearVO) => {
  visible.value = true;
  title.value = row?.academicYearId ? '编辑学年' : '新建学年';
  Object.assign(form, defaultForm(), row ?? {});
  form.termCount = row?.termCount || 2;
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (form.academicYearId) {
      await updateAcademicYear({ ...form });
    } else {
      const payload = { ...form };
      delete payload.academicYearId;
      await addAcademicYear(payload);
    }
    ElMessage.success(form.academicYearId ? '修改成功' : '已创建学年与学期');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
