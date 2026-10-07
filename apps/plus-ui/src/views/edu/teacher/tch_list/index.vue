<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有教师管理的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!--
              查询区分组（page-field-layout 第 1、2 节）：
              教育信息：学校 → 任教年级 → 任教班级 → 任教学科 → 教育角色；职业信息：在职状态；检索信息：关键字
            -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学校" prop="schoolId" data-layout-group="教育信息">
                <el-select v-model="queryParams.schoolId" placeholder="请选择学校" style="width: 180px" @change="handleSchoolChange">
                  <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
                </el-select>
              </el-form-item>
              <el-form-item label="任教年级" prop="gradeId" data-layout-group="教育信息">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable style="width: 150px" @change="handleGradeChange">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
              <el-form-item label="任教班级" prop="classId" data-layout-group="教育信息">
                <el-select v-model="queryParams.classId" placeholder="全部班级" clearable style="width: 150px">
                  <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
                </el-select>
              </el-form-item>
              <el-form-item label="任教学科" prop="subjectCode" data-layout-group="教育信息">
                <el-select v-model="queryParams.subjectCode" placeholder="全部学科" clearable style="width: 140px">
                  <el-option v-for="item in subjectOptions" :key="item.subjectCode" :label="item.subjectName" :value="item.subjectCode" />
                </el-select>
              </el-form-item>
              <el-form-item label="教育角色" prop="eduRole" data-layout-group="教育信息">
                <el-select v-model="queryParams.eduRole" placeholder="全部角色" clearable style="width: 140px">
                  <el-option v-for="item in EDU_ROLE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="在职状态" prop="employmentStatus" data-layout-group="职业信息">
                <el-select v-model="queryParams.employmentStatus" placeholder="全部状态" clearable style="width: 130px">
                  <el-option v-for="item in EMPLOYMENT_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键字" prop="keyword" data-layout-group="检索信息">
                <el-input v-model="queryParams.keyword" placeholder="工号 / 姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
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
              <el-button v-hasPermi="['person.teacher:create']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['person.teacher:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="teacherList">
          <el-table-column v-if="columns[0].visible" label="工号" prop="teacherNo" width="120" data-layout-group="基础信息" />
          <el-table-column
            v-if="columns[1].visible"
            label="姓名"
            prop="teacherName"
            width="110"
            :show-overflow-tooltip="true"
            data-layout-group="基础信息"
          />
          <el-table-column v-if="columns[2].visible" label="性别" prop="gender" width="70" align="center" data-layout-group="基础信息" />
          <el-table-column v-if="columns[3].visible" label="所属学校" prop="schoolName" width="160" data-layout-group="教育信息" />
          <el-table-column v-if="columns[4].visible" label="教育角色" prop="eduRoles" width="150" data-layout-group="教育信息">
            <template #default="scope">{{ scope.row.eduRoles || '未分配' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[5].visible" label="任教学科" prop="subjectNames" width="150" data-layout-group="教育信息">
            <template #default="scope">{{ scope.row.subjectNames || '—' }}</template>
          </el-table-column>
          <el-table-column
            v-if="columns[6].visible"
            label="任课班级数"
            prop="teachingClassCount"
            width="110"
            align="center"
            data-layout-group="教育信息"
          />
          <el-table-column v-if="columns[7].visible" label="在职状态" prop="employmentStatus" width="100" align="center" data-layout-group="职业信息">
            <template #default="scope">
              <el-tag :type="scope.row.employmentStatus === '在职' ? 'success' : 'info'" size="small">
                {{ scope.row.employmentStatus || '在职' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[8].visible" label="联系电话" prop="teacherPhone" width="130" data-layout-group="联系方式" />

          <el-table-column fixed="right" label="操作" width="200" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['person.teacher:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-if="scope.row.employmentStatus === '在职'" link type="primary" @click="handleAssign">设置任教</el-button>
              <el-button v-else link type="primary" @click="handleRevokeLeave">撤销离职登记</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到教师数据" />
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

      <teacher-form-dialog ref="formDialogRef" :school-options="schoolOptions" @success="getList" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { ElMessage } from 'element-plus';
import TeacherFormDialog from './components/TeacherFormDialog.vue';
import { EDU_ROLE_OPTIONS, EMPLOYMENT_STATUS_OPTIONS, useTeacherList } from './composables/useTeacherList';
import type { TeacherVO } from '@/api/edu/teacher/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduTeacherList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const {
  loading,
  showSearch,
  total,
  teacherList,
  queryParams,
  columns,
  schoolOptions,
  gradeOptions,
  classOptions,
  subjectOptions,
  getList,
  handleQuery,
  resetQuery,
  handleExport,
  handleSchoolChange,
  handleGradeChange
} = useTeacherList();

const formDialogRef = ref<InstanceType<typeof TeacherFormDialog>>();
const queryFormRef = ref();

const canRead = computed(() => checkPermi(['person.teacher:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
};

const handleUpdate = (row: TeacherVO) => {
  formDialogRef.value?.open(row);
};

/** 任教关系与撤销离职登记在阶段 6 的后续批次交付（DP-01：班主任唯一写入口在班级管理） */
const handleAssign = () => {
  ElMessage.info('设置任教关系在阶段 6 的下一批交付');
};

const handleRevokeLeave = () => {
  ElMessage.info('撤销离职登记在阶段 6 的下一批交付');
};
</script>
