<template>
  <div class="p-2" v-loading="loading">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">升班校验</h2>
        <el-tag type="primary" size="small">向导 3 / 4</el-tag>
        <el-tag type="info" size="small">校验通过才允许执行（REQ-PRM-027）</el-tag>
      </div>
      <div class="mt-2 text-xs">任务：{{ task?.taskNo || '—' }} · 目标学年学期：{{ task?.targetTermName || '—' }}</div>
      <el-steps class="mt-4" :active="2" align-center finish-status="success">
        <el-step title="选择学年学期" />
        <el-step title="预览与调整" />
        <el-step title="校验" />
        <el-step title="执行与结果" />
      </el-steps>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-alert class="mb-2" :type="blocked ? 'error' : 'success'" :closable="false" :title="blocked ? '存在阻塞项，不能执行' : '校验通过，可以执行'">
        {{
          blocked
            ? `共 ${blockedCount} 条明细未通过校验，请回到预览与调整修正后重新校验。`
            : '全部明细均已通过校验，执行将按学年追加、不改写历史（BR-PROMO-001）。'
        }}
      </el-alert>

      <el-table v-loading="itemLoading" border :data="items">
        <el-table-column label="学号" prop="studentNo" width="150" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="110" data-layout-group="学生信息" />
        <el-table-column label="源班级" prop="sourceClassName" min-width="180" data-layout-group="学生信息" />
        <el-table-column label="目标班级 / 去向" prop="targetClassName" min-width="190" data-layout-group="去向信息" />
        <el-table-column label="校验级别" prop="status" width="120" align="center" data-layout-group="校验信息">
          <template #default="scope">{{ itemStatusLabel(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column label="校验说明" prop="errorMsg" min-width="230" data-layout-group="校验信息" />
        <el-table-column fixed="right" label="操作" width="110" data-layout-group="操作">
          <template #default>
            <el-button v-hasPermi="['promotion.batch:update']" link type="primary" @click="goPreview">去修正</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有校验明细" />
        </template>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <div class="flex justify-between">
        <el-button v-hasPermi="['promotion.batch:update']" @click="goPreview">上一步：预览与调整</el-button>
        <div class="flex gap-2">
          <el-button v-hasPermi="['promotion.batch:update']" :loading="loading" @click="handleRevalidate">重新校验</el-button>
          <el-button @click="goList">返回任务列表（只读）</el-button>
          <el-button v-hasPermi="['promotion.batch:update']" type="primary" :disabled="blocked" :loading="executing" @click="handleExecute">
            执行升班
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { executePromotionTask, getPromotionTask, listPromotionItem, validatePromotionTask } from '@/api/edu/promotion';
import type { PromotionItemVO, PromotionTaskVO } from '@/api/edu/promotion/types';
import { ITEM_STATUS_LABELS } from '@/views/edu/promotion/prm_preview/composables/usePromotionPreview';

defineOptions({ name: 'EduPromotionValidate' });

const route = useRoute();
const router = useRouter();
const taskId = String(route.query.taskId ?? '');

const loading = ref(false);
const itemLoading = ref(false);
const executing = ref(false);
const items = ref<PromotionItemVO[]>([]);
const task = ref<PromotionTaskVO>();

const blockedCount = computed(() => items.value.filter((item) => item.status === 'error').length);
const blocked = computed(() => blockedCount.value > 0);
const itemStatusLabel = (value?: string) => (value ? (ITEM_STATUS_LABELS[value] ?? value) : '—');

const loadAll = async () => {
  loading.value = true;
  itemLoading.value = true;
  try {
    const res = await getPromotionTask(taskId);
    task.value = res.data;
    const itemRes = await listPromotionItem(taskId);
    items.value = itemRes.data ?? [];
  } finally {
    loading.value = false;
    itemLoading.value = false;
  }
};

const goPreview = () => router.push({ path: '/edu/promotion/preview', query: { taskId } });
const goList = () => router.push('/edu/promotion/list');

const handleRevalidate = async () => {
  await validatePromotionTask(taskId);
  ElMessage.success('已重新提交校验');
  await loadAll();
};

/** 执行升班：异步进行，确认后跳转执行与结果页（REQ-PRM-027 / 028） */
const handleExecute = async () => {
  await ElMessageBox.confirm('执行将按学年追加写入学籍与班级关系，不改写历史数据，确认执行？', '执行升班', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    type: 'warning'
  });
  executing.value = true;
  try {
    await executePromotionTask(taskId);
    ElMessage.success('已提交执行，进入执行与结果页');
    router.push({ path: '/edu/promotion/execute', query: { taskId } });
  } finally {
    executing.value = false;
  }
};

onMounted(() => {
  if (!taskId) {
    ElMessage.warning('缺少任务上下文，请从升班任务列表进入');
    return;
  }
  loadAll();
});
</script>
