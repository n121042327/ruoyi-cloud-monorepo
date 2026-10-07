<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有审计日志的查看权限" />
    </el-card>

    <template v-else>
      <el-card shadow="hover" class="mb-[10px]">
        <!-- 查询区分组：时间信息（时间范围）→ 检索信息（操作人 / 操作类型 / 结果） -->
        <el-form :model="queryParams" :inline="true">
          <el-form-item label="时间范围" data-layout-group="时间信息">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px"
            />
          </el-form-item>
          <el-form-item label="操作人" prop="operator" data-layout-group="检索信息">
            <el-input v-model="queryParams.operator" placeholder="姓名 / 账号" clearable style="width: 160px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="操作类型" prop="actionType" data-layout-group="检索信息">
            <el-select v-model="queryParams.actionType" placeholder="全部类型" clearable style="width: 140px">
              <el-option v-for="item in ACTION_TYPE_OPTIONS" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="结果" prop="result" data-layout-group="检索信息">
            <el-select v-model="queryParams.result" placeholder="全部结果" clearable style="width: 120px">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failed" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <el-table v-loading="loading" border :data="logList">
          <el-table-column label="时间" prop="operateTime" width="170" data-layout-group="时间信息" />
          <el-table-column label="操作人" prop="operator" width="120" data-layout-group="操作信息" />
          <el-table-column label="角色" prop="operatorRole" width="120" data-layout-group="操作信息" />
          <el-table-column label="操作类型" prop="actionType" width="110" align="center" data-layout-group="操作信息" />
          <el-table-column label="对象类型" prop="objectType" width="120" data-layout-group="对象信息" />
          <el-table-column label="对象标识" prop="objectId" min-width="180" :show-overflow-tooltip="true" data-layout-group="对象信息" />
          <el-table-column label="结果" prop="result" width="90" align="center" data-layout-group="操作信息">
            <template #default="scope">
              <el-tag :type="scope.row.result === 'failed' ? 'danger' : 'success'" size="small">
                {{ scope.row.result === 'failed' ? '失败' : '成功' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源 IP" prop="sourceIp" width="140" data-layout-group="操作信息" />
          <el-table-column fixed="right" label="操作" width="100" data-layout-group="操作">
            <template #default>
              <el-button link type="primary" @click="handleDetail">详情</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到日志数据" />
          </template>
        </el-table>

        <pagination
          v-if="total > 0"
          v-model:total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { listOperationLog } from '@/api/edu/audit';
import type { OperationLogQuery, OperationLogVO } from '@/api/edu/audit/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduAuditLogList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

/** 操作类型（审计 PRD 4.1 记录范围） */
const ACTION_TYPE_OPTIONS = ['新增', '修改', '删除', '导入', '执行', '审批', '授权', '状态变更'];

const loading = ref(false);
const total = ref(0);
const logList = ref<OperationLogVO[]>([]);
const dateRange = ref<string[]>([]);

const queryParams = reactive<OperationLogQuery>({ pageNum: 1, pageSize: 20, operator: '', actionType: '', result: '', keyword: '' });

const canRead = computed(() => checkPermi(['audit.log:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listOperationLog({
      ...queryParams,
      beginTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    });
    logList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const handleReset = () => {
  dateRange.value = [];
  queryParams.operator = '';
  queryParams.actionType = '';
  queryParams.result = '';
  queryParams.keyword = '';
  queryParams.pageNum = 1;
  getList();
};

/** 日志导出按当前筛选与数据范围生成，导出行为本身写审计（REQ-AUD-026 / 029） */
const handleExport = () => {
  proxy?.download(
    'edu/audit/log/export',
    { ...queryParams, beginTime: dateRange.value?.[0], endTime: dateRange.value?.[1] },
    `operation_log_${new Date().getTime()}.xlsx`
  );
};

const handleDetail = () => {
  ElMessage.info('日志详情抽屉在阶段 6 的下一批交付');
};

onMounted(getList);
</script>
