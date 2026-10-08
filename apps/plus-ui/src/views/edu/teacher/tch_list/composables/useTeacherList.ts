import { getCurrentInstance, onMounted, reactive, ref } from 'vue';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import { listSubjectOption } from '@/api/edu/subject';
import type { SubjectOptionVO } from '@/api/edu/subject/types';
import { listTeacher } from '@/api/edu/teacher';
import type { TeacherQuery, TeacherVO } from '@/api/edu/teacher/types';

/** 教育角色（05-permission-matrix.yaml 的学校级角色） */
export const EDU_ROLE_OPTIONS = [
  { value: 'school_leader', label: '校领导' },
  { value: 'academic_director', label: '教务主任' },
  { value: 'grade_leader', label: '年级主任' },
  { value: 'homeroom', label: '班主任' },
  { value: 'subject_teacher', label: '任课教师' }
];

/** 在职状态（与原型 teacher-list.html 的选项一致） */
export const EMPLOYMENT_STATUS_OPTIONS = [
  { value: '在职', label: '在职' },
  { value: '离职', label: '离职' },
  { value: '调离', label: '调离' }
];

/** 教师列表页的查询、分页与下拉取数 */
export function useTeacherList() {
  const { proxy } = getCurrentInstance() as ComponentInternalInstance;

  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const teacherList = ref<TeacherVO[]>([]);
  const schoolOptions = ref<SchoolVO[]>([]);
  const gradeOptions = ref<GradeVO[]>([]);
  const classOptions = ref<ClassVO[]>([]);
  const subjectOptions = ref<SubjectOptionVO[]>([]);

  const queryParams = reactive<TeacherQuery>({
    pageNum: 1,
    pageSize: 20,
    schoolId: '',
    gradeId: '',
    classId: '',
    subjectCode: '',
    eduRole: '',
    employmentStatus: '',
    keyword: ''
  });

  const columns = ref([
    { key: 0, label: '工号', visible: true },
    { key: 1, label: '姓名', visible: true },
    { key: 2, label: '性别', visible: true },
    { key: 3, label: '所属学校', visible: true },
    { key: 4, label: '教育角色', visible: true },
    { key: 5, label: '任教学科', visible: true },
    { key: 6, label: '任课班级数', visible: true },
    { key: 7, label: '在职状态', visible: true },
    { key: 8, label: '联系电话', visible: true }
  ]);

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listTeacher({ ...queryParams });
      teacherList.value = res.rows ?? [];
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
    queryParams.gradeId = '';
    queryParams.classId = '';
    queryParams.subjectCode = '';
    queryParams.eduRole = '';
    queryParams.employmentStatus = '';
    queryParams.keyword = '';
    queryParams.pageNum = 1;
    getList();
  };

  /** 导出按当前筛选与数据范围生成（operationId exportTeacher） */
  const handleExport = () => {
    proxy?.download('edu/teacher/export', { ...queryParams }, `teacher_${new Date().getTime()}.xlsx`);
  };

  /** 学校变化：任教年级 / 任教班级按所选学校重取；任教学科是平台级配置不收窄（GAP-047） */
  const handleSchoolChange = async () => {
    queryParams.gradeId = '';
    queryParams.classId = '';
    await Promise.all([loadGradeOptions(), loadClassOptions()]);
    getList();
  };

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

  const loadSubjectOptions = async () => {
    try {
      const res = await listSubjectOption();
      subjectOptions.value = res.data ?? [];
    } catch {
      subjectOptions.value = [];
    }
  };

  onMounted(async () => {
    await loadSchoolOptions();
    await Promise.all([loadGradeOptions(), loadClassOptions(), loadSubjectOptions()]);
    await getList();
  });

  return {
    loading,
    showSearch,
    total,
    teacherList,
    queryParams,
    columns,
    schoolOptions,
    gradeOptions,
    classOptions,
    subjectOptions,
    getList,
    handleQuery,
    resetQuery,
    handleExport,
    handleSchoolChange,
    handleGradeChange
  };
}
