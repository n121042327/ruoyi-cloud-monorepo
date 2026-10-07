import { getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { listGrade } from '@/api/edu/grade';
import type { GradeQuery, GradeVO } from '@/api/edu/grade/types';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import { STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

/** 年级列表页的查询、分页与下拉取数 */
export function useGradeList() {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;

  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const gradeList = ref<GradeVO[]>([]);
  const schoolOptions = ref<SchoolVO[]>([]);

  const queryParams = reactive<GradeQuery>({
    pageNum: 1,
    pageSize: 20,
    schoolId: '',
    stageCode: '',
    enrollYear: '',
    leaderUserId: ''
  });

  const columns = ref([
    { key: 0, label: '入学年份', visible: true },
    { key: 1, label: '年级名称', visible: true },
    { key: 2, label: '序号', visible: true },
    { key: 3, label: '年级主任', visible: true },
    { key: 4, label: '班级数', visible: true },
    { key: 5, label: '在读学生数', visible: true },
    { key: 6, label: '状态', visible: true }
  ]);

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listGrade({ ...queryParams });
      gradeList.value = res.rows ?? [];
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
    queryParams.stageCode = '';
    queryParams.enrollYear = '';
    queryParams.leaderUserId = '';
    queryParams.pageNum = 1;
    getList();
  };

  /** 导出按当前筛选与数据范围生成（operationId exportGrade，异步） */
  const handleExport = () => {
    proxy?.download('edu/grade/export', { ...queryParams }, `grade_${new Date().getTime()}.xlsx`);
  };

  const loadSchoolOptions = async () => {
    try {
      const res = await listSchool();
      schoolOptions.value = res.data ?? [];
      if (!queryParams.schoolId) {
        queryParams.schoolId = schoolOptions.value.find((item) => item.current)?.schoolId ?? schoolOptions.value[0]?.schoolId ?? '';
      }
    } catch {
      schoolOptions.value = [];
    }
  };

  const handleSchoolChange = () => {
    queryParams.stageCode = '';
    queryParams.enrollYear = '';
    queryParams.leaderUserId = '';
    getList();
  };

  onMounted(async () => {
    await loadSchoolOptions();
    await getList();
  });

  return {
    loading,
    showSearch,
    total,
    gradeList,
    queryParams,
    columns,
    schoolOptions,
    stageOptions: STAGE_CODE_OPTIONS,
    getList,
    handleQuery,
    resetQuery,
    handleExport,
    handleSchoolChange
  };
}
