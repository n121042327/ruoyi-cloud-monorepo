<template>
  <el-dialog v-model="visible" :title="form.assignmentId ? '编辑任教关系' : '新增任教关系'" width="600px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <h3 class="form-section-title" data-layout-group="任教信息">任教信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学年学期" prop="termId">
            <el-select v-model="form.termId" placeholder="请选择学年学期" class="w-full">
              <el-option
                v-for="item in termOptions"
                :key="item.termId"
                :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
                :value="item.termId"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学科" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="请选择学科" class="w-full">
              <el-option v-for="item in subjectOptions" :key="item.subjectId" :label="item.subjectName" :value="item.subjectId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="任教教师" prop="teacherId">
            <el-select v-model="form.teacherId" placeholder="请选择任教教师" class="w-full">
              <el-option
                v-for="item in teacherOptions"
                :key="item.teacherId"
                :label="`${item.teacherName}${item.employmentStatus && item.employmentStatus !== '在职' ? `（${item.employmentStatus}）` : ''}`"
                :value="item.teacherId"
                :disabled="!!item.employmentStatus && item.employmentStatus !== '在职'"
              />
            </el-select>
            <div class="hint">离职 / 调离的教师不可新增任教（非在职只保留查看与撤销离职登记）。</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班级类型" prop="classType">
            <el-select v-model="form.classType" class="w-full">
              <el-option label="行政班" value="行政班" />
              <el-option label="教学班" value="教学班" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="班级" prop="classId">
            <el-select v-model="form.classId" placeholder="请选择班级" class="w-full">
              <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="周课时">
            <el-input-number v-model="form.weeklyHours" :min="0" :max="40" controls-position="right" class="w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效期间">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="生效日期" class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button v-hasPermi="['person.teaching_assignment:create']" type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { saveTeachingAssignment } from '@/api/edu/teacher';
import type { TeachingAssignmentForm, TeachingAssignmentVO } from '@/api/edu/teacher/types';
import type { ClassVO } from '@/api/edu/class/types';
import type { SubjectVO } from '@/api/edu/subject/types';
import type { TermVO } from '@/api/edu/term/types';
import type { TeacherVO } from '@/api/edu/teacher/types';

const props = withDefaults(
  defineProps<{
    termOptions?: TermVO[];
    subjectOptions?: SubjectVO[];
    teacherOptions?: TeacherVO[];
    classOptions?: ClassVO[];
  }>(),
  { termOptions: () => [], subjectOptions: () => [], teacherOptions: () => [], classOptions: () => [] }
);

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): TeachingAssignmentForm => ({
  assignmentId: undefined,
  termId: '',
  subjectId: '',
  teacherId: '',
  classType: '行政班',
  classId: '',
  weeklyHours: 5,
  startDate: ''
});

const form = reactive<TeachingAssignmentForm>(defaultForm());

const rules: FormRules = {
  termId: [{ required: true, message: '请选择学年学期', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择任教教师', trigger: 'change' }],
  classType: [{ required: true, message: '请选择班级类型', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }]
};

/** 打开弹窗；传入 row 为编辑 */
const open = (row?: TeachingAssignmentVO, presetClassId?: string) => {
  visible.value = true;
  Object.assign(form, defaultForm());
  if (row) {
    Object.assign(form, {
      assignmentId: row.assignmentId,
      termId: row.termId ?? '',
      subjectId: row.subjectCode ?? '',
      teacherId: row.teacherId ?? '',
      classType: row.classType ?? '行政班',
      classId: row.classId ?? '',
      weeklyHours: row.weeklyHours ?? 5,
      startDate: row.startDate ?? ''
    });
  } else {
    if (presetClassId) form.classId = presetClassId;
    const current = props.termOptions.find((item) => item.current);
    form.termId = current?.termId ?? props.termOptions[0]?.termId ?? '';
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await saveTeachingAssignment({ ...form });
    ElMessage.success(form.assignmentId ? '修改成功' : '新增成功');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
