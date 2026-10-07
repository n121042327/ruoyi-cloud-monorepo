<template>
  <el-card shadow="hover">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span>任教关系</span>
          <el-tag type="success" size="small" v-if="editable">可编辑</el-tag>
          <el-tag type="info" size="small" v-else>只读</el-tag>
          <span class="text-xs">共 {{ total }} 条</span>
        </div>
        <div class="flex gap-2">
          <el-button v-if="editable" v-hasPermi="['person.teaching_assignment:create']" type="primary" plain icon="Plus" @click="emit('create')">
            新增任教关系
          </el-button>
          <el-button :loading="loading" @click="emit('refresh')">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" border :data="list">
      <el-table-column label="学科" prop="subjectName" width="110" data-layout-group="任教信息" />
      <el-table-column label="任教教师" prop="teacherName" min-width="170" data-layout-group="任教信息">
        <template #default="scope">
          {{ scope.row.teacherName }}
          <el-tag v-if="scope.row.crossSchool" class="ml-1" type="warning" size="small">跨校</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="班级" prop="className" min-width="180" data-layout-group="任教信息" />
      <el-table-column label="班级类型" prop="classType" width="110" align="center" data-layout-group="任教信息" />
      <el-table-column label="周课时" prop="weeklyHours" width="90" align="center" data-layout-group="任教信息" />
      <el-table-column label="状态" prop="status" width="110" align="center" data-layout-group="任教信息">
        <template #default="scope">
          <el-tag :type="scope.row.status === '生效中' ? 'success' : 'info'" size="small">{{ scope.row.status || '生效中' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150" data-layout-group="操作">
        <template #default="scope">
          <el-button v-if="editable" v-hasPermi="['person.teaching_assignment:create']" link type="primary" @click="emit('edit', scope.row)">
            编辑
          </el-button>
          <el-button v-if="editable" v-hasPermi="['person.teaching_assignment:create']" link type="danger" @click="emit('remove', scope.row)">
            结束任教
          </el-button>
          <span v-if="!editable" class="text-xs text-gray-400">只读</span>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="没有查询到任教关系" />
      </template>
    </el-table>

    <pagination v-if="total > 0" v-model:total="total" v-model:page="pageNum" v-model:limit="pageSize" @pagination="emit('refresh')" />
  </el-card>
</template>

<script setup lang="ts">
import type { TeachingAssignmentVO } from '@/api/edu/teacher/types';

defineProps<{
  list?: TeachingAssignmentVO[];
  loading?: boolean;
  editable?: boolean;
}>();

const total = defineModel<number>('total', { default: 0 });
const pageNum = defineModel<number>('pageNum', { default: 1 });
const pageSize = defineModel<number>('pageSize', { default: 20 });

const emit = defineEmits<{
  create: [];
  edit: [row: TeachingAssignmentVO];
  remove: [row: TeachingAssignmentVO];
  refresh: [];
}>();
</script>
