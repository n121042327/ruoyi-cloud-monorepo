<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/promotion-transfer.html 的 .page-head -->
    <div class="page-head">
      <h1>跨校转学（转入校接收）</h1>
      <span class="scope-hint">数据范围：本校（转入校） · 只处理发往本校的转学单</span>
    </div>
    <el-card shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <span>跨校转学（转入校）</span>
          <div class="flex items-center gap-2">
            <el-tag type="info">只暴露必要字段</el-tag>
            <el-tag>向导 {{ activeStep }} / 4</el-tag>
          </div>
        </div>
      </template>
      <el-steps :active="activeStep - 1" align-center finish-status="success">
        <el-step title="待接收转学单" />
        <el-step title="核对信息与接收" />
        <el-step title="接收确认" />
        <el-step title="报到" />
      </el-steps>
      <el-alert class="mt-3" type="info" :closable="false" title="接收前，学生不会被计入转入校的任何在读数与花名册；接收动作本身即审批。" />
    </el-card>

    <!-- 步骤 1：待接收转学单 -->
    <el-card v-if="activeStep === 1" shadow="never" class="mt-3" data-layout-group="待接收转学单">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 1 · 待接收转学单</span>
          <el-tag type="info">只列发往本校且未完成的转学单</el-tag>
        </div>
      </template>
      <el-table v-loading="loading" border :data="transferList" highlight-current-row @current-change="handleSelectTransfer">
        <el-table-column width="50" align="center">
          <template #default="scope">
            <el-radio v-model="selectedTransferId" :value="scope.row.transferId">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="学号" prop="studentNo" width="130" />
        <el-table-column label="姓名" prop="studentName" width="110" />
        <el-table-column label="年级" prop="fromGradeName" width="130" />
        <el-table-column label="班级" width="130">
          <template #default>—</template>
        </el-table-column>
        <el-table-column label="学籍状态" width="120" align="center">
          <template #default>
            <el-tag type="warning" size="small">转入未报到</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可执行状态" min-width="220">
          <template #default="scope">
            转出校：{{ scope.row.fromSchoolName || '—' }} · 申请日期 {{ scope.row.effectiveDate || '—' }} · 待接收
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有待接收的转学单" />
        </template>
      </el-table>
    </el-card>

    <!-- 步骤 2：核对信息与接收 -->
    <el-card v-else-if="activeStep === 2" shadow="never" class="mt-3" data-layout-group="核对信息与接收">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 2 · 核对信息与接收</span>
          <el-tag type="info">只暴露必要字段</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border class="mb-3">
        <el-descriptions-item label="学生">{{ selectedTransfer?.studentName }} · {{ selectedTransfer?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ selectedTransfer?.gender || '—' }}</el-descriptions-item>
        <el-descriptions-item label="原学校">{{ selectedTransfer?.fromSchoolName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="原年级">{{ selectedTransfer?.fromGradeName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="目标年级" prop="toGradeId">
          <el-select v-model="form.toGradeId" placeholder="请选择" class="w-full" @change="handleGradeChange">
            <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入班级（接收时指定）" prop="toClassId">
          <el-select v-model="form.toClassId" placeholder="暂不指定（报到时候再分班）" clearable class="w-full">
            <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
          </el-select>
          <div class="hint">接收前，学生不会被计入转入校的任何在读数与花名册。</div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 步骤 3：接收确认 -->
    <el-card v-else-if="activeStep === 3" shadow="never" class="mt-3" data-layout-group="接收确认">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 3 · 接收确认</span>
          <el-tag type="info">转学单只读字段预览</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生">
          {{ selectedTransfer?.studentName }} · {{ selectedTransfer?.studentNo }}（原 {{ selectedTransfer?.fromSchoolName }}）
        </el-descriptions-item>
        <el-descriptions-item label="目标年级 / 班级">{{ gradeName }} / {{ className || '暂不分班' }}</el-descriptions-item>
        <el-descriptions-item label="学号">保持不变</el-descriptions-item>
        <el-descriptions-item label="接收后的状态机">建立「转入未报到」记录，进入待报到名单</el-descriptions-item>
      </el-descriptions>
      <el-alert
        class="mt-3"
        type="warning"
        :closable="false"
        title="接收后：学生在报名校建立「转入未报到」记录；报到后转为「在读」并进入本校在读名单与花名册。接收前撤销：记录逻辑删除并留痕。"
      />
    </el-card>

    <!-- 步骤 4：报到 -->
    <el-card v-else shadow="never" class="mt-3" data-layout-group="报到">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 4 · 报到</span>
          <el-tag type="success">已接收待报到</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="转学单号">{{ result.transferNo || selectedTransfer?.transferNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ checkedIn ? '已报到（在读）' : '已接收（转入未报到）' }}</el-descriptions-item>
        <el-descriptions-item label="学号">不变</el-descriptions-item>
        <el-descriptions-item label="两侧审计">已写入</el-descriptions-item>
      </el-descriptions>
      <div class="hint mt-2">报到时指定班级并由「报到」动作把状态转为在读。</div>
    </el-card>

    <el-card shadow="never" class="mt-3">
      <div class="flex justify-between items-center">
        <el-button :disabled="activeStep === 1" @click="activeStep -= 1">上一步</el-button>
        <div class="flex items-center gap-2">
          <el-button v-if="activeStep === 4 && !checkedIn" @click="handleCheckIn">办理报到</el-button>
          <el-button v-if="activeStep === 4" @click="handleCancel">撤销申请</el-button>
          <el-button v-if="activeStep < 3" :disabled="activeStep === 1 && !selectedTransferId" type="primary" @click="handleNext"> 下一步 </el-button>
          <el-button v-else-if="activeStep === 3" type="primary" :loading="submitting" @click="handleAccept">接收</el-button>
          <el-button v-else @click="handleBackToList">返回学生列表</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listGrade } from '@/api/edu/grade';
import type { GradeVO } from '@/api/edu/grade/types';
import { acceptTransfer, cancelTransfer, checkInTransfer, listTransfer } from '@/api/edu/promotion';
import type { TransferAcceptForm, TransferOrderVO } from '@/api/edu/promotion/types';

defineOptions({ name: 'EduPromotionTransfer' });

const router = useRouter();

const activeStep = ref(1);
const loading = ref(false);
const submitting = ref(false);
const checkedIn = ref(false);
const formRef = ref<FormInstance>();
const transferList = ref<TransferOrderVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);
const classOptions = ref<ClassVO[]>([]);
const selectedTransferId = ref('');
const result = ref<Partial<TransferOrderVO>>({});

const form = reactive<TransferAcceptForm>({ transferId: '', toGradeId: '', toClassId: '' });

const selectedTransfer = computed(() => transferList.value.find((item) => item.transferId === selectedTransferId.value));
const gradeName = computed(() => gradeOptions.value.find((item) => item.gradeId === form.toGradeId)?.gradeName ?? '—');
const className = computed(() => classOptions.value.find((item) => item.classId === form.toClassId)?.className ?? '');

const rules: FormRules = {
  toGradeId: [{ required: true, message: '请选择目标年级', trigger: 'change' }]
};

const loadTransfers = async () => {
  loading.value = true;
  try {
    const res = await listTransfer({ pageNum: 1, pageSize: 100 });
    transferList.value = res.rows ?? [];
  } finally {
    loading.value = false;
  }
};

const loadGradeOptions = async () => {
  try {
    const res = await listGrade({});
    gradeOptions.value = res.data ?? [];
  } catch {
    gradeOptions.value = [];
  }
};

const handleSelectTransfer = (row?: TransferOrderVO) => {
  if (row) {
    selectedTransferId.value = row.transferId;
  }
};

const handleGradeChange = async () => {
  form.toClassId = '';
  try {
    const res = await listClass({ gradeId: form.toGradeId });
    classOptions.value = res.data ?? [];
  } catch {
    classOptions.value = [];
  }
};

const handleNext = async () => {
  if (activeStep.value === 1) {
    if (!selectedTransferId.value) {
      ElMessage.warning('请先选择要接收的转学单');
      return;
    }
    form.transferId = selectedTransferId.value;
    activeStep.value = 2;
    return;
  }
  if (activeStep.value === 2) {
    await formRef.value?.validate();
    activeStep.value = 3;
  }
};

/** 接收：动作本身即审批，接收后学生进入待报到名单（不计入在读） */
const handleAccept = async () => {
  submitting.value = true;
  try {
    const res = await acceptTransfer({ ...form });
    result.value = res.data ?? {};
    activeStep.value = 4;
    ElMessage.success('已接收转学单');
  } finally {
    submitting.value = false;
  }
};

/** 办理报到：状态由「转入未报到」转为「在读」 */
const handleCheckIn = async () => {
  const transferId = result.value.transferId || selectedTransferId.value;
  if (!transferId) {
    return;
  }
  submitting.value = true;
  try {
    await checkInTransfer(transferId, { toClassId: form.toClassId });
    checkedIn.value = true;
    ElMessage.success('已完成报到，学生状态转为在读');
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  const transferId = result.value.transferId || selectedTransferId.value;
  if (!transferId) {
    return;
  }
  ElMessageBox.confirm('确认撤销本次接收？记录逻辑删除并留痕，转出校不产生转出记录。', '撤销确认', {
    confirmButtonText: '确认撤销',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await cancelTransfer(transferId);
      ElMessage.success('已撤销接收');
      await loadTransfers();
      activeStep.value = 1;
      selectedTransferId.value = '';
      result.value = {};
    })
    .catch(() => undefined);
};

const handleBackToList = () => {
  router.push('/edu/student/list');
};

onMounted(async () => {
  await Promise.all([loadTransfers(), loadGradeOptions()]);
});
</script>
