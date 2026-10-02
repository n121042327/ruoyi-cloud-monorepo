/** 年级（年级管理模块） */
export interface GradeVO {
  gradeId: string;
  gradeName: string;
  schoolId: string;
  stageCode: string;
  enrollYear?: string;
}

/** 年级查询参数 */
export interface GradeQuery extends Partial<PageQuery> {
  schoolId?: string;
  stageCode?: string;
}
