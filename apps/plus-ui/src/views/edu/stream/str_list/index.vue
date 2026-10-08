<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有选科清单的查看权限" />
    </el-card>

    <template v-else>
      <el-card v-show="showSearch" shadow="hover" class="mb-2">
        <el-form :model="queryParams" label-width="90px">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="学年学期" data-layout-group="选科范围">
                <el-select v-model="queryParams.termId" placeholder="全部学年学期" clearable class="w-full">
                  <el-option
                    v-for="item in termOptions"
                    :key="item.termId"
                    :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
                    :value="item.termId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="年级" data-layout-group="选科范围">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable class="w-full">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="首选" data-layout-group="选科范围">
                <el-select v-model="queryParams.primarySubjectCode" placeholder="全部首选" clearable class="w-full">
                  <el-option label="物理" value="physics" />
                  <el-option label="历史" value="history" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态" data-layout-group="选科范围">
                <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="w-full">
                  <el-option label="已生效" value="已生效" />
                  <el-option label="待审批" value="待审批" />
                  <el-option label="未选择" value="未选择" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="关键词" data-layout-group="选科范围">
                <el-input v-model="queryParams.keyword" placeholder="姓名 / 学号" clearable @keyup.enter="handleQuery" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
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
              <el-button v-hasPermi="['stream.selection:read']" type="primary" plain icon="DataAnalysis" @click="goStat">组合分布统计</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['stream.config:read']" type="primary" plain icon="SetUp" @click="goConfig">选科配置</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['data.export:export']" type="primary" plain icon="Download" @click="handleExport">导出</el-button>
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
          title="选科结果决定教学班归属；行政班不变（教学班与行政班是两套独立关系）。截止后的变更走审批（BR-STREAM-005）。"
        />

        <el-table v-loading="loading" border :data="list">
          <el-table-column label="学号" prop="studentNo" width="130" data-layout-group="学生信息" />
          <el-table-column label="姓名" prop="studentName" width="100" data-layout-group="学生信息" />
          <el-table-column label="年级 / 班级" prop="className" min-width="150" data-layout-group="学生信息">
            <template #default="scope">{{ [scope.row.gradeName, scope.row.className].filter(Boolean).join(' / ') || '—' }}</template>
          </el-table-column>
          <el-table-column label="首选" prop="primarySubjectName" width="100" align="center" data-layout-group="选科信息" />
          <el-table-column label="再选" prop="secondarySubjectNames" min-width="170" data-layout-group="选科信息">
            <template #default="scope">{{ (scope.row.secondarySubjectNames ?? []).join(' · ') || '—' }}</template>
          </el-table-column>
          <el-table-column label="组合" prop="combination" min-width="190" data-layout-group="选科信息" />
          <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="选科信息">
            <template #default="scope">
              <el-tag :type="scope.row.status === '已生效' ? 'success' : 'warning'" size="small">{{ scope.row.status || '未选择' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="180" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['stream.change_request:create']" link type="primary" @click="handleChange(scope.row)">变更申请</el-button>
              <el-button link type="primary" @click="handleHistory(scope.row)">查看历史</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="没有查询到选科记录" />
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

      <StreamHistorySection ref="historyRef" />
    </template>

    <StreamChangeDialog ref="changeRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { exportStreamSelection, listStreamSelection } from '@/api/edu/stream';
import type { StreamSelectionQuery, StreamSelectionVO } from '@/api/edu/stream/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { checkPermi } from '@/utils/permission';
import StreamChangeDialog from './components/StreamChangeDialog.vue';
import StreamHistorySection from './components/StreamHistorySection.vue';

defineOptions({ name: 'EduStreamList' });

const router = useRouter();

const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const list = ref<StreamSelectionVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);
const changeRef = ref<InstanceType<typeof StreamChangeDialog>>();
const historyRef = ref<InstanceType<typeof StreamHistorySection>>();

const queryParams = reactive<StreamSelectionQuery>({
  pageNum: 1,
  pageSize: 20,
  termId: '',
  gradeId: '',
  primarySubjectCode: '',
  status: '',
  keyword: ''
});

const columns = ref([
  { key: 0, label: '学号', visible: true },
  { key: 1, label: '姓名', visible: true },
  { key: 2, label: '年级 / 班级', visible: true },
  { key: 3, label: '首选', visible: true },
  { key: 4, label: '再选', visible: true },
  { key: 5, label: '组合', visible: true },
  { key: 6, label: '状态', visible: true }
]);

const canRead = computed(() => checkPermi(['stream.selection:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listStreamSelection({ ...queryParams });
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
  queryParams.termId = '';
  queryParams.gradeId = '';
  queryParams.primarySubjectCode = '';
  queryParams.status = '';
  queryParams.keyword = '';
  handleQuery();
};

const goStat = () => router.push('/edu/stream/stat');
const goConfig = () => router.push('/edu/stream/config');
const handleChange = (row: StreamSelectionVO) => changeRef.value?.open(row);
const handleHistory = (row: StreamSelectionVO) => historyRef.value?.load({ studentId: row.studentId });

/** 导出选科清单（权限 data.export:export） */
const handleExport = async () => {
  await exportStreamSelection({ ...queryParams });
  ElMessage.success('已生成选科清单，开始下载');
};

const loadOptions = async () => {
  const [termRes, gradeRes] = await Promise.all([listTerm({}), listGrade({ pageNum: 1, pageSize: 200 })]);
  termOptions.value = termRes.data ?? [];
  gradeOptions.value = gradeRes.rows ?? [];
};

onMounted(() => {
  loadOptions();
  getList();
});
</script>
