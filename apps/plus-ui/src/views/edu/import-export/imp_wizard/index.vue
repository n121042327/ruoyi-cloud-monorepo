<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/import-wizard.html 的 .page-head -->
    <div class="page-head">
      <h1>导入向导</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">≤ 5000 行 / 10 MB · 同步校验 ≤ 30 秒</el-tag>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <el-tag type="primary" size="small">向导 {{ step }} / 4</el-tag>
        <el-tag type="info" size="small">同一批次重复提交不产生重复数据</el-tag>
      </div>
      <el-steps class="mt-4" :active="step - 1" align-center finish-status="success">
        <el-step title="选择模板" />
        <el-step title="上传与校验" />
        <el-step title="校验结果" />
        <el-step title="执行与进度" />
      </el-steps>
    </el-card>

    <!-- 步骤 1 · 选择模板 -->
    <el-card v-show="step === 1" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>步骤 1 · 选择导入模块与模板</span>
          <el-button
            v-hasPermi="['data.import:import']"
            type="primary"
            plain
            icon="Download"
            :disabled="!form.moduleCode"
            @click="handleDownloadTemplate"
          >
            下载模板
          </el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px">
        <h3 class="form-section-title" data-layout-group="模板信息">模板信息</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="导入模块">
              <el-select v-model="form.moduleCode" placeholder="请选择导入模块" class="w-full">
                <el-option v-for="item in templateOptions" :key="item.module" :label="item.moduleName || item.module" :value="item.module" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板版本">
              <el-input :model-value="currentTemplate?.version || '—'" disabled />
              <div class="hint">模板字段变更必须升版本；旧版模板会先报「模板版本过期」强提示，仍可继续。</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div v-if="currentTemplate?.columns?.length" class="mb-2">
        <div class="text-xs mb-1">模板列（共 {{ currentTemplate.columns.length }} 列）</div>
        <el-tag v-for="col in currentTemplate.columns" :key="col.name" class="mr-1 mb-1" :type="col.required ? 'danger' : 'info'" effect="plain">
          {{ col.name }}{{ col.required ? '（必填）' : '（选填）' }}
        </el-tag>
      </div>

      <div class="flex justify-end">
        <el-button type="primary" :disabled="!form.moduleCode" @click="step = 2">下一步：上传与校验</el-button>
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

      <el-form :model="form" label-width="120px" class="mt-3">
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
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一目标班级">
              <el-select v-model="form.classId" placeholder="留空表示按文件里的目标班级列" clearable class="w-full">
                <el-option v-for="item in classOptions" :key="item.classId" :label="item.className" :value="item.classId" />
              </el-select>
              <div class="hint">填写后文件里的目标班级列被忽略，全部导入该班级。</div>
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
        <el-table-column label="姓名" prop="objectName" width="140" data-layout-group="校验结果" />
        <el-table-column label="失败原因" prop="errorMsg" min-width="420" data-layout-group="校验结果" />
        <template #empty>
          <el-empty description="全部行校验通过" />
        </template>
      </el-table>

      <div class="mt-3">
        <div class="text-xs mb-1">失败行处理</div>
        <el-radio-group v-model="failMode">
          <el-radio :value="'skip'">忽略失败行，只导入可执行的行</el-radio>
          <el-radio :value="'abort'">全部修正后重新上传</el-radio>
        </el-radio-group>
      </div>

      <div class="flex justify-between mt-3">
        <el-button @click="step = 2">上一步：重新上传</el-button>
        <el-button
          v-hasPermi="['data.import:import']"
          type="primary"
          :disabled="failMode === 'abort' || !validateResult?.validCount"
          @click="handleExecute"
        >
          确认执行（可执行 {{ validateResult?.validCount ?? 0 }} 行）
        </el-button>
      </div>
      <div class="hint mt-2">选择「全部修正后重新上传」时执行按钮禁用；学号对照表在导入结果文件里给出。</div>
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
          <el-button @click="goTaskCenter">去异步任务中心（只读）</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import {
  downloadImportFailedRows,
  downloadImportResult,
  downloadImportTemplate,
  executeImport,
  getAsyncTask,
  listImportTemplate,
  validateImportFile
} from '@/api/edu/importExport';
import type { AsyncTaskVO, ImportTemplateVO, ImportValidateVO } from '@/api/edu/importExport/types';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduImportWizard' });

const router = useRouter();

const step = ref(1);
const validating = ref(false);
const taskLoading = ref(false);
const fileList = ref<{ name: string }[]>([]);
const uploadFile = ref<File>();
const validateResult = ref<ImportValidateVO>();
const task = ref<AsyncTaskVO>();
const taskNo = ref('');
const failMode = ref<'skip' | 'abort'>('skip');
const templateOptions = ref<ImportTemplateVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const classOptions = ref<ClassVO[]>([]);

const form = reactive({ moduleCode: '', termId: '', classId: '' });

const currentTemplate = computed(() => templateOptions.value.find((item) => item.module === form.moduleCode));

const handleFileChange = (uploadFileItem: { raw?: File; name: string }) => {
  uploadFile.value = uploadFileItem.raw;
  fileList.value = [{ name: uploadFileItem.name }];
};

const handleDownloadTemplate = async () => {
  if (!form.moduleCode) return;
  await downloadImportTemplate(form.moduleCode);
  ElMessage.success(`已生成「${currentTemplate.value?.moduleName ?? form.moduleCode}」导入模板（${currentTemplate.value?.version ?? ''}），开始下载`);
};

/** 上传并同步校验：校验阶段不写业务数据 */
const handleValidate = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择 .xlsx 文件');
    return;
  }
  validating.value = true;
  try {
    const res = await validateImportFile(uploadFile.value, {
      module: form.moduleCode,
      termId: form.termId,
      duplicatePolicy: failMode.value === 'abort' ? 'fail' : 'skip'
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
  ElMessage.success('已生成结果摘要与对照表，开始下载');
};

const goTaskCenter = () => router.push('/edu/async-task/list');

onMounted(async () => {
  const [templateRes, termRes, classRes] = await Promise.all([listImportTemplate(), listTerm({}), listClass({ pageNum: 1, pageSize: 500 })]);
  templateOptions.value = templateRes.rows ?? templateRes.data ?? [];
  termOptions.value = termRes.data ?? [];
  classOptions.value = classRes.rows ?? [];
  const current = termOptions.value.find((item) => item.current);
  form.termId = current?.termId ?? termOptions.value[0]?.termId ?? '';
});
</script>
