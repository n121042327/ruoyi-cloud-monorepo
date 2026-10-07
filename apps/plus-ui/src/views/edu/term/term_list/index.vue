<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学年学期的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['org.term:update']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="false" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="yearList">
        <el-table-column label="学年" prop="academicYearCode" width="160" data-layout-group="教育信息" />
        <el-table-column label="开始日期" prop="startDate" width="130" align="center" data-layout-group="教育信息" />
        <el-table-column label="结束日期" prop="endDate" width="130" align="center" data-layout-group="教育信息" />
        <el-table-column label="学期数" prop="termCount" width="90" align="center" data-layout-group="管理信息" />
        <el-table-column label="当前学年学期" prop="currentTermName" min-width="200" data-layout-group="管理信息">
          <template #default="scope">{{ scope.row.currentTermName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="管理信息">
          <template #default="scope">
            <el-tag :type="scope.row.status === '已归档' ? 'info' : 'success'" size="small">{{ scope.row.status || '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['org.term:update']" link type="primary" @click="handleSetCurrent(scope.row)">设为当前</el-button>
            <el-button v-hasPermi="['org.term:update']" link type="primary" @click="handleTerms">学期管理</el-button>
            <el-button v-hasPermi="['org.term:remove']" link type="primary" @click="handleArchive">归档</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="没有查询到学年数据" />
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listAcademicYear } from '@/api/edu/term';
import type { AcademicYearQuery, AcademicYearVO } from '@/api/edu/term/types';
import { setCurrentTerm } from '@/api/edu/term';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduTermList' });

const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const yearList = ref<AcademicYearVO[]>([]);

const queryParams = reactive<AcademicYearQuery>({ pageNum: 1, pageSize: 20, schoolId: '', academicYearCode: '', status: '' });

const columns = ref([
  { key: 0, label: '学年', visible: true },
  { key: 1, label: '开始日期', visible: true },
  { key: 2, label: '结束日期', visible: true },
  { key: 3, label: '学期数', visible: true },
  { key: 4, label: '当前学年学期', visible: true },
  { key: 5, label: '状态', visible: true }
]);

const canRead = computed(() => checkPermi(['org.term:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listAcademicYear({ ...queryParams });
    yearList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

/**
 * 设为当前学年学期：作用在学期级别，需要二次确认。
 * 学年行本身没有 termId，这里先提示从「学期管理」进入选择具体学期；
 * 若后端在学年行直接返回当前学期 ID，则走 setCurrentTerm。
 */
const handleSetCurrent = async (row: AcademicYearVO) => {
  const termId = (row as AcademicYearVO & { currentTermId?: string }).currentTermId;
  if (!termId) {
    ElMessage.info('请在「学期管理」中选择要设为当前的学期');
    return;
  }
  await ElMessageBox.confirm('设为当前学年学期后，各模块的默认筛选与上下文都会切换，确认继续？', '设置当前学年学期', {
    confirmButtonText: '确认切换',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await setCurrentTerm(termId);
  ElMessage.success('已切换当前学年学期');
  await getList();
};

const handleTerms = () => {
  ElMessage.info('学期管理在阶段 6 的下一批交付');
};

const handleArchive = () => {
  ElMessage.info('学年归档在阶段 6 的下一批交付');
};

onMounted(getList);
</script>
