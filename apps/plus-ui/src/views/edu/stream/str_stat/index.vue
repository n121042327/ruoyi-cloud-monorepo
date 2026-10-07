<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">组合分布统计</h2>
        <el-tag type="primary" size="small">{{ stat.termName || '当前学年学期' }}</el-tag>
      </div>
      <div class="flex gap-2 mt-3">
        <el-button v-hasPermi="['data.export:export']" icon="Download" @click="handleExport">导出选科清单</el-button>
        <el-button icon="Refresh" @click="loadAll">刷新</el-button>
        <el-button v-hasPermi="['stream.selection:read']" @click="unselectedRef?.open(termId)">查看未选科学生</el-button>
        <el-button v-hasPermi="['stream.selection:read']" type="primary" @click="goGenClass">按组合生成教学班</el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-row :gutter="16">
        <el-col :span="6"><el-statistic title="已选科人数" :value="stat.selectedCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="未选科人数" :value="stat.unselectedCount ?? 0" /></el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">首选分布</div>
          <div class="mt-1">
            <el-tag v-for="item in stat.primaryDistribution ?? []" :key="item.subjectName" class="mr-1" type="success" effect="plain">
              {{ item.subjectName }} {{ item.memberCount ?? 0 }}（{{ item.ratio || '—' }}）
            </el-tag>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="text-xs text-gray-500">口径</div>
          <div class="mt-1 text-xs">图表与下方明细表同源（同一接口 getStreamStat），分母为已选科人数。</div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>组合分布</span>
          <el-tag type="info" size="small">共 {{ (stat.combinations ?? []).length }} 种组合</el-tag>
        </div>
      </template>

      <div class="mb-3">
        <div v-for="item in stat.combinations ?? []" :key="item.subjectCombination" class="mb-2">
          <div class="flex items-center justify-between text-xs">
            <span>{{ item.subjectCombination || '—' }}</span>
            <span>{{ item.memberCount ?? 0 }} 人 · {{ item.ratio || '—' }}</span>
          </div>
          <el-progress :percentage="ratioPercent(item.ratio)" :show-text="false" :stroke-width="10" />
        </div>
        <el-empty v-if="!(stat.combinations ?? []).length" description="暂无组合分布数据" />
      </div>

      <el-table border :data="stat.combinations ?? []">
        <el-table-column label="组合" prop="subjectCombination" min-width="240" data-layout-group="组合信息" />
        <el-table-column label="首选" prop="primarySubjectName" width="100" align="center" data-layout-group="组合信息" />
        <el-table-column label="再选" prop="secondarySubjectNames" min-width="190" data-layout-group="组合信息">
          <template #default="scope">{{ (scope.row.secondarySubjectNames ?? []).join(' · ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="人数" prop="memberCount" width="100" align="center" data-layout-group="统计信息" />
        <el-table-column label="占比" prop="ratio" width="110" align="center" data-layout-group="统计信息" />
        <el-table-column fixed="right" label="操作" width="180" data-layout-group="操作">
          <template #default="scope">
            <el-button link type="primary" @click="handleViewStudents(scope.row)">查看学生</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无组合明细" />
        </template>
      </el-table>
    </el-card>

    <SubjectStatTable :list="stat.subjects ?? []" :loading="loading" />

    <UnselectedStudentDialog ref="unselectedRef" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { exportStreamSelection, getStreamStat } from '@/api/edu/stream';
import type { StreamStatCombinationVO, StreamStatVO } from '@/api/edu/stream/types';
import UnselectedStudentDialog from '@/views/edu/stream/str_config/components/UnselectedStudentDialog.vue';
import SubjectStatTable from './components/SubjectStatTable.vue';

defineOptions({ name: 'EduStreamStat' });

const router = useRouter();

const loading = ref(false);
const termId = ref('');
const stat = ref<StreamStatVO>({});
const unselectedRef = ref<InstanceType<typeof UnselectedStudentDialog>>();

/** 占比字符串（如「42.3%」）转进度条百分比 */
const ratioPercent = (ratio?: string) => {
  if (!ratio) return 0;
  const value = Number.parseFloat(ratio.replace('%', ''));
  return Number.isFinite(value) ? Math.min(100, Math.max(0, value)) : 0;
};

const loadAll = async () => {
  loading.value = true;
  try {
    const res = await getStreamStat({ termId: termId.value });
    stat.value = res.data ?? {};
    termId.value = stat.value.termId ?? termId.value;
  } finally {
    loading.value = false;
  }
};

const goGenClass = () => router.push('/edu/stream/generate-class');

const handleExport = async () => {
  await exportStreamSelection({ termId: termId.value });
  ElMessage.success('已生成选科清单，开始下载');
};

const handleViewStudents = (row: StreamStatCombinationVO) => {
  router.push({ path: '/edu/stream/list', query: { combination: row.subjectCombination ?? '' } });
};

onMounted(loadAll);
</script>
