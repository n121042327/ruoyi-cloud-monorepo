<template>
  <el-dialog v-model="visible" :title="title" width="640px" append-to-body>
    <el-table v-loading="loading" :data="leaders" border size="small" class="mb-3">
      <el-table-column label="年级主任" width="140">
        <template #default="scope">{{ scope.row.teacherName || scope.row.userName || '—' }}</template>
      </el-table-column>
      <el-table-column label="是否主管" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isPrimary === '1' ? 'success' : 'info'" size="small">
            {{ scope.row.isPrimary === '1' ? '主管' : '协助' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">{{ scope.row.status === '1' ? '在任' : '已离任' }}</template>
      </el-table-column>
      <el-table-column label="学年学期" prop="termId" width="150" show-overflow-tooltip />
      <el-table-column fixed="right" label="操作" width="110" data-layout-group="操作">
        <template #default="scope">
          <el-button v-if="scope.row.status === '1'" link type="danger" @click="handleRemove(scope.row)">离任</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="该年级暂无年级主任" />
      </template>
    </el-table>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="新增年级主任" prop="teacherId">
        <el-select v-model="form.teacherId" placeholder="请选择教师" filterable class="w-full">
          <el-option
            v-for="item in teacherOptions"
            :key="item.teacherId"
            :label="item.teacherNo ? `${item.teacherName}（${item.teacherNo}）` : item.teacherName"
            :value="item.teacherId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否主管" prop="isPrimary">
        <el-radio-group v-model="form.isPrimary">
          <el-radio value="1">主管</el-radio>
          <el-radio value="0">协助</el-radio>
        </el-radio-group>
        <div class="hint">一个年级同一学年学期只允许一名主管</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">指定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { listGradeLeader, removeGradeLeader, saveGradeLeader } from '@/api/edu/grade';
import type { GradeLeaderVO, GradeVO } from '@/api/edu/grade/types';
import type { TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduGradeLeaderDialog' });

const props = withDefaults(defineProps<{ teacherOptions?: TeacherVO[] }>(), {
  teacherOptions: () => []
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const loading = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const leaders = ref<GradeLeaderVO[]>([]);
const grade = ref<GradeVO>({} as GradeVO);

const form = reactive({ teacherId: '', isPrimary: '0' });

const title = computed(() => `指定年级主任 · ${grade.value.gradeName ?? '—'}`);

const rules: FormRules = {
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }]
};

const loadLeaders = async () => {
  loading.value = true;
  try {
    const res = await listGradeLeader(grade.value.gradeId);
    leaders.value = (res.data ?? []) as GradeLeaderVO[];
  } finally {
    loading.value = false;
  }
};

const open = async (row: GradeVO) => {
  visible.value = true;
  grade.value = row;
  form.teacherId = '';
  form.isPrimary = '0';
  formRef.value?.clearValidate();
  await loadLeaders();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await saveGradeLeader(grade.value.gradeId, {
      schoolId: grade.value.schoolId,
      teacherId: form.teacherId,
      isPrimary: form.isPrimary
    });
    ElMessage.success('年级主任已指定');
    form.teacherId = '';
    await loadLeaders();
    emit('success');
  } finally {
    submitting.value = false;
  }
};

const handleRemove = async (row: GradeLeaderVO) => {
  const { value } = await ElMessageBox.prompt('年级主任离任只置为已离任，不物理删除记录。', '年级主任离任', {
    inputPlaceholder: '离任原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 2 ? true : '请填写离任原因（至少 2 个字）'),
    type: 'warning'
  });
  await removeGradeLeader(grade.value.gradeId, row.leaderId, value);
  ElMessage.success('已登记离任');
  await loadLeaders();
  emit('success');
};

defineExpose({ open });
</script>
