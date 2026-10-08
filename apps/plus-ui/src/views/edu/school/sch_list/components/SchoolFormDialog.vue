<template>
  <el-dialog v-model="visible" :title="title" width="620px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <!-- 基础信息（page-field-layout：语义分组优先） -->
      <h3 class="form-section-title" data-layout-group="基础信息">基础信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="如 云溪实验学校" maxlength="100" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学校编码" prop="schoolCode">
            <el-input v-model="form.schoolCode" placeholder="如 SCH-201" maxlength="50" clearable :disabled="isEdit" />
            <div class="hint">父租户内唯一（BR-ORG-011）；编码是导入 / 导出对照表的键，保存后变更需单独申请（updateSchoolCode）。</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学校类型" prop="schoolType">
            <el-select v-model="form.schoolType" placeholder="请选择" class="w-full" clearable>
              <el-option label="公办" value="public" />
              <el-option label="民办" value="private" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 管理信息：学校与租户一一对应，绑定关系不可修改（REQ-SCH-022） -->
      <h3 class="form-section-title" data-layout-group="管理信息">管理信息</h3>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属租户">
            <el-input v-model="tenantLabel" disabled />
            <div class="hint">学校与租户一一对应（BR-ORG-002）；跨租户建校由平台运营的开通流程完成，建校后不可迁移。</div>
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
import { computed, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { addSchool, updateSchool } from '@/api/edu/school';
import type { SchoolForm, SchoolVO } from '@/api/edu/school/types';
import { useUserStore } from '@/store/modules/user';

defineOptions({ name: 'EduSchoolFormDialog' });

const emit = defineEmits<{ success: [] }>();

const userStore = useUserStore();

const visible = ref(false);
const title = ref('新建学校');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): SchoolForm => ({ schoolId: undefined, schoolCode: '', schoolName: '', schoolType: 'public', tenantId: '' });

const form = reactive<SchoolForm>(defaultForm());

const isEdit = computed(() => !!form.schoolId);

const tenantLabel = computed(() => form.tenantId || String(userStore.tenantId || '') || '当前登录租户');

const rules: FormRules = {
  schoolName: [{ required: true, message: '请填写学校名称', trigger: 'blur' }],
  schoolCode: [{ required: true, message: '请填写学校编码', trigger: 'blur' }]
};

/**
 * 建议编码：原型把「学校编码」写成系统生成，但后端契约要求显式传入（EduSchoolBo.schoolCode 非空），
 * 这里按当前租户给一个可修改的建议值，真正的生成规则待产品确认（见 decisions.md D-185 / GAP-102）。
 */
const suggestSchoolCode = () => {
  const tenant = String(userStore.tenantId || '').trim();
  return tenant ? `SCH-${tenant.slice(-6)}` : 'SCH-';
};

/** 打开弹窗：不传 row 为新建，传 row 为编辑 */
const open = (row?: SchoolVO) => {
  Object.assign(form, defaultForm());
  if (row) {
    title.value = `编辑学校 · ${row.schoolName}`;
    Object.assign(form, {
      schoolId: row.schoolId,
      schoolCode: row.schoolCode ?? '',
      schoolName: row.schoolName ?? '',
      schoolType: row.schoolType || 'public',
      tenantId: row.tenantId ?? ''
    });
  } else {
    title.value = '新建学校';
    form.schoolCode = suggestSchoolCode();
  }
  visible.value = true;
};

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  submitting.value = true;
  try {
    if (isEdit.value) {
      await updateSchool({ ...form });
      ElMessage.success('已保存学校');
    } else {
      await addSchool({ ...form });
      ElMessage.success('已创建学校，编码可后续单独申请变更');
    }
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
