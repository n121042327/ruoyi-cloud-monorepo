import { onMounted, reactive, ref } from 'vue';
import { listSubject } from '@/api/edu/subject';
import type { SubjectQuery, SubjectVO } from '@/api/edu/subject/types';
import { STAGE_CODE_LABEL, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

/** 选科角色（06-field-dictionary.yaml：none / primary / secondary） */
export const STREAM_ROLE_OPTIONS = [
  { value: 'none', label: '不参与' },
  { value: 'primary', label: '首选' },
  { value: 'secondary', label: '再选' }
];

/** 学段编码数组 → 中文（列表展示「启用学段」用；字段来自 edu_subject_stage） */
export const stageLabel = (codes?: string[]) =>
  (codes ?? [])
    .map((code) => STAGE_CODE_LABEL[code] ?? code)
    .filter(Boolean)
    .join(' / ');

/** 学科列表页的查询与分页 */
export function useSubjectList() {
  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const subjectList = ref<SubjectVO[]>([]);

  const queryParams = reactive<SubjectQuery>({
    pageNum: 1,
    pageSize: 20,
    keyword: '',
    stageCode: '',
    filterStreamRole: '',
    filterStatus: ''
  });

  const columns = ref([
    { key: 0, label: '学科编码', visible: true },
    { key: 1, label: '学科名称', visible: true },
    { key: 2, label: '启用学段', visible: true },
    { key: 3, label: '参与 3+1+2', visible: true },
    { key: 4, label: '选科角色', visible: true },
    { key: 5, label: '排序号', visible: true },
    { key: 6, label: '状态', visible: true }
  ]);

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listSubject({ ...queryParams });
      subjectList.value = res.rows ?? [];
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
    queryParams.keyword = '';
    queryParams.stageCode = '';
    queryParams.filterStreamRole = '';
    queryParams.filterStatus = '';
    queryParams.pageNum = 1;
    getList();
  };

  onMounted(getList);

  return {
    loading,
    showSearch,
    total,
    subjectList,
    queryParams,
    columns,
    stageOptions: STAGE_CODE_OPTIONS,
    getList,
    handleQuery,
    resetQuery
  };
}
