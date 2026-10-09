<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">学生选科（3+1+2）</h2>
        <el-tag type="primary" size="small">{{ current.termName || '当前学年学期' }}</el-tag>
        <el-tag :type="current.status === '已生效' ? 'success' : 'warning'" size="small">{{ current.status || '未选择' }}</el-tag>
      </div>
      <div class="text-xs mt-2">
        选科开放期：{{ config.openFrom || '—' }} — {{ config.deadline || '—' }}（{{ config.periodStatus || '未开始' }}）
        <span class="ml-2">截止后提交的变更需校级管理员审批</span>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>第一步 · 首选科目（1 门）</span>
          <el-tag type="info" size="small">物理 / 历史（固定）</el-tag>
        </div>
      </template>
      <el-radio-group v-model="form.primarySubjectCode" size="large">
        <el-radio-button v-for="item in primarySubjects" :key="item.code" :value="item.code">{{ item.name }}</el-radio-button>
      </el-radio-group>
      <div class="hint mt-2">首选科目只能是物理或历史，不提供其它选项。</div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>第二步 · 再选科目（4 选 2）</span>
          <el-tag :type="form.secondarySubjectCodes.length === 2 ? 'success' : 'warning'" size="small">
            已选 {{ form.secondarySubjectCodes.length }} / 2
          </el-tag>
        </div>
      </template>
      <el-checkbox-group v-model="form.secondarySubjectCodes" :max="2">
        <el-checkbox-button v-for="item in secondarySubjects" :key="item.code" :value="item.code">{{ item.name }}</el-checkbox-button>
      </el-checkbox-group>
      <el-alert
        v-if="form.secondarySubjectCodes.length !== 2"
        class="mt-2"
        type="error"
        :closable="false"
        title="选择数量不符"
        :description="`再选科目必须选满 2 门且不能重复（当前 ${form.secondarySubjectCodes.length} 门）。`"
      />
      <div class="hint mt-2">再选科目固定 4 门中选 2 门；组合结果 = 1 门首选 + 2 门再选，共 12 种组合。</div>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center gap-2">
          <span>当前选科结果</span>
          <el-tag type="success" size="small">已生效 · {{ current.effectiveDate || '—' }}</el-tag>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="6">
          <div class="text-xs text-gray-500">首选</div>
          <div class="mt-1">{{ current.primarySubjectName || '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">再选</div>
          <div class="mt-1">{{ (current.secondarySubjectNames ?? []).join(' · ') || '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">组合</div>
          <div class="mt-1">{{ current.combination || '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">教学班</div>
          <div class="mt-1">{{ (current.teachingClassNames ?? []).join(' · ') || '—' }}</div>
        </el-col>
      </el-row>

      <el-alert
        v-if="overdue"
        class="mt-3"
        type="warning"
        :closable="false"
        title="已截止"
        description="当前已过选科截止时间：提交会作为变更申请进入校级管理员审批，审批通过前保持原选科不变。"
      />
      <div class="hint mt-3">选科结果决定教学班归属；行政班不变（教学班与行政班是两套独立关系）。</div>

      <div class="flex items-center justify-between mt-3">
        <el-button @click="handleHistory">查看选科历史</el-button>
        <div class="flex gap-2">
          <el-button @click="resetCurrent">恢复当前结果</el-button>
          <el-button v-hasPermi="['stream.selection:update']" type="primary" :loading="submitting" @click="handleSubmit">提交选科</el-button>
        </div>
      </div>
    </el-card>

    <StreamHistoryDialog ref="historyRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMyStream, getStreamConfig, getStreamOption, submitMyStream, updateMyStream } from '@/api/edu/stream';
import type { MyStreamForm, MyStreamVO, StreamConfigVO, StreamSubjectOption } from '@/api/edu/stream/types';
import StreamHistoryDialog from './components/StreamHistoryDialog.vue';

defineOptions({ name: 'EduStreamStudent' });

const loading = ref(false);
const submitting = ref(false);
const current = ref<MyStreamVO>({});
const config = ref<StreamConfigVO>({});
const primarySubjects = ref<StreamSubjectOption[]>([]);
const secondarySubjects = ref<StreamSubjectOption[]>([]);
const historyRef = ref<InstanceType<typeof StreamHistoryDialog>>();

const form = reactive<MyStreamForm>({ primarySubjectCode: '', secondarySubjectCodes: [], reason: '' });

/** 已过截止时间：提交转为变更申请（BR-STREAM-005） */
const overdue = computed(() => {
  if (!config.value.deadline) return false;
  return new Date().getTime() > new Date(config.value.deadline.replace(/-/g, '/')).getTime();
});

const resetCurrent = () => {
  form.primarySubjectCode = current.value.primarySubjectCode ?? '';
  form.secondarySubjectCodes = [...(current.value.secondarySubjectCodes ?? [])];
  form.reason = '';
};

const loadAll = async () => {
  loading.value = true;
  try {
    const [myRes, cfgRes, optRes] = await Promise.all([getMyStream(), getStreamConfig(), getStreamOption()]);
    current.value = myRes.data ?? {};
    config.value = cfgRes.data ?? {};
    primarySubjects.value = optRes.data?.primarySubjects ?? [];
    secondarySubjects.value = optRes.data?.secondarySubjects ?? [];
    resetCurrent();
  } finally {
    loading.value = false;
  }
};

const handleHistory = () => historyRef.value?.open();

/**
 * 提交选科：开放期内直接生效（首次 submitMyStream / 变更 updateMyStream）；
 * 截止后必须填写变更原因并进入审批待办（BR-STREAM-005）。
 */
const handleSubmit = async () => {
  if (!form.primarySubjectCode) {
    ElMessage.warning('请选择首选科目（物理 / 历史）');
    return;
  }
  if (form.secondarySubjectCodes.length !== 2) {
    ElMessage.warning('再选科目必须选满 2 门且不能重复');
    return;
  }

  if (overdue.value) {
    const { value } = await ElMessageBox.prompt(
      '当前已过选科截止时间：提交会作为变更申请进入校级管理员审批，审批通过前保持原选科不变。请填写变更原因',
      '提交变更申请',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputPlaceholder: '如：家庭与升学规划调整，申请改选历史 + 政治 + 地理',
        inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '变更原因至少 5 个字')
      }
    );
    form.reason = value;
  }

  submitting.value = true;
  try {
    if (current.value.status === '已生效') {
      await updateMyStream({ ...form });
    } else {
      await submitMyStream({ ...form });
    }
    ElMessage.success(
      overdue.value ? '已提交选科变更申请，待校级管理员审批（审批通过前保持原选科不变）' : '已提交选科：在开放期内直接生效，选科结果决定教学班归属'
    );
    await loadAll();
  } finally {
    submitting.value = false;
  }
};

onMounted(loadAll);
</script>
