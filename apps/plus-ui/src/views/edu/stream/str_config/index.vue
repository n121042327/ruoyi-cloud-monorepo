<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">选科配置</h2>
        <el-tag type="info" size="small">数据范围：本校（DS-04）· 可写</el-tag>
        <el-tag type="primary" size="small">{{ config.termName || '当前学年学期' }}</el-tag>
        <el-tag :type="periodTagType" size="small">{{ config.periodStatus || '未开始' }}</el-tag>
      </div>
      <div class="text-xs mt-2">
        选科开放期：{{ config.openFrom || '—' }} — {{ config.deadline || '—' }}
        <span class="ml-2">逾期变更需校级管理员审批（BR-STREAM-005）</span>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>规则（固定，不可自由配置）</span>
          <el-tag type="info" size="small">3+1+2</el-tag>
        </div>
      </template>
      <h3 class="form-section-title" data-layout-group="固定规则">首选科目（1 门）</h3>
      <div class="flex gap-2 mb-2">
        <el-tag v-for="item in primarySubjects" :key="item.code" type="success" effect="plain">{{ item.name }}</el-tag>
      </div>
      <div class="hint mb-3">首选科目固定为物理 / 历史两门（BV-STREAM-001 / BR-SUBJECT-003），学校与教务主任都不能增减。</div>

      <h3 class="form-section-title" data-layout-group="固定规则">再选科目（4 选 2）</h3>
      <div class="flex gap-2 mb-2">
        <el-tag v-for="item in secondarySubjects" :key="item.code" type="success" effect="plain">{{ item.name }}</el-tag>
      </div>
      <div class="hint">再选科目固定为化学 / 生物 / 思想政治 / 地理（BR-STREAM-002），必须选满 2 门且不能重复；组合结果共 12 种。</div>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center gap-2">
          <span>开放期与审批</span>
          <el-tag type="info" size="small">saveStreamConfig</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <h3 class="form-section-title" data-layout-group="开放期与审批">开放期与审批</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开放日期" prop="openFrom">
              <el-date-picker v-model="form.openFrom" type="date" value-format="YYYY-MM-DD" placeholder="请选择开放日期" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="deadline">
              <el-date-picker
                v-model="form.deadline"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择截止时间"
                class="w-full"
              />
              <div class="hint">开放日期不能晚于截止时间；截止后学生只能提交变更申请（转审批）。</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="逾期变更">
              <el-radio-group v-model="form.overdueRequiresApproval">
                <el-radio :value="true">需校级管理员审批（默认）</el-radio>
                <el-radio :value="false">不允许逾期变更</el-radio>
              </el-radio-group>
              <div class="hint">默认「需校级管理员审批」（BR-STREAM-005）：逾期提交后进入审批待办。</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="适用学年学期">
              <el-input :model-value="config.termName || '当前学年学期'" disabled />
              <div class="hint">配置按学年学期保存；切换当前学期后需重新确认开放期。</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="flex items-center justify-between">
        <div class="flex gap-2">
          <el-button v-hasPermi="['stream.selection:read']" @click="goList">查看选科清单</el-button>
          <el-button v-hasPermi="['stream.selection:read']" @click="handleUnselected">查看未选科学生</el-button>
        </div>
        <el-button v-hasPermi="['stream.config:update']" type="primary" :loading="submitting" @click="handleSave">保存配置</el-button>
      </div>
    </el-card>

    <UnselectedStudentDialog ref="unselectedRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { getStreamConfig, getStreamOption, saveStreamConfig } from '@/api/edu/stream';
import type { StreamConfigForm, StreamConfigVO, StreamSubjectOption } from '@/api/edu/stream/types';
import UnselectedStudentDialog from './components/UnselectedStudentDialog.vue';

defineOptions({ name: 'EduStreamConfig' });

const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const config = ref<StreamConfigVO>({});
const primarySubjects = ref<StreamSubjectOption[]>([]);
const secondarySubjects = ref<StreamSubjectOption[]>([]);
const unselectedRef = ref<InstanceType<typeof UnselectedStudentDialog>>();

const form = reactive<StreamConfigForm>({
  termId: '',
  openFrom: '',
  deadline: '',
  overdueRequiresApproval: true
});

const rules: FormRules = {
  openFrom: [{ required: true, message: '请选择开放日期', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
};

const periodTagType = computed(() => {
  if (config.value.periodStatus === '进行中') return 'success';
  if (config.value.periodStatus === '已截止') return 'info';
  return 'warning';
});

const loadAll = async () => {
  loading.value = true;
  try {
    const [cfgRes, optRes] = await Promise.all([getStreamConfig(), getStreamOption()]);
    config.value = cfgRes.data ?? {};
    primarySubjects.value = optRes.data?.primarySubjects ?? [];
    secondarySubjects.value = optRes.data?.secondarySubjects ?? [];
    Object.assign(form, {
      termId: config.value.termId ?? '',
      openFrom: config.value.openFrom ?? '',
      deadline: config.value.deadline ?? '',
      overdueRequiresApproval: config.value.overdueRequiresApproval ?? true
    });
  } finally {
    loading.value = false;
  }
};

const goList = () => router.push('/edu/stream/list');

const handleUnselected = () => unselectedRef.value?.open(form.termId);

/** 保存配置：开放日期不能晚于截止时间，变更写审计（NFR-AUDIT-01） */
const handleSave = async () => {
  await formRef.value?.validate();
  if (form.openFrom && form.deadline && form.openFrom > form.deadline) {
    ElMessage.error('开放日期不能晚于截止时间');
    return;
  }
  submitting.value = true;
  try {
    await saveStreamConfig({ ...form });
    ElMessage.success('已保存选科配置：开放期与逾期审批口径生效，变更写审计');
    await loadAll();
  } finally {
    submitting.value = false;
  }
};

onMounted(loadAll);
</script>
