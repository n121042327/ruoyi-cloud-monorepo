<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/promotion-history.html 的 .page-head -->
    <div class="page-head">
      <h1>异动历史</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">共 {{ total }} 条异动记录</el-tag>
      <el-tag type="info">追加式 · 不可删除</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有异动历史的查看权限" />
    </el-card>

    <template v-else>
      <el-card shadow="hover" class="mb-[10px]">
        <!-- 查询区分组：教育信息（学年学期 → 年级）→ 异动信息（异动类型 → 生效日期） -->
        <el-form :model="queryParams" :inline="true">
          <el-form-item label="学年学期" prop="termId" data-layout-group="教育信息">
            <el-select v-model="queryParams.termId" placeholder="请选择学年学期" clearable style="width: 200px">
              <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName" :value="item.termId" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="gradeId" data-layout-group="教育信息">
            <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable style="width: 150px">
              <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
            </el-select>
          </el-form-item>
          <el-form-item label="异动类型" prop="changeType" data-layout-group="异动信息">
            <el-select v-model="queryParams.changeType" placeholder="全部类型" clearable style="width: 150px">
              <el-option v-for="item in CHANGE_TYPE_OPTIONS" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="生效日期" prop="effectiveDate" data-layout-group="异动信息">
            <el-date-picker v-model="queryParams.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <el-table v-loading="loading" border :data="historyList">
          <el-table-column label="学号" prop="studentNo" width="130" data-layout-group="学生信息" />
          <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
          <el-table-column label="异动类型" prop="changeType" width="110" align="center" data-layout-group="异动信息" />
          <el-table-column label="生效日期" prop="effectiveDate" width="120" align="center" data-layout-group="异动信息" />
          <el-table-column label="原状态" prop="beforeStatus" width="110" align="center" data-layout-group="异动信息" />
          <el-table-column label="新状态" prop="afterStatus" width="110" align="center" data-layout-group="异动信息" />
          <el-table-column label="操作人" prop="operator" width="120" data-layout-group="异动信息" />
          <el-table-column label="原因" prop="reason" min-width="200" :show-overflow-tooltip="true" data-layout-group="异动信息" />
          <el-table-column fixed="right" label="操作" width="100" data-layout-group="操作">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)">查看</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到异动记录" />
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listEnrollmentChange } from '@/api/edu/promotion';
import type { EnrollmentChangeQuery, EnrollmentChangeVO } from '@/api/edu/promotion/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { checkPermi } from '@/utils/permission';
import { ElMessageBox } from 'element-plus';

defineOptions({ name: 'EduPromotionHistory' });

/** 异动类型（升班 PRD 4.6 的类型表） */
const CHANGE_TYPE_OPTIONS = ['休学', '复学', '转出', '转入未报到', '退学', '开除', '结业', '肄业', '出国', '失踪', '死亡', '留级'];

const loading = ref(false);
const total = ref(0);
const historyList = ref<EnrollmentChangeVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);

const queryParams = reactive<EnrollmentChangeQuery>({ pageNum: 1, pageSize: 20, termId: '', gradeId: '', changeType: '', effectiveDate: '' });

const canRead = computed(() => checkPermi(['enrollment.status:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listEnrollmentChange({ ...queryParams });
    historyList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const handleReset = () => {
  queryParams.termId = '';
  queryParams.gradeId = '';
  queryParams.changeType = '';
  queryParams.effectiveDate = '';
  queryParams.pageNum = 1;
  getList();
};

onMounted(async () => {
  try {
    const [termRes, gradeRes] = await Promise.all([listTerm({}), listGrade({})]);
    termOptions.value = termRes.data ?? [];
    gradeOptions.value = gradeRes.data ?? [];
    queryParams.termId = termOptions.value.find((item) => item.current)?.termId ?? '';
  } catch {
    termOptions.value = [];
    gradeOptions.value = [];
  }
  await getList();
});

/** 查看异动详情（只读，异动记录追加式不可修改） */
const handleDetail = (row: EnrollmentChangeVO) => {
  ElMessageBox.alert(
    `${row.studentName ?? '—'}（${row.studentNo ?? '—'}）\n异动类型：${row.changeType ?? '—'}\n生效日期：${row.effectiveDate ?? '—'}\n原状态：${row.beforeStatus ?? '—'} → 新状态：${row.afterStatus ?? '—'}\n操作人：${row.operator ?? '—'}\n原因：${row.reason ?? '—'}`,
    '异动详情',
    { confirmButtonText: '知道了' }
  );
};
</script>
