<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学校管理的查看权限" />
    </el-card>

    <template v-else>
      <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/school-list.html 的 .page-head -->
      <div class="page-head">
        <h1>学校管理</h1>
        <span class="scope-hint">数据范围：本租户（DS-04）· 可写</span>
        <el-tag type="primary">共 {{ total }} 所学校</el-tag>
        <el-tag type="info">教学数据落在学校租户上（BR-ORG-001）</el-tag>
      </div>

      <!-- 查询区独立成卡：与高保真一致（filter 一张卡，工具栏 + 表格一张卡） -->
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-2">
          <el-card shadow="hover">
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学校名称" prop="keyword" data-layout-group="基础信息">
                <el-input v-model="queryParams.keyword" placeholder="学校名称 / 学校编码" clearable style="width: 200px" @keyup.enter="getList" />
              </el-form-item>
              <el-form-item label="学段" prop="stageCode" data-layout-group="教育信息">
                <el-select v-model="queryParams.stageCode" placeholder="全部学段" clearable style="width: 130px">
                  <el-option v-for="item in STAGE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="schoolStatus" data-layout-group="管理信息">
                <el-select v-model="queryParams.schoolStatus" placeholder="全部状态" clearable style="width: 130px">
                  <el-option label="启用中" value="active" />
                  <el-option label="已停用" value="disabled" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
                <el-button icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </transition>

      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.school:create']" type="primary" plain icon="Plus" @click="handleAdd">新建学校</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.school:update']" plain icon="Guide" @click="handleInit">开通初始化</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.school:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.school:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="schoolList">
          <el-table-column
            v-if="columns[0].visible"
            label="学校名称"
            prop="schoolName"
            min-width="200"
            :show-overflow-tooltip="true"
            data-layout-group="基础信息"
          />
          <el-table-column v-if="columns[1].visible" label="学校编码" prop="schoolCode" width="130" data-layout-group="基础信息" />
          <el-table-column v-if="columns[2].visible" label="开设学段" prop="stageCodes" width="180" data-layout-group="教育信息">
            <template #default="scope">{{ stageLabel(scope.row.stageCodes) || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[3].visible" label="校区数" prop="campusCount" width="90" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[4].visible" label="班级数" prop="classCount" width="90" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[5].visible" label="在读学生" prop="studentCount" width="100" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[6].visible" label="状态" prop="schoolStatus" width="100" align="center" data-layout-group="管理信息">
            <template #default="scope">
              <el-tag :type="scope.row.schoolStatus === 'disabled' ? 'info' : 'success'" size="small">
                {{ scope.row.schoolStatus === 'disabled' ? '已停用' : '启用中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[7].visible" label="所属租户" prop="tenantId" width="140" data-layout-group="管理信息" />
          <el-table-column fixed="right" label="操作" width="220" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['org.school:update']" link type="primary" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.schoolStatus !== 'disabled'"
                v-hasPermi="['org.school:update']"
                link
                type="primary"
                @click="handleDisable(scope.row)"
              >
                停用
              </el-button>
              <el-button v-else v-hasPermi="['org.school:update']" link type="primary" @click="handleEnable(scope.row)">启用</el-button>
              <el-button v-hasPermi="['org.school:update']" link type="primary" @click="handleCampus">校区管理</el-button>
              <el-button v-hasPermi="['org.school:update']" link type="primary" @click="handleStage">学段配置</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到学校数据" />
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

    <school-form-dialog ref="formDialogRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import SchoolFormDialog from './components/SchoolFormDialog.vue';
import { disableSchool, enableSchool, listSchool } from '@/api/edu/school';
import type { SchoolQuery } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import { STAGE_CODE_LABEL, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduSchoolList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const schoolList = ref<SchoolVO[]>([]);

const queryParams = reactive<SchoolQuery>({ pageNum: 1, pageSize: 20, keyword: '', stageCode: '', schoolStatus: '' });

const columns = ref([
  { key: 0, label: '学校名称', visible: true },
  { key: 1, label: '学校编码', visible: true },
  { key: 2, label: '开设学段', visible: true },
  { key: 3, label: '校区数', visible: true },
  { key: 4, label: '班级数', visible: true },
  { key: 5, label: '在读学生', visible: true },
  { key: 6, label: '状态', visible: true },
  { key: 7, label: '所属租户', visible: true }
]);

const formDialogRef = ref<InstanceType<typeof SchoolFormDialog>>();

const canRead = computed(() => checkPermi(['org.school:read']));
const STAGE_OPTIONS = STAGE_CODE_OPTIONS;
const queryFormRef = ref();

const handleReset = () => {
  queryParams.keyword = '';
  queryParams.stageCode = '';
  queryParams.schoolStatus = '';
  queryParams.pageNum = 1;
  getList();
};

/** 用学校模块自己的学段编码展示（与年级模块共用同一份字典） */
const stageLabel = (codes?: string) =>
  (codes ?? '')
    .split(',')
    .map((code) => STAGE_CODE_LABEL[code.trim()] ?? code.trim())
    .filter(Boolean)
    .join(' / ');

const getList = async () => {
  loading.value = true;
  try {
    const res = await listSchool({ ...queryParams });
    schoolList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

/** 导出学校清单需显式授权，且导出写审计（REQ-SCH-008） */
const handleExport = () => {
  proxy?.download('edu/school/export', { ...queryParams }, `school_${new Date().getTime()}.xlsx`);
};

/** 停用：二次确认并填写原因；停用不等于删除（REQ-SCH-036 ~ 039） */
const handleDisable = async (row: SchoolVO) => {
  const { value } = await ElMessageBox.prompt(
    `停用「${row.schoolName}」后，该校租户下所有人员登录与写操作都会被拒绝，历史数据完整保留。请填写停用原因。`,
    '停用学校',
    { confirmButtonText: '确认停用', cancelButtonText: '取消', inputPlaceholder: '停用原因（必填）', inputValidator: (v) => !!v || '请填写停用原因' }
  );
  await disableSchool(row.schoolId, value);
  ElMessage.success('已停用学校');
  await getList();
};

const handleEnable = async (row: SchoolVO) => {
  await ElMessageBox.confirm(`确认启用「${row.schoolName}」？`, '启用学校', {
    confirmButtonText: '确认启用',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await enableSchool(row.schoolId);
  ElMessage.success('已启用学校');
  await getList();
};

/** 新建 / 编辑学校（弹窗内自行做必填校验） */
const handleAdd = () => {
  formDialogRef.value?.open();
};

const handleUpdate = (row: SchoolVO) => {
  formDialogRef.value?.open(row);
};

/** 开通初始化：高保真工具栏第 2 个入口，跳 PAGE-SCH-INIT（REQ-SCH-019 / 045） */
const handleInit = () => {
  router.push('/edu/school/init');
};

/** 校区管理：高保真里点「校区管理」进校区页（PAGE-SCH-CAMPUS） */
const handleCampus = () => {
  router.push('/edu/school/campus');
};

const handleStage = () => {
  ElMessage.info('学段配置在阶段 6 的下一批交付');
};

onMounted(getList);
</script>
