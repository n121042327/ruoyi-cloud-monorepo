/** 班级（班级管理模块） */
export interface ClassVO {
  classId: string;
  className: string;
  schoolId: string;
  schoolName?: string;
  campusId?: string;
  campusName?: string;
  gradeId: string;
  gradeName?: string;
  stageCode?: string;
  classType?: string;
  headTeacherId?: string;
  headTeacherName?: string;
  classroom?: string;
  classCapacity?: number;
  studentCount?: number;
  /** 班级状态：正常 / 已停用（停用班级不可作为调班目标） */
  status?: string;
  /** 在读人数与容量（仅提示，不阻塞） */
  enrolledCount?: number;
  capacity?: number;
  termId?: string;
  remark?: string;
}

/** 班级查询参数 */
export interface ClassQuery extends Partial<PageQuery> {
  schoolId?: string;
  campusId?: string;
  termId?: string;
  gradeId?: string;
  classType?: string;
  headTeacherId?: string;
  keyword?: string;
}

/** 班级新增 / 编辑表单（必填：学年学期、年级、班级名称、班级类型） */
export interface ClassForm {
  classId?: string;
  termId: string;
  gradeId: string;
  className: string;
  classType: string;
  headTeacherId?: string;
  campusId?: string;
  classroom?: string;
  classCapacity?: number;
  remark?: string;
}

/**
 * 调班 / 批量迁学生（POST /edu/class/roster/transfer）
 *
 * 单条 = 调班，多条 = 批量迁移（D-067）；学生侧调班复用该接口（CR-029 / DP-01）。
 */
export interface ClassTransferForm {
  /** 学生 ID（批量时忽略，改用 studentIds） */
  studentId?: string;
  /** 批量迁学生时的学生 ID 列表 */
  studentIds?: string[];
  /** 目标班级 ID（字段字典 target_class_id，必填） */
  targetClassId: string;
  /** 生效日期（effective_date，必填） */
  effectiveDate: string;
  /** 调班原因（remark） */
  remark?: string;
}
