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
          <el-table-column
            v-if="columns[2].visible"
            label="年级名称"
            prop="gradeName"
            width="150"
            :show-overflow-tooltip="true"
            data-layout-group="教育信息"
          />
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

          <el-table-column fixed="right" label="操作" width="200" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['org.grade:update']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['org.grade:update']" link type="primary" @click="handleLeader">指定年级主任</el-button>
              <el-button v-hasPermi="['org.grade:remove']" link type="primary" @click="handleArchive">归档</el-button>
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { ElMessage } from 'element-plus';
import GradeFormDialog from './components/GradeFormDialog.vue';
import { useGradeList } from './composables/useGradeList';
import type { GradeVO } from '@/api/edu/grade/types';
import { checkPermi } from '@/utils/permission';
import { STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';

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
const stageLabel = (code?: string) => (code ? (STAGE_CODE_LABEL[code] ?? code) : '—');

const queryFormRef = ref();

const canRead = computed(() => checkPermi(['org.grade:read']));

const handleAdd = () => {
  formDialogRef.value?.open();
};

const handleUpdate = (row: GradeVO) => {
  formDialogRef.value?.open(row);
};

/** 指定年级主任与归档在后续批次交付（本批已给出入口，先不做假流程） */
const handleLeader = () => {
  ElMessage.info('指定年级主任在阶段 6 的下一批交付');
};

const handleArchive = () => {
  ElMessage.info('年级归档在阶段 6 的下一批交付');
};
</script>
