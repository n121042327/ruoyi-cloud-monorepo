<template>
  <div class="p-2">
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
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="studentList">
          <el-table-column v-if="columns[0].visible" label="学号" prop="studentNo" width="130" data-layout-group="基础信息" />
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
          <el-table-column fixed="right" label="操作" width="120" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['person.student:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import StudentFormDialog from './components/StudentFormDialog.vue';
import { useStudentList } from './composables/useStudentList';
import { ENROLLMENT_STATUS_FILTER_OPTIONS, ENROLLMENT_STATUS_LABEL, GENDER_OPTIONS, STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';
import type { StudentForm, StudentVO } from '@/api/edu/student/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduStudentList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

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

/** 页面级读权限：无权限时不渲染内容（路由侧另有菜单权限兜底） */
const canRead = computed(() => checkPermi(['person.student:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
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
</script>
