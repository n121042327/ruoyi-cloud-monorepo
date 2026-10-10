<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/student-import.html 的 .page-head -->
    <div class="page-head">
      <h1>学生批量导入</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">≤ 5000 行 / 10 MB · 同步校验 ≤ 30 秒</el-tag>
    </div>
    <el-card shadow="never">
      <template #header>
        <div class="flex justify-between items-center">
          <span>学生批量导入</span>
          <div class="flex items-center gap-2">
            <el-tag type="info">≤ 5000 行 / 10 MB / 30 秒</el-tag>
            <el-tag>向导 {{ activeStep }} / 4</el-tag>
          </div>
        </div>
      </template>
      <el-steps :active="activeStep - 1" align-center finish-status="success">
        <el-step title="下载模板" />
        <el-step title="上传与校验" />
        <el-step title="校验结果" />
        <el-step title="执行与进度" />
      </el-steps>
      <el-alert class="mt-3" type="info" :closable="false" title="两阶段导入：先校验并展示结果，确认后才异步执行；失败行可下载明细逐条修正。" />
    </el-card>

    <!-- 步骤 1：下载模板 -->
    <el-card v-if="activeStep === 1" shadow="never" class="mt-3" data-layout-group="下载模板">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 1 · 下载学生批量导入模板</span>
          <el-tag type="info">当前版本 {{ templateVersion || 'student-v3' }}</el-tag>
        </div>
      </template>
      <el-button v-hasPermi="['data.import:import']" type="primary" icon="Download" @click="handleDownloadTemplate">下载模板</el-button>
      <el-descriptions v-if="templateColumns.length" :column="2" border class="mt-3">
        <el-descriptions-item v-for="item in templateColumns" :key="item.name" :label="item.name">
          {{ item.required ? '必填' : '选填' }}{{ item.note ? ' · ' + item.note : '' }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="hint mt-2">模板版本与字段字典版本对应，字段变更必须升版本；使用旧版模板会先报「模板版本过期」强提示，仍可继续。</div>
    </el-card>

    <!-- 步骤 2：上传与校验 -->
    <el-card v-else-if="activeStep === 2" shadow="never" class="mt-3" data-layout-group="上传与校验">
      <template #header><span>步骤 2 · 上传文件并同步校验</span></template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="导入文件">
          <el-upload :show-file-list="true" :limit="1" accept=".xlsx,.csv" :auto-upload="false" :on-change="handleFileChange">
            <el-button icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="hint">支持 xlsx / csv；单文件 ≤ 10 MB、行数 ≤ 5000，超出时在上传阶段直接拒绝。</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="showSchoolSelect" label="目标学校">
          <el-select v-model="form.schoolId" placeholder="请选择目标学校" style="width: 260px" @change="handleSchoolChange">
            <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
          </el-select>
          <div class="hint">当前账号可管理多所学校，导入的学生都写到选中的学校。</div>
        </el-form-item>
        <el-form-item label="目标学年学期">
          <el-select v-model="form.termId" placeholder="请选择" clearable style="width: 260px">
            <el-option v-for="item in termOptions" :key="item.termId" :label="item.termName" :value="item.termId" />
          </el-select>
          <div class="hint">导入的学籍与班级关系都落在该学年学期。</div>
        </el-form-item>
        <el-form-item label="重复数据">
          <el-radio-group v-model="form.duplicatePolicy">
            <el-radio value="skip">跳过重复行（默认）</el-radio>
            <el-radio value="reject">整批失败，全部修正后重传</el-radio>
          </el-radio-group>
          <div class="hint">重复判定：证件号平台唯一+ 姓名与入学年份的组合；同一批次重复提交不产生重复数据。</div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 步骤 3：校验结果 -->
    <el-card v-else-if="activeStep === 3" shadow="never" class="mt-3" data-layout-group="校验结果">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 3 · 校验结果</span>
          <el-button v-if="validateResult?.batchNo" link type="primary" @click="handleDownloadFailedRows">下载失败明细</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border class="mb-3">
        <el-descriptions-item label="文件总行数">{{ validateResult?.rowTotal ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="可执行">{{ validateResult?.validCount ?? 0 }} 行</el-descriptions-item>
        <el-descriptions-item label="失败">{{ validateResult?.invalidCount ?? 0 }} 行</el-descriptions-item>
      </el-descriptions>
      <el-table :data="validateResult?.invalidRows ?? []" border>
        <el-table-column label="行号" prop="rowNo" width="90" align="center" />
        <el-table-column label="对象" prop="objectName" width="140" />
        <el-table-column label="失败原因" prop="failReason" min-width="260" />
        <template #empty>
          <el-empty description="校验全部通过" :image-size="60" />
        </template>
      </el-table>
      <div class="hint mt-2">失败行可下载明细逐条修正后作为新批次重新上传。</div>
    </el-card>

    <!-- 步骤 4：执行与进度 -->
    <el-card v-else shadow="never" class="mt-3" data-layout-group="执行与进度">
      <template #header>
        <div class="flex justify-between items-center">
          <span>步骤 4 · 执行与进度</span>
          <el-tag :type="taskStatusTag">{{ taskStatusText }}</el-tag>
        </div>
      </template>
      <el-progress :percentage="taskProgress" :stroke-width="14" />
      <el-descriptions :column="2" border class="mt-3">
        <el-descriptions-item label="任务编号">{{ task.taskNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="成功 / 失败"> {{ task.successCount ?? 0 }} 行 / {{ task.failedCount ?? 0 }} 行 </el-descriptions-item>
        <el-descriptions-item label="配额">同校 1 / 3，超出排队</el-descriptions-item>
        <el-descriptions-item label="可以离开页面">导入异步执行，可在异步任务中心看进度与结果文件</el-descriptions-item>
      </el-descriptions>
      <div class="mt-3 flex gap-2">
        <el-button v-if="task.taskNo" @click="handleDownloadResult">下载学号对照表</el-button>
        <el-button type="primary" plain @click="handleRestart">重新导入</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-3">
      <div class="flex justify-between items-center">
        <el-button :disabled="activeStep === 1" @click="activeStep -= 1">上一步</el-button>
        <div class="flex items-center gap-2">
          <el-button v-if="activeStep === 1" type="primary" @click="activeStep = 2">下一步</el-button>
          <el-button v-else-if="activeStep === 2" type="primary" :loading="validating" @click="handleValidate">上传并校验</el-button>
          <el-button v-else-if="activeStep === 3" type="primary" :loading="submitting" @click="handleExecute">确认执行（异步）</el-button>
          <el-button v-else @click="handleBackToList">返回学生列表</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { UploadFile, UploadFiles } from 'element-plus';
import { ElMessage } from 'element-plus';
import {
  downloadImportFailedRows,
  downloadImportResult,
  downloadImportTemplate,
  executeImport,
  getAsyncTask,
  listImportTemplate,
  uploadImportFile,
  validateImportFile
} from '@/api/edu/importExport';
import { downloadByFileRef } from '@/utils/eduFileRef';
import type { AsyncTaskVO, ImportValidateVO } from '@/api/edu/importExport/types';
import { listTerm } from '@/api/edu/term';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduStudentImport' });

const router = useRouter();

const activeStep = ref(1);
const validating = ref(false);
const submitting = ref(false);
const templateVersion = ref('');
const templateColumns = ref<Array<{ name: string; required: boolean; note?: string }>>([]);
const termOptions = ref<TermVO[]>([]);
const schoolOptions = ref<SchoolVO[]>([]);
const selectedFile = ref<File>();
const validateResult = ref<ImportValidateVO>();
const task = ref<Partial<AsyncTaskVO>>({});

const form = reactive({ termId: '', duplicatePolicy: 'skip', schoolId: '' });

/** 目标学校下拉只在可管理多所学校时出现：学校租户固定本校，页面与原型保持一致（GAP-115） */
const showSchoolSelect = computed(() => schoolOptions.value.length > 1);

const taskProgress = computed(() => Math.min(100, Math.max(0, Number(task.value.progressPercent ?? 0))));
const taskStatusText = computed(() => {
  const map: Record<string, string> = {
    queued: '排队中',
    running: '执行中',
    succeeded: '已完成',
    partial_failed: '部分失败',
    failed: '失败'
  };
  return map[task.value.taskStatus ?? 'queued'] ?? '执行中';
});
const taskStatusTag = computed(() => {
  if (task.value.taskStatus === 'succeeded') return 'success';
  if (task.value.taskStatus === 'partial_failed' || task.value.taskStatus === 'failed') return 'danger';
  return 'warning';
});

const loadTemplate = async () => {
  try {
    const res = await listImportTemplate();
    const student = (res.data ?? []).find((item) => item.moduleCode === 'student');
    templateVersion.value = student?.templateVersion ?? '';
    templateColumns.value = [];
  } catch {
    templateColumns.value = [];
  }
};

/** 目标学校：学校租户只有一所（下拉隐藏）；多校账号必选 */
const loadSchoolOptions = async () => {
  try {
    const res = await listSchool();
    schoolOptions.value = res.data ?? [];
  } catch {
    schoolOptions.value = [];
  }
  if (schoolOptions.value.length === 1) {
    form.schoolId = schoolOptions.value[0].schoolId ?? '';
  }
};

/** 学年学期属于具体学校：选定学校后按该校重载 */
const loadTermOptions = async () => {
  try {
    const res = await listTerm(form.schoolId ? { schoolId: form.schoolId } : {});
    termOptions.value = res.data ?? [];
    form.termId = termOptions.value.find((item) => item.current)?.termId ?? '';
  } catch {
    termOptions.value = [];
  }
};

const handleSchoolChange = async () => {
  form.termId = '';
  await loadTermOptions();
};

/** 下载模板（通用接口，按模块取模板） */
const handleDownloadTemplate = async () => {
  const res = await downloadImportTemplate('student');
  downloadByFileRef(res.data, `student_import_template_${templateVersion.value || 'v3'}.xlsx`);
};

const handleFileChange = (file: UploadFile, _files: UploadFiles) => {
  selectedFile.value = file.raw;
};

const handleValidate = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }
  if (showSchoolSelect.value && !form.schoolId) {
    ElMessage.warning('当前账号可管理多个学校，请先选择目标学校');
    return;
  }
  validating.value = true;
  try {
    const upload = await uploadImportFile(selectedFile.value);
    const fileId = upload.data?.ossId;
    if (!fileId) {
      ElMessage.error('文件上传失败，请重试');
      return;
    }
    const res = await validateImportFile({
      moduleCode: 'student',
      fileId,
      fileName: selectedFile.value.name,
      termId: form.termId || undefined,
      schoolId: form.schoolId || undefined,
      strategy: form.duplicatePolicy === 'reject' ? 'fail' : 'skip'
    });
    validateResult.value = res.data;
    activeStep.value = 3;
    ElMessage.success('校验完成');
  } finally {
    validating.value = false;
  }
};

const handleDownloadFailedRows = async () => {
  const batchNo = validateResult.value?.batchNo;
  if (!batchNo) return;
  const res = await downloadImportFailedRows(batchNo);
  downloadByFileRef(res.data, `import_failed_rows_${batchNo}.csv`);
};

const handleExecute = async () => {
  const batchNo = validateResult.value?.batchNo;
  if (!batchNo) return;
  submitting.value = true;
  try {
    const res = await executeImport({ batchNo });
    task.value = res.data ?? {};
    activeStep.value = 4;
    ElMessage.success('已开始异步执行');
    if (task.value.taskNo) {
      const detail = await getAsyncTask(task.value.taskNo);
      task.value = { ...task.value, ...(detail.data ?? {}) };
    }
  } finally {
    submitting.value = false;
  }
};

const handleDownloadResult = async () => {
  const batchNo = validateResult.value?.batchNo;
  if (!batchNo) return;
  const res = await downloadImportResult(batchNo);
  downloadByFileRef(res.data, `import_result_${batchNo}.xlsx`);
};

const handleRestart = () => {
  activeStep.value = 1;
  selectedFile.value = undefined;
  validateResult.value = undefined;
  task.value = {};
};

const handleBackToList = () => {
  router.push('/edu/student/list');
};

onMounted(async () => {
  await loadSchoolOptions();
  await Promise.all([loadTemplate(), loadTermOptions()]);
});
</script>
