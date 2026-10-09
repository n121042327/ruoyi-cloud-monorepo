<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/promotion-list.html 的 .page-head -->
    <div class="page-head">
      <h1>升班与学籍异动</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">共 {{ total }} 个升班任务</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有升班任务的查看权限" />
    </el-card>

    <template v-else>
      <!-- 查询区：教育信息 → 任务信息（PRD 6.1 第 2 节） -->
      <el-card v-show="showSearch" shadow="hover" class="mb-2">
        <el-form :model="queryParams" label-width="90px">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="学校" data-layout-group="教育信息">
                <el-input v-model="queryParams.schoolId" placeholder="本校（升班任务不跨校共享）" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="源学年学期" data-layout-group="教育信息">
                <el-input v-model="queryParams.sourceTermId" placeholder="全部源学期" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="目标学年学期" data-layout-group="教育信息">
                <el-input v-model="queryParams.targetTermId" placeholder="全部目标学期" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态" data-layout-group="任务信息">
                <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="w-full">
                  <el-option v-for="(label, value) in PROMOTION_STATUS_LABELS" :key="value" :label="label" :value="value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="创建人" data-layout-group="任务信息">
                <el-input v-model="queryParams.createBy" placeholder="全部" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="任务编号" data-layout-group="任务信息">
                <el-input v-model="queryParams.taskNo" placeholder="PRM-20260930-0012" clearable @keyup.enter="handleQuery" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <div class="flex justify-end gap-2">
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button v-hasPermi="['promotion.batch:create']" type="primary" plain icon="Plus" @click="handleCreate">新建升班任务</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['promotion.batch:read']" type="primary" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="false" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-alert
          class="mb-3"
          type="info"
          :closable="false"
          title="状态口径：草稿没有升班明细，因此「学生总数 / 成功 / 失败」显示为「—」。按钮权限：新建 = promotion.batch:create；预览 / 执行 / 重试 / 取消 = promotion.batch:update；升班没有审批环节，校领导与年级主任只读。"
        />

        <el-table v-loading="loading" border :data="taskList">
          <el-table-column label="任务编号" prop="taskNo" min-width="170" data-layout-group="任务基础信息" />
          <el-table-column label="源学年学期" prop="sourceTermName" min-width="180" data-layout-group="升班范围" />
          <el-table-column label="目标学年学期" prop="targetTermName" min-width="180" data-layout-group="升班范围" />
          <el-table-column label="状态" prop="status" width="130" align="center" data-layout-group="执行情况">
            <template #default="scope">
              <el-tag :type="statusType(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="学生总数" prop="totalCount" width="100" align="center" data-layout-group="执行情况">
            <template #default="scope">{{ countText(scope.row, scope.row.totalCount) }}</template>
          </el-table-column>
          <el-table-column label="成功" prop="successCount" width="80" align="center" data-layout-group="执行情况">
            <template #default="scope">{{ countText(scope.row, scope.row.successCount) }}</template>
          </el-table-column>
          <el-table-column label="失败" prop="failedCount" width="80" align="center" data-layout-group="执行情况">
            <template #default="scope">{{ countText(scope.row, scope.row.failedCount) }}</template>
          </el-table-column>
          <el-table-column label="创建人" prop="createBy" width="110" data-layout-group="任务基础信息" />
          <el-table-column label="创建时间" prop="createTime" width="170" align="center" data-layout-group="任务基础信息" />
          <el-table-column fixed="right" label="操作" width="240" data-layout-group="操作">
            <template #default="scope">
              <template v-if="scope.row.status === 'draft'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goPreview(scope.row)">预览</el-button>
                <el-button v-hasPermi="['promotion.batch:update']" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
              </template>
              <template v-else-if="scope.row.status === 'previewed'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goValidate(scope.row)">执行</el-button>
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goPreview(scope.row)">重新预览</el-button>
                <el-button v-hasPermi="['promotion.batch:update']" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
              </template>
              <template v-else-if="scope.row.status === 'validating'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goValidate(scope.row)">查看校验</el-button>
                <el-button v-hasPermi="['promotion.batch:update']" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
              </template>
              <template v-else-if="scope.row.status === 'running'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goResult(scope.row)">查看进度</el-button>
                <el-button v-hasPermi="['promotion.batch:update']" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
              </template>
              <template v-else-if="scope.row.status === 'succeeded'">
                <el-button v-hasPermi="['promotion.batch:read']" link type="primary" @click="goResult(scope.row)">查看结果</el-button>
                <el-button v-hasPermi="['data.export:export']" link type="primary" @click="handleExportResult(scope.row)">结果导出</el-button>
              </template>
              <template v-else-if="scope.row.status === 'partial_failed' || scope.row.status === 'failed'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="handleRetry(scope.row)">重试失败项</el-button>
                <el-button v-hasPermi="['promotion.batch:read']" link type="primary" @click="goResult(scope.row)">查看结果</el-button>
              </template>
              <template v-else-if="scope.row.status === 'cancelled'">
                <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="handleRetry(scope.row)">继续执行剩余项</el-button>
                <el-button v-hasPermi="['promotion.batch:read']" link type="primary" @click="goResult(scope.row)">查看结果</el-button>
              </template>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到升班任务" />
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

    <CancelPromotionDialog ref="cancelDialogRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { exportPromotionResult, exportPromotionTask } from '@/api/edu/promotion';
import type { PromotionTaskVO } from '@/api/edu/promotion/types';
import { PROMOTION_STATUS_LABELS, usePromotionTaskList } from './composables/usePromotionTaskList';
import CancelPromotionDialog from './components/CancelPromotionDialog.vue';

defineOptions({ name: 'EduPromotionTaskList' });

const router = useRouter();
const { loading, showSearch, total, taskList, queryParams, columns, canRead, getList, resetQuery, statusLabel, statusType, countText, handleRetry } =
  usePromotionTaskList();

const cancelDialogRef = ref<InstanceType<typeof CancelPromotionDialog>>();

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const handleCreate = () => {
  router.push('/edu/promotion/create');
};

const goPreview = (row: PromotionTaskVO) => router.push({ path: '/edu/promotion/preview', query: { taskId: row.taskId } });
const goValidate = (row: PromotionTaskVO) => router.push({ path: '/edu/promotion/validate', query: { taskId: row.taskId } });
const goResult = (row: PromotionTaskVO) => router.push({ path: '/edu/promotion/result', query: { taskId: row.taskId } });

const handleCancel = (row: PromotionTaskVO) => {
  cancelDialogRef.value?.open(row);
};

/** 导出升班任务台账（REQ-PRM-019；年级主任导出限本人负责年级） */
const handleExport = async () => {
  await exportPromotionTask({ ...queryParams });
  ElMessage.success('已生成升班任务台账，开始下载');
};

/** 导出升班结果（成功清单 / 失败清单 / 留级清单 / 毕业清单，REQ-PRM-034） */
const handleExportResult = async (row: PromotionTaskVO) => {
  await exportPromotionResult(row.taskId);
  ElMessage.success('已生成结果报告，开始下载');
};

onMounted(getList);
</script>
