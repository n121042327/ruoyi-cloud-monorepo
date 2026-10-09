<template>
  <div>
    <el-table :data="rows" border>
      <el-table-column label="监护人姓名" min-width="140">
        <template #default="scope">
          <el-input v-model="scope.row.guardianName" placeholder="请输入姓名" />
        </template>
      </el-table-column>
      <el-table-column label="与学生关系" width="150">
        <template #default="scope">
          <el-select v-model="scope.row.relation" placeholder="请选择">
            <el-option v-for="item in GUARDIAN_RELATION_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="监护人电话" width="160">
        <template #default="scope">
          <el-input v-model="scope.row.guardianPhone" placeholder="可留空" maxlength="11" />
        </template>
      </el-table-column>
      <el-table-column label="主要联系人" width="110" align="center">
        <template #default="scope">
          <el-radio v-model="primaryIndex" :value="scope.$index" @change="handlePrimaryChange(scope.$index)">&nbsp;</el-radio>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="140">
        <template #default="scope">
          <el-input v-model="scope.row.remark" placeholder="可留空" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center" data-layout-group="操作">
        <template #default="scope">
          <el-button link type="danger" @click="removeRow(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无监护人信息" :image-size="60" />
      </template>
    </el-table>
    <el-button class="mt-2" :disabled="rows.length >= 3" icon="Plus" @click="addRow">添加监护人</el-button>
    <div class="hint mt-1">同一家长可关联多个孩子，绑定上限 3；监护人手机号是敏感字段，列表默认掩码展示。</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { GuardianForm } from '@/api/edu/student/types';
import { GUARDIAN_RELATION_OPTIONS } from '@/enums/edu/StudentEnum';

/**
 * 监护人编辑表格。
 *
 * 行数据由本组件持有，父组件通过 `setRows` / `getRows` 读写，避免直接修改 props
 * （apps/plus-ui/AGENTS.md 第 4 节：组件间数据流用 props + emits，不反向修改 props）。
 */
const rows = ref<GuardianForm[]>([]);
const primaryIndex = ref(-1);

const addRow = () => {
  if (rows.value.length >= 3) {
    ElMessage.warning('监护人数量上限为 3');
    return;
  }
  rows.value.push({ guardianName: '', relation: '', guardianPhone: '', isPrimary: false, remark: '' });
};

const removeRow = (index: number) => {
  rows.value.splice(index, 1);
  if (primaryIndex.value >= rows.value.length) {
    primaryIndex.value = rows.value.length - 1;
  }
};

/** 主要联系人在同一学生下唯一 */
const handlePrimaryChange = (index: number) => {
  rows.value.forEach((item, i) => {
    item.isPrimary = i === index;
  });
};

const setRows = (list: GuardianForm[]) => {
  rows.value = (list ?? []).map((item) => ({ ...item }));
  const index = rows.value.findIndex((item) => item.isPrimary);
  primaryIndex.value = index;
};

/** 校验并返回可提交的行；名称为空的行视为未填写，直接跳过 */
const getRows = (): GuardianForm[] => rows.value.filter((item) => item.guardianName && item.relation);

defineExpose({ setRows, getRows, addRow });
</script>
