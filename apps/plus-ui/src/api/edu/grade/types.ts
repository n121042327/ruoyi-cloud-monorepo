/** 年级（年级管理模块） */
export interface GradeVO {
  gradeId: string;
  gradeName: string;
  schoolId: string;
  stageCode: string;
  enrollYear?: string;
  /** 学段内序号 */
  gradeLevel?: number;
  /** 年级主任姓名（多个以顿号分隔） */
  leaderNames?: string;
  leaderUserId?: string;
  /** 统计值与状态 */
  classCount?: number;
  studentCount?: number;
  gradeStatus?: string;
}

/** 年级查询参数 */
export interface GradeQuery extends Partial<PageQuery> {
  schoolId?: string;
  stageCode?: string;
  enrollYear?: string;
  leaderUserId?: string;
}

/** 年级新增 / 编辑表单 */
export interface GradeForm {
  gradeId?: string;
  schoolId: string;
  stageCode: string;
  enrollYear: string;
  /** 学段内序号：小学 1-6、初中 1-3、高中 1-3（REQ-GRD-012） */
  gradeLevel: number;
  gradeName: string;
}
