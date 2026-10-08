<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <el-button link type="primary" @click="goList">← 返回班级列表</el-button>
        <h2 class="text-base font-medium">班级合并</h2>
        <el-tag type="info" size="small">源班级并入目标班级；源班级置为已停用，不物理删除</el-tag>
      </div>
      <div class="text-xs mt-2">
        合并只影响行政班在班关系，按学年追加、不改写历史（<span class="mono">BR-PROMO-001</span>）； 教学班与选科关系不受影响。本页按
        <span class="mono">GAP-088</span> 的推荐方案实现（页面树未提供原型）。
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>合并范围</span>
          <el-tag type="warning" size="small">至少 1 个源班级</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <h3 class="form-section-title" data-layout-group="合并范围">合并范围</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学年学期">
              <el-input :model-value="termName || '当前学年学期'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合并生效日期" prop="effectiveDate">
              <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择生效日期" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源班级" prop="sourceClassIds">
              <el-select v-model="form.sourceClassIds" multiple placeholder="请选择要并入的源班级" class="w-full">
                <el-option
                  v-for="item in sourceOptions"
                  :key="item.classId"
                  :label="`${item.className}（在读 ${item.studentCount ?? item.enrolledCount ?? 0} 人）`"
                  :value="item.classId"
                  :disabled="!!item.studentCount && item.studentCount > 200"
                />
              </el-select>
              <div class="hint">停用班级不可作为源班级；在读人数超 200 的班级需要先拆分再合并。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标班级" prop="targetClassId">
              <el-select v-model="form.targetClassId" placeholder="请选择目标班级" class="w-full">
                <el-option v-for="item in targetOptions" :key="item.classId" :label="item.className" :value="item.classId" />
              </el-select>
              <div class="hint">目标班级不能出现在源班级列表里；目标班级容量只提示不阻塞。</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="合并说明" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="2"
                maxlength="200"
                show-word-limit
                placeholder="如：高一 (2)(3) 班因选科走班拆分，剩余学生并入高一 (1) 班"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>影响预览</span>
          <el-tag :type="ready ? 'success' : 'warning'" size="small">{{ ready ? '可以合并' : '请先补齐合并范围' }}</el-tag>
        </div>
      </template>

      <el-alert class="mb-3" type="info" :closable="false">
        <template #title>合并后会发生什么</template>
        <div>
          ① 源班级的全体在读学生在目标班级下新增在班关系；② 源班级状态置为「已停用」并保留历史花名册； ③ 已在源班级结束（休学 /
          转出等）的学生不参与合并。动作写审计且不可静默回滚。
        </div>
      </el-alert>

      <el-row :gutter="16">
        <el-col :span="8"><el-statistic title="源班级数" :value="form.sourceClassIds.length" /></el-col>
        <el-col :span="8"><el-statistic title="预计迁移学生" :value="estimatedStudents" /></el-col>
        <el-col :span="8">
          <div class="text-xs text-gray-500">目标班级</div>
          <div class="mt-1">{{ targetName || '—' }}</div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover">
      <div class="flex justify-between">
        <el-button @click="goList">取消</el-button>
        <el-button v-hasPermi="['org.class:update']" type="primary" :disabled="!ready" :loading="submitting" @click="handleSubmit">
          确认合并
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listClass, mergeClass } from '@/api/edu/class';
import type { ClassMergeForm, ClassVO } from '@/api/edu/class/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduClassMerge' });

const router = useRouter();

const submitting = ref(false);
const formRef = ref<FormInstance>();
const classOptions = ref<ClassVO[]>([]);
const termName = ref('');

const form = reactive<ClassMergeForm>({
  sourceClassIds: [],
  targetClassId: '',
  effectiveDate: '',
  remark: ''
});

const rules: FormRules = {
  sourceClassIds: [{ required: true, message: '请选择至少一个源班级', trigger: 'change' }],
  targetClassId: [{ required: true, message: '请选择目标班级', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择合并生效日期', trigger: 'change' }]
};

const activeClasses = computed(() => classOptions.value.filter((item) => item.status !== '已停用'));
const sourceOptions = computed(() => activeClasses.value);
const targetOptions = computed(() => activeClasses.value.filter((item) => !form.sourceClassIds.includes(item.classId)));
const targetName = computed(() => activeClasses.value.find((item) => item.classId === form.targetClassId)?.className ?? '');

const estimatedStudents = computed(() =>
  form.sourceClassIds.reduce((sum, classId) => {
    const item = activeClasses.value.find((row) => row.classId === classId);
    return sum + (item?.studentCount ?? item?.enrolledCount ?? 0);
  }, 0)
);

const ready = computed(
  () => form.sourceClassIds.length > 0 && !!form.targetClassId && !!form.effectiveDate && !form.sourceClassIds.includes(form.targetClassId)
);

const goList = () => router.push('/edu/class/list');

/** 确认合并：源班级学生并入目标班级，源班级置停用，写审计（mergeClass） */
const handleSubmit = async () => {
  await formRef.value?.validate();
  if (form.sourceClassIds.includes(form.targetClassId)) {
    ElMessage.error('目标班级不能出现在源班级列表中');
    return;
  }
  await ElMessageBox.confirm(
    `确认把 ${form.sourceClassIds.length} 个源班级（预计 ${estimatedStudents.value} 名学生）并入「${targetName.value}」？源班级将置为已停用并保留历史花名册。`,
    '班级合并',
    { confirmButtonText: '确认合并', cancelButtonText: '取消', type: 'warning' }
  );
  submitting.value = true;
  try {
    await mergeClass({ ...form });
    ElMessage.success('已合并：源班级学生已并入目标班级，源班级置为已停用并保留历史数据');
    goList();
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  const [classRes, termRes] = await Promise.all([listClass({ pageNum: 1, pageSize: 500 }), listTerm({})]);
  classOptions.value = classRes.rows ?? [];
  const current: TermVO | undefined = (termRes.data ?? []).find((item) => item.current);
  termName.value = current ? `${current.academicYearName ?? ''} ${current.termName}`.trim() : '';
  form.effectiveDate = new Date().toISOString().slice(0, 10);
});
</script>
