<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有选科变更审批的查看权限" />
    </el-card>

    <template v-else>
      <el-card v-show="showSearch" shadow="hover" class="mb-2">
        <el-form :model="queryParams" label-width="90px">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="状态" data-layout-group="申请范围">
                <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="w-full">
                  <el-option label="待审批" value="待审批" />
                  <el-option label="已通过" value="已通过" />
                  <el-option label="已驳回" value="已驳回" />
                  <el-option label="已撤销" value="已撤销" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="年级" data-layout-group="申请范围">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable class="w-full">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="关键词" data-layout-group="申请范围">
                <el-input v-model="queryParams.keyword" placeholder="按申请单号 / 姓名 / 学号" clearable @keyup.enter="handleQuery" />
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
              <el-button type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
            </el-col>
            <el-col :span="16">
              <span class="text-xs">默认只看「待审批」；查看全部状态能看到已通过与已驳回的历史申请。</span>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="false" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-alert
          class="mb-3"
          type="info"
          :closable="false"
          title="审批通过后学生选科立即生效，教学班名单不会自动同步，需教务主任人工触发增量生成并核对差异。驳回意见必填。"
        />

        <el-table v-loading="loading" border :data="list">
          <el-table-column label="申请单号" prop="requestNo" width="130" data-layout-group="申请信息" />
          <el-table-column label="学生" prop="studentName" width="150" data-layout-group="学生信息">
            <template #default="scope">{{ scope.row.studentName }} · {{ scope.row.studentNo }}</template>
          </el-table-column>
          <el-table-column label="年级 / 班级" prop="className" min-width="140" data-layout-group="学生信息">
            <template #default="scope">{{ [scope.row.gradeName, scope.row.className].filter(Boolean).join(' / ') || '—' }}</template>
          </el-table-column>
          <el-table-column label="原组合" prop="beforeCombination" min-width="170" data-layout-group="组合信息" />
          <el-table-column label="新组合" prop="afterCombination" min-width="170" data-layout-group="组合信息" />
          <el-table-column label="提交" prop="applyTime" width="170" data-layout-group="申请信息" />
          <el-table-column label="状态" prop="status" width="100" align="center" data-layout-group="申请信息">
            <template #default="scope">
              <el-tag :type="statusType(scope.row.status)" size="small">{{ scope.row.status || '待审批' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="120" data-layout-group="操作">
            <template #default="scope">
              <el-button
                v-hasPermi="['stream.change_request:approve']"
                :disabled="scope.row.status && scope.row.status !== '待审批'"
                link
                type="primary"
                @click="handleApprove(scope.row)"
              >
                审批
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="没有待审批的选科变更申请" />
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

    <ApproveDialog ref="approveRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { listStreamChangeRequest } from '@/api/edu/stream';
import type { StreamChangeRequestQuery, StreamChangeRequestVO } from '@/api/edu/stream/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { checkPermi } from '@/utils/permission';
import ApproveDialog from './components/ApproveDialog.vue';

defineOptions({ name: 'EduStreamApprove' });

const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const list = ref<StreamChangeRequestVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);
const approveRef = ref<InstanceType<typeof ApproveDialog>>();

const queryParams = reactive<StreamChangeRequestQuery>({
  pageNum: 1,
  pageSize: 20,
  status: '待审批',
  gradeId: '',
  keyword: ''
});

const columns = ref([
  { key: 0, label: '申请单号', visible: true },
  { key: 1, label: '学生', visible: true },
  { key: 2, label: '年级 / 班级', visible: true },
  { key: 3, label: '原组合', visible: true },
  { key: 4, label: '新组合', visible: true },
  { key: 5, label: '提交', visible: true },
  { key: 6, label: '状态', visible: true }
]);

const canRead = computed(() => checkPermi(['stream.change_request:read']));

const statusType = (status?: string): 'warning' | 'primary' | 'success' | 'info' | 'danger' => {
  if (status === '已通过') return 'success';
  if (status === '已驳回') return 'danger';
  if (status === '已撤销') return 'info';
  return 'warning';
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listStreamChangeRequest({ ...queryParams });
    list.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.status = '待审批';
  queryParams.gradeId = '';
  queryParams.keyword = '';
  handleQuery();
};

const handleApprove = (row: StreamChangeRequestVO) => approveRef.value?.open(row);

onMounted(async () => {
  const gradeRes = await listGrade({ pageNum: 1, pageSize: 200 });
  gradeOptions.value = gradeRes.rows ?? [];
  getList();
});
</script>
