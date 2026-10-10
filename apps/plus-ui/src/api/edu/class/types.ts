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
  termName?: string;
  headTeacherStartDate?: string;
  headTeacherEndDate?: string;
  classStatus?: string;
  subjectCombination?: string;
  updateTime?: string;
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

/**
 * 班级花名册行（PAGE-CLS-DETAIL / PAGE-CLS-MOVE 共用）
 *
 * 学生主体是平台级实体，班级关系落在在校记录与班级关系两段式上；
 * 花名册只暴露必要字段，`joinCheck` / `moveCheck` 为加入与迁移的校验结论。
 */
export interface ClassRosterVO {
  studentId: string;
  studentNo?: string;
  studentName?: string;
  gender?: string;
  /** 学籍状态：在读 / 休学 / 转入未报到 / 出国保留学籍 等 */
  enrollmentStatus?: string;
  /** 当前行政班 */
  currentClassId?: string;
  currentClassName?: string;
  /** 加入日期（花名册默认按加入日期倒序） */
  joinDate?: string;
  /** 监护人姓名（PAGE-CLS-DETAIL，按角色控制可见性，GAP-104） */
  guardianName?: string;
  /** 监护人联系电话（**默认掩码**，全量查看走学生详情） */
  guardianPhone?: string;
  /** 离开日期（移出后保留历史） */
  leaveDate?: string;
  /** 关系状态：在读 / 已移出 */
  status?: string;
  /** 加入校验结论（PAGE-CLS-ROSTER-ADD） */
  joinCheck?: string;
  /** 迁移校验结果（PAGE-CLS-MOVE） */
  moveCheck?: string;
}

/** 花名册查询参数 */
export interface ClassRosterQuery extends Partial<PageQuery> {
  /** 关系状态：studying = 只看在读成员 */
  status?: string;
  /** 学号 / 姓名关键字 */
  keyword?: string;
  enrollmentStatus?: string;
  /** 生效日期（调班 / 迁移按学年追加，必填） */
  effectiveDate?: string;
}

/** 添加学生到行政班（POST /edu/class/{id}/roster，权限 org.class:update） */
export interface ClassRosterAddForm {
  /** 目标班级 ID */
  classId: string;
  /** 选中的学生 ID 列表 */
  studentIds: string[];
  /** 生效日期 */
  effectiveDate: string;
  /** 加入说明 */
  remark?: string;
}

/**
 * 教学班（PAGE-CLS-TEACHING）
 *
 * 行政班与教学班是两套独立关系，选科组合不等于行政班；教学班的创建入口唯一在
 * 「按组合生成教学班」向导（CR-017 裁决），本页只做查询、详情与停用。
 */
export interface TeachingClassVO {
  classId: string;
  /** 教学班名称，如「高一 · 物化生 A 层」 */
  className: string;
  termId?: string;
  termName?: string;
  gradeId?: string;
  gradeName?: string;
  /** 组合 / 学科，如「物理 + 化学 + 生物」 */
  subjectCombination?: string;
  /** 成员数 */
  memberCount?: number;
  /** 任课教师（只读展示） */
  teacherName?: string;
  /** 状态：正常 / 已停用 */
  status?: string;
  remark?: string;
}

/** 教学班查询参数（PAGE-CLS-TEACHING 查询区） */
export interface TeachingClassQuery extends Partial<PageQuery> {
  termId?: string;
  gradeId?: string;
  /** 组合 / 学科 */
  combination?: string;
  status?: string;
  teacherId?: string;
}

/**
 * 班级合并（PAGE-CLS-MERGE，mergeClass）
 *
 * 把多个源班级并入一个目标班级：源班级的学生在目标班级下新增在班关系，
 * 源班级本身置为已停用（不物理删除，历史数据保留）。GAP-088 取推荐方案：
 * 页面树未提供原型，按接口做一个精简合并向导。
 */
export interface ClassMergeForm {
  /** 源班级 ID 列表（至少 1 个，不能包含目标班级） */
  sourceClassIds: string[];
  /** 目标班级 ID */
  targetClassId: string;
  /** 生效日期 */
  effectiveDate: string;
  /** 合并说明 */
  remark?: string;
}
