<template>
  <el-dialog v-model="visible" :title="title" width="720px" append-to-body>
    <!-- 字段与栅格对齐高保真 prototypes/high-fidelity/v1/pages/school-list.html：
         新建 = 学校名称 | 所属租户 / 开设学段（整行）/ 学校类型；编辑 = 学校名称 | 学校编码只读 / 所属租户只读 | 学校类型 -->
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="如 云溪实验学校" maxlength="100" clearable />
            <div v-if="!isEdit" class="hint">名称在同一父租户下不可重复；学校编码由系统生成，变更需单独申请（updateSchoolCode）。</div>
          </el-form-item>
        </el-col>

        <el-col v-if="!isEdit" :span="12">
          <el-form-item label="所属租户" prop="tenantId">
            <el-select v-model="form.tenantId" disabled class="w-full">
              <el-option :label="tenantLabel" :value="form.tenantId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-else :span="12">
          <el-form-item label="学校编码">
            <el-input v-model="form.schoolCode" disabled />
            <div class="hint">编码变更需单独申请（updateSchoolCode）并写审计；编码是导入 / 导出对照表的键。</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="!isEdit" :gutter="20">
        <el-col :span="24">
          <el-form-item label="开设学段" prop="stageCodes">
            <el-checkbox-group v-model="form.stageCodes">
              <el-checkbox v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-checkbox>
            </el-checkbox-group>
            <div class="hint">学段决定可创建的年级与升学路径（BR-GRADE-006）；学段序号固定映射（小学 1–6、初中 / 高中 1–3）。</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学校类型" prop="schoolType">
            <el-select v-model="form.schoolType" placeholder="请选择" class="w-full" clearable>
              <el-option label="公办" value="public" />
              <el-option label="民办" value="private" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-if="isEdit" :span="12">
          <el-form-item label="所属租户">
            <el-input v-model="tenantLabel" disabled />
            <div class="hint">学校不能跨租户迁移；需要换集团时新建学校并迁移数据。</div>
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
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';
import { useUserStore } from '@/store/modules/user';

defineOptions({ name: 'EduSchoolFormDialog' });

const emit = defineEmits<{ success: [] }>();

const userStore = useUserStore();

const visible = ref(false);
const title = ref('新建学校');
const submitting = ref(false);
const formRef = ref<FormInstance>();

const defaultForm = (): SchoolForm => ({
  schoolId: undefined,
  schoolCode: '',
  schoolName: '',
  schoolType: 'public',
  tenantId: '',
  stageCodes: []
});

const form = reactive<SchoolForm>(defaultForm());

const isEdit = computed(() => !!form.schoolId);

const tenantLabel = computed(() => form.tenantId || String(userStore.tenantId || '') || '当前登录租户');

/** 学校名称与开设学段为必填（原型 PAGE-SCH-CREATE）；编码由系统生成，前端按租户生成可追溯的建议值 */
const rules: FormRules = {
  schoolName: [{ required: true, message: '请填写学校名称', trigger: 'blur' }],
  stageCodes: [{ type: 'array', required: true, min: 1, message: '请至少选择一个学段', trigger: 'change' }]
};

/** 学校编码由系统生成（原型口径）：`SCH-<租户后 6 位>`；变更走 updateSchoolCode 单独申请（BR-ORG-011） */
const generateSchoolCode = () => {
  const tenant = String(userStore.tenantId || '').trim();
  return tenant ? `SCH-${tenant.slice(-6)}` : `SCH-${Date.now().toString().slice(-6)}`;
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
      tenantId: row.tenantId ?? '',
      stageCodes: []
    });
  } else {
    title.value = '新建学校';
    form.schoolCode = generateSchoolCode();
    form.tenantId = String(userStore.tenantId || '');
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
      ElMessage.success('已保存学校信息；编码变更需单独申请并写审计');
    } else {
      await addSchool({ ...form });
      ElMessage.success('已创建学校：编码由系统生成、默认启用，可在详情里配置校区');
    }
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
