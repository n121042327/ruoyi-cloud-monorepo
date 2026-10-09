<template>
  <el-dialog v-model="visible" width="560px" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <span>学年归档 · {{ year?.academicYearCode || '' }} 学年</span>
        <el-tag type="danger" size="small">危险动作</el-tag>
      </div>
    </template>

    <el-alert class="mb-2" type="warning" :closable="false">
      <template #title>影响范围</template>
      <div>
        归档后该学年移出新建业务的可选列表（新建班级 / 编班 / 导入等），<b>历史数据与统计保留</b>； 撤销归档需租户管理员（<span class="mono"
          >revokeArchiveAcademicYear</span
        >）。
      </div>
    </el-alert>

    <el-alert class="mb-3" type="info" :closable="false" :loading="loadingRef">
      <template #title>引用检查结果</template>
      <div v-if="reference">
        该学年仍有 <b>{{ reference.classCount ?? 0 }} 个班级</b>、<b>{{ reference.teachingRelationCount ?? 0 }} 条任教关系</b>、
        <b>{{ reference.rosterCount ?? 0 }} 名花名册学生</b>与 <b>{{ reference.subjectChoiceCount ?? 0 }} 名选科学生</b> 关联；
        归档不影响这些历史数据的可查性。
      </div>
      <div v-else>尚未取得引用检查结果。</div>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <h3 class="form-section-title" data-layout-group="归档信息">归档信息</h3>
      <el-form-item label="归档原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="如：2025-2026 学年已结束，学年数据归档"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" plain :loading="submitting" @click="submitForm">确认归档</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { archiveAcademicYear, checkTermReference } from '@/api/edu/term';
import type { AcademicYearReference, AcademicYearVO } from '@/api/edu/term/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const loadingRef = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const year = ref<AcademicYearVO>();
const reference = ref<AcademicYearReference>();

const form = reactive<{ reason: string }>({ reason: '' });

const rules: FormRules = {
  reason: [
    { required: true, message: '请填写归档原因（至少 5 个字）', trigger: 'blur' },
    { min: 5, message: '归档原因至少 5 个字', trigger: 'blur' }
  ]
};

/** 打开归档弹窗并拉取引用检查结果 */
const open = async (row: AcademicYearVO) => {
  visible.value = true;
  year.value = row;
  form.reason = '';
  reference.value = undefined;
  loadingRef.value = true;
  try {
    const res = await checkTermReference(row.academicYearId);
    reference.value = res.data;
  } finally {
    loadingRef.value = false;
  }
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (!year.value) return;
  submitting.value = true;
  try {
    await archiveAcademicYear(year.value.academicYearId, form.reason);
    ElMessage.success('已归档该学年：移出新建业务的可选列表，历史数据保留');
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

defineExpose({ open });
</script>
