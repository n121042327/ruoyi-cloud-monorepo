<template>
  <el-dialog v-model="visible" :title="title" width="900px" append-to-body>
    <el-form :inline="true" :model="query" class="mb-2">
      <el-form-item label="结果">
        <el-select v-model="query.result" placeholder="全部" clearable style="width: 120px">
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="对象 / 失败原因" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border size="small">
      <el-table-column label="行号" prop="rowNo" width="80" align="center" data-layout-group="行明细" />
      <el-table-column label="对象" prop="objectName" width="160" show-overflow-tooltip data-layout-group="行明细" />
      <el-table-column label="结果" width="90" align="center" data-layout-group="行明细">
        <template #default="scope">
          <el-tag :type="scope.row.result === 'success' ? 'success' : 'danger'" size="small">
            {{ scope.row.result === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" prop="failReason" min-width="200" show-overflow-tooltip data-layout-group="行明细" />
      <el-table-column label="原始数据" prop="rawData" min-width="200" show-overflow-tooltip data-layout-group="行明细" />
      <template #empty>
        <el-empty description="该批次暂无行明细" />
      </template>
    </el-table>

    <pagination v-if="total > 0" v-model:total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />

    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { listImportRows } from '@/api/edu/importExport';
import type { ImportErrorVO } from '@/api/edu/importExport/types';

defineOptions({ name: 'EduTaskRowsDialog' });

const visible = ref(false);
const loading = ref(false);
const total = ref(0);
const rows = ref<ImportErrorVO[]>([]);
const batchNo = ref('');

const query = reactive({ result: '', keyword: '', pageNum: 1, pageSize: 20 });

const title = computed(() => `任务行明细 · ${batchNo.value || '—'}`);

const load = async () => {
  if (!batchNo.value) return;
  loading.value = true;
  try {
    const res = await listImportRows(batchNo.value, { ...query, result: query.result || undefined, keyword: query.keyword || undefined });
    rows.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  query.pageNum = 1;
  load();
};

const resetQuery = () => {
  query.result = '';
  query.keyword = '';
  query.pageNum = 1;
  load();
};

const open = (no: string) => {
  visible.value = true;
  batchNo.value = no;
  query.result = '';
  query.keyword = '';
  query.pageNum = 1;
  load();
};

defineExpose({ open });
</script>
