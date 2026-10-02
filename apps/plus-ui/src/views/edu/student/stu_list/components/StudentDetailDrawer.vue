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
            <span class="mono">{{ detail.studentPhone || '—' }}</span>
            <!-- 联系电话明文查看的接口契约未在阶段 4 登记（原型 data-api="-"），见 GAP-084；本批保持掩码。 -->
            <el-tag size="small" type="warning" class="ml-2">明文查看待契约（GAP-084）</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="联系地址">{{ detail.address || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="mt-3">
        <template #header>
          <div class="flex justify-between items-center">
            <span data-layout-group="监护人">监护人（上限 3）</span>
            <el-tag size="small" type="info">班主任为唯一写入口</el-tag>
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
        </el-table>
        <el-empty v-if="!guardians.length && !loading" description="暂无监护人信息" :image-size="60" />
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
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getStudent, listStudentChangeLog, listStudentGuardian, viewStudentIdCard } from '@/api/edu/student';
import type { GuardianVO, StudentChangeLogVO, StudentVO } from '@/api/edu/student/types';
import { ENROLLMENT_STATUS_LABEL, STAGE_CODE_LABEL } from '@/enums/edu/StudentEnum';

const emit = defineEmits<{ edit: [student: StudentVO] }>();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const visible = ref(false);
const loading = ref(false);
const detail = ref<Partial<StudentVO>>({});
const guardians = ref<GuardianVO[]>([]);
const changeLogs = ref<StudentChangeLogVO[]>([]);
/** 一次会话内揭示的证件号全量值；不落库、不缓存 */
const idCardFull = ref('');

const enrollmentStatusLabel = computed(() => ENROLLMENT_STATUS_LABEL[detail.value.enrollmentStatus ?? ''] ?? '—');

const open = async (studentId: string) => {
  visible.value = true;
  loading.value = true;
  idCardFull.value = '';
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

const handleEdit = () => {
  emit('edit', detail.value as StudentVO);
};

defineExpose({ open });
</script>
