<template>
  <el-drawer v-model="visible" size="600px" :title="`对象变更时间线 · ${objectLabel}`" append-to-body>
    <div v-loading="loading">
      <el-alert type="info" :closable="false" title="时间线按对象聚合该对象的全部变更记录，只读且不可删除。" />
      <el-timeline v-if="logs.length" class="mt-3">
        <el-timeline-item v-for="item in logs" :key="item.logId" :timestamp="item.operateTime">
          <b>{{ item.actionType || '变更' }}</b>
          <el-tag size="small" class="ml-2" :type="item.result === 'failed' ? 'danger' : 'success'">
            {{ item.result === 'failed' ? '失败' : '成功' }}
          </el-tag>
          <div class="text-xs mt-1">操作人 {{ item.operator || '—' }} · {{ item.operatorRole || '—' }} · 来源 {{ item.sourceIp || '—' }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else-if="!loading" description="该对象暂无变更记录" :image-size="60" />
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { listObjectChangeLog } from '@/api/edu/audit';
import type { OperationLogVO } from '@/api/edu/audit/types';

const visible = ref(false);
const loading = ref(false);
const logs = ref<OperationLogVO[]>([]);
const target = ref<{ objectType?: string; objectId?: string }>({});

const objectLabel = computed(() => `${target.value.objectType || '—'} · ${target.value.objectId || '—'}`);

/** 打开时间线：按对象类型 + 对象标识取全部变更记录 */
const open = async (row: OperationLogVO) => {
  visible.value = true;
  target.value = { objectType: row.objectType, objectId: row.objectId };
  logs.value = [];
  if (!row.objectType || !row.objectId) {
    return;
  }
  loading.value = true;
  try {
    const res = await listObjectChangeLog(row.objectType, row.objectId);
    logs.value = res.data ?? [];
  } catch {
    logs.value = [];
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
