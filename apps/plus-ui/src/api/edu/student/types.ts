/**
 * 学生管理接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 student 模块；
 * 查询参数与 PRD「8.1 listStudent 查询参数」逐条对齐（CR-035 / GAP-066）。
 * 主键一律按字符串处理（后端 Long 序列化为字符串，见 apps/plus-ui/AGENTS.md 第 5 节）。
 */

/** 学生列表查询参数 */
export interface StudentQuery extends PageQuery {
  /** 学校上下文；学校用户固定本校，平台运营与超级管理员可切换 */
  schoolId?: string;
  /** 学年学期 */
  termId?: string;
  /** 年级 */
  gradeId?: string;
  /** 班级 */
  classId?: string;
  /** 学籍状态，多值以逗号分隔 */
  enrollmentStatus?: string;
  /** 性别 */
  gender?: string;
  /** 入学年份 */
  enrollYear?: string;
  /** 学段 */
  stageCode?: string;
  /** 关键字，命中范围：学号 / 姓名 / 全国学籍号 */
  keyword?: string;
  /** 证件号后四位，需 person.student:read_sensitive */
  idCardSuffix?: string;
  /** 排序字段：studentNo / studentName / enrollYear / className / updateTime */
  sortBy?: string;
  /** 排序方向：asc / desc */
  sortOrder?: string;
}

/** 学生列表行 */
export interface StudentVO extends BaseEntity {
  /** 学生主体 ID */
  studentId: string;
  /** 学号（系统统一发号，永不回收） */
  studentNo: string;
  /** 全国学籍号 */
  nationalStudentNo?: string;
  /** 姓名 */
  studentName: string;
  /** 性别 */
  gender: string;
  /** 入学年份 */
  enrollYear: string;
  /** 学段 */
  stageCode: string;
  /** 年级 */
  gradeId: string;
  /** 年级名称 */
  gradeName: string;
  /** 班级 */
  classId: string;
  /** 班级名称 */
  className: string;
  /** 学籍状态 */
  enrollmentStatus: string;
  /** 证件号（后端返回掩码值；明文经 viewStudentIdCard 单独获取） */
  idCardNo?: string;
  /** 证件类型 */
  idType?: string;
  /** 出生日期 */
  birthDate?: string;
  /** 联系地址 */
  address?: string;
  /** 联系电话（默认掩码展示） */
  studentPhone: string;
  /** 学生照片文件 ID */
  photoFileId?: string;
}

/** 学生新增 / 编辑表单（对应原型 PAGE-STU-CREATE 的三步：学籍信息 / 证件与联系 / 监护人） */
export interface StudentForm {
  studentId?: string;
  /** 全国学籍号，可空；以 G / L 开头 */
  nationalStudentNo?: string;
  studentName: string;
  gender: string;
  enrollYear: string;
  stageCode: string;
  gradeId: string;
  classId?: string;
  /** 证件信息 */
  idType?: string;
  idCardNo?: string;
  birthDate?: string;
  /** 联系方式 */
  studentPhone?: string;
  address?: string;
  /** 学生照片文件 ID（上传接口在阶段 6 后续批次） */
  photoFileId?: string;
}

/** 监护人（上限 3，对应 REQ-STU-022） */
export interface GuardianForm {
  guardianId?: string;
  guardianName: string;
  relation: string;
  guardianPhone?: string;
  /** 是否主要联系人；同一学生最多一条 */
  isPrimary?: boolean;
  remark?: string;
}

/** 监护人列表行 */
export interface GuardianVO extends GuardianForm {
  guardianId: string;
  /** 手机号掩码 */
  guardianPhoneMasked?: string;
}

/** 学生变更记录（来自审计模块，只读） */
export interface StudentChangeLogVO {
  changeId: string;
  /** 变更类型：班级变更 / 学籍状态 / 监护人变更 等 */
  changeType: string;
  /** 变更标签，如「调班」「在读 → 在读」 */
  changeTag?: string;
  /** 变更摘要，含前后值与操作人 */
  summary: string;
  operatorName?: string;
  changeTime: string;
}

/** 当前状态可执行的异动选项（GET /edu/student/{id}/status-options） */
/** 在校记录（`edu_student_enrollment`；GET /edu/student/{id}/enrollment） */
export interface StudentEnrollmentVO {
  enrollmentId?: string;
  schoolId?: string;
  studentId?: string;
  /** 入学日期 */
  enrollDate?: string;
  /** 学籍状态（码值见 enrollment_status 枚举） */
  enrollmentStatus?: string;
  /** 状态生效日期 */
  statusEffectiveDate?: string;
  /** 离校日期 */
  leaveDate?: string;
  campusId?: string;
  entryGradeId?: string;
  remark?: string;
}

export interface EnrollmentStatusOptionVO {
  /** 异动动作码，如 suspend / abroad / missing / transfer_out / withdraw */
  value: string;
  /** 展示文案，含状态流转，如「休学（在读 → 休学）」 */
  label: string;
  /** 是否需要校级管理员审批 */
  needApproval?: boolean;
  /** 是否必须指定复学 / 报到后的班级 */
  needClass?: boolean;
  /** 当前状态下不可用（如义务教育阶段的开除） */
  disabled?: boolean;
  /** 不可用原因 */
  disabledReason?: string;
}

/** 学籍异动提交（POST /edu/student/{id}/enrollment-change） */
export interface EnrollmentChangeForm {
  /** 异动类型（字段字典 change_type，必填） */
  changeType: string;
  /** 生效日期（effective_date，必填） */
  effectiveDate: string;
  /** 复学 / 报到后的班级（class_id，条件必填） */
  classId?: string;
  /** 原因（reason，必填，至少 5 个字） */
  reason: string;
}
