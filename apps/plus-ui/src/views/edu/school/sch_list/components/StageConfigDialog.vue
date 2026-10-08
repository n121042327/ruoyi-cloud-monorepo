<template>
  <el-dialog v-model="visible" title="学段配置" width="560px" append-to-body>
    <!-- PAGE-SCH-STAGE：至少选择一个学段；已被年级引用的学段置灰并说明原因（REQ-SCH-032 ~ 035） -->
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="开设学段" prop="stageCodes">
        <el-checkbox-group v-model="form.stageCodes">
          <el-checkbox v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :value="item.value" :disabled="lockedCodes.includes(item.value)">
            {{ item.label }}
          </el-checkbox>
        </el-checkbox-group>
        <div class="hint">
          至少选择一个学段；学段序号固定映射（小学 1–6、初中 / 高中 1–3，RV-GRD-03）；未开设的学段不能建对应年级（REQ-SCH-033）。
        </div>
        <div v-if="lockedCodes.length" class="hint">已开设且已有年级的学段不能移除：{{ lockedLabel }}（REQ-SCH-034）；需要调整请先处理对应年级。</div>
        <div v-if="schoolName" class="hint">当前学校：{{ schoolName }}</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存配置</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { listSchoolStage, saveSchoolStage } from '@/api/edu/school';
import type { SchoolStageVO, SchoolVO } from '@/api/edu/school/types';
import { STAGE_CODE_LABEL, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSchoolStageDialog' });

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const schoolId = ref('');
const schoolName = ref('');
const formRef = ref<FormInstance>();
const lockedCodes = ref<string[]>([]);

const form = reactive<{ stageCodes: string[] }>({ stageCodes: [] });

const lockedLabel = computed(() =>
  lockedCodes.value
    .map((code) => STAGE_CODE_LABEL[code] ?? code)
    .filter(Boolean)
    .join('、')
);

const rules: FormRules = {
  stageCodes: [{ type: 'array', required: true, min: 1, message: '至少选择一个学段', trigger: 'change' }]
};

/** 打开弹窗：加载该校已开设学段，gradeCount > 0 的学段锁定（不允许移除） */
const open = async (row: SchoolVO) => {
  schoolId.value = row.schoolId;
  schoolName.value = row.schoolName ?? '';
  form.stageCodes = [];
  lockedCodes.value = [];
  visible.value = true;
  const res = await listSchoolStage(row.schoolId);
  const rows: SchoolStageVO[] = res.rows ?? [];
  form.stageCodes = rows.filter((item) => item.status === '1').map((item) => item.stageCode as string);
  lockedCodes.value = rows.filter((item) => (item.gradeCount ?? 0) > 0).map((item) => item.stageCode as string);
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  submitting.value = true;
  try {
    await saveSchoolStage(schoolId.value, { stageCodes: [...form.stageCodes] });
    ElMessage.success('已保存学段配置：配置后可创建对应年级（BR-GRADE-006）');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
