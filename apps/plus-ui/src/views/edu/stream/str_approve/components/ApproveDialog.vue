<template>
  <el-dialog v-model="visible" width="640px" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <span
          >审批选科变更 · <span class="mono">{{ row?.requestNo || '' }}</span></span
        >
        <el-tag type="warning" size="small">校级管理员</el-tag>
      </div>
    </template>

    <el-alert class="mb-3" type="info" :closable="false">
      <template #title>影响面</template>
      <div>
        审批通过后该学生的选科<b>立即生效</b>，教学班名单<b>不会自动同步</b>，需教务主任人工触发增量生成并核对差异（<span class="mono"
          >REQ-STR-060</span
        >）。
      </div>
    </el-alert>

    <el-descriptions :column="3" border class="mb-3">
      <el-descriptions-item label="学生">{{ row?.studentName || '—' }} · {{ row?.studentNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="年级 / 班级">
        {{ [row?.gradeName, row?.className].filter(Boolean).join(' · ') || '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ row?.applyTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <h3 class="form-section-title" data-layout-group="组合对比">组合对比</h3>
    <el-descriptions :column="1" border class="mb-3">
      <el-descriptions-item label="原组合（当前生效）">{{ row?.beforeCombination || '—' }}</el-descriptions-item>
      <el-descriptions-item label="新组合（申请）">{{ row?.afterCombination || '—' }}</el-descriptions-item>
      <el-descriptions-item label="申请原因">{{ row?.reason || '—' }}</el-descriptions-item>
    </el-descriptions>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <h3 class="form-section-title" data-layout-group="审批意见">审批意见</h3>
      <el-form-item label="审批意见" prop="opinion">
        <el-input
          v-model="form.opinion"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="驳回时必填（至少 5 个字）；同意时可留空或补充说明"
        />
      </el-form-item>
    </el-form>
    <div class="hint">
      驳回意见必填（<span class="mono">REQ-STR-037</span>）；审批动作写入审计，含审批人、意见与时间（<span class="mono">REQ-STR-039</span>）。
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleReject">驳回</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApprove">同意并生效</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { approveStreamChangeRequest } from '@/api/edu/stream';
import type { StreamChangeRequestVO } from '@/api/edu/stream/types';

const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const row = ref<StreamChangeRequestVO>();
const form = reactive<{ opinion: string }>({ opinion: '' });

const rules: FormRules = {
  opinion: [{ required: false }]
};

const open = (target: StreamChangeRequestVO) => {
  visible.value = true;
  row.value = target;
  form.opinion = '';
};

const submit = async (approved: boolean) => {
  if (!approved && (!form.opinion || form.opinion.trim().length < 5)) {
    ElMessage.warning('驳回时必须填写审批意见（至少 5 个字）');
    return;
  }
  if (!row.value) return;
  submitting.value = true;
  try {
    await approveStreamChangeRequest({ requestId: row.value.requestId, approved, opinion: form.opinion });
    ElMessage.success(
      approved
        ? '已通过该变更申请：学生选科立即生效并记录生效时间与审批人，相关统计与缓存已失效'
        : '已驳回该变更申请：原选科保持不变，驳回意见已记入审计并通知申请人'
    );
    visible.value = false;
    emit('success');
  } finally {
    submitting.value = false;
  }
};

const handleApprove = () => submit(true);
const handleReject = () => submit(false);

defineExpose({ open });
</script>
