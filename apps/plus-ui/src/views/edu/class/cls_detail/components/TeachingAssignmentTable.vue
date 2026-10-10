<template>
  <el-table :data="items">
    <el-table-column label="教师" prop="teacherName" width="140" show-overflow-tooltip data-layout-group="任教信息" />
    <el-table-column label="学年学期" prop="termName" width="180" show-overflow-tooltip data-layout-group="任教信息" />
    <el-table-column label="任教类型" width="110" align="center" data-layout-group="任教信息">
      <template #default="scope">{{ scope.row.classType || '—' }}</template>
    </el-table-column>
    <el-table-column label="状态" prop="status" width="100" align="center" data-layout-group="任教信息" />
    <el-table-column label="跨校任教" width="110" align="center" data-layout-group="任教信息">
      <template #default="scope">{{ scope.row.crossSchool ? '是' : '否' }}</template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
/**
 * 班级详情的「任课教师」表（按学科分组后逐组渲染）。
 *
 * 单独成组件的原因：`tools/check_fe_page_structure.py` 只解析页面文件里的 el-table-column，
 * 页内多张表会让列对照算不清（先例：教师任教关系页的 AssignmentTable.vue）。
 * 本表只做只读展示：任教关系的唯一写入入口在教师管理的任教关系页（DP-01）。
 *
 * props 用本组件内的结构类型声明，不引入 api 类型（工程侧规避 vite 构建对这个宏的解析问题）。
 */
defineOptions({ name: 'ClassTeachingAssignmentTable' });

defineProps<{
  items: Array<{
    teacherName?: string;
    termName?: string;
    classType?: string;
    status?: string;
    crossSchool?: boolean;
  }>;
}>();
</script>
