<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有教学班的查看权限" />
    </el-card>

    <template v-else>
      <el-card v-show="showSearch" shadow="hover" class="mb-2">
        <el-form :model="queryParams" label-width="90px">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="学年学期" data-layout-group="教学班范围">
                <el-select v-model="queryParams.termId" placeholder="全部学年学期" clearable class="w-full">
                  <el-option
                    v-for="item in termOptions"
                    :key="item.termId"
                    :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
                    :value="item.termId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="年级" data-layout-group="教学班范围">
                <el-select v-model="queryParams.gradeId" placeholder="全部年级" clearable class="w-full">
                  <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="组合 / 学科" data-layout-group="教学班范围">
                <el-input v-model="queryParams.combination" placeholder="如 物理 + 化学 + 生物" clearable @keyup.enter="handleQuery" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态" data-layout-group="教学班范围">
                <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="w-full">
                  <el-option label="正常" value="正常" />
                  <el-option label="已停用" value="已停用" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <div class="flex justify-end gap-2">
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10">
            <el-col :span="1.5">
              <el-button v-hasPermi="['org.teaching_class:create']" type="primary" plain icon="Plus" @click="goGenerate">去生成教学班</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
            </el-col>
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="false" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-alert
          class="mb-3"
          type="info"
          :closable="false"
          title="行政班与教学班是两套独立关系，选科组合不等于行政班；教学班的创建入口唯一在「按组合生成教学班」向导，本页只做查询、详情与停用。"
        />

        <el-table v-loading="loading" border :data="classList">
          <el-table-column label="教学班名称" prop="className" min-width="200" data-layout-group="教学班信息" />
          <el-table-column label="学年学期" prop="termName" width="170" data-layout-group="教学班信息" />
          <el-table-column label="年级" prop="gradeName" width="120" data-layout-group="教学班信息" />
          <el-table-column label="组合 / 学科" prop="subjectCombination" min-width="200" data-layout-group="教学班信息" />
          <el-table-column label="人数" prop="memberCount" width="90" align="center" data-layout-group="教学班信息" />
          <el-table-column label="任课教师" prop="teacherName" width="150" data-layout-group="教学班信息" />
          <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="教学班信息">
            <template #default="scope">
              <el-tag :type="scope.row.status === '已停用' ? 'info' : 'success'" size="small">{{ scope.row.status || '正常' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="140" data-layout-group="操作">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
              <el-button
                v-if="scope.row.status !== '已停用'"
                v-hasPermi="['org.teaching_class:update']"
                link
                type="danger"
                @click="handleDisable(scope.row)"
              >
                停用
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="没有查询到教学班" />
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

    <TeachingClassDetailDrawer ref="detailRef" />
    <TeachingClassDisableDialog ref="disableRef" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { listTeachingClass } from '@/api/edu/class';
import type { TeachingClassQuery, TeachingClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { checkPermi } from '@/utils/permission';
import TeachingClassDetailDrawer from './components/TeachingClassDetailDrawer.vue';
import TeachingClassDisableDialog from './components/TeachingClassDisableDialog.vue';

defineOptions({ name: 'EduClassTeaching' });

const router = useRouter();

const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const classList = ref<TeachingClassVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);
const detailRef = ref<InstanceType<typeof TeachingClassDetailDrawer>>();
const disableRef = ref<InstanceType<typeof TeachingClassDisableDialog>>();

const queryParams = reactive<TeachingClassQuery>({
  pageNum: 1,
  pageSize: 20,
  termId: '',
  gradeId: '',
  combination: '',
  status: ''
});

const columns = ref([
  { key: 0, label: '教学班名称', visible: true },
  { key: 1, label: '学年学期', visible: true },
  { key: 2, label: '年级', visible: true },
  { key: 3, label: '组合 / 学科', visible: true },
  { key: 4, label: '人数', visible: true },
  { key: 5, label: '任课教师', visible: true },
  { key: 6, label: '状态', visible: true }
]);

const canRead = computed(() => checkPermi(['org.teaching_class:read']));

const getList = async () => {
  loading.value = true;
  try {
    const res = await listTeachingClass({ ...queryParams });
    classList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.termId = '';
  queryParams.gradeId = '';
  queryParams.combination = '';
  queryParams.status = '';
  handleQuery();
};

const goGenerate = () => router.push('/edu/stream/generate-class');
const handleDetail = (row: TeachingClassVO) => detailRef.value?.open(row);
const handleDisable = (row: TeachingClassVO) => disableRef.value?.open(row);

const loadOptions = async () => {
  const [termRes, gradeRes] = await Promise.all([listTerm({}), listGrade({ pageNum: 1, pageSize: 200 })]);
  termOptions.value = termRes.data ?? [];
  gradeOptions.value = gradeRes.rows ?? [];
};

onMounted(() => {
  loadOptions();
  getList();
});
</script>
