import { getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { listClass } from '@/api/edu/class';
import type { ClassQuery, ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listCampus, listSchool } from '@/api/edu/school';
import type { CampusVO, SchoolVO } from '@/api/edu/school/types';
import { listTeacher } from '@/api/edu/teacher';
import type { TeacherVO } from '@/api/edu/teacher/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';

/** 班级类型（字段字典 class_type 的取值：行政班 / 教学班） */
export const CLASS_TYPE_OPTIONS = [
  { value: 'administrative', label: '行政班' },
  { value: 'teaching', label: '教学班' }
];

/** 班级列表页的查询、分页与下拉取数（页面只留模板与绑定） */
export function useClassList() {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;

  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const classList = ref<ClassVO[]>([]);

  const schoolOptions = ref<SchoolVO[]>([]);
  const campusOptions = ref<CampusVO[]>([]);
  const termOptions = ref<TermVO[]>([]);
  const gradeOptions = ref<GradeVO[]>([]);
  const teacherOptions = ref<TeacherVO[]>([]);

  const queryParams = reactive<ClassQuery>({
    pageNum: 1,
    pageSize: 20,
    schoolId: '',
    campusId: '',
    termId: '',
    gradeId: '',
    classType: '',
    headTeacherId: '',
    keyword: ''
  });

  /** 列配置：顺序与原型 class-list.html 的表头一致 */
  const columns = ref([
    { key: 0, label: '校区', visible: true },
    { key: 1, label: '年级', visible: true },
    { key: 2, label: '班级名称', visible: true },
    { key: 3, label: '类型', visible: true },
    { key: 4, label: '班主任', visible: true },
    { key: 5, label: '教室', visible: true },
    { key: 6, label: '容量', visible: true },
    { key: 7, label: '在读', visible: true },
    { key: 8, label: '状态', visible: true }
  ]);

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listClass({ ...queryParams });
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
    queryParams.campusId = '';
    queryParams.termId = '';
    queryParams.gradeId = '';
    queryParams.classType = '';
    queryParams.headTeacherId = '';
    queryParams.keyword = '';
    queryParams.pageNum = 1;
    getList();
  };

  /** 导出按当前筛选与数据范围生成（operationId exportClass，异步） */
  const handleExport = () => {
    proxy?.download('edu/class/export', { ...queryParams }, `class_${new Date().getTime()}.xlsx`);
  };

  /** 学校变化：校区 / 学年学期 / 年级 / 班主任按所选学校重取，失效值清空（GAP-047 口径） */
  const handleSchoolChange = async () => {
    queryParams.campusId = '';
    queryParams.termId = '';
    queryParams.gradeId = '';
    queryParams.headTeacherId = '';
    await Promise.all([loadCampusOptions(), loadTermOptions(), loadGradeOptions(), loadTeacherOptions()]);
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

  const loadCampusOptions = async () => {
    if (!queryParams.schoolId) {
      campusOptions.value = [];
      return;
    }
    try {
      const res = await listCampus(queryParams.schoolId);
      campusOptions.value = res.data ?? [];
    } catch {
      campusOptions.value = [];
    }
  };

  const loadTermOptions = async () => {
    try {
      const res = await listTerm({ schoolId: queryParams.schoolId });
      termOptions.value = res.data ?? [];
      if (!queryParams.termId) {
        queryParams.termId = termOptions.value.find((item) => item.current)?.termId ?? '';
      }
    } catch {
      termOptions.value = [];
    }
  };

  const loadGradeOptions = async () => {
    try {
      const res = await listGrade({ schoolId: queryParams.schoolId });
      gradeOptions.value = res.data ?? [];
    } catch {
      gradeOptions.value = [];
    }
  };

  const loadTeacherOptions = async () => {
    try {
      const res = await listTeacher({ schoolId: queryParams.schoolId, pageNum: 1, pageSize: 200 });
      teacherOptions.value = res.rows ?? [];
    } catch {
      teacherOptions.value = [];
    }
  };

  onMounted(async () => {
    await loadSchoolOptions();
    await Promise.all([loadCampusOptions(), loadTermOptions(), loadGradeOptions(), loadTeacherOptions()]);
    await getList();
  });

  return {
    loading,
    showSearch,
    total,
    classList,
    queryParams,
    columns,
    schoolOptions,
    campusOptions,
    termOptions,
    gradeOptions,
    teacherOptions,
    getList,
    handleQuery,
    resetQuery,
    handleExport,
    handleSchoolChange
  };
}
