import { computed, reactive, ref } from 'vue';
import { listPromotionTask, retryPromotionTask } from '@/api/edu/promotion';
import type { PromotionTaskQuery, PromotionTaskVO } from '@/api/edu/promotion/types';
import { checkPermi } from '@/utils/permission';
import { ElMessage, ElMessageBox } from 'element-plus';

/** 状态码 → 中文标签（字典 FD-promotion_task_status，REQ-PRM-003 / 011） */
export const PROMOTION_STATUS_LABELS: Record<string, string> = {
  draft: '草稿',
  previewed: '已预览待确认',
  validating: '校验中',
  running: '执行中',
  succeeded: '已完成',
  partial_failed: '部分失败',
  failed: '失败',
  cancelled: '已取消'
};

/** 状态 → 标签颜色 */
export const PROMOTION_STATUS_TYPES: Record<string, 'warning' | 'primary' | 'success' | 'info' | 'danger'> = {
  draft: 'info',
  previewed: 'warning',
  validating: 'warning',
  running: 'primary',
  succeeded: 'success',
  partial_failed: 'danger',
  failed: 'danger',
  cancelled: 'info'
};

/**
 * 升班任务列表：查询、分页、按状态派生的行内动作。
 *
 * 数据范围：本校（`DS-04`）；升班任务不跨校共享（`BR-DATA-018`）。
 */
export function usePromotionTaskList() {
  const loading = ref(false);
  const showSearch = ref(true);
  const total = ref(0);
  const taskList = ref<PromotionTaskVO[]>([]);

  const queryParams = reactive<PromotionTaskQuery>({
    pageNum: 1,
    pageSize: 20,
    schoolId: '',
    sourceTermId: '',
    targetTermId: '',
    status: '',
    createBy: '',
    taskNo: ''
  });

  const columns = ref([
    { key: 0, label: '任务编号', visible: true },
    { key: 1, label: '源学年学期', visible: true },
    { key: 2, label: '目标学年学期', visible: true },
    { key: 3, label: '状态', visible: true },
    { key: 4, label: '学生总数', visible: true },
    { key: 5, label: '成功', visible: true },
    { key: 6, label: '失败', visible: true },
    { key: 7, label: '创建人', visible: true },
    { key: 8, label: '创建时间', visible: true }
  ]);

  const canRead = computed(() => checkPermi(['promotion.batch:read']));
  const canWrite = computed(() => checkPermi(['promotion.batch:update']));
  const canCreate = computed(() => checkPermi(['promotion.batch:create']));

  const getList = async () => {
    loading.value = true;
    try {
      const res = await listPromotionTask({ ...queryParams });
      taskList.value = res.rows ?? [];
      total.value = res.total ?? 0;
    } finally {
      loading.value = false;
    }
  };

  const resetQuery = () => {
    queryParams.sourceTermId = '';
    queryParams.targetTermId = '';
    queryParams.status = '';
    queryParams.createBy = '';
    queryParams.taskNo = '';
    queryParams.pageNum = 1;
    getList();
  };

  const statusLabel = (status?: string) => (status ? (PROMOTION_STATUS_LABELS[status] ?? status) : '—');
  const statusType = (status?: string): 'warning' | 'primary' | 'success' | 'info' | 'danger' =>
    status ? (PROMOTION_STATUS_TYPES[status] ?? 'info') : 'info';

  /** 草稿没有升班明细，三个计数显示「—」（REQ-PRM-003 / 011） */
  const countText = (row: PromotionTaskVO, value?: number) => {
    if (row.status === 'draft') return '—';
    return value === undefined || value === null ? '—' : String(value);
  };

  /** 重试失败项 / 继续执行剩余项：只处理失败项，已成功记录不重复执行（REQ-PRM-032 / BR-PROMO-003） */
  const handleRetry = async (row: PromotionTaskVO) => {
    const tip =
      row.status === 'cancelled'
        ? '继续执行剩余项：已完成的学生保留，只处理剩余未升班的学生，确认继续？'
        : '重试只重新处理失败项，已成功的记录不重复执行，确认继续？';
    await ElMessageBox.confirm(tip, '重试升班任务', {
      confirmButtonText: '确认重试',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await retryPromotionTask(row.taskId, '列表页发起重试');
    ElMessage.success('已提交重试，请到执行结果页查看进度');
  };

  return {
    loading,
    showSearch,
    total,
    taskList,
    queryParams,
    columns,
    canRead,
    canWrite,
    canCreate,
    getList,
    resetQuery,
    statusLabel,
    statusType,
    countText,
    handleRetry
  };
}
