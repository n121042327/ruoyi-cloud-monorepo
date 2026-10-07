<template>
  <el-drawer v-model="visible" size="680px" :title="`学生详情 · ${detail.studentName || ''}`" append-to-body>
    <div v-loading="loading">
      <el-alert
        type="info"
        :closable="false"
        title="学生详情是只读视图：学籍状态只有「学籍异动」一个写入入口，班级归属只有「调班 / 班级管理」一个写入入口（DP-01）。"
      />

      <el-card shadow="never" class="mt-3">
        <template #header>
          <div class="flex justify-between items-center">
            <span>学生信息</span>
            <el-tag v-if="detail.enrollmentStatus" size="small" type="success">{{ enrollmentStatusLabel }}</el-tag>
          </div>
        </template>

        <h4 class="form-section-title" data-layout-group="基础信息">基础信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ detail.studentNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="全国学籍号">{{ detail.nationalStudentNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.studentName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ detail.gender || '—' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="form-section-title" data-layout-group="教育信息">教育信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入学年份 / 学段">
            {{ detail.enrollYear || '—' }} · {{ STAGE_CODE_LABEL[detail.stageCode] ?? detail.stageCode ?? '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="年级 / 班级"> {{ detail.gradeName || '—' }} · {{ detail.className || '未编班' }} </el-descriptions-item>
          <el-descriptions-item label="学籍状态">
            {{ enrollmentStatusLabel }}
            <span class="text-xs ml-2">{{ detail.enrollmentStatus === 'enrolled' ? '计入在读名单' : '不计入在读名单' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="form-section-title" data-layout-group="证件信息">证件信息</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="证件类型">{{ detail.idType || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="证件号码">
            <span class="mono">{{ idCardFull || detail.idCardNo || '—' }}</span>
            <el-button
              v-hasPermi="['person.student:read_sensitive']"
              link
              type="primary"
              class="ml-2"
              :disabled="!detail.studentId"
              @click="handleRevealIdCard"
            >
              查看完整
            </el-button>
            <div class="hint">默认掩码；查看全量需要 read_sensitive 且写敏感数据访问日志。</div>
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="form-section-title" data-layout-group="联系方式">联系方式</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="联系电话">
            <span class="mono">{{ phoneFull || detail.studentPhone || '—' }}</span>
            <el-button
              v-hasPermi="['person.student_contact:read_contact']"
              link
              type="primary"
              class="ml-2"
              :disabled="!detail.studentId"
              @click="handleRevealPhone"
            >
              查看完整
            </el-button>
            <div class="hint">默认掩码；查看全量需要 read_contact 且写敏感数据访问日志。</div>
          </el-descriptions-item>
          <el-descriptions-item label="联系地址">{{ detail.address || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="mt-3">
        <template #header>
          <div class="flex justify-between items-center">
            <span data-layout-group="监护人">监护人（上限 3）</span>
            <div class="flex items-center gap-2">
              <el-tag size="small" type="info">班主任为唯一写入口</el-tag>
              <el-button v-if="canEditGuardian" v-hasPermi="['person.student_guardian:update']" icon="Edit" @click="handleEditGuardian">
                编辑监护人信息
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="guardians" border>
          <el-table-column label="监护人姓名" prop="guardianName" min-width="120" />
          <el-table-column label="与学生关系" prop="relation" width="110" align="center" />
          <el-table-column label="监护人电话" prop="guardianPhone" width="140">
            <template #default="scope">
              <span class="mono">{{ scope.row.guardianPhone || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="主要联系人" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isPrimary" type="success" size="small">默认</el-tag>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column v-if="canEditGuardian" label="操作" width="90" align="center" data-layout-group="操作">
            <template #default="scope">
              <el-button v-hasPermi="['person.student_guardian:update']" link type="primary" @click="handleUnbindGuardian(scope.row)">
                解绑
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无监护人信息" :image-size="60" />
          </template>
        </el-table>
        <div class="hint">
          监护人手机号不单独作为登录名；一个家长可关联多个孩子，绑定上限 3；解绑需班主任确认，同一字段同时只允许一条待审核（GAP-018）。
        </div>
      </el-card>

      <el-card shadow="never" class="mt-3">
        <template #header>
          <div class="flex justify-between items-center">
            <span data-layout-group="变更记录">变更记录</span>
            <el-tag size="small" type="info">追加式，不可删除</el-tag>
          </div>
        </template>
        <el-timeline v-if="changeLogs.length">
          <el-timeline-item v-for="item in changeLogs" :key="item.changeId" :timestamp="proxy?.parseTime(item.changeTime)">
            <b>{{ item.changeType }}</b>
            <el-tag v-if="item.changeTag" size="small" class="ml-2">{{ item.changeTag }}</el-tag>
            <div class="text-xs mt-1">{{ item.summary }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else-if="!loading" description="暂无变更记录" :image-size="60" />
      </el-card>
    </div>

    <template #footer>
      <el-button v-hasPermi="['person.student:update']" @click="handleEdit">编辑</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>

    <!-- 编辑监护人信息：班主任 / 教务主任为写入口 -->
    <el-dialog v-model="guardianDialog.visible" title="编辑监护人信息" width="860px" append-to-body>
      <guardian-table ref="guardianTableRef" />
      <template #footer>
        <el-button @click="guardianDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="guardianDialog.saving" @click="handleSaveGuardian">保存</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getStudent,
  listStudentChangeLog,
  listStudentGuardian,
  saveStudentGuardian,
  unbindStudentGuardian,
  viewStudentIdCard,
  viewStudentPhone
} from '@/api/edu/student';
import type { GuardianVO, StudentChangeLogVO, StudentVO } from '@/api/edu/student/types';
import { ENROLLMENT_STATUS_LABEL, STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';
import GuardianTable from './GuardianTable.vue';
import { checkRole } from '@/utils/permission';

const emit = defineEmits<{ edit: [student: StudentVO] }>();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const visible = ref(false);
const loading = ref(false);
const detail = ref<Partial<StudentVO>>({});
const guardians = ref<GuardianVO[]>([]);
const changeLogs = ref<StudentChangeLogVO[]>([]);
/** 一次会话内揭示的证件号全量值；不落库、不缓存 */
const idCardFull = ref('');
/** 一次会话内揭示的联系电话全量值；不落库、不缓存 */
const phoneFull = ref('');
const guardianTableRef = ref<InstanceType<typeof GuardianTable>>();
const guardianDialog = ref({ visible: false, saving: false });

/** 监护人写入口：教务主任 / 班主任（与原型 data-role-visible 一致） */
const canEditGuardian = computed(() => checkRole(['academic_director', 'homeroom']) || checkRole(['super_admin']));

const enrollmentStatusLabel = computed(() => ENROLLMENT_STATUS_LABEL[detail.value.enrollmentStatus ?? ''] ?? '—');

const open = async (studentId: string) => {
  visible.value = true;
  loading.value = true;
  idCardFull.value = '';
  phoneFull.value = '';
  try {
    const [student, guardianList, logs] = await Promise.all([getStudent(studentId), listStudentGuardian(studentId), listStudentChangeLog(studentId)]);
    detail.value = student.data ?? {};
    guardians.value = guardianList.data ?? [];
    changeLogs.value = logs.data ?? [];
  } catch {
    detail.value = {};
    guardians.value = [];
    changeLogs.value = [];
  } finally {
    loading.value = false;
  }
};

const handleRevealIdCard = async () => {
  const studentId = detail.value.studentId;
  if (!studentId) {
    return;
  }
  const res = await viewStudentIdCard(studentId);
  idCardFull.value = res.data?.idCardNo ?? '';
  ElMessage.success('已展示完整证件号，本次查看已写入敏感数据访问日志');
};

const handleRevealPhone = async () => {
  const studentId = detail.value.studentId;
  if (!studentId) {
    return;
  }
  const res = await viewStudentPhone(studentId);
  phoneFull.value = res.data?.studentPhone ?? '';
  ElMessage.success('已展示完整联系电话，本次查看已写入敏感数据访问日志');
};

const handleEdit = () => {
  emit('edit', detail.value as StudentVO);
};

const loadGuardians = async () => {
  const studentId = detail.value.studentId;
  if (!studentId) {
    guardians.value = [];
    return;
  }
  const res = await listStudentGuardian(studentId);
  guardians.value = res.data ?? [];
};

const handleEditGuardian = () => {
  guardianDialog.value.visible = true;
  guardianTableRef.value?.setRows(guardians.value.map((item) => ({ ...item })));
};

const handleSaveGuardian = async () => {
  const studentId = detail.value.studentId;
  if (!studentId) {
    return;
  }
  guardianDialog.value.saving = true;
  try {
    for (const guardian of guardianTableRef.value?.getRows() ?? []) {
      await saveStudentGuardian(studentId, guardian);
    }
    ElMessage.success('已保存监护人信息');
    guardianDialog.value.visible = false;
    await loadGuardians();
  } finally {
    guardianDialog.value.saving = false;
  }
};

/** 解绑：二次确认，文案含对象名（AGENTS 第 3 节：危险操作必须二次确认） */
const handleUnbindGuardian = (row: GuardianVO) => {
  const studentId = detail.value.studentId;
  if (!studentId) {
    return;
  }
  ElMessageBox.confirm(`确认解绑监护人「${row.guardianName}」？解绑需班主任确认，未通过可重提。`, '解绑确认', {
    confirmButtonText: '提交解绑申请',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await unbindStudentGuardian(studentId, row.guardianId);
      ElMessage.success('已提交解绑申请');
      await loadGuardians();
    })
    .catch(() => undefined);
};

defineExpose({ open });
</script>
