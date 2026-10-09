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
/**
 * 任教关系行（PAGE-TCH-ASSIGN）
 *
 * 一个班级 + 学科 + 教师 + 学年学期构成一条任教关系；教学班与行政班都要能挂任教关系。
 */
export interface TeachingAssignmentVO {
  assignmentId: string;
  /** 学科编码与名称 */
  subjectCode?: string;
  subjectName?: string;
  teacherId?: string;
  teacherName?: string;
  classId?: string;
  className?: string;
  /** 班级类型：行政班 / 教学班 */
  classType?: string;
  /** 周课时 */
  weeklyHours?: number;
  /** 生效期间 */
  startDate?: string;
  endDate?: string;
  /** 状态：生效中 / 待补 / 已结束 */
  status?: string;
  termId?: string;
  termName?: string;
  /** 是否跨校任教（跨校需运营方授权且只读） */
  crossSchool?: boolean;
}

/** 任教关系查询参数（PAGE-TCH-ASSIGN 查询区） */
export interface TeachingAssignmentQuery extends Partial<PageQuery> {
  /** 视角：class（按班级）/ teacher（按教师） */
  view?: string;
  termId?: string;
  keyword?: string;
  subjectId?: string;
  classId?: string;
  teacherId?: string;
}

/** 新增 / 编辑任教关系（saveTeachingAssignment） */
export interface TeachingAssignmentForm {
  assignmentId?: string;
  termId: string;
  subjectId: string;
  teacherId: string;
  classType: string;
  classId: string;
  weeklyHours?: number;
  startDate?: string;
  endDate?: string;
}

/** 复制上一学年任教关系（copyTeachingAssignment） */
export interface TeachingAssignmentCopyForm {
  sourceTermId: string;
  targetTermId: string;
}

/** 教育角色分配表单（POST /edu/teacher/{id}/role） */
export interface TeacherRoleForm {
  userId?: string;
  teacherId?: string;
  /** 教育角色编码（05-permission-matrix.yaml 的 edu_role） */
  eduRole: string;
  startDate?: string;
  endDate?: string;
  reason?: string;
}

/** 教师教育角色行（GET /edu/teacher/{id}/role） */
export interface TeacherRoleVO {
  userRoleId: string;
  schoolId?: string;
  userId?: string;
  userName?: string;
  teacherId?: string;
  teacherName?: string;
  eduRole?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}
