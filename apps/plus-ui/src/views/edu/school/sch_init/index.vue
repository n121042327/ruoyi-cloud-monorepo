<template>
  <div class="p-2">
    <el-card shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <span>开通初始化{{ schoolName ? ' · ' + schoolName : '' }}</span>
          <el-tag>步骤 {{ activeStep }} / 4</el-tag>
        </div>
      </template>
      <el-steps :active="activeStep - 1" align-center finish-status="success">
        <el-step title="学校基本信息" />
        <el-step title="学段与年级" />
        <el-step title="学年学期" />
        <el-step title="执行与结果" />
      </el-steps>
      <el-alert
        class="mt-3"
        type="info"
        :closable="false"
        title="初始化会一次性完成学年学期、学科模板与基础角色；动作幂等，重复执行不会产生重复数据（REQ-SCH-019 / 045）。"
      />
    </el-card>

    <!-- 步骤 1：学校基本信息 -->
    <el-card v-if="activeStep === 1" shadow="never" class="mt-3" data-layout-group="学校基本信息">
      <template #header><span>步骤 1 · 学校基本信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学校名称">{{ schoolName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学校编码">{{ schoolCode || '—' }}</el-descriptions-item>
      </el-descriptions>
      <div class="hint mt-2">学校与租户的绑定关系一经创建不可修改（REQ-SCH-022）。</div>
    </el-card>

    <!-- 步骤 2：学段与年级 -->
    <el-card v-else-if="activeStep === 2" shadow="never" class="mt-3" data-layout-group="学段与年级">
      <template #header><span>步骤 2 · 学段与年级</span></template>
      <el-form label-width="120px">
        <el-form-item label="开设学段">
          <el-checkbox-group v-model="form.stageCodes">
            <el-checkbox v-for="item in STAGE_CODE_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-checkbox>
          </el-checkbox-group>
          <div class="hint">至少选择一个学段；学段是年级可选范围的前置条件（REQ-SCH-032 / 033）。</div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 步骤 3：学年学期 -->
    <el-card v-else-if="activeStep === 3" shadow="never" class="mt-3" data-layout-group="学年学期">
      <template #header><span>步骤 3 · 学年学期</span></template>
      <el-form label-width="120px">
        <el-form-item label="学年编码">
          <el-input v-model="form.academicYearCode" placeholder="如 2026-2027" style="width: 220px" />
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 300px"
          />
        </el-form-item>
      </el-form>
      <div class="hint">学年编码必须是连续的两个自然年（结束年 = 起始年 + 1），且校内唯一（REQ-TERM-008 / 009）。</div>
    </el-card>

    <!-- 步骤 4：执行与结果 -->
    <el-card v-else shadow="never" class="mt-3" data-layout-group="执行与结果">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 4 · 执行与结果</span>
          <el-tag :type="result ? 'success' : 'info'">{{ result ? '初始化完成' : '待执行' }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="开设学段">{{ form.stageCodes.map((c) => STAGE_CODE_LABEL[c] ?? c).join(' / ') || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学年学期"
          >{{ form.academicYearCode || '—' }}（{{ dateRange[0] || '—' }} ~ {{ dateRange[1] || '—' }}）</el-descriptions-item
        >
        <el-descriptions-item label="学科模板">按学段预置标准学科清单</el-descriptions-item>
        <el-descriptions-item label="基础角色">校领导 / 教务主任 / 年级主任 / 班主任 / 任课教师</el-descriptions-item>
      </el-descriptions>
      <el-alert
        v-if="result"
        class="mt-3"
        type="success"
        :closable="false"
        title="初始化已完成：学年学期、学科模板与基础角色均已就位，可继续配置年级与班级。"
      />
      <div class="hint mt-2">初始化在 30 秒内完成，动作写审计（REQ-SCH-045 / REQ-SCH-020）。</div>
    </el-card>

    <el-card shadow="never" class="mt-3">
      <div class="flex justify-between items-center">
        <el-button :disabled="activeStep === 1" @click="activeStep -= 1">上一步</el-button>
        <div class="flex items-center gap-2">
          <el-button v-if="activeStep < 4" type="primary" @click="handleNext">下一步</el-button>
          <el-button v-else type="primary" :loading="submitting" :disabled="!!result" @click="handleInit">执行初始化</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getCurrentSchool, initSchoolBaseline } from '@/api/edu/school';
import { STAGE_CODE_LABEL, STAGE_CODE_OPTIONS } from '@/enums/edu/StudentEnum';

defineOptions({ name: 'EduSchoolInit' });

const activeStep = ref(1);
const submitting = ref(false);
const schoolId = ref('');
const schoolName = ref('');
const schoolCode = ref('');
const dateRange = ref<string[]>([]);
const result = ref(false);

const form = reactive({ stageCodes: [] as string[], academicYearCode: '' });

const handleNext = () => {
  if (activeStep.value === 2 && !form.stageCodes.length) {
    ElMessage.warning('请至少选择一个开设学段');
    return;
  }
  if (activeStep.value === 3 && (!form.academicYearCode || dateRange.value.length < 2)) {
    ElMessage.warning('请填写学年编码与起止日期');
    return;
  }
  activeStep.value += 1;
};

/** 执行初始化：幂等，重复点击不会产生重复数据 */
const handleInit = async () => {
  if (!schoolId.value) {
    ElMessage.warning('未取到当前学校，无法初始化');
    return;
  }
  submitting.value = true;
  try {
    await initSchoolBaseline(schoolId.value, {
      stageCodes: form.stageCodes,
      academicYearCode: form.academicYearCode,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    });
    result.value = true;
    ElMessage.success('初始化完成');
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  try {
    const res = await getCurrentSchool();
    schoolId.value = res.data?.schoolId ?? '';
    schoolName.value = res.data?.schoolName ?? '';
    schoolCode.value = res.data?.schoolCode ?? '';
  } catch {
    schoolId.value = '';
  }
});
</script>
