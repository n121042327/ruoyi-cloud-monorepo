<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/class-detail.html 的 .page-head -->
    <div class="page-head">
      <el-button link icon="Back" @click="goList">返回列表</el-button>
      <h1>{{ detail.className || '班级详情' }}</h1>
      <span class="scope-hint">数据范围：本校 · {{ detail.termName || '当前学年学期' }}</span>
      <el-tag type="primary">{{ detail.gradeName || '—' }} · {{ classTypeText }}</el-tag>
      <el-tag v-if="statusText" :type="statusTagType">{{ statusText }}</el-tag>
      <div class="ml-auto flex gap-2">
        <el-button v-hasPermi="['org.class:update']" plain icon="Edit" @click="handleEdit">编辑班级</el-button>
        <el-button v-hasPermi="['org.class:update']" plain @click="leaderRef?.open(detail)">指定班主任</el-button>
        <el-button v-hasPermi="['org.class:export']" plain icon="Download" @click="handleExport">导出花名册</el-button>
        <el-button plain icon="Refresh" @click="loadAll">刷新</el-button>
      </div>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有班级详情的查看权限" />
    </el-card>

    <template v-else>
      <el-card shadow="hover" class="mb-2">
        <h3 class="form-section-title">基本信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="班级名称">{{ detail.className || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属学校">{{ detail.schoolName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detail.gradeName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学年学期">{{ detail.termName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="班级类型">{{ classTypeText }}</el-descriptions-item>
          <el-descriptions-item label="教室">{{ detail.classroom || '—' }}</el-descriptions-item>
          <el-descriptions-item label="班主任">{{ detail.headTeacherName || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="在读 / 容量"> {{ detail.enrolledCount ?? 0 }} / {{ detail.classCapacity || '不限' }} </el-descriptions-item>
          <el-descriptions-item label="班级状态">{{ statusText }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="hover">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="`花名册 (${rosterTotal})`" name="roster">
            <div class="flex items-center gap-2 mb-2">
              <el-radio-group v-model="rosterScope" @change="handleRosterQuery">
                <el-radio-button value="studying">在读成员</el-radio-button>
                <el-radio-button value="all">全部成员</el-radio-button>
              </el-radio-group>
            </div>

            <el-form :inline="true" :model="rosterQuery">
              <el-form-item label="关键字">
                <el-input v-model="rosterQuery.keyword" placeholder="学号 / 姓名" clearable style="width: 200px" @keyup.enter="handleRosterQuery" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleRosterQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetRosterQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb-2">
              <el-col :span="1.5">
                <el-button v-hasPermi="['org.class:update']" type="primary" plain icon="Plus" @click="handleAddStudent"> 添加学生 </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button
                  v-hasPermi="['org.class:update']"
                  type="danger"
                  plain
                  icon="Delete"
                  :disabled="!selectedIds.length"
                  @click="handleBatchRemove"
                >
                  批量移出
                </el-button>
              </el-col>
            </el-row>

            <el-table v-loading="loading" :data="rosterList" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="42" />
              <el-table-column label="学号" prop="studentNo" width="130" show-overflow-tooltip data-layout-group="学生信息" />
              <el-table-column label="姓名" prop="studentName" width="120" show-overflow-tooltip data-layout-group="学生信息" />
              <el-table-column label="性别" prop="gender" width="70" align="center" data-layout-group="学生信息" />
              <el-table-column label="学籍状态" prop="enrollmentStatus" width="120" align="center" data-layout-group="学生信息" />
              <el-table-column label="加入日期" prop="joinDate" width="120" align="center" data-layout-group="班级信息" />
              <el-table-column
                v-if="canSeeGuardian"
                label="监护人"
                prop="guardianName"
                width="110"
                show-overflow-tooltip
                data-layout-group="联系方式"
              >
                <template #default="scope">{{ scope.row.guardianName || '—' }}</template>
              </el-table-column>
              <el-table-column v-if="canSeeGuardian" label="联系电话" prop="guardianPhone" width="130" data-layout-group="联系方式">
                <template #default="scope"
                  ><span class="mono">{{ scope.row.guardianPhone || '—' }}</span></template
                >
              </el-table-column>
              <el-table-column label="班级归属" width="130" show-overflow-tooltip data-layout-group="班级信息">
                <template #default="scope">{{ scope.row.currentClassName || '本班' }}</template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="100" data-layout-group="操作">
                <template #default="scope">
                  <el-button v-hasPermi="['org.class:update']" link type="danger" @click="handleRemove(scope.row)"> 移出 </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="花名册暂无成员" />
              </template>
            </el-table>
            <pagination
              v-if="rosterTotal > 0"
              v-model:total="rosterTotal"
              v-model:page="rosterQuery.pageNum"
              v-model:limit="rosterQuery.pageSize"
              @pagination="loadRoster"
            />
          </el-tab-pane>

          <el-tab-pane :label="`任课教师 (${assignments.length})`" name="teachers">
            <el-empty v-if="!assignments.length" description="尚未设置任课关系（写入入口在教师管理的任教关系）" />
            <div v-for="group in assignmentGroups" :key="group.subject" class="mb-3">
              <h4 class="mb-1 font-medium">{{ group.subject }}</h4>
              <ClassTeachingAssignmentTable :items="group.items" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="班主任任职历史" name="leader">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="现任班主任">{{ detail.headTeacherName || '未指定' }}</el-descriptions-item>
              <el-descriptions-item label="任职开始">{{ detail.headTeacherStartDate || '—' }}</el-descriptions-item>
            </el-descriptions>
            <el-alert
              class="mt-2"
              type="info"
              :closable="false"
              title="完整任职历史需要后端提供班主任变更历史接口；当前的任命与变更过程可在「变更记录」中查看。"
            />
          </el-tab-pane>

          <el-tab-pane label="变更记录" name="changes">
            <ClassChangeLogTable :changes="changes" :loading="changesLoading" />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <ClassFormDialog
        ref="formDialogRef"
        :term-options="termOptions"
        :grade-options="gradeOptions"
        :teacher-options="teacherOptions"
        @success="loadAll"
      />
      <HeadTeacherDialog ref="leaderRef" :teacher-options="teacherOptions" @success="loadAll" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { exportClassRoster, getClass, listClassRoster, listClassTeachingAssignment, removeClassRoster } from '@/api/edu/class';
import type { ClassRosterVO, ClassVO } from '@/api/edu/class/types';
import { listObjectChangeLog } from '@/api/edu/audit';
import { checkPermi, checkRole } from '@/utils/permission';
import type { OperationLogVO } from '@/api/edu/audit/types';
import { listTeacher } from '@/api/edu/teacher';
import type { TeacherVO, TeachingAssignmentVO } from '@/api/edu/teacher/types';
import type { GradeVO } from '@/api/edu/grade/types';
import { listGrade } from '@/api/edu/grade';
import type { TermVO } from '@/api/edu/term/types';
import { listTerm } from '@/api/edu/term';
import ClassFormDialog from '../cls_list/components/ClassFormDialog.vue';
import HeadTeacherDialog from './components/HeadTeacherDialog.vue';
import ClassTeachingAssignmentTable from './components/TeachingAssignmentTable.vue';
import ClassChangeLogTable from './components/ClassChangeLogTable.vue';

defineOptions({ name: 'EduClassDetail' });

const route = useRoute();
const router = useRouter();

const classId = computed(() => String(route.query.classId ?? ''));

const loading = ref(false);
const changesLoading = ref(false);
const activeTab = ref('roster');
const canRead = computed(() => checkPermi(['org.class:read']));
/**
 * 监护人 / 联系电话两列的角色可见性。
 *
 * 原型 class-detail.html 第 181—182 行的 data-role-visible 限定为
 * academic_director / grade_leader / homeroom / super_admin；联系电话由后端默认掩码返回。
 */
const canSeeGuardian = computed(() => checkRole(['academic_director', 'grade_leader', 'homeroom', 'super_admin']));

const detail = ref<ClassVO>({} as ClassVO);
const rosterList = ref<ClassRosterVO[]>([]);
const rosterTotal = ref(0);
const rosterScope = ref<'studying' | 'all'>('studying');
const selectedIds = ref<string[]>([]);
const assignments = ref<TeachingAssignmentVO[]>([]);
const changes = ref<OperationLogVO[]>([]);
const teacherOptions = ref<TeacherVO[]>([]);
const termOptions = ref<TermVO[]>([]);
const gradeOptions = ref<GradeVO[]>([]);

const formDialogRef = ref<InstanceType<typeof ClassFormDialog>>();
const leaderRef = ref<InstanceType<typeof HeadTeacherDialog>>();

const rosterQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: 'studying'
});

const classTypeText = computed(() => {
  const map: Record<string, string> = { administration: '行政班', teaching: '教学班' };
  return map[String(detail.value.classType ?? '')] ?? detail.value.classType ?? '—';
});

const statusText = computed(() => {
  if (!detail.value.classId) return '';
  return detail.value.classStatus === 'disabled' ? '已停用' : '正常';
});
const statusTagType = computed(() => (detail.value.classStatus === 'disabled' ? 'info' : 'success'));

const assignmentGroups = computed(() => {
  const groups: { subject: string; items: TeachingAssignmentVO[] }[] = [];
  for (const item of assignments.value) {
    const subject = item.subjectName || '未指定学科';
    let group = groups.find((g) => g.subject === subject);
    if (!group) {
      group = { subject, items: [] };
      groups.push(group);
    }
    group.items.push(item);
  }
  return groups;
});

const goList = () => {
  router.push({ path: '/edu/class/list' });
};

const loadDetail = async () => {
  if (!classId.value) return;
  const res = await getClass(classId.value);
  detail.value = (res.data ?? {}) as ClassVO;
};

const loadRoster = async () => {
  if (!classId.value) return;
  loading.value = true;
  try {
    const res = await listClassRoster(classId.value, {
      ...rosterQuery,
      status: rosterScope.value === 'all' ? undefined : 'studying'
    });
    rosterList.value = res.rows ?? [];
    rosterTotal.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const handleRosterQuery = () => {
  rosterQuery.pageNum = 1;
  loadRoster();
};

const resetRosterQuery = () => {
  rosterQuery.keyword = '';
  rosterQuery.pageNum = 1;
  loadRoster();
};

const handleSelectionChange = (rows: ClassRosterVO[]) => {
  selectedIds.value = rows.map((r) => r.studentId);
};

const loadAssignments = async () => {
  if (!classId.value) return;
  const res = await listClassTeachingAssignment(classId.value);
  assignments.value = (res.data ?? []) as TeachingAssignmentVO[];
};

const loadChanges = async () => {
  if (!classId.value) return;
  changesLoading.value = true;
  try {
    const res = await listObjectChangeLog('class', classId.value);
    changes.value = ((res.data ?? []) as OperationLogVO[]).slice(0, 50);
  } finally {
    changesLoading.value = false;
  }
};

const loadOptions = async () => {
  const [teacherRes, termRes, gradeRes] = await Promise.all([
    listTeacher({ pageNum: 1, pageSize: 200, employmentStatus: '在职' }),
    listTerm({}),
    listGrade({})
  ]);
  teacherOptions.value = teacherRes.rows ?? [];
  termOptions.value = termRes.rows ?? [];
  gradeOptions.value = gradeRes.rows ?? [];
};

const loadAll = async () => {
  try {
    await loadDetail();
  } catch {
    ElMessage.error('班级不存在或不在当前数据范围内');
    return;
  }
  await Promise.all([loadRoster(), loadAssignments(), loadChanges()]);
};

const handleEdit = () => {
  formDialogRef.value?.open(detail.value);
};

const handleAddStudent = () => {
  router.push({ path: '/edu/class/roster/add', query: { classId: classId.value } });
};

const removeOne = async (row: ClassRosterVO, reason: string) => {
  await removeClassRoster(classId.value, row.studentId, reason);
};

const handleRemove = async (row: ClassRosterVO) => {
  const { value } = await ElMessageBox.prompt(`确认将「${row.studentName ?? '该学生'}」移出本班？`, '移出学生', {
    inputPlaceholder: '移出原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 2 ? true : '请填写移出原因（至少 2 个字）'),
    type: 'warning'
  });
  await removeOne(row, value);
  ElMessage.success('已移出');
  await loadRoster();
};

const handleBatchRemove = async () => {
  const { value } = await ElMessageBox.prompt(`确认移出选中的 ${selectedIds.value.length} 名学生？`, '批量移出', {
    inputPlaceholder: '移出原因（必填）',
    inputValidator: (text: string) => (text && text.trim().length >= 2 ? true : '请填写移出原因（至少 2 个字）'),
    type: 'warning'
  });
  const rows = rosterList.value.filter((r) => selectedIds.value.includes(r.studentId));
  const failed: string[] = [];
  for (const row of rows) {
    try {
      await removeOne(row, value);
    } catch {
      failed.push(row.studentName ?? row.studentId);
    }
  }
  if (failed.length) {
    ElMessage.warning(`已移出 ${rows.length - failed.length} 人，${failed.length} 人失败：${failed.join('、')}`);
  } else {
    ElMessage.success(`已移出 ${rows.length} 人`);
  }
  selectedIds.value = [];
  await loadRoster();
};

const handleExport = async () => {
  await exportClassRoster(classId.value, {});
  ElMessage.success('已提交导出任务，请到异步任务中心下载结果文件');
};

onMounted(async () => {
  if (!classId.value) {
    ElMessage.warning('缺少班级上下文，请从班级列表进入');
    goList();
    return;
  }
  await loadOptions();
  await loadAll();
});
</script>
