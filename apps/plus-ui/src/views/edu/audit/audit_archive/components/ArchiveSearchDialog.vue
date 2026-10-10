<template>
  <el-dialog v-model="visible" title="检索归档日志" width="900px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="归档数据只读；检索结果同样受数据范围约束并写访问审计。" />

    <el-form :inline="true" :model="query" class="mb-2">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-input v-model="query.actionType" placeholder="如 修改" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="对象标识 / 操作人" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">检索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border size="small">
      <el-table-column label="时间" prop="operateTime" width="170" data-layout-group="时间信息" />
      <el-table-column label="操作人" prop="operator" width="110" data-layout-group="操作信息" />
      <el-table-column label="角色" prop="operatorRole" width="100" data-layout-group="操作信息" />
      <el-table-column label="操作类型" prop="actionType" width="100" align="center" data-layout-group="操作信息" />
      <el-table-column label="对象类型" prop="objectType" width="110" data-layout-group="对象信息" />
      <el-table-column label="对象标识" prop="objectId" width="150" show-overflow-tooltip data-layout-group="对象信息" />
      <el-table-column label="结果" prop="result" width="90" align="center" data-layout-group="操作信息" />
      <el-table-column label="来源 IP" prop="sourceIp" width="130" data-layout-group="操作信息" />
      <template #empty>
        <el-empty description="该条件下没有归档记录" />
      </template>
    </el-table>

    <pagination v-if="total > 0" v-model:total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />

    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { searchArchivedLog } from '@/api/edu/audit';
import type { ArchiveSearchForm, OperationLogVO } from '@/api/edu/audit/types';

defineOptions({ name: 'EduArchiveSearchDialog' });

const visible = ref(false);
const loading = ref(false);
const total = ref(0);
const rows = ref<OperationLogVO[]>([]);
const range = ref<[string, string] | null>(null);

const query = reactive<ArchiveSearchForm>({
  archiveNo: '',
  actionType: '',
  keyword: '',
  rangeStart: '',
  rangeEnd: '',
  pageNum: 1,
  pageSize: 20
});

const load = async () => {
  query.rangeStart = range.value?.[0] ?? '';
  query.rangeEnd = range.value?.[1] ?? '';
  loading.value = true;
  try {
    const res = await searchArchivedLog({ ...query });
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
  range.value = null;
  query.actionType = '';
  query.keyword = '';
  query.pageNum = 1;
  load();
};

const open = () => {
  visible.value = true;
  handleQuery();
};

defineExpose({ open });
</script>
