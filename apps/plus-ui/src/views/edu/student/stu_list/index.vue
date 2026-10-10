<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/student-list.html 的 .page-head -->
    <div class="page-head">
      <h1>学生管理</h1>
      <span class="scope-hint">数据范围：本校 · 全部年级 · 含历史状态</span>
      <el-tag type="primary">共 {{ total }} 名学生</el-tag>
    </div>
    <!-- 无权限：不显示页面内容，也不显示报错（apps/plus-ui/AGENTS.md 第 6 节） -->
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学生管理的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!--
              查询区分组（docs/00-governance/page-field-layout.md 第 1、2 节）：
              第一行 教育信息：学校 → 学年学期 → 年级 → 班级 → 学籍状态
              第一行 检索信息：关键字；第二行 学生信息：性别 → 入学年份 → 证件号后四位
              与原型 prototypes/functional/v2/pages/student-list.html 的查询区同序。
            -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学校" prop="schoolId" data-layout-group="教育信息">
                <el-select
                  v-model="queryParams.schoolId"
                  :disabled="!canSwitchSchool"
                  placeholder="请选择学校"
                  style="width: 180px"
                  @change="handleSchoolChange"
                >
                  <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
                </el-select>
              </el-form-item>
              <el-form-item label="学年学期" prop="termId" data-layout-group="教育信息">
                <el-select v-model="queryParams.termId" placeholder="请选择学年学期" clearable style="width: 200px">
                  <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName" :value="item.termId" />
                </el-select>
              </el-form-item>
              <el-form-item label="年级" prop="gradeId" data-layout-group="教育信息">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable style="width: 160px" @change="handleGradeChange">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
              <el-form-item label="班级" prop="classId" data-layout-group="教育信息">
                <el-select v-model="queryParams.classId" placeholder="全部班级" clearable style="width: 160px">
                  <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
                </el-select>
              </el-form-item>
              <el-form-item label="学籍状态" prop="enrollmentStatus" data-layout-group="教育信息">
                <el-select v-model="enrollmentStatusList" multiple collapse-tags collapse-tags-tooltip placeholder="全部状态" style="width: 240px">
                  <el-option v-for="item in ENROLLMENT_STATUS_FILTER_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键字" prop="keyword" data-layout-group="检索信息">
                <el-input
                  v-model="queryParams.keyword"
                  placeholder="学号 / 姓名 / 全国学籍号"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="性别" prop="gender" data-layout-group="学生信息">
                <el-select v-model="queryParams.gender" placeholder="全部" clearable style="width: 110px">
                  <el-option v-for="item in GENDER_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="入学年份" prop="enrollYear" data-layout-group="学生信息">
                <el-input v-model="queryParams.enrollYear" placeholder="如 2026" maxlength="4" clearable style="width: 120px" />
              </el-form-item>
              <el-form-item label="证件号后四位" prop="idCardSuffix" data-layout-group="检索信息">
                <el-input v-model="queryParams.idCardSuffix" placeholder="4 位数字" maxlength="4" clearable style="width: 120px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </transition>

      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button v-hasPermi="['person.student:create']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['person.student:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['data.export:export']" plain icon="Download" @click="handleBatchExport">批量导出</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.class:update']" plain icon="Switch" @click="handleBatchTransfer">批量调班</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="studentList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" data-layout-group="选择" />
          <el-table-column v-if="columns[0].visible" label="学号" prop="studentNo" width="130" data-layout-group="基础信息">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)">{{ scope.row.studentNo }}</el-button>
            </template>
          </el-table-column>
          <el-table-column
            v-if="columns[1].visible"
            label="姓名"
            prop="studentName"
            width="110"
            :show-overflow-tooltip="true"
            data-layout-group="基础信息"
          />
          <el-table-column v-if="columns[2].visible" label="性别" prop="gender" width="70" align="center" data-layout-group="基础信息" />
          <el-table-column v-if="columns[3].visible" label="入学年份" prop="enrollYear" width="100" align="center" data-layout-group="教育信息" />
          <el-table-column v-if="columns[4].visible" label="学段" prop="stageCode" width="80" align="center" data-layout-group="教育信息">
            <template #default="scope">
              <span>{{ STAGE_CODE_LABEL[scope.row.stageCode] ?? scope.row.stageCode }}</span>
            </template>
          </el-table-column>
          <el-table-column
            v-if="columns[5].visible"
            label="年级"
            prop="gradeName"
            width="120"
            :show-overflow-tooltip="true"
            data-layout-group="教育信息"
          />
          <el-table-column
            v-if="columns[6].visible"
            label="班级"
            prop="className"
            width="120"
            :show-overflow-tooltip="true"
            data-layout-group="教育信息"
          />
          <el-table-column v-if="columns[7].visible" label="学籍状态" prop="enrollmentStatus" width="110" align="center" data-layout-group="教育信息">
            <template #default="scope">
              <el-tag :type="scope.row.enrollmentStatus === 'enrolled' ? 'success' : 'info'">
                {{ ENROLLMENT_STATUS_LABEL[scope.row.enrollmentStatus] ?? scope.row.enrollmentStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[8].visible" label="联系电话" prop="studentPhone" width="130" data-layout-group="联系方式" />
          <el-table-column v-if="columns[9].visible" label="更新时间" prop="updateTime" width="160" align="center" data-layout-group="管理信息">
            <template #default="scope">
              <span>{{ proxy?.parseTime(scope.row.updateTime) }}</span>
            </template>
          </el-table-column>

          <!-- 操作列：固定最右，独立分组（page-field-layout 第 4 节） -->
          <el-table-column fixed="right" label="操作" width="300" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['person.student:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['enrollment.status:update']" link type="primary" @click="handleStatus(scope.row)">学籍异动</el-button>
              <el-button v-hasPermi="['org.class:update']" link type="primary" @click="handleTransfer(scope.row)">调班</el-button>
              <el-button v-hasPermi="['person.student:read']" link type="primary" @click="activationRef?.open(scope.row)">激活码</el-button>
              <el-dropdown class="ml-2" @command="(cmd: string) => handleMore(cmd, scope.row)">
                <el-button link type="primary"
                  >更多<el-icon class="ml-1"><arrow-down /></el-icon
                ></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="studentNo">变更学号</el-dropdown-item>
                    <el-dropdown-item command="resetPwd">重置密码</el-dropdown-item>
                    <el-dropdown-item command="export">导出学生</el-dropdown-item>
                    <el-dropdown-item v-hasPermi="['person.student:remove']" command="delete" divided>删除学生</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到学生数据" />
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

      <student-form-dialog ref="formDialogRef" :school-id="queryParams.schoolId" @success="getList" />
      <student-detail-drawer
        ref="detailDrawerRef"
        @edit="handleEditFromDrawer"
        @status="handleStatus"
        @transfer="handleTransfer"
        @promotion-change="handlePromotionChange"
        @cross-transfer="handleCrossTransfer"
      />
      <student-status-dialog ref="statusDialogRef" :school-id="queryParams.schoolId" @success="getList" />
      <student-status-dialog ref="promotionDialogRef" mode="promotion" :school-id="queryParams.schoolId" @success="getList" />
      <student-transfer-dialog ref="transferDialogRef" :school-id="queryParams.schoolId" @success="getList" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import StudentDetailDrawer from './components/StudentDetailDrawer.vue';
import StudentFormDialog from './components/StudentFormDialog.vue';
import StudentNoDialog from './components/StudentNoDialog.vue';
import StudentActivationDialog from './components/StudentActivationDialog.vue';
import StudentStatusDialog from './components/StudentStatusDialog.vue';
import StudentTransferDialog from './components/StudentTransferDialog.vue';
import { useStudentList } from './composables/useStudentList';
import { ENROLLMENT_STATUS_FILTER_OPTIONS, ENROLLMENT_STATUS_LABEL, GENDER_OPTIONS, STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';
import type { StudentForm, StudentVO } from '@/api/edu/student/types';
import { checkPermi } from '@/utils/permission';
import { exportStudent, removeStudent, resetStudentPassword } from '@/api/edu/student';

defineOptions({ name: 'EduStudentList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const {
  loading,
  showSearch,
  total,
  studentList,
  queryFormRef,
  queryParams,
  columns,
  canSwitchSchool,
  enrollmentStatusList,
  schoolOptions,
  termOptions,
  gradeOptions,
  classOptions,
  getList,
  handleQuery,
  resetQuery,
  handleExport,
  handleSchoolChange,
  handleGradeChange
} = useStudentList();

const formDialogRef = ref<InstanceType<typeof StudentFormDialog>>();
const studentNoRef = ref<InstanceType<typeof StudentNoDialog>>();
const activationRef = ref<InstanceType<typeof StudentActivationDialog>>();
const detailDrawerRef = ref<InstanceType<typeof StudentDetailDrawer>>();
const statusDialogRef = ref<InstanceType<typeof StudentStatusDialog>>();
const promotionDialogRef = ref<InstanceType<typeof StudentStatusDialog>>();
const transferDialogRef = ref<InstanceType<typeof StudentTransferDialog>>();
const selectedRows = ref<StudentVO[]>([]);

/** 页面级读权限：无权限时不渲染内容（路由侧另有菜单权限兜底） */
const canRead = computed(() => checkPermi(['person.student:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
};

/** 点击学号打开只读详情抽屉（原型 PAGE-STU-DETAIL） */
const handleDetail = (row: StudentVO) => {
  detailDrawerRef.value?.open(row.studentId);
};

/** 抽屉内「编辑」：关闭抽屉并打开编辑弹窗 */
const handleEditFromDrawer = (row: StudentVO) => {
  handleUpdate(row);
};

/** 学籍异动：状态写入入口只有这一个（PRD 4.4） */
const handleStatus = (row: StudentVO) => {
  statusDialogRef.value?.open(row);
};

/** 调班：复用班级模块的 transferClass（DP-01） */
const handleTransfer = (row: StudentVO) => {
  transferDialogRef.value?.open(row);
};

/** 异动登记（升班口径）：与学生侧「学籍异动」同一字段、同一接口 */
const handlePromotionChange = (row: StudentVO) => {
  promotionDialogRef.value?.open(row);
};

/** 跨校转学：跳转转出校向导（学号带过去，步骤 1 预选） */
const handleCrossTransfer = (row: StudentVO) => {
  router.push({ path: '/edu/student/cross-transfer', query: { studentId: row.studentId } });
};

const handleSelectionChange = (rows: StudentVO[]) => {
  selectedRows.value = rows;
};

/** 批量操作前置校验：至少勾选 1 行（原型 ACT-STU-019 / ACT-STU-020） */
const ensureBatchSelection = (): boolean => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要处理的学生；批量操作至少需要 1 行。');
    return false;
  }
  return true;
};

/** 批量导出：按当前筛选 + 已勾选学生导出（导出前由后端重新解析数据范围） */
const handleBatchExport = () => {
  if (!ensureBatchSelection()) {
    return;
  }
  proxy?.download(
    'edu/student/export',
    { ...queryParams, studentIds: selectedRows.value.map((item) => item.studentId).join(',') },
    `student_${new Date().getTime()}.xlsx`
  );
};

/** 批量调班：多条学生一次迁移，逐条校验范围，任一条越权整体拒绝 */
const handleBatchTransfer = () => {
  if (!ensureBatchSelection()) {
    return;
  }
  transferDialogRef.value?.openBatch(selectedRows.value);
};

const handleUpdate = (row: StudentVO) => {
  const form: Partial<StudentForm> & { studentNo?: string } = {
    studentId: row.studentId,
    nationalStudentNo: row.nationalStudentNo,
    studentName: row.studentName,
    gender: row.gender,
    enrollYear: row.enrollYear,
    stageCode: row.stageCode,
    gradeId: row.gradeId,
    classId: row.classId,
    studentNo: row.studentNo
  };
  formDialogRef.value?.open(form);
};

/** 操作列「更多」：变更学号、重置密码、导出学生、删除学生 */
const handleMore = async (command: string, row: StudentVO) => {
  if (command === 'studentNo') {
    studentNoRef.value?.open(row);
    return;
  }
  if (command === 'resetPwd') {
    await ElMessageBox.confirm(`确认重置「${row.studentName}」的账号密码？`, '重置密码', { type: 'warning' });
    await resetStudentPassword(row.studentId);
    ElMessage.success('密码已重置');
    return;
  }
  if (command === 'export') {
    await exportStudent({ studentIds: [row.studentId] });
    ElMessage.success('已提交导出任务，请到异步任务中心下载');
    return;
  }
  if (command === 'delete') {
    await handleDelete(row);
  }
};

/**
 * 逻辑删除学生（REQ-STU-075 / REQ-STU-078）
 *
 * 前端只负责二次确认与必填原因；有无在读关系、有无异动记录一律由后端判断，
 * 后端拒绝时原样展示后端错误信息（D-204：不做前端预判、不绕过校验）。
 */
const handleDelete = async (row: StudentVO) => {
  const { value } = await ElMessageBox.prompt(`确认删除「${row.studentName}」？删除后学号作废，永不回收。`, '删除学生', {
    inputPlaceholder: '删除原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写删除原因（至少 5 个字）'),
    type: 'warning'
  });
  await removeStudent(row.studentId, value);
  ElMessage.success('学生已删除');
  getList();
};
</script>
