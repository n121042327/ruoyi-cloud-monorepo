<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/class-list.html 的 .page-head -->
    <div class="page-head">
      <h1>班级管理</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期全部班级</span>
      <el-tag type="primary">共 {{ total }} 个班级</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有班级管理的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!--
              查询区分组（page-field-layout 第 1、2 节）：
              教育信息：学校 → 校区 → 学年学期 → 年级；管理信息：班级类型 → 班主任；检索信息：关键字
              与原型 prototypes/functional/v2/pages/class-list.html 同序。
            -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学校" prop="schoolId" data-layout-group="教育信息">
                <el-select v-model="queryParams.schoolId" placeholder="请选择学校" style="width: 180px" @change="handleSchoolChange">
                  <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
                </el-select>
              </el-form-item>
              <el-form-item label="校区" prop="campusId" data-layout-group="教育信息">
                <el-select v-model="queryParams.campusId" placeholder="全部校区" clearable style="width: 150px">
                  <el-option v-for="item in campusOptions" :key="item.campusId" :label="item.campusName" :value="item.campusId" />
                </el-select>
              </el-form-item>
              <el-form-item label="学年学期" prop="termId" data-layout-group="教育信息">
                <el-select v-model="queryParams.termId" placeholder="请选择学年学期" clearable style="width: 200px">
                  <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName" :value="item.termId" />
                </el-select>
              </el-form-item>
              <el-form-item label="年级" prop="gradeId" data-layout-group="教育信息">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable style="width: 160px">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
              <el-form-item label="班级类型" prop="classType" data-layout-group="管理信息">
                <el-select v-model="queryParams.classType" placeholder="全部类型" clearable style="width: 130px">
                  <el-option v-for="item in CLASS_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="班主任" prop="headTeacherId" data-layout-group="管理信息">
                <el-select v-model="queryParams.headTeacherId" placeholder="全部班主任" clearable filterable style="width: 160px">
                  <el-option v-for="item in teacherOptions" :key="item.teacherId" :label="item.teacherName" :value="item.teacherId" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键字" prop="keyword" data-layout-group="检索信息">
                <el-input
                  v-model="queryParams.keyword"
                  placeholder="班级名称 / 班主任姓名"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleQuery"
                />
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
              <el-button v-hasPermi="['org.class:create']" type="primary" plain icon="Plus" @click="handleAdd">新建</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.class:create']" type="primary" plain icon="Guide" @click="batchRef?.open()">批量生成班级</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.class:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="classList">
          <el-table-column v-if="columns[0].visible" label="校区" prop="campusName" width="140" data-layout-group="教育信息" />
          <el-table-column v-if="columns[1].visible" label="年级" prop="gradeName" width="130" data-layout-group="教育信息" />
          <el-table-column
            v-if="columns[2].visible"
            label="班级名称"
            prop="className"
            width="140"
            :show-overflow-tooltip="true"
            data-layout-group="教育信息"
          />
          <el-table-column v-if="columns[3].visible" label="类型" prop="classType" width="90" align="center" data-layout-group="教育信息">
            <template #default="scope">
              <el-tag size="small">{{ CLASS_TYPE_LABEL[scope.row.classType] ?? scope.row.classType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[4].visible" label="班主任" prop="headTeacherName" width="120" data-layout-group="教育信息">
            <template #default="scope">{{ scope.row.headTeacherName || '未指定' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[5].visible" label="教室" prop="classroom" width="110" data-layout-group="教育信息" />
          <el-table-column v-if="columns[6].visible" label="容量" prop="classCapacity" width="80" align="center" data-layout-group="管理信息" />
          <el-table-column v-if="columns[7].visible" label="在读" prop="studentCount" width="80" align="center" data-layout-group="管理信息">
            <template #default="scope">
              <span :class="{ 'text-red-500': scope.row.classCapacity && scope.row.studentCount > scope.row.classCapacity }">
                {{ scope.row.studentCount ?? 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[8].visible" label="状态" prop="classStatus" width="90" align="center" data-layout-group="管理信息">
            <template #default="scope">
              <el-tag :type="scope.row.classStatus === 'disabled' ? 'info' : 'success'" size="small">
                {{ scope.row.classStatus === 'disabled' ? '已停用' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="270" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['org.class:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['org.class:update']" link type="primary" @click="handleRoster(scope.row)">花名册</el-button>
              <el-button
                v-if="scope.row.status !== 'disabled'"
                v-hasPermi="['org.class:update']"
                link
                type="danger"
                @click="handleDisable(scope.row)"
              >
                停用
              </el-button>
              <el-dropdown v-hasPermi="['org.class:remove']" class="ml-2" @command="(cmd: string) => handleMore(cmd, scope.row)">
                <el-button link type="primary"
                  >更多<el-icon class="ml-1"><arrow-down /></el-icon
                ></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete">删除班级</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到班级数据" />
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

      <class-form-dialog
        ref="formDialogRef"
        :term-options="termOptions"
        :grade-options="gradeOptions"
        :teacher-options="teacherOptions"
        :campus-options="campusOptions"
        @success="getList"
      />
      <ClassBatchDialog ref="batchRef" :term-options="termOptions" :grade-options="gradeOptions" @success="getList" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import ClassFormDialog from './components/ClassFormDialog.vue';
import ClassBatchDialog from './components/ClassBatchDialog.vue';
import { CLASS_TYPE_OPTIONS, useClassList } from './composables/useClassList';
import type { ClassVO } from '@/api/edu/class/types';
import { disableClass, removeClass } from '@/api/edu/class';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduClassList' });

const router = useRouter();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const {
  loading,
  showSearch,
  total,
  classList,
  queryParams,
  columns,
  schoolOptions,
  campusOptions,
  termOptions,
  gradeOptions,
  teacherOptions,
  getList,
  handleQuery,
  resetQuery,
  handleExport,
  handleSchoolChange
} = useClassList();

const formDialogRef = ref<InstanceType<typeof ClassFormDialog>>();
const batchRef = ref<InstanceType<typeof ClassBatchDialog>>();
const queryFormRef = ref();

const CLASS_TYPE_LABEL: Record<string, string> = CLASS_TYPE_OPTIONS.reduce(
  (acc, item) => {
    acc[item.value] = item.label;
    return acc;
  },
  {} as Record<string, string>
);

const canRead = computed(() => checkPermi(['org.class:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
};

const handleUpdate = (row: ClassVO) => {
  formDialogRef.value?.open(row);
};

/** 花名册与班级详情同属 PAGE-CLS-DETAIL（详情页承载花名册、任课教师、任职历史与变更记录） */
const handleRoster = (row: ClassVO) => {
  router.push({ path: '/edu/class/detail', query: { classId: row.classId } });
};

/** 停用班级（有在读学生不允许删除、只允许停用，BR-CLASS-006） */
const handleDisable = async (row: ClassVO) => {
  const { value } = await ElMessageBox.prompt(`确认停用「${row.className}」？`, '停用班级', {
    inputPlaceholder: '停用原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写停用原因（至少 5 个字）'),
    type: 'warning'
  });
  await disableClass(row.classId, value);
  ElMessage.success('班级已停用');
  getList();
};

/** 操作列「更多」 */
const handleMore = async (command: string, row: ClassVO) => {
  if (command === 'delete') {
    await handleDelete(row);
  }
};

/**
 * 逻辑删除班级（REQ-CLS-043 / REQ-CLS-046）
 *
 * 前端只负责二次确认与必填原因；有无在读学生、有无任教关系一律由后端判断，
 * 后端拒绝时原样展示后端错误信息（D-204：不做前端预判、不绕过校验）。
 */
const handleDelete = async (row: ClassVO) => {
  const { value } = await ElMessageBox.prompt(`确认删除「${row.className}」？有在读学生时后端会拒绝，请改用停用。`, '删除班级', {
    inputPlaceholder: '删除原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写删除原因（至少 5 个字）'),
    type: 'warning'
  });
  await removeClass(row.classId, value);
  ElMessage.success('班级已删除');
  getList();
};
</script>
