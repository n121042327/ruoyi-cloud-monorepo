<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/subject-list.html 的 .page-head -->
    <div class="page-head">
      <h1>学科与配置</h1>
      <span class="scope-hint">数据范围：本校 · 可写</span>
      <el-tag type="primary">共 {{ total }} 个学科</el-tag>
      <el-tag type="info">首选 / 再选是固定集合</el-tag>
    </div>

    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学科配置的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!-- 查询区分组：基础信息（学科名称 / 编码）→ 教育信息（学段）→ 配置信息（选科角色 → 状态） -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学科名称 / 编码" prop="keyword" data-layout-group="基础信息">
                <el-input v-model="queryParams.keyword" placeholder="请输入" clearable style="width: 180px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="学段" prop="stageCode" data-layout-group="教育信息">
                <el-select v-model="queryParams.stageCode" placeholder="全部学段" clearable style="width: 130px">
                  <el-option v-for="item in stageOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="选科角色" prop="filterStreamRole" data-layout-group="配置信息">
                <el-select v-model="queryParams.filterStreamRole" placeholder="全部角色" clearable style="width: 130px">
                  <el-option v-for="item in STREAM_ROLE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="filterStatus" data-layout-group="配置信息">
                <el-select v-model="queryParams.filterStatus" placeholder="全部状态" clearable style="width: 120px">
                  <el-option label="启用" value="active" />
                  <el-option label="停用" value="disabled" />
                </el-select>
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
              <el-button v-hasPermi="['org.subject:create']" type="primary" plain icon="Plus" @click="formRef?.open()">新建学科</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.subject:create']" plain icon="Guide" @click="batchRef?.open()">按学段批量初始化</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="subjectList">
          <el-table-column v-if="columns[0].visible" label="学科编码" prop="subjectCode" width="130" data-layout-group="基础信息" />
          <el-table-column v-if="columns[1].visible" label="学科名称" prop="subjectName" width="140" data-layout-group="基础信息" />
          <el-table-column v-if="columns[2].visible" label="启用学段" min-width="180" data-layout-group="教育信息">
            <template #default="scope">{{ stageLabel(scope.row.stageCodes) || '未启用' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[3].visible" label="参与 3+1+2" width="120" align="center" data-layout-group="配置信息">
            <template #default="scope">
              <el-tag :type="scope.row.streamEnabled === '1' ? 'success' : 'info'" size="small">
                {{ scope.row.streamEnabled === '1' ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[4].visible" label="选科角色" width="110" align="center" data-layout-group="配置信息">
            <template #default="scope">
              {{ STREAM_ROLE_LABEL[scope.row.streamRole ?? 'none'] ?? '不参与' }}
            </template>
          </el-table-column>
          <el-table-column v-if="columns[5].visible" label="排序号" prop="sortNo" width="90" align="center" data-layout-group="配置信息" />
          <el-table-column v-if="columns[6].visible" label="状态" width="100" align="center" data-layout-group="配置信息">
            <template #default="scope">
              <el-tag :type="scope.row.subjectStatus === 'disabled' ? 'info' : 'success'" size="small">
                {{ scope.row.subjectStatus === 'disabled' ? '停用' : '启用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column fixed="right" label="操作" width="350" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['org.subject:update']" link type="primary" @click="formRef?.open(scope.row)">编辑</el-button>
              <el-button v-hasPermi="['org.subject:update']" link type="primary" @click="streamRef?.open(scope.row)">选科角色</el-button>
              <el-button v-hasPermi="['org.subject:update']" link type="primary" @click="stageRef?.open(scope.row)">学段配置</el-button>
              <el-button
                v-if="scope.row.subjectStatus !== 'disabled'"
                v-hasPermi="['org.subject:update']"
                link
                type="danger"
                @click="handleDisable(scope.row)"
              >
                停用
              </el-button>
              <el-button v-else v-hasPermi="['org.subject:update']" link type="primary" @click="handleEnable(scope.row)">启用</el-button>
              <el-button v-hasPermi="['org.subject:read']" link type="primary" @click="handleReference(scope.row)">引用检查</el-button>
              <el-dropdown v-hasPermi="['org.subject:remove']" class="ml-2" @command="(cmd: string) => handleMore(cmd, scope.row)">
                <el-button link type="primary"
                  >更多<el-icon class="ml-1"><arrow-down /></el-icon
                ></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete">删除学科</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到学科数据" />
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

      <SubjectFormDialog ref="formRef" @success="getList" />
      <SubjectStageDialog ref="stageRef" @success="getList" />
      <SubjectStreamDialog ref="streamRef" @success="getList" />
      <SubjectBatchDialog ref="batchRef" @success="getList" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { STREAM_ROLE_OPTIONS, stageLabel, useSubjectList } from './composables/useSubjectList';
import { checkPermi } from '@/utils/permission';
import { checkSubjectReference, disableSubject, enableSubject, removeSubject } from '@/api/edu/subject';
import type { SubjectVO } from '@/api/edu/subject/types';
import SubjectFormDialog from './components/SubjectFormDialog.vue';
import SubjectStageDialog from './components/SubjectStageDialog.vue';
import SubjectStreamDialog from './components/SubjectStreamDialog.vue';
import SubjectBatchDialog from './components/SubjectBatchDialog.vue';

defineOptions({ name: 'EduSubjectList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { loading, showSearch, total, subjectList, queryParams, columns, stageOptions, getList, handleQuery, resetQuery } = useSubjectList();

const queryFormRef = ref();
const canRead = computed(() => checkPermi(['org.subject:read']));

const formRef = ref<InstanceType<typeof SubjectFormDialog>>();
const stageRef = ref<InstanceType<typeof SubjectStageDialog>>();
const streamRef = ref<InstanceType<typeof SubjectStreamDialog>>();
const batchRef = ref<InstanceType<typeof SubjectBatchDialog>>();

const STREAM_ROLE_LABEL: Record<string, string> = STREAM_ROLE_OPTIONS.reduce(
  (acc, item) => {
    acc[item.value] = item.label;
    return acc;
  },
  {} as Record<string, string>
);

/** 停用：有引用也能停用，但必须填原因（BR-SUBJECT-006 口径） */
const handleDisable = async (row: SubjectVO) => {
  const { value } = await ElMessageBox.prompt(`确认停用「${row.subjectName}」？`, '停用学科', {
    inputPlaceholder: '停用原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写停用原因（至少 5 个字）'),
    type: 'warning'
  });
  await disableSubject(row.subjectId, value);
  ElMessage.success('已停用');
  getList();
};

const handleEnable = async (row: SubjectVO) => {
  await enableSubject(row.subjectId);
  ElMessage.success('已启用');
  getList();
};

/** 操作列「更多」 */
const handleMore = async (command: string, row: SubjectVO) => {
  if (command === 'delete') {
    await handleDelete(row);
  }
};

/**
 * 逻辑删除学科（REQ-SUB-030 / REQ-SUB-031 / REQ-SUB-033）
 *
 * 前端只负责二次确认与必填原因；任教关系 / 教学班 / 学生选科三类引用一律由后端判断，
 * 后端拒绝时原样展示后端错误信息（D-204：不做前端预判、不绕过校验）。
 */
const handleDelete = async (row: SubjectVO) => {
  const { value } = await ElMessageBox.prompt(`确认删除「${row.subjectName}」？有引用时后端会拒绝，请改用停用。`, '删除学科', {
    inputPlaceholder: '删除原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '请填写删除原因（至少 5 个字）'),
    type: 'warning'
  });
  await removeSubject(row.subjectId, value);
  ElMessage.success('学科已删除');
  getList();
};

/** 引用检查：任教关系 / 教学班 / 学生选科三类引用 */
const handleReference = async (row: SubjectVO) => {
  const res = await checkSubjectReference(row.subjectId);
  const data = res.data ?? {};
  ElMessageBox.alert(
    `任教关系：${data.teachingAssignmentCount ?? 0} 条\n教学班：${data.teachingClassCount ?? 0} 个\n学生选科：${data.studentStreamCount ?? 0} 条\n\n` +
      (data.referenced ? '存在引用，只能停用不能删除。' : '暂无引用，可以删除。'),
    `引用检查 · ${row.subjectName}`,
    { confirmButtonText: '知道了' }
  );
};
</script>
