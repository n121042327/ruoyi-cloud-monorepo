/** 班级（班级管理模块） */
export interface ClassVO {
  classId: string;
  className: string;
  schoolId: string;
  gradeId: string;
  stageCode?: string;
  classType?: string;
}

/** 班级查询参数 */
export interface ClassQuery extends Partial<PageQuery> {
  schoolId?: string;
  gradeId?: string;
}
