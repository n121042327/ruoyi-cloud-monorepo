<template>
  <el-dialog v-model="visible" :title="title" width="620px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 教育信息：学校 → 学段 → 入学年份 → 学段内序号 → 年级名称 -->
      <h3 class="form-section-title" data-layout-group="教育信息">教育信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学校" prop="schoolId">
            <el-select v-model="form.schoolId" placeholder="请选择学校" class="w-full" :disabled="!!form.gradeId">
              <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学段" prop="stageCode">
            <el-select v-model="form.stageCode" placeholder="请选择学段" class="w-full" :disabled="!!form.gradeId">
              <el-option v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入学年份" prop="enrollYear">
            <el-input v-model="form.enrollYear" placeholder="如 2026" maxlength="4" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学段内序号" prop="gradeLevel">
            <el-input-number v-model="form.gradeLevel" :min="1" :max="6" controls-position="right" class="w-full" :disabled="!!form.gradeId" />
            <div class="hint">小学 1-6、初中 1-3、高中 1-3；学段与序号一经创建不可修改（REQ-GRD-018）。</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年级名称" prop="gradeName">
            <el-input v-model="form.gradeName" placeholder="默认按规则生成，可微调" clearable />
          </el-form-item>
        </el-col>
      </el-row>
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
import { addGrade, updateGrade } from '@/api/edu/grade';
import type { GradeForm, GradeVO } from '@/api/edu/grade/types';
import type { SchoolVO } from '@/api/edu/school/types';
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

const props = withDefaults(defineProps<{ schoolOptions?: SchoolVO[] }>(), { schoolOptions: () => [] });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新增年级');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): GradeForm => ({ gradeId: undefined, schoolId: '', stageCode: '', enrollYear: '', gradeLevel: 1, gradeName: '' });

const form = reactive<GradeForm>(defaultForm());

const rules: FormRules = {
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }],
  stageCode: [{ required: true, message: '请选择学段', trigger: 'change' }],
  enrollYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  gradeLevel: [{ required: true, message: '请输入学段内序号', trigger: 'change' }],
  gradeName: [{ required: true, message: '请输入年级名称', trigger: 'blur' }]
};

const open = (row?: GradeVO) => {
  visible.value = true;
  title.value = row?.gradeId ? '编辑年级' : '新增年级';
  Object.assign(form, defaultForm(), row ?? {});
  if (!row?.gradeId && !form.schoolId && props.schoolOptions.length) {
    form.schoolId = props.schoolOptions.find((item) => item.current)?.schoolId ?? props.schoolOptions[0].schoolId;
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (form.gradeId) {
      await updateGrade({ ...form });
    } else {
      const payload = { ...form };
      delete payload.gradeId;
      await addGrade(payload);
    }
    ElMessage.success(form.gradeId ? '修改成功' : '新增成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
