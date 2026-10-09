<template>
  <el-dialog v-model="visible" :title="title" width="720px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 教育信息：学年学期 → 年级 → 班级名称 → 班级类型 -->
      <h3 class="form-section-title" data-layout-group="教育信息">教育信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学年学期" prop="termId">
            <el-select v-model="form.termId" placeholder="请选择学年学期" class="w-full" :disabled="!!form.classId">
              <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName" :value="item.termId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年级" prop="gradeId">
            <el-select v-model="form.gradeId" placeholder="请选择年级" class="w-full" :disabled="!!form.classId">
              <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班级名称" prop="className">
            <el-input v-model="form.className" placeholder="请输入班级名称" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班级类型" prop="classType">
            <el-select v-model="form.classType" placeholder="请选择班级类型" class="w-full" :disabled="!!form.classId">
              <el-option v-for="item in CLASS_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <div v-if="form.classId" class="hint mb-2">学年学期与年级一经创建不可修改；班级类型在存在花名册后不可修改。</div>

      <!-- 管理信息：班主任 → 校区 → 教室 → 容量 -->
      <h3 class="form-section-title" data-layout-group="管理信息">管理信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="班主任" prop="headTeacherId">
            <el-select v-model="form.headTeacherId" placeholder="可留空后指定" clearable class="w-full">
              <el-option v-for="item in teacherOptions" :key="item.teacherId" :label="item.teacherName" :value="item.teacherId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="校区" prop="campusId">
            <el-select v-model="form.campusId" placeholder="请选择校区" clearable class="w-full">
              <el-option v-for="item in campusOptions" :key="item.campusId" :label="item.campusName" :value="item.campusId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="教室" prop="classroom">
            <el-input v-model="form.classroom" placeholder="请输入教室" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="容量" prop="classCapacity">
            <el-input-number v-model="form.classCapacity" :min="0" controls-position="right" class="w-full" />
            <div class="hint">容量仅作参考，超出时只提示不阻塞。</div>
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
import { addClass, updateClass } from '@/api/edu/class';
import type { ClassForm, ClassVO } from '@/api/edu/class/types';
import type { CampusVO } from '@/api/edu/school/types';
import type { TeacherVO } from '@/api/edu/teacher/types';
import type { GradeVO } from '@/api/edu/grade/types';
import type { TermVO } from '@/api/edu/term/types';
import { CLASS_TYPE_OPTIONS } from '../composables/useClassList';

interface Props {
  termOptions?: TermVO[];
  gradeOptions?: GradeVO[];
  teacherOptions?: TeacherVO[];
  campusOptions?: CampusVO[];
}

const props = withDefaults(defineProps<Props>(), {
  termOptions: () => [],
  gradeOptions: () => [],
  teacherOptions: () => [],
  campusOptions: () => []
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const title = ref('新建班级');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): ClassForm => ({
  classId: undefined,
  termId: '',
  gradeId: '',
  className: '',
  classType: 'administrative',
  headTeacherId: '',
  campusId: '',
  classroom: '',
  classCapacity: undefined,
  remark: ''
});

const form = reactive<ClassForm>(defaultForm());

const rules: FormRules = {
  termId: [{ required: true, message: '请选择学年学期', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  classType: [{ required: true, message: '请选择班级类型', trigger: 'change' }]
};

/** 打开弹窗：不传 row 为新建，传 row 为编辑 */
const open = (row?: ClassVO) => {
  visible.value = true;
  title.value = row?.classId ? '编辑班级' : '新建班级';
  Object.assign(form, defaultForm(), row ?? {});
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    if (form.classId) {
      await updateClass({ ...form });
    } else {
      const payload = { ...form };
      delete payload.classId;
      await addClass(payload);
    }
    ElMessage.success(form.classId ? '修改成功' : '新建成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
