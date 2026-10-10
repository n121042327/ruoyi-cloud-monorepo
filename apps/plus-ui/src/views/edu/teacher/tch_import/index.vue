<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/teacher-import.html 的 .page-head -->
    <div class="page-head">
      <h1>教师批量导入</h1>
      <span class="scope-hint">数据范围：本校 · 当前学年学期</span>
      <el-tag type="primary">≤ 3000 行 / 10 MB · 同步校验 ≤ 30 秒</el-tag>
    </div>
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center gap-3">
        <h2 class="text-base font-medium">教师导入</h2>
        <el-tag type="primary" size="small">向导 {{ step }} / 4</el-tag>
        <el-tag type="info" size="small">导入幂等：同一批次重复提交不产生重复数据</el-tag>
      </div>
      <el-steps class="mt-4" :active="step - 1" align-center finish-status="success">
        <el-step title="下载模板" />
        <el-step title="上传与校验" />
        <el-step title="校验结果" />
        <el-step title="执行与进度" />
      </el-steps>
      <div class="text-xs mt-2">
        模块快捷入口：模板与校验规则由教师模块声明，执行走教师模块的
        <span class="mono">importTeacherValidate</span> / <span class="mono">importTeacherExecute</span>； 保存成功后自动创建登录账号。
      </div>
    </el-card>

    <el-card v-show="step === 1" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span>步骤 1 · 下载教师导入模板</span>
            <el-tag type="info" size="small">当前版本 teacher-v1</el-tag>
          </div>
          <el-button v-hasPermi="['data.import:import']" type="primary" plain icon="Download" @click="handleDownloadTemplate">下载模板</el-button>
        </div>
      </template>
      <el-alert class="mb-2" type="warning" :closable="false">
        <template #title>模板版本口径</template>
        <div>
          模板版本与字段字典版本对应，模板字段变更必须升版本； 模板列含工号、姓名、性别、教育角色、任教学科、联系电话等，必填列以模板标注为准。
        </div>
      </el-alert>
      <div class="flex justify-end">
        <el-button type="primary" @click="step = 2">下一步：上传与校验</el-button>
      </div>
    </el-card>

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
          <el-col v-if="showSchoolSelect" :span="12">
            <el-form-item label="目标学校">
              <el-select v-model="form.schoolId" placeholder="请选择目标学校" class="w-full" @change="handleSchoolChange">
                <el-option v-for="item in schoolOptions" :key="item.schoolId" :label="item.schoolName" :value="item.schoolId" />
              </el-select>
              <div class="hint">当前账号可管理多所学校，教师档案与账号都建到选中的学校。</div>
            </el-form-item>
          </el-col>
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
              <div class="hint">导入的任教关系与岗位信息落在该学年学期。</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重复数据">
              <el-select v-model="form.duplicatePolicy" class="w-full">
                <el-option label="跳过重复行（默认）" value="skip" />
                <el-option label="整批失败，全部修正后重传" value="fail" />
              </el-select>
              <div class="hint">重复判定：同一学校内工号唯一；同一教师同一学年学期同一「班级 + 学科」只允许一条任教关系。</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="flex justify-between">
        <el-button @click="step = 1">上一步</el-button>
        <el-button type="primary" :loading="validating" :disabled="!fileList.length" @click="handleValidate">上传并校验</el-button>
      </div>
    </el-card>

    <el-card v-show="step === 3" shadow="hover" class="mb-2">
      <template #header>
        <div class="flex items-center justify-between">
          <span>步骤 3 · 校验结果</span>
          <el-button v-hasPermi="['data.import:import']" icon="Download" @click="handleDownloadFailed">下载失败明细</el-button>
        </div>
      </template>

      <el-row :gutter="16" class="mb-3">
        <el-col :span="8"><el-statistic title="文件总行数" :value="validateResult?.rowTotal ?? 0" /></el-col>
        <el-col :span="8"><el-statistic title="可执行" :value="validateResult?.validCount ?? 0" /></el-col>
        <el-col :span="8"><el-statistic title="失败" :value="validateResult?.invalidCount ?? 0" /></el-col>
      </el-row>

      <el-table v-loading="validating" border :data="validateResult?.invalidRows ?? []">
        <el-table-column label="行号" prop="rowNo" width="90" align="center" data-layout-group="校验结果" />
        <el-table-column label="对象" prop="objectName" width="140" data-layout-group="校验结果" />
        <el-table-column label="失败原因" prop="failReason" min-width="420" data-layout-group="校验结果" />
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

      <el-alert
        class="mt-3"
        type="info"
        :closable="false"
        title="导入是异步任务：可离开本页，任务中心的进度与结果文件是同一份数据。教师账号在导入成功后自动创建。"
      />

      <div class="flex justify-between mt-3">
        <el-button v-hasPermi="['data.async_task:read']" @click="goTaskCenter">查看异步任务</el-button>
        <div class="flex gap-2">
          <el-button v-hasPermi="['data.import:import']" icon="Download" @click="handleDownloadResult">下载结果</el-button>
          <el-button @click="goTeacherList">返回教师列表</el-button>
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
import { downloadTeacherImportTemplate, importTeacherExecute, importTeacherValidate, listTeacher } from '@/api/edu/teacher';
import { downloadImportFailedRows, downloadImportResult, getAsyncTask, uploadImportFile } from '@/api/edu/importExport';
import { downloadByFileRef } from '@/utils/eduFileRef';
import type { AsyncTaskVO, ImportValidateVO } from '@/api/edu/importExport/types';
import { listTerm } from '@/api/edu/term';
import { listSchool } from '@/api/edu/school';
import type { SchoolVO } from '@/api/edu/school/types';
import type { TermVO } from '@/api/edu/term/types';

defineOptions({ name: 'EduTeacherImport' });

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
const schoolOptions = ref<SchoolVO[]>([]);

/** 目标学校下拉只在可管理多所学校时出现：学校租户固定本校，页面与原型保持一致（GAP-115） */
const showSchoolSelect = computed(() => schoolOptions.value.length > 1);

const form = reactive({ termId: '', duplicatePolicy: 'skip', schoolId: '' });

const handleFileChange = (uploadFileItem: { raw?: File; name: string }) => {
  uploadFile.value = uploadFileItem.raw;
  fileList.value = [{ name: uploadFileItem.name }];
};

const handleDownloadTemplate = async () => {
  const res = await downloadTeacherImportTemplate();
  downloadByFileRef(res.data, 'teacher-template.xlsx');
};

/** 上传并同步校验（importTeacherValidate），校验阶段不写业务数据 */
const handleValidate = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择 .xlsx 文件');
    return;
  }
  if (showSchoolSelect.value && !form.schoolId) {
    ElMessage.warning('当前账号可管理多个学校，请先选择目标学校');
    return;
  }
  validating.value = true;
  try {
    const upload = await uploadImportFile(uploadFile.value);
    const fileId = upload.data?.ossId;
    if (!fileId) {
      ElMessage.error('文件上传失败，请重试');
      return;
    }
    const res = await importTeacherValidate({
      fileId,
      fileName: uploadFile.value.name,
      termId: form.termId || undefined,
      schoolId: form.schoolId || undefined,
      strategy: form.duplicatePolicy === 'reject' ? 'fail' : 'skip'
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
  const res = await downloadImportFailedRows(validateResult.value.batchNo);
  downloadByFileRef(res.data, `failed-rows-${validateResult.value.batchNo}.csv`);
};

/** 确认执行（importTeacherExecute）：异步，返回任务编号 */
const handleExecute = async () => {
  if (!validateResult.value?.batchNo) return;
  const res = await importTeacherExecute({ batchNo: validateResult.value.batchNo });
  task.value = res.data;
  taskNo.value = res.data?.taskNo ?? '';
  step.value = 4;
  ElMessage.success('已提交执行，教师账号将在导入成功后自动创建');
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
  const res = await downloadImportResult(validateResult.value.batchNo);
  downloadByFileRef(res.data, `import-result-${validateResult.value.batchNo}.xlsx`);
};

const goTaskCenter = () => router.push('/edu/async-task/list');
const goTeacherList = () => router.push('/edu/teacher/list');

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
  const res = await listTerm(form.schoolId ? { schoolId: form.schoolId } : {});
  termOptions.value = res.data ?? [];
  const current = termOptions.value.find((item) => item.current);
  form.termId = current?.termId ?? termOptions.value[0]?.termId ?? '';
};

const handleSchoolChange = async () => {
  form.termId = '';
  await loadTermOptions();
};

onMounted(async () => {
  await loadSchoolOptions();
  await Promise.all([loadTermOptions(), listTeacher({ pageNum: 1, pageSize: 1 })]);
});
</script>
