import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';
import type { FormInstance } from 'element-plus';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import { listStudent } from '@/api/edu/student';
import type { StudentQuery, StudentVO } from '@/api/edu/student/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { useUserStore } from '@/store/modules/user';

/** 可切换学校的角色：平台运营与超级管理员；学校侧角色锁定本校（学生 PRD 5.2 / DS-01） */
const SWITCH_SCHOOL_ROLES = ['platform_ops', 'super_admin'];

/**
 * 学生列表页的查询、分页与下拉取数。
 *
 * 页面只保留模板与绑定；请求、状态与级联规则都在这里（apps/plus-ui/AGENTS.md 第 4 节）。
 */
export function useStudentList() {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;
  const userStore = useUserStore();

  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const studentList = ref<StudentVO[]>([]);
  const queryFormRef = ref<FormInstance>();

  const schoolOptions = ref<SchoolVO[]>([]);
  const termOptions = ref<TermVO[]>([]);
  const gradeOptions = ref<GradeVO[]>([]);
  const classOptions = ref<ClassVO[]>([]);
  /** 学籍状态多选：提交时按 OpenAPI 的约定拼成逗号分隔字符串 */
  const enrollmentStatusList = ref<string[]>([]);

  const queryParams = reactive<StudentQuery>({
    pageNum: 1,
    pageSize: 20,
    schoolId: '',
    termId: '',
    gradeId: '',
    classId: '',
    gender: '',
    enrollYear: '',
    keyword: '',
    idCardSuffix: ''
  });

  /** 列配置：顺序与原型 student-list.html 的表头一致，含 data-layout-group 分组 */
  const columns = ref([
    { key: 0, label: '学号', visible: true },
    { key: 1, label: '姓名', visible: true },
    { key: 2, label: '性别', visible: true },
    { key: 3, label: '入学年份', visible: true },
    { key: 4, label: '学段', visible: true },
    { key: 5, label: '年级', visible: true },
    { key: 6, label: '班级', visible: true },
    { key: 7, label: '学籍状态', visible: true },
    { key: 8, label: '联系电话', visible: true },
    { key: 9, label: '更新时间', visible: true }
  ]);

  const canSwitchSchool = computed(() => userStore.roles.some((role) => SWITCH_SCHOOL_ROLES.includes(role)));

  /** 组装查询参数；学籍状态多选转成逗号分隔字符串 */
  const buildQuery = (): StudentQuery => ({
    ...queryParams,
    enrollmentStatus: enrollmentStatusList.value.join(',')
  });

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listStudent(buildQuery());
      studentList.value = res.rows ?? [];
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
    queryFormRef.value?.resetFields();
    queryParams.pageNum = 1;
    queryParams.schoolId = schoolOptions.value.find((item) => item.current)?.schoolId ?? queryParams.schoolId;
    queryParams.termId = '';
    queryParams.gradeId = '';
    queryParams.classId = '';
    queryParams.gender = '';
    queryParams.enrollYear = '';
    queryParams.keyword = '';
    queryParams.idCardSuffix = '';
    enrollmentStatusList.value = [];
    classOptions.value = [];
    getList();
  };

  /** 导出按当前筛选与数据范围生成（operationId exportStudent） */
  const handleExport = () => {
    proxy?.download('edu/student/export', { ...buildQuery() }, `student_${new Date().getTime()}.xlsx`);
  };

  /** 学校变化：学年学期与年级按所选学校重取，班级与失效值清空（GAP-047 口径） */
  const handleSchoolChange = async () => {
    queryParams.termId = '';
    queryParams.gradeId = '';
    queryParams.classId = '';
    classOptions.value = [];
    await Promise.all([loadTermOptions(), loadGradeOptions()]);
  };

  /** 年级变化：班级只保留该年级的选项（学生列表按年级收窄班级，已确认规则） */
  const handleGradeChange = async () => {
    queryParams.classId = '';
    await loadClassOptions();
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

  const loadClassOptions = async () => {
    try {
      const res = await listClass({ schoolId: queryParams.schoolId, gradeId: queryParams.gradeId });
      classOptions.value = res.data ?? [];
    } catch {
      classOptions.value = [];
    }
  };

  const initOptions = async () => {
    await loadSchoolOptions();
    await Promise.all([loadTermOptions(), loadGradeOptions()]);
  };

  onMounted(async () => {
    await initOptions();
    await getList();
  });

  return {
    loading,
    showSearch,
    total,
    studentList,
    queryFormRef,
    queryParams,
    columns,
    canSwitchSchool,
    enrollmentStatusList,
    schoolOptions,
    termOptions,
    gradeOptions,
    classOptions,
    getList,
    handleQuery,
    resetQuery,
    handleExport,
    handleSchoolChange,
    handleGradeChange,
    initOptions
  };
}
