<template>
  <el-dialog v-model="visible" :title="title" width="720px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
      <!-- 学校身份与归属同属一个「基础信息」组：分组标题 + 2×2 栅格。
           窄列里不再塞长提示，说明统一收到表单底部的单行 .hint，
           依据 docs/00-governance/page-field-layout.md 第 1、2、3 节。 -->
      <h3 class="form-section-title" data-layout-group="基础信息">基础信息</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="如 云溪实验学校" maxlength="100" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学校编码" prop="schoolCode">
            <el-input v-model="form.schoolCode" placeholder="如 SCH-546223" maxlength="50" clearable :disabled="isEdit" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学校类型" prop="schoolType">
            <el-select v-model="form.schoolType" placeholder="请选择" class="w-full" clearable>
              <el-option label="公办" value="public" />
              <el-option label="民办" value="private" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属租户">
            <el-input v-model="tenantLabel" disabled />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="hint">学校与租户一一对应（BR-ORG-002），建校后不可跨租户迁移；学校编码父租户内唯一，变更需单独申请（updateSchoolCode）。</div>
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
