<template>
  <el-dialog v-model="visible" title="批量生成班级" width="560px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="按「起始序号 ~ 结束序号」生成同年级的多个班级，已存在的班级名会被后端拒绝。" />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="学年学期" prop="termId">
        <el-select v-model="form.termId" placeholder="请选择学年学期" class="w-full">
          <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName || item.termId" :value="item.termId" />
        </el-select>
      </el-form-item>
      <el-form-item label="年级" prop="gradeId">
        <el-select v-model="form.gradeId" placeholder="请选择年级" class="w-full">
          <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级类型" prop="classType">
        <el-radio-group v-model="form.classType">
          <el-radio value="administrative">行政班</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="序号范围" prop="range">
        <div class="flex items-center gap-2">
          <el-input-number v-model="form.startNo" :min="1" :max="99" controls-position="right" />
          <span>~</span>
          <el-input-number v-model="form.endNo" :min="1" :max="99" controls-position="right" />
          <span class="text-xs">共 {{ count }} 个班级</span>
        </div>
      </el-form-item>
      <el-form-item label="容量">
        <el-input-number v-model="form.classCapacity" :min="1" :max="200" controls-position="right" />
        <div class="hint">容量只提示不强制（BR-CLASS-005）</div>
      </el-form-item>
      <el-form-item label="预览">
        <div class="text-xs">{{ preview }}</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!count" @click="submitForm">生成 {{ count }} 个班级</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { batchAddClass } from '@/api/edu/class';
import type { GradeVO } from '@/api/edu/grade/types';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduClassBatchDialog' });

const props = withDefaults(defineProps<{ termOptions?: TermVO[]; gradeOptions?: GradeVO[]; schoolId?: string }>(), {
  termOptions: () => [],
  gradeOptions: () => [],
  schoolId: ''
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  termId: '',
  gradeId: '',
  classType: 'administrative',
  startNo: 1,
  endNo: 4,
  classCapacity: 45
});

const count = computed(() => Math.max(0, form.endNo - form.startNo + 1));

const gradeName = computed(() => props.gradeOptions.find((g) => g.gradeId === form.gradeId)?.gradeName ?? '年级');

const preview = computed(() =>
  count.value > 0
    ? Array.from({ length: Math.min(count.value, 6) }, (_, i) => `${gradeName.value} (${form.startNo + i}) 班`).join('、') +
      (count.value > 6 ? ' …' : '')
    : '—'
);

const rules: FormRules = {
  termId: [{ required: true, message: '请选择学年学期', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  classType: [{ required: true, message: '请选择班级类型', trigger: 'change' }],
  range: [
    {
      validator: (_rule, _value, callback) => {
        if (form.endNo < form.startNo) {
          callback(new Error('结束序号不能小于起始序号'));
          return;
        }
        callback();
      },
      trigger: 'change'
    }
  ]
};

const open = () => {
  visible.value = true;
  form.termId = '';
  form.gradeId = '';
  form.classType = 'administrative';
  form.startNo = 1;
  form.endNo = 4;
  form.classCapacity = 45;
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  await formRef.value?.validate();
  const classList = Array.from({ length: count.value }, (_, i) => ({
    termId: form.termId,
    gradeId: form.gradeId,
    className: `${gradeName.value} (${form.startNo + i}) 班`,
    classType: form.classType,
    classCapacity: form.classCapacity
  }));
  submitting.value = true;
  try {
    await batchAddClass({ termId: form.termId, schoolId: props.schoolId, classList });
    ElMessage.success(`已生成 ${count.value} 个班级`);
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
