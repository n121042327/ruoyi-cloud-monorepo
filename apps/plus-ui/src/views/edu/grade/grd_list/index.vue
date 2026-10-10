<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/grade-list.html 的 .page-head -->
    <div class="page-head">
      <h1>年级管理</h1>
      <span class="scope-hint">数据范围：本校 · 小学 / 初中 / 高中全部年级</span>
      <el-tag type="primary">共 {{ total }} 个年级</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有年级管理的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!-- 查询区分组（page-field-layout 第 1、2 节）：教育信息（学校 → 学段 → 入学年份 → 年级主任） -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学校" prop="schoolId" data-layout-group="教育信息">
                <el-select v-model="queryParams.schoolId" placeholder="请选择学校" style="width: 180px" @change="handleSchoolChange">
                  <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
                </el-select>
              </el-form-item>
              <el-form-item label="学段" prop="stageCode" data-layout-group="教育信息">
                <el-select v-model="queryParams.stageCode" placeholder="全部学段" clearable style="width: 120px">
                  <el-option v-for="item in stageOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="入学年份" prop="enrollYear" data-layout-group="教育信息">
                <el-input v-model="queryParams.enrollYear" placeholder="如 2026" maxlength="4" clearable style="width: 120px" />
              </el-form-item>
              <el-form-item label="年级主任" prop="leaderUserId" data-layout-group="教育信息">
                <el-input v-model="queryParams.leaderUserId" placeholder="按姓名检索" clearable style="width: 160px" />
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
              <el-button v-hasPermi="['org.grade:create']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.grade:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="gradeList">
          <el-table-column v-if="columns[0].visible" label="学段" prop="stageCode" width="110" align="center" data-layout-group="教育信息">
            <template #default="scope">{{ stageLabel(scope.row.stageCode) }}</template>
          </el-table-column>
          <el-table-column v-if="columns[1].visible" label="入学年份" prop="enrollYear" width="100" align="center" data-layout-group="教育信息" />
          <el-table-column v-if="columns[2].visible" label="年级名称" prop="gradeName" min-width="160" data-layout-group="教育信息">
            <template #default="scope">
              <el-button link type="primary" @click="detailRef?.open(scope.row)">{{ scope.row.gradeName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[3].visible" label="序号" prop="gradeLevel" width="80" align="center" data-layout-group="教育信息" />
          <el-table-column v-if="columns[4].visible" label="年级主任" prop="leaderNames" width="150" data-layout-group="教育信息">
            <template #default="scope">{{ scope.row.leaderNames || '未指定' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[5].visible" label="班级数" prop="classCount" width="90" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[6].visible" label="在读学生数" prop="studentCount" width="110" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[7].visible" label="状态" prop="gradeStatus" width="100" align="center" data-layout-group="管理信息">
            <template #default="scope">
              <el-tag :type="scope.row.gradeStatus === 'archived' ? 'info' : 'success'" size="small">
                {{ scope.row.gradeStatus === 'archived' ? '已归档' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="370" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['org.grade:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['org.grade:update']" link type="primary" @click="leaderRef?.open(scope.row)">指定年级主任</el-button>
              <el-button v-hasPermi="['org.grade:read']" link type="primary" @click="promotionRef?.open()">升班视图</el-button>
              <el-button
                v-if="scope.row.gradeStatus !== 'archived'"
                v-hasPermi="['org.grade:update']"
                link
                type="danger"
                @click="handleArchive(scope.row)"
                >归档</el-button
              >
              <el-dropdown v-hasPermi="['org.grade:remove']" class="ml-2" @command="(cmd: string) => handleMore(cmd, scope.row)">
                <el-button link type="primary"
                  >更多<el-icon class="ml-1"><arrow-down /></el-icon
                ></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete">删除年级</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到年级数据" />
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

      <grade-form-dialog ref="formDialogRef" :school-options="schoolOptions" @success="getList" />
      <GradeLeaderDialog ref="leaderRef" :teacher-options="teacherOptions" @success="getList" />
      <GradePromotionDialog ref="promotionRef" />
    </template>

    <GradeDetailDrawer ref="detailRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import GradeFormDialog from './components/GradeFormDialog.vue';
import GradeLeaderDialog from './components/GradeLeaderDialog.vue';
import GradeDetailDrawer from './components/GradeDetailDrawer.vue';
import GradePromotionDialog from './components/GradePromotionDialog.vue';
import { useGradeList } from './composables/useGradeList';
import type { GradeVO } from '@/api/edu/grade/types';
import { checkPermi } from '@/utils/permission';
import { STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';
import { archiveGrade, removeGrade } from '@/api/edu/grade';
import { listTeacher } from '@/api/edu/teacher';
import type { TeacherVO } from '@/api/edu/teacher/types';

defineOptions({ name: 'EduGradeList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const {
  loading,
  showSearch,
  total,
  gradeList,
  queryParams,
  columns,
  schoolOptions,
  stageOptions,
  getList,
  handleQuery,
  resetQuery,
  handleExport,
  handleSchoolChange
} = useGradeList();

const formDialogRef = ref<InstanceType<typeof GradeFormDialog>>();
const leaderRef = ref<InstanceType<typeof GradeLeaderDialog>>();
const promotionRef = ref<InstanceType<typeof GradePromotionDialog>>();
const detailRef = ref<InstanceType<typeof GradeDetailDrawer>>();
const stageLabel = (code?: string) => (code ? (STAGE_CODE_LABEL[code] ?? code) : '—');

const queryFormRef = ref();
const teacherOptions = ref<TeacherVO[]>([]);

const canRead = computed(() => checkPermi(['org.grade:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
};

const handleUpdate = (row: GradeVO) => {
  formDialogRef.value?.open(row);
};

/** 指定年级主任与归档在后续批次交付（本批已给出入口，先不做假流程） */

/** 年级归档（归档后不允许新增班级，只读保留） */
const handleArchive = async (row: GradeVO) => {
  const { value } = await ElMessageBox.prompt(`确认归档「${row.gradeName}」？归档后不允许新增班级。`, '年级归档', {
    inputPlaceholder: '归档原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写归档原因（至少 5 个字）'),
    type: 'warning'
  });
  await archiveGrade(row.gradeId, value);
  ElMessage.success('年级已归档');
  getList();
};

/** 操作列「更多」 */
const handleMore = async (command: string, row: GradeVO) => {
  if (command === 'delete') {
    await handleDelete(row);
  }
};

/**
 * 逻辑删除年级（REQ-GRD-030 / BR-GRADE-005）
 *
 * 前端只负责二次确认与必填原因；有无班级、有无学生关系一律由后端判断，
 * 后端拒绝时原样展示后端错误信息（D-204：不做前端预判、不绕过校验）。
 */
const handleDelete = async (row: GradeVO) => {
  const { value } = await ElMessageBox.prompt(`确认删除「${row.gradeName}」？有班级或学生关系时后端会拒绝，请改用归档。`, '删除年级', {
    inputPlaceholder: '删除原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写删除原因（至少 5 个字）'),
    type: 'warning'
  });
  await removeGrade(row.gradeId, value);
  ElMessage.success('年级已删除');
  getList();
};

/** 年级主任下拉取数（指定弹窗用） */
const loadTeacherOptions = async () => {
  const res = await listTeacher({ pageNum: 1, pageSize: 200, employmentStatus: '在职' });
  teacherOptions.value = res.rows ?? [];
};

onMounted(loadTeacherOptions);
</script>
