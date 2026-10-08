<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-2">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <el-button link type="primary" @click="goDetail">← 返回班级详情</el-button>
          <h2 class="text-base font-medium">添加学生</h2>
          <el-tag type="info" size="small">学生班级归属的唯一写入入口是班级管理（DP-01）</el-tag>
        </div>
        <span class="text-xs">目标班级：{{ classId || '未指定' }}</span>
      </div>
    </el-card>

    <el-card shadow="hover" class="mb-2">
      <el-alert class="mb-3" type="info" :closable="false">
        <template #title>加入口径</template>
        <div>
          只有在读学生可以加入行政班；休学 / 转入未报到 / 出国保留学籍等状态会被「加入校验」拦下（<span class="mono">REQ-CLS-030</span>）。
          已在其他行政班的学生需先走调班或迁学生流程，不能重复挂班（<span class="mono">REQ-CLS-031</span>）。加入按学年追加，不改写历史（<span
            class="mono"
            >BR-PROMO-001</span
          >）。
        </div>
      </el-alert>

      <el-form :model="queryParams" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名 / 学号" data-layout-group="查询条件">
              <el-input v-model="queryParams.keyword" placeholder="输入姓名或学号" clearable @keyup.enter="getList" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="flex items-center gap-2 mb-4">
              <span class="text-xs">名单范围</span>
              <el-radio-group v-model="poolView" size="small">
                <el-radio-button label="available">本班可加入</el-radio-button>
                <el-radio-button label="all">全部学生</el-radio-button>
              </el-radio-group>
              <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table v-loading="loading" border :data="studentList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" :selectable="isSelectable" />
        <el-table-column label="学号" prop="studentNo" width="140" data-layout-group="学生信息" />
        <el-table-column label="姓名" prop="studentName" width="120" data-layout-group="学生信息" />
        <el-table-column label="性别" prop="gender" width="70" align="center" data-layout-group="学生信息" />
        <el-table-column label="学籍状态" prop="enrollmentStatus" width="130" align="center" data-layout-group="学生信息" />
        <el-table-column label="当前行政班" prop="currentClassName" min-width="150" data-layout-group="班级信息">
          <template #default="scope">{{ scope.row.currentClassName || '—' }}</template>
        </el-table-column>
        <el-table-column label="加入校验" prop="joinCheck" min-width="200" data-layout-group="校验信息">
          <template #default="scope">
            <el-tag v-if="scope.row.joinCheck" :type="scope.row.currentClassId ? 'danger' : 'success'" size="small">
              {{ scope.row.joinCheck }}
            </el-tag>
            <span v-else>可加入</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有查询到可加入的学生" />
        </template>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <div class="flex items-center justify-between mt-3">
        <span class="text-xs">已选 {{ selected.length }} 人</span>
        <div class="flex gap-2">
          <el-button @click="goDetail">取消</el-button>
          <el-button v-hasPermi="['org.class:update']" type="primary" :loading="submitting" :disabled="!selected.length" @click="handleSubmit">
            确认加入
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { addClassRoster } from '@/api/edu/class';
import type { ClassRosterVO } from '@/api/edu/class/types';
import { listStudent } from '@/api/edu/student';

defineOptions({ name: 'EduClassRosterAdd' });

const route = useRoute();
const router = useRouter();
const classId = String(route.query.classId ?? '');

const loading = ref(false);
const submitting = ref(false);
const poolView = ref<'available' | 'all'>('available');
const total = ref(0);
const studentList = ref<ClassRosterVO[]>([]);
const selected = ref<ClassRosterVO[]>([]);

const queryParams = reactive({ pageNum: 1, pageSize: 20, keyword: '' });

const isSelectable = (row: ClassRosterVO) => !row.currentClassId && !row.joinCheck;

const handleSelectionChange = (rows: ClassRosterVO[]) => {
  selected.value = rows;
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listStudent({ ...queryParams, classId: poolView.value === 'available' ? classId : '' });
    studentList.value = res.rows ?? [];
    total.value = res.total ?? 0;
  } finally {
    loading.value = false;
  }
};

const goDetail = () => router.push({ path: '/edu/class/list', query: classId ? { classId } : {} });

/** 确认加入：写班级关系，必须带生效日期与说明（REQ-CLS-030 / 031） */
const handleSubmit = async () => {
  if (!classId) {
    ElMessage.warning('缺少目标班级，请从班级详情进入');
    return;
  }
  const { value } = await ElMessageBox.prompt(`确认将选中的 ${selected.value.length} 名学生加入本班？请填写加入说明（会随操作写审计）`, '添加学生', {
    confirmButtonText: '确认加入',
    cancelButtonText: '取消',
    inputPlaceholder: '如：2026-2027 学年 第一学期编班结果',
    inputValidator: (text: string) => (text && text.trim().length >= 2 ? true : '请填写加入说明（至少 2 个字）')
  });
  submitting.value = true;
  try {
    await addClassRoster({
      classId,
      studentIds: selected.value.map((item) => item.studentId),
      effectiveDate: new Date().toISOString().slice(0, 10),
      remark: value
    });
    ElMessage.success('已加入本班：班级关系统一写入班级管理，学生管理只读展示');
    goDetail();
  } finally {
    submitting.value = false;
  }
};

onMounted(getList);
</script>
