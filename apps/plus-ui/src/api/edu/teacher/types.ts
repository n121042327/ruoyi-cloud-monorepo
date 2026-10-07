/** 教师（教师管理模块） */
export interface TeacherVO {
  teacherId: string;
  teacherName: string;
  teacherNo?: string;
  schoolId?: string;
  /** 在职状态：在职 / 离职 / 调离 */
  employmentStatus?: string;
}

/** 教师查询参数 */
export interface TeacherQuery extends Partial<PageQuery> {
  schoolId?: string;
  keyword?: string;
}
