<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/class-move-students.html 的 .page-head -->
    <div class="page-head">
      <h1>批量迁学生</h1>
      <span class="scope-hint">数据范围：本校 · 教务主任与年级主任可迁移</span>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <el-button link type="primary" @click="goList">← 返回班级列表</el-button>
          <h2 class="text-base font-medium">批量迁学生</h2>
          <el-tag type="info" size="small">批量迁移 = 多条调班（D-067），写入入口唯一在班级管理</el-tag>
        </div>
        <span class="text-xs">已选 {{ selected.length }} 人</span>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-alert class="mb-3" type="info" :closable="false">
        <template #title>迁移口径</template>
        <div>
          迁移按学年追加、不改写历史；迁移后原班的在班关系在目标学年学期结束，新班从生效日期开始。
          每名学生逐条写入结果，失败项单独列出原因；迁移写审计且不可静默回滚。
        </div>
      </el-alert>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <div class="text-xs mb-1">源班级</div>
          <el-select v-model="sourceClassId" placeholder="请选择源班级" class="w-full" @change="getRoster">
            <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
          </el-select>
        </div>
        <div>
          <div class="text-xs mb-1">目标班级 <span class="text-red-500">*</span></div>
          <el-select v-model="targetClassId" placeholder="请选择目标班级" class="w-full">
            <el-option
              v-for="item in targetClassOptions"
              :key="item.classId"
              :label="item.className"
              :value="item.classId"
              :disabled="item.classId === sourceClassId"
            />
          </el-select>
          <div class="text-xs text-gray-500 mt-1">停用班级不可作为迁移目标；目标班级与源班级不能相同。</div>
        </div>
      </div>

      <div class="mt-4">
        <div class="text-xs mb-1">迁移说明</div>
        <el-input v-model="remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：高二 (1) 班拆分，学生并入高二 (2) 班" />
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>源班级花名册</span>
          <el-button :loading="loading" @click="getRoster">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="loading" border :data="roster" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" />
        <el-table-column label="学号" prop="studentNo" width="150" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="150" data-layout-group="学生信息" />
        <el-table-column label="学籍状态" prop="enrollmentStatus" width="130" align="center" data-layout-group="学生信息" />
        <el-table-column label="当前行政班" prop="currentClassName" min-width="180" data-layout-group="班级信息" />
        <template #empty>
          <el-empty description="请选择源班级后加载花名册" />
        </template>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <template #header>迁移确认</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="源班级">{{ sourceClassName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="目标班级">{{ targetClassName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="迁移人数">{{ selected.length }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ today }}</el-descriptions-item>
        <el-descriptions-item label="校验结果" :span="2">
          <el-tag v-if="!selected.length" type="info" size="small">请先勾选要迁移的学生</el-tag>
          <el-tag v-else-if="!targetClassId" type="warning" size="small">请先选择目标班级</el-tag>
          <el-tag v-else type="success" size="small">可以迁移</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="flex justify-end gap-2 mt-3">
        <el-button @click="goList">取消</el-button>
        <el-button
          v-hasPermi="['org.class:update']"
          type="primary"
          :loading="submitting"
          :disabled="!selected.length || !targetClassId"
          @click="handleSubmit"
        >
          确认迁移
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listClass, listClassRoster, transferClass } from '@/api/edu/class';
import type { ClassRosterVO, ClassVO } from '@/api/edu/class/types';

defineOptions({ name: 'EduClassMove' });

const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const classOptions = ref<ClassVO[]>([]);
const roster = ref<ClassRosterVO[]>([]);
const selected = ref<ClassRosterVO[]>([]);
const sourceClassId = ref('');
const targetClassId = ref('');
const remark = ref('');

const today = new Date().toISOString().slice(0, 10);

/** 目标班级候选：正常状态的班级（停用班级不可作为迁移目标） */
const targetClassOptions = computed(() => classOptions.value.filter((item) => item.status !== '已停用'));
const sourceClassName = computed(() => classOptions.value.find((item) => item.classId === sourceClassId.value)?.className ?? '');
const targetClassName = computed(() => classOptions.value.find((item) => item.classId === targetClassId.value)?.className ?? '');

const handleSelectionChange = (rows: ClassRosterVO[]) => {
  selected.value = rows;
};

const loadClasses = async () => {
  const res = await listClass({ pageNum: 1, pageSize: 200 });
  classOptions.value = res.rows ?? [];
};

const getRoster = async () => {
  if (!sourceClassId.value) {
    roster.value = [];
    return;
  }
  loading.value = true;
  try {
    const res = await listClassRoster(sourceClassId.value);
    roster.value = res.rows ?? [];
  } finally {
    loading.value = false;
  }
};

const goList = () => router.push('/edu/class/list');

/** 批量迁移：单次提交多学生，接口与调班一致（D-067 / REQ-CLS-036） */
const handleSubmit = async () => {
  await ElMessageBox.confirm(
    `确认将 ${selected.value.length} 名学生从「${sourceClassName.value}」迁移到「${targetClassName.value}」？迁移按学年追加，不改写历史。`,
    '批量迁学生',
    { confirmButtonText: '确认迁移', cancelButtonText: '取消', type: 'warning' }
  );
  submitting.value = true;
  try {
    await transferClass({
      studentIds: selected.value.map((item) => item.studentId),
      targetClassId: targetClassId.value,
      effectiveDate: today,
      remark: remark.value
    });
    ElMessage.success('已提交批量迁移：逐条写入结果，可在班级详情查看新的在班关系');
    goList();
  } finally {
    submitting.value = false;
  }
};

onMounted(loadClasses);
</script>
