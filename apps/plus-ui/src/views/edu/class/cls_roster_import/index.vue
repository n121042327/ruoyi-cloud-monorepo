<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/class-import-roster.html 的 .page-head -->
    <div class="page-head">
      <h1>编班表导入</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">≤ 5000 行 / 10 MB · 同步校验 ≤ 30 秒</el-tag>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">编班表导入</h2>
        <el-tag type="primary" size="small">向导 {{ step }} / 4</el-tag>
        <el-tag type="info" size="small">导入幂等，同一批次重复提交不产生重复数据</el-tag>
      </div>
      <el-steps class="mt-4" :active="step - 1" align-center finish-status="success">
        <el-step title="下载模板" />
        <el-step title="上传与校验" />
        <el-step title="校验结果" />
        <el-step title="执行与进度" />
      </el-steps>
      <div class="text-xs mt-2">
        与「导入导出 → 导入向导」同一套四步流程；本页是班级管理列表「导入编班表」的模块快捷入口。 模板一行一学生、含目标班级列，模板列 4
        列：学号（必填）/ 姓名（选填，用于核对）/ 目标班级（必填）/ 班级类型（选填，默认行政班）。
      </div>
    </el-card>

    <!-- 步骤 1 · 下载模板 -->
    <el-card v-show="step === 1" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span>步骤 1 · 下载编班表导入模板</span>
            <el-tag type="info" size="small">当前版本 roster-v1</el-tag>
          </div>
          <el-button v-hasPermi="['data.import:import']" type="primary" plain icon="Download" @click="handleDownloadTemplate">下载模板</el-button>
        </div>
      </template>
      <el-alert class="mb-2" type="warning" :closable="false">
        <template #title>模板版本口径</template>
        <div>模板版本与字段字典版本对应，模板字段变更必须升版本； 用旧版模板导入时先报「模板版本过期」强提示，仍可继续。</div>
      </el-alert>
      <ol class="list-decimal pl-5 text-xs">
        <li>学号（必填）</li>
        <li>姓名（选填，用于核对）</li>
        <li>目标班级（必填，按班级定位）</li>
        <li>班级类型（选填，默认行政班）</li>
      </ol>
      <div class="flex justify-end mt-3">
        <el-button type="primary" @click="step = 2">下一步：上传与校验</el-button>
      </div>
    </el-card>

    <!-- 步骤 2 · 上传与校验 -->
    <el-card v-show="step === 2" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center gap-2">
          <span>步骤 2 · 上传文件并同步校验</span>
          <el-tag type="info" size="small">≤ 5000 行 / 10 MB / 30 秒</el-tag>
        </div>
      </template>
      <el-upload drag :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="handleFileChange" :file-list="fileList">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">把 .xlsx 文件拖到这里，或 <em>点击选择文件</em></div>
        <template #tip>
          <div class="el-upload__tip">校验阶段不写业务数据；失败明细临时文件有效期 7 天。</div>
        </template>
      </el-upload>

      <el-form :model="form" label-width="110px" class="mt-3">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="目标学年学期">
              <el-select v-model="form.termId" placeholder="请选择目标学年学期" class="w-full">
                <el-option
                  v-for="item in termOptions"
                  :key="item.termId"
                  :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
                  :value="item.termId"
                />
              </el-select>
              <div class="hint">导入的班级与学生关系都落在该学年学期。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重复数据">
              <el-select v-model="form.duplicatePolicy" class="w-full">
                <el-option label="跳过重复行（默认）" value="skip" />
                <el-option label="整批失败，全部修正后重传" value="fail" />
              </el-select>
              <div class="hint">同一学生在一个学年学期只允许一条行政班关系。</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="flex justify-between">
        <el-button @click="step = 1">上一步</el-button>
        <el-button type="primary" :loading="validating" :disabled="!fileList.length" @click="handleValidate">上传并校验</el-button>
      </div>
    </el-card>

    <!-- 步骤 3 · 校验结果 -->
    <el-card v-show="step === 3" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>步骤 3 · 校验结果</span>
          <el-button v-hasPermi="['data.import:import']" icon="Download" @click="handleDownloadFailed">下载失败明细</el-button>
        </div>
      </template>

      <el-row :gutter="16" class="mb-3">
        <el-col :span="8"><el-statistic title="文件总行数" :value="validateResult?.totalCount ?? 0" /></el-col>
        <el-col :span="8"><el-statistic title="可执行" :value="validateResult?.validCount ?? 0" /></el-col>
        <el-col :span="8"><el-statistic title="失败" :value="validateResult?.invalidCount ?? 0" /></el-col>
      </el-row>

      <el-table v-loading="validating" border :data="validateResult?.errors ?? []">
        <el-table-column label="行号" prop="rowNo" width="90" align="center" data-layout-group="校验结果" />
        <el-table-column label="对象" prop="objectName" width="140" data-layout-group="校验结果" />
        <el-table-column label="失败原因" prop="errorMsg" min-width="420" data-layout-group="校验结果" />
        <template #empty>
          <el-empty description="全部行校验通过" />
        </template>
      </el-table>

      <div class="flex justify-between mt-3">
        <el-button @click="step = 2">上一步：重新上传</el-button>
        <el-button v-hasPermi="['data.import:import']" type="primary" :disabled="!validateResult?.validCount" @click="handleExecute">
          确认执行（可执行 {{ validateResult?.validCount ?? 0 }} 行）
        </el-button>
      </div>
    </el-card>

    <!-- 步骤 4 · 执行与进度 -->
    <el-card v-show="step === 4" shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span>步骤 4 · 执行与进度</span>
          <el-button :loading="taskLoading" @click="loadTask">刷新进度</el-button>
        </div>
      </template>

      <el-progress :percentage="task?.progressPercent ?? 0" :stroke-width="16" />
      <el-row :gutter="16" class="mt-4">
        <el-col :span="6">
          <div class="text-xs text-gray-500">任务编号</div>
          <div class="mt-1 mono">{{ task?.taskNo || '—' }}</div>
        </el-col>
        <el-col :span="6"><el-statistic title="总数" :value="task?.totalCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="成功" :value="task?.successCount ?? 0" /></el-col>
        <el-col :span="6"><el-statistic title="失败" :value="task?.failedCount ?? 0" /></el-col>
      </el-row>

      <el-alert class="mt-3" type="info" :closable="false" title="导入是异步任务：可离开本页，任务中心的进度与结果文件是同一份数据。" />

      <div class="flex justify-between mt-3">
        <el-button v-hasPermi="['data.async_task:read']" @click="goTaskCenter">查看异步任务</el-button>
        <div class="flex gap-2">
          <el-button v-hasPermi="['data.import:import']" icon="Download" @click="handleDownloadResult">下载结果</el-button>
          <el-button @click="goClassList">返回班级列表</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import {
  downloadImportFailedRows,
  downloadImportResult,
  downloadImportTemplate,
  executeImport,
  getAsyncTask,
  validateImportFile
} from '@/api/edu/importExport';
import type { AsyncTaskVO, ImportValidateVO } from '@/api/edu/importExport/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduClassRosterImport' });

/**
 * 导入模块编码：编班表。
 *
 * 取值必须是 `class_roster`：`edu_import_template.module_code` 的列注释（schema/DDL）与后端
 * `EduClassController.importRosterValidate` 用的都是这个编码；写成 classRoster 会查不到模板。
 */
const IMPORT_MODULE = 'class_roster';

const router = useRouter();

const step = ref(1);
const validating = ref(false);
const taskLoading = ref(false);
const fileList = ref<{ name: string }[]>([]);
const uploadFile = ref<File>();
const validateResult = ref<ImportValidateVO>();
const task = ref<AsyncTaskVO>();
const taskNo = ref('');
const termOptions = ref<TermVO[]>([]);

const form = reactive({ termId: '', duplicatePolicy: 'skip' });

const handleFileChange = (uploadFileItem: { raw?: File; name: string }) => {
  uploadFile.value = uploadFileItem.raw;
  fileList.value = [{ name: uploadFileItem.name }];
};

/** 步骤 1：下载模板（下载写审计） */
const handleDownloadTemplate = async () => {
  await downloadImportTemplate(IMPORT_MODULE);
  ElMessage.success('已生成编班表导入模板 roster-v1（含列名、必填标记、格式说明与示例行），开始下载');
};

/** 步骤 2 → 3：上传并同步校验，校验阶段不写业务数据 */
const handleValidate = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择 .xlsx 文件');
    return;
  }
  validating.value = true;
  try {
    const res = await validateImportFile(uploadFile.value, {
      module: IMPORT_MODULE,
      termId: form.termId,
      duplicatePolicy: form.duplicatePolicy
    });
    validateResult.value = res.data;
    step.value = 3;
    ElMessage.success(`校验完成：可执行 ${res.data?.validCount ?? 0} 行，失败 ${res.data?.invalidCount ?? 0} 行`);
  } finally {
    validating.value = false;
  }
};

const handleDownloadFailed = async () => {
  if (!validateResult.value?.batchNo) return;
  await downloadImportFailedRows(validateResult.value.batchNo);
  ElMessage.success('已生成失败明细文件（标注行号与原因），开始下载');
};

/** 步骤 3 → 4：确认执行，返回异步任务编号 */
const handleExecute = async () => {
  if (!validateResult.value?.batchNo) return;
  const res = await executeImport({ batchNo: validateResult.value.batchNo });
  task.value = res.data;
  taskNo.value = res.data?.taskNo ?? '';
  step.value = 4;
  ElMessage.success('已提交执行，任务异步进行');
};

const loadTask = async () => {
  if (!taskNo.value) return;
  taskLoading.value = true;
  try {
    const res = await getAsyncTask(taskNo.value);
    task.value = res.data;
  } finally {
    taskLoading.value = false;
  }
};

const handleDownloadResult = async () => {
  if (!validateResult.value?.batchNo) return;
  await downloadImportResult(validateResult.value.batchNo);
  ElMessage.success('已生成结果摘要，开始下载');
};

const goTaskCenter = () => router.push('/edu/async-task/list');
const goClassList = () => router.push('/edu/class/list');

onMounted(async () => {
  const res = await listTerm({});
  termOptions.value = res.data ?? [];
  const current = termOptions.value.find((item) => item.current);
  form.termId = current?.termId ?? termOptions.value[0]?.termId ?? '';
});
</script>
