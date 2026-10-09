<template>
  <div class="p-2">
    <!-- 页面标题区：对齐高保真 prototypes/high-fidelity/v1/pages/school-campus.html 的 .page-head -->
    <div class="page-head">
      <h1>校区管理</h1>
      <span class="scope-hint">数据范围：本校 · 可写</span>
      <el-tag type="primary">共 {{ total }} 个校区</el-tag>
    </div>
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有校区管理的查看权限" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button v-hasPermi="['org.school:read']" type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
          </el-col>
          <el-col :span="12">
            <span class="text-xs">当前学校：{{ schoolName || '—' }}</span>
          </el-col>
        </el-row>
      </template>

      <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="校区不参与数据权限判定（校领导与教务主任看到本校全部校区数据）；已被班级引用的校区不允许删除，只允许停用。"
      />

      <el-table v-loading="loading" border :data="campusList">
        <el-table-column label="校区名称" prop="campusName" min-width="180" data-layout-group="校区信息" />
        <el-table-column label="校区编码" prop="campusCode" width="130" data-layout-group="校区信息" />
        <el-table-column label="地址" prop="address" min-width="200" :show-overflow-tooltip="true" data-layout-group="校区信息" />
        <el-table-column label="负责人" prop="leader" width="120" data-layout-group="校区信息" />
        <el-table-column label="班级数" prop="classCount" width="90" align="center" data-layout-group="管理信息" />
        <el-table-column label="状态" prop="status" width="100" align="center" data-layout-group="管理信息">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'disabled' ? 'info' : 'success'" size="small">
              {{ scope.row.status === 'disabled' ? '已停用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120" data-layout-group="操作">
          <template #default="scope">
            <el-button v-hasPermi="['org.school:update']" link type="primary" @click="handleDisable(scope.row)">停用</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="本校暂无校区" />
        </template>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCurrentSchool, listCampus, removeCampus } from '@/api/edu/school';
import type { CampusVO } from '@/api/edu/school/types';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduSchoolCampus' });

const loading = ref(false);
const total = ref(0);
const campusList = ref<CampusVO[]>([]);
const schoolName = ref('');
const queryParams = reactive({ pageNum: 1, pageSize: 20 });

const canRead = computed(() => checkPermi(['org.school:read']));

const getList = async () => {
  loading.value = true;
  try {
    const school = await getCurrentSchool();
    schoolName.value = school.data?.schoolName ?? '';
    const schoolId = school.data?.schoolId;
    if (!schoolId) {
      campusList.value = [];
      total.value = 0;
      return;
    }
    const res = await listCampus(schoolId);
    campusList.value = res.data ?? [];
    total.value = campusList.value.length;
  } catch {
    campusList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

/** 停用：已被班级引用的校区不允许删除，只允许停用（REQ-SCH-030） */
const handleDisable = async (row: CampusVO) => {
  const { value } = await ElMessageBox.prompt(`确认停用校区「${row.campusName}」？已被班级引用的校区只允许停用，请填写原因。`, '停用校区', {
    confirmButtonText: '确认停用',
    cancelButtonText: '取消',
    inputPlaceholder: '停用原因（必填）',
    inputValidator: (v) => !!v || '请填写停用原因'
  });
  await removeCampus(row.campusId, value);
  ElMessage.success('已停用校区');
  await getList();
};

onMounted(getList);
</script>
