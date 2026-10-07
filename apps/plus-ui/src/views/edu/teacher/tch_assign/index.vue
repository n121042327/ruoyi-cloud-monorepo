<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <h2 class="text-base font-medium">教师任教关系</h2>
          <el-tag type="info" size="small">{{ termLabel || '当前学年学期' }}</el-tag>
        </div>
        <div class="flex gap-2">
          <el-button v-hasPermi="['person.teaching_assignment:create']" icon="CopyDocument" @click="copyRef?.open()">复制上一学年</el-button>
          <el-button v-hasPermi="['data.export:export']" icon="Download" @click="handleExport">导出任教关系</el-button>
          <el-button icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </div>
      <div class="text-xs mt-2">数据范围：教务主任可见全校；年级主任限本年级；平台运营只读并留痕。跨校任教需运营方授权且只读。</div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-form :model="queryParams" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="视角" data-layout-group="查询条件">
              <el-radio-group v-model="queryParams.view" @change="handleViewChange">
                <el-radio-button :value="'class'">按班级</el-radio-button>
                <el-radio-button :value="'teacher'">按教师</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学年学期" data-layout-group="查询条件">
              <el-select v-model="queryParams.termId" placeholder="请选择学年学期" clearable class="w-full">
                <el-option
                  v-for="item in termOptions"
                  :key="item.termId"
                  :label="`${item.academicYearName ?? ''} ${item.termName}`.trim()"
                  :value="item.termId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关键字" data-layout-group="查询条件">
              <el-input v-model="queryParams.keyword" placeholder="按教师 / 班级 / 学科" clearable @keyup.enter="handleQuery" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>{{ queryParams.view === 'class' ? '选择班级' : '选择教师' }}</span>
              <el-tag type="info" size="small">{{ queryParams.view === 'class' ? '按班级视角' : '按教师视角' }}</el-tag>
            </div>
          </template>
          <el-input v-model="pickerKeyword" placeholder="搜索" clearable class="mb-2" />
          <div v-if="queryParams.view === 'class'" class="max-h-96 overflow-auto">
            <div
              v-for="item in filteredClasses"
              :key="item.classId"
              class="picker-item"
              :class="{ 'picker-item-on': item.classId === queryParams.classId }"
              @click="selectClass(item.classId)"
            >
              <div>{{ item.className }}</div>
              <div class="text-xs text-gray-500">{{ [item.gradeName, item.status].filter(Boolean).join(' · ') }}</div>
            </div>
            <el-empty v-if="!filteredClasses.length" description="没有匹配的班级" />
          </div>
          <div v-else class="max-h-96 overflow-auto">
            <div
              v-for="item in filteredTeachers"
              :key="item.teacherId"
              class="picker-item"
              :class="{ 'picker-item-on': item.teacherId === queryParams.teacherId }"
              @click="selectTeacher(item.teacherId)"
            >
              <div>{{ item.teacherName }}</div>
              <div class="text-xs text-gray-500">{{ item.subjectNames || '无任教学科' }}</div>
            </div>
            <el-empty v-if="!filteredTeachers.length" description="没有匹配的教师" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <AssignmentTable
          v-model:total="total"
          v-model:page-num="pageNum"
          v-model:page-size="pageSize"
          :list="list"
          :loading="loading"
          :editable="editable"
          @create="formRef?.open(undefined, queryParams.classId)"
          @edit="handleEdit"
          @remove="handleRemove"
          @refresh="getList"
        />
      </el-col>
    </el-row>

    <AssignmentFormDialog
      ref="formRef"
      :term-options="termOptions"
      :subject-options="subjectOptions"
      :teacher-options="teacherOptions"
      :class-options="classOptions"
      @success="getList"
    />
    <CopyAssignDialog ref="copyRef" :term-options="termOptions" @success="getList" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listTeacher, listTeachingAssignment, removeTeachingAssignment } from '@/api/edu/teacher';
import type { TeacherVO, TeachingAssignmentQuery, TeachingAssignmentVO } from '@/api/edu/teacher/types';
import { listClass } from '@/api/edu/class';
import type { ClassVO } from '@/api/edu/class/types';
import { listSubject } from '@/api/edu/subject';
import type { SubjectVO } from '@/api/edu/subject/types';
import { listTerm } from '@/api/edu/term';
import type { TermVO } from '@/api/edu/term/types';
import { exportData } from '@/api/edu/importExport';
import { checkPermi } from '@/utils/permission';
import AssignmentTable from './components/AssignmentTable.vue';
import AssignmentFormDialog from './components/AssignmentFormDialog.vue';
import CopyAssignDialog from './components/CopyAssignDialog.vue';

defineOptions({ name: 'EduTeacherAssign' });

const loading = ref(false);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(20);
const pickerKeyword = ref('');
const list = ref<TeachingAssignmentVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const subjectOptions = ref<SubjectVO[]>([]);
const teacherOptions = ref<TeacherVO[]>([]);
const classOptions = ref<ClassVO[]>([]);
const formRef = ref<InstanceType<typeof AssignmentFormDialog>>();
const copyRef = ref<InstanceType<typeof CopyAssignDialog>>();

const queryParams = reactive<TeachingAssignmentQuery>({
  view: 'class',
  termId: '',
  keyword: '',
  classId: '',
  teacherId: ''
});

/** 只有教务主任与超管可编辑；校领导 / 年级主任 / 平台运营只读 */
const editable = computed(() => checkPermi(['person.teaching_assignment:create']));

const termLabel = computed(() => {
  const current = termOptions.value.find((item) => item.termId === queryParams.termId);
  return current ? `${current.academicYearName ?? ''} ${current.termName}`.trim() : '';
});

const filteredClasses = computed(() => classOptions.value.filter((item) => !pickerKeyword.value || item.className.includes(pickerKeyword.value)));
const filteredTeachers = computed(() =>
  teacherOptions.value.filter((item) => !pickerKeyword.value || item.teacherName.includes(pickerKeyword.value))
);

const getList = async () => {
  loading.value = true;
  try {
    const res = await listTeachingAssignment({ ...queryParams, pageNum: pageNum.value, pageSize: pageSize.value });
    list.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  pageNum.value = 1;
  getList();
};

const handleViewChange = () => {
  queryParams.classId = '';
  queryParams.teacherId = '';
  handleQuery();
};

const selectClass = (classId: string) => {
  queryParams.classId = classId;
  queryParams.teacherId = '';
  handleQuery();
};

const selectTeacher = (teacherId: string) => {
  queryParams.teacherId = teacherId;
  queryParams.classId = '';
  handleQuery();
};

const handleEdit = (row: TeachingAssignmentVO) => formRef.value?.open(row);

/** 结束任教关系：必填原因，写审计 */
const handleRemove = async (row: TeachingAssignmentVO) => {
  const { value } = await ElMessageBox.prompt(
    `确认结束「${row.className ?? ''} · ${row.subjectName ?? ''} · ${row.teacherName ?? ''}」这条任教关系？请填写原因`,
    '结束任教关系',
    {
      confirmButtonText: '确认结束',
      cancelButtonText: '取消',
      inputPlaceholder: '如：教师岗位调整，本学期不再任教该班',
      inputValidator: (text: string) => (text && text.trim().length >= 5 ? true : '原因至少 5 个字')
    }
  );
  await removeTeachingAssignment(row.assignmentId, value);
  ElMessage.success('已结束该任教关系，动作写审计');
  await getList();
};

/**
 * 导出任教关系：原型引用 `exportTeachingAssignment`，但契约里没有该 operationId（GAP-089），
 * 按 GAP-086 的同一口径统一走导入导出模块的通用导出 `exportData`。
 */
const handleExport = async () => {
  await exportData({ module: 'teachingAssignment', termId: queryParams.termId, classId: queryParams.classId, teacherId: queryParams.teacherId });
  ElMessage.success('已生成任教关系导出文件，开始下载');
};

onMounted(async () => {
  const [termRes, subjectRes, teacherRes, classRes] = await Promise.all([
    listTerm({}),
    listSubject({ pageNum: 1, pageSize: 200 }),
    listTeacher({ pageNum: 1, pageSize: 500 }),
    listClass({ pageNum: 1, pageSize: 500 })
  ]);
  termOptions.value = termRes.data ?? [];
  subjectOptions.value = subjectRes.rows ?? [];
  teacherOptions.value = teacherRes.rows ?? [];
  classOptions.value = classRes.rows ?? [];
  const current = termOptions.value.find((item) => item.current);
  queryParams.termId = current?.termId ?? termOptions.value[0]?.termId ?? '';
  getList();
});
</script>

<style scoped>
.picker-item {
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.picker-item:hover {
  background: var(--el-fill-color-light);
}

.picker-item-on {
  background: var(--el-color-primary-light-9);
  border-left: 3px solid var(--el-color-primary);
}
</style>
