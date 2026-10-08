import { computed, ref } from 'vue';
import { batchUpdatePromotionItem, listPromotionItem, previewPromotionTask } from '@/api/edu/promotion';
import type { PromotionItemVO, PromotionTaskVO } from '@/api/edu/promotion/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { checkPermi } from '@/utils/permission';

/** 结果类型字典：升班 / 留级 / 毕业 / 结业（FD-promotion_result_type） */
export const RESULT_TYPE_LABELS: Record<string, string> = {
  promote: '升班',
  repeat: '留级',
  graduate: '毕业',
  complete: '结业'
};

/** 明细状态字典 */
export const ITEM_STATUS_LABELS: Record<string, string> = {
  pending: '待处理',
  adjusted: '已调整',
  valid: '校验通过',
  error: '校验不通过',
  success: '已升班',
  failed: '失败'
};

/** 源班级分组（预览页左栏，REQ-PRM-018 的批量指定对象） */
export interface SourceClassGroup {
  sourceClassId: string;
  sourceClassName: string;
  count: number;
  adjusted: number;
  targetClassId: string;
}

/**
 * 升班预览：加载任务上下文与明细、按源班级分组、批量指定目标班级。
 *
 * 预览不写入任何学生数据（REQ-PRM-020）；重新预览会覆盖旧明细（REQ-PRM-021）。
 */
export function usePromotionPreview(taskId: string) {
  const loading = ref(false);
  const itemLoading = ref(false);
  const items = ref<PromotionItemVO[]>([]);
  const task = ref<PromotionTaskVO>();
  const sourceFilter = ref('');

  const canWrite = computed(() => checkPermi(['promotion.batch:update']));

  const filteredItems = computed(() => (sourceFilter.value ? items.value.filter((item) => item.sourceClassId === sourceFilter.value) : items.value));

  /** 源班级分组：按源班级聚合在读人数与已调整人数 */
  const sourceClasses = computed<SourceClassGroup[]>(() => {
    const map = new Map<string, SourceClassGroup>();
    items.value.forEach((item) => {
      const key = item.sourceClassId ?? '';
      if (!key) return;
      const group = map.get(key) ?? {
        sourceClassId: key,
        sourceClassName: item.sourceClassName ?? key,
        count: 0,
        adjusted: 0,
        targetClassId: ''
      };
      group.count += 1;
      if (item.status === 'adjusted') group.adjusted += 1;
      if (!group.targetClassId && item.targetClassId) group.targetClassId = item.targetClassId;
      map.set(key, group);
    });
    return Array.from(map.values());
  });

  const adjustedCount = computed(() => items.value.filter((item) => item.status === 'adjusted').length);

  const loadItems = async () => {
    itemLoading.value = true;
    try {
      const res = await listPromotionItem(taskId, { sourceClassId: sourceFilter.value || undefined });
      items.value = res.data ?? res.rows ?? [];
    } finally {
      itemLoading.value = false;
    }
  };

  const loadTask = async (source: () => Promise<PromotionTaskVO | undefined>) => {
    loading.value = true;
    try {
      task.value = await source();
      await loadItems();
    } finally {
      loading.value = false;
    }
  };

  /** 重新生成预览：覆盖旧明细，需二次确认（REQ-PRM-021） */
  const handlePreview = async () => {
    await ElMessageBox.confirm('重新生成预览会覆盖旧明细，确认继续？', '重新预览', {
      confirmButtonText: '确认重新预览',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await previewPromotionTask(taskId);
    ElMessage.success('已重新生成升班预览（未写入任何学生数据）');
    await loadItems();
  };

  /** 按源班级整体指定目标班级（REQ-PRM-018） */
  const applyToClass = async (group: SourceClassGroup) => {
    if (!group.targetClassId) {
      ElMessage.warning('请先选择目标班级');
      return;
    }
    await batchUpdatePromotionItem(taskId, {
      sourceClassId: group.sourceClassId,
      targetClassId: group.targetClassId,
      resultType: 'promote'
    });
    ElMessage.success(`已按源班级整体指定目标班级：本班 ${group.count} 名学生的去向已更新；仍可逐条调整`);
    await loadItems();
  };

  const resultTypeLabel = (value?: string) => (value ? (RESULT_TYPE_LABELS[value] ?? value) : '—');
  const itemStatusLabel = (value?: string) => (value ? (ITEM_STATUS_LABELS[value] ?? value) : '—');

  return {
    loading,
    itemLoading,
    items,
    task,
    sourceFilter,
    filteredItems,
    sourceClasses,
    adjustedCount,
    canWrite,
    loadItems,
    loadTask,
    handlePreview,
    applyToClass,
    resultTypeLabel,
    itemStatusLabel
  };
}
