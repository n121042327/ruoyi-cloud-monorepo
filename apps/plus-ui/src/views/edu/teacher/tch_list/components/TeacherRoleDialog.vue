<template>
  <el-dialog v-model="visible" :title="title" width="620px" append-to-body>
    <el-table v-loading="loading" :data="roles" border size="small" class="mb-3">
      <el-table-column label="教育角色" prop="eduRole" width="140" />
      <el-table-column label="生效日期" width="120" align="center">
        <template #default="scope">{{ scope.row.startDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="结束日期" width="120" align="center">
        <template #default="scope">{{ scope.row.endDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'disabled' ? 'info' : 'success'" size="small">
            {{ scope.row.status === 'disabled' ? '已停用' : '生效中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="100" data-layout-group="操作">
        <template #default="scope">
          <el-button link type="danger" @click="handleRemove(scope.row)">移除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="该教师暂无教育角色" />
      </template>
    </el-table>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="新增角色" prop="eduRole">
        <el-select v-model="form.eduRole" placeholder="请选择教育角色" class="w-full">
          <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="默认当天" class="w-full" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">分配角色</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { assignTeacherRole, listTeacherRole, removeTeacherRole } from '@/api/edu/teacher';
import type { TeacherRoleVO, TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduTeacherRoleDialog' });

const props = withDefaults(defineProps<{ roleOptions?: { value: string; label: string }[] }>(), {
  roleOptions: () => []
});

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const loading = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const roles = ref<TeacherRoleVO[]>([]);
const teacher = ref<TeacherVO>({} as TeacherVO);

const form = reactive({ eduRole: '', startDate: '' });

const title = computed(() => `教育角色分配 · ${teacher.value.teacherName ?? '—'}`);

const rules: FormRules = {
  eduRole: [{ required: true, message: '请选择教育角色', trigger: 'change' }]
};

const loadRoles = async () => {
  loading.value = true;
  try {
    const res = await listTeacherRole(teacher.value.teacherId);
    roles.value = (res.data ?? []) as TeacherRoleVO[];
  } finally {
    loading.value = false;
  }
};

const open = async (row: TeacherVO) => {
  visible.value = true;
  teacher.value = row;
  form.eduRole = '';
  form.startDate = '';
  formRef.value?.clearValidate();
  await loadRoles();
};

const submitForm = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    await assignTeacherRole(teacher.value.teacherId, {
      teacherId: teacher.value.teacherId,
      eduRole: form.eduRole,
      ...(form.startDate ? { startDate: form.startDate } : {})
    });
    ElMessage.success('角色已分配');
    form.eduRole = '';
    await loadRoles();
    emit('success');
  } finally {
    submitting.value = false;
  }
};

const handleRemove = async (row: TeacherRoleVO) => {
  const { value } = await ElMessageBox.prompt('移除该教育角色需要填写原因（写审计）。', '移除角色', {
    inputPlaceholder: '原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 2 ? true : '请填写原因（至少 2 个字）'),
    type: 'warning'
  });
  await removeTeacherRole(teacher.value.teacherId, row.userRoleId, value);
  ElMessage.success('角色已移除');
  await loadRoles();
  emit('success');
};

defineExpose({ open });
</script>
