<template>
  <el-drawer v-model="visible" size="680px" :title="title" append-to-body>
    <template #header>
      <div class="flex items-center gap-2">
        <h2 class="text-base font-medium">年级详情 · {{ detail.gradeName || '—' }}</h2>
        <el-tag :type="detail.gradeStatus === 'archived' ? 'info' : 'success'" size="small">
          {{ statusText }}
        </el-tag>
        <el-tag type="info" size="small">学段内序号 {{ detail.gradeLevel ?? '—' }}</el-tag>
      </div>
    </template>

    <el-alert
      class="mb-3"
      type="info"
      :closable="false"
      title="年级详情是只读视图：升班的唯一执行入口在升班模块，这里只做「学段内序号 +1」的参考。"
    />

    <h4 class="form-section-title" data-layout-group="基础信息">基础信息</h4>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="年级名称">{{ detail.gradeName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="学段">{{ STAGE_LABEL[detail.stageCode ?? ''] ?? detail.stageCode ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="入学年份">{{ detail.enrollYear || '—' }}</el-descriptions-item>
      <el-descriptions-item label="学段内序号">{{ detail.gradeLevel ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="所属学校">{{ detail.schoolName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusText }}</el-descriptions-item>
      <el-descriptions-item label="年级主任">{{ detail.leaderNames || '未指定' }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <el-alert
      class="mt-3"
      type="info"
      :closable="false"
      title="年级名称由「入学年份 + 学段 + 学段内序号」生成，允许人工微调；学段与学段内序号创建后不可修改。"
    />

    <h4 class="form-section-title mt-4" data-layout-group="下辖班级">
      下辖班级
      <el-tag class="ml-2" type="primary" size="small">共 {{ classes.length }} 个行政班</el-tag>
    </h4>
    <el-table v-loading="classLoading" :data="classes" border size="small">
      <el-table-column label="班级名称" prop="className" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" prop="classType" width="100" align="center" />
      <el-table-column label="班主任" prop="headTeacherName" width="120" />
      <el-table-column label="在读 / 容量" width="120" align="center">
        <template #default="scope">{{ scope.row.studentCount ?? 0 }} / {{ scope.row.classCapacity || '不限' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">{{ scope.row.status === 'disabled' ? '已停用' : '正常' }}</template>
      </el-table-column>
      <template #empty>
        <el-empty description="该年级暂无班级" :image-size="60" />
      </template>
    </el-table>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduGradeDetailDrawer' });

const STAGE_LABEL = STAGE_CODE_LABEL;

const visible = ref(false);
const classLoading = ref(false);
const detail = ref<GradeVO>({} as GradeVO);
const classes = ref<ClassVO[]>([]);

const title = computed(() => `年级详情 · ${detail.value.gradeName ?? '—'}`);

const statusText = computed(() => {
  const map: Record<string, string> = { normal: '在读', archived: '已归档' };
  return map[String(detail.value.gradeStatus ?? '')] ?? detail.value.gradeStatus ?? '—';
});

const open = async (row: GradeVO) => {
  visible.value = true;
  detail.value = row;
  classes.value = [];
  classLoading.value = true;
  try {
    const [detailRes, classRes] = await Promise.all([
      getGrade(row.gradeId).catch(() => null),
      listClass({ gradeId: row.gradeId, pageNum: 1, pageSize: 50 }).catch(() => null)
    ]);
    if (detailRes?.data) {
      detail.value = detailRes.data;
    }
    classes.value = classRes?.rows ?? [];
  } finally {
    classLoading.value = false;
  }
};

defineExpose({ open });
</script>
