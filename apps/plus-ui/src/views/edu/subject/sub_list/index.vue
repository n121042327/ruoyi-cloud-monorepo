<template>
  <div class="p-2">
    <el-card v-if="!canRead" shadow="hover">
      <el-empty description="当前账号没有学科配置的查看权限" />
    </el-card>

    <template v-else>
      <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
        <div v-show="showSearch" class="mb-[10px]">
          <el-card shadow="hover">
            <!-- 查询区分组：基础信息（学科名称 / 编码）→ 教育信息（学段）→ 配置信息（选科角色 → 状态） -->
            <el-form ref="queryFormRef" :model="queryParams" :inline="true">
              <el-form-item label="学科名称 / 编码" prop="keyword" data-layout-group="基础信息">
                <el-input v-model="queryParams.keyword" placeholder="请输入" clearable style="width: 180px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="学段" prop="stageCode" data-layout-group="教育信息">
                <el-select v-model="queryParams.stageCode" placeholder="全部学段" clearable style="width: 130px">
                  <el-option v-for="item in stageOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="选科角色" prop="streamRole" data-layout-group="配置信息">
                <el-select v-model="queryParams.streamRole" placeholder="全部角色" clearable style="width: 130px">
                  <el-option v-for="item in STREAM_ROLE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="status" data-layout-group="配置信息">
                <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
                  <el-option label="启用" value="enabled" />
                  <el-option label="停用" value="disabled" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </transition>

      <el-card shadow="hover">
        <template #header>
          <el-row :gutter="10">
            <right-toolbar v-model:show-search="showSearch" :columns="columns" :search="true" @query-table="getList"></right-toolbar>
          </el-row>
        </template>

        <el-table v-loading="loading" border :data="subjectList">
          <el-table-column v-if="columns[0].visible" label="学科名称" prop="subjectName" width="150" data-layout-group="基础信息" />
          <el-table-column v-if="columns[1].visible" label="启用学段" prop="enabledStages" min-width="180" data-layout-group="教育信息">
            <template #default="scope">{{ stageLabel(scope.row.enabledStages) || '未启用' }}</template>
          </el-table-column>
          <el-table-column v-if="columns[2].visible" label="参与 3+1+2" prop="streamEnabled" width="120" align="center" data-layout-group="配置信息">
            <template #default="scope">
              <el-tag :type="scope.row.streamEnabled ? 'success' : 'info'" size="small">{{ scope.row.streamEnabled ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="columns[3].visible" label="选科角色" prop="streamRole" width="120" align="center" data-layout-group="配置信息">
            <template #default="scope">
              {{ STREAM_ROLE_LABEL[scope.row.streamRole ?? 'none'] ?? '不参与' }}
            </template>
          </el-table-column>
          <el-table-column v-if="columns[4].visible" label="排序号" prop="sortNo" width="90" align="center" data-layout-group="配置信息" />
          <el-table-column v-if="columns[5].visible" label="状态" prop="status" width="100" align="center" data-layout-group="配置信息">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'disabled' ? 'info' : 'success'" size="small">
                {{ scope.row.status === 'disabled' ? '停用' : '启用' }}
              </el-tag>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="没有查询到学科数据" />
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
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref } from 'vue';
import { STREAM_ROLE_OPTIONS, stageLabel, useSubjectList } from './composables/useSubjectList';
import { checkPermi } from '@/utils/permission';

defineOptions({ name: 'EduSubjectList' });

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const { loading, showSearch, total, subjectList, queryParams, columns, stageOptions, getList, handleQuery, resetQuery } = useSubjectList();

const queryFormRef = ref();
const canRead = computed(() => checkPermi(['org.subject:read']));

const STREAM_ROLE_LABEL: Record<string, string> = STREAM_ROLE_OPTIONS.reduce(
  (acc, item) => {
    acc[item.value] = item.label;
    return acc;
  },
  {} as Record<string, string>
);
</script>
