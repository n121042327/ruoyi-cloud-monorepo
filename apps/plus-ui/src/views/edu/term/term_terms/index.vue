<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/term-terms.html 的 .page-head -->
    <div class="page-head">
      <h1>学期管理</h1>
      <span class="scope-hint">数据范围：本校 · 可写</span>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学期管理的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['org.term:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
          <el-col :span="16">
            <span class="text-xs">所属学年：{{ academicYearName || '—' }}（{{ academicYearId || '未指定' }}）</span>
          </el-col>
        </el-row>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="每个学年至少一个学期；学期日期必须落在学年范围内且互不重叠；已被班级、任教关系或花名册引用的学期不允许删除。"
      />

      <el-table v-loading="loading" border :data="termList">
        <el-table-column label="学期" prop="termName" min-width="180" data-layout-group="学期信息" />
        <el-table-column label="开始日期" prop="startDate" width="130" align="center" data-layout-group="学期信息" />
        <el-table-column label="结束日期" prop="endDate" width="130" align="center" data-layout-group="学期信息" />
        <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="学期信息">
          <template #default="scope">
            <el-tag :type="scope.row.status === '已归档' ? 'info' : 'success'" size="small">{{ scope.row.status || '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="班级数" prop="classCount" width="90" align="center" data-layout-group="统计信息" />
        <el-table-column label="在读学生" prop="studentCount" width="110" align="center" data-layout-group="统计信息" />
        <el-table-column fixed="right" label="操作" width="120" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['org.term:remove']" link type="primary" @click="handleRemove(scope.row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="该学年暂无学期" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listTerm, removeTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduTermTerms' });

const route = useRoute();

const loading = ref(false);
const termList = ref<TermVO[]>([]);
const academicYearId = ref(typeof route.query.academicYearId === 'string' ? route.query.academicYearId : '');
const academicYearName = ref(typeof route.query.academicYearName === 'string' ? route.query.academicYearName : '');

const canRead = computed(() => checkPermi(['org.term:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listTerm({ academicYearId: academicYearId.value });
    termList.value = res.data ?? [];
    if (!academicYearName.value && termList.value.length) {
      academicYearName.value = termList.value[0].academicYearName ?? '';
    }
  } finally {
    loading.value = false;
  }
};

/** 删除学期：已被引用的学期后端会拒绝并说明是哪一类引用（REQ-TERM-019 / 036） */
const handleRemove = async (row: TermVO) => {
  const { value } = await ElMessageBox.prompt(
    `确认删除学期「${row.termName}」？已被班级、任教关系或花名册引用的学期不允许删除，请填写原因。`,
    '删除学期',
    { confirmButtonText: '确认删除', cancelButtonText: '取消', inputPlaceholder: '删除原因（必填）', inputValidator: (v) => !!v || '请填写删除原因' }
  );
  await removeTerm(row.termId, value);
  ElMessage.success('已删除学期');
  await getList();
};

onMounted(getList);
</script>
