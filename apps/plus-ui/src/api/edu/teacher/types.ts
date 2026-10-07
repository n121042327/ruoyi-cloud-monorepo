/** 教师（教师管理模块） */
export interface TeacherVO {
  teacherId: string;
  teacherName: string;
  teacherNo?: string;
  gender?: string;
  schoolId?: string;
  schoolName?: string;
  /** 教育角色（多个以顿号分隔） */
  eduRoles?: string;
  /** 任教学科（多个以顿号分隔） */
  subjectNames?: string;
  /** 任课班级数 */
  teachingClassCount?: number;
  /** 联系电话（掩码） */
  teacherPhone?: string;
  /** 在职状态：在职 / 离职 / 调离 */
  employmentStatus?: string;
}

/** 教师查询参数 */
export interface TeacherQuery extends Partial<PageQuery> {
  schoolId?: string;
  gradeId?: string;
  classId?: string;
  subjectCode?: string;
  eduRole?: string;
  employmentStatus?: string;
  keyword?: string;
}

/** 教师新增 / 编辑表单 */
export interface TeacherForm {
  teacherId?: string;
  teacherNo: string;
  teacherName: string;
  gender: string;
  schoolId: string;
  teacherPhone?: string;
  email?: string;
  entryDate?: string;
  remark?: string;
}
