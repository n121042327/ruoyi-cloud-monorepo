<template>
  <el-dialog v-model="visible" title="升班只读视图" width="720px" append-to-body>
    <el-alert class="mb-3" type="info" :closable="false" title="这里只做「学段内序号 +1」的只读参考；升班的执行入口在升班模块，年级管理不写数据。" />
    <el-table v-loading="loading" :data="rows" border size="small">
      <el-table-column label="学段" width="100" align="center">
        <template #default="scope">{{ STAGE_LABEL[scope.row.stageCode] ?? scope.row.stageCode }}</template>
      </el-table-column>
      <el-table-column label="年级" prop="gradeName" width="140" />
      <el-table-column label="学段内序号" prop="gradeLevel" width="110" align="center" />
      <el-table-column label="升班去向" width="140">
        <template #default="scope">{{ nextLabel(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="班级数" prop="classCount" width="90" align="center" />
      <el-table-column label="在读学生数" prop="studentCount" width="110" align="center" />
      <template #empty>
        <el-empty description="暂无可展示的年级" />
      </template>
    </el-table>

    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { getGradePromotionView } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduGradePromotionDialog' });

const STAGE_LABEL = STAGE_CODE_LABEL;

const visible = ref(false);
const loading = ref(false);
const rows = ref<GradeVO[]>([]);

/** 学段上限：小学 6 / 初中 3 / 高中 3（REQ-GRD-012） */
const maxLevel: Record<string, number> = { primary: 6, junior: 3, senior: 3 };

const nextLabel = (row: GradeVO) => {
  const level = row.gradeLevel ?? 0;
  const max = maxLevel[String(row.stageCode)] ?? 3;
  if (level >= max) {
    return '毕业（学段结束）';
  }
  // 同学段内的下一级：年级名通常形如「高一」，按序号 +1 展示为「序号 +1」
  return `学段内序号 ${level + 1}`;
};

const open = async () => {
  visible.value = true;
  loading.value = true;
  try {
    const res = await getGradePromotionView();
    rows.value = (res.data ?? []) as GradeVO[];
  } finally {
    loading.value = false;
  }
};

defineExpose({ open });
</script>
