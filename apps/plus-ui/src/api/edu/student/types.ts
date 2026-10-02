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
  /** 联系电话（默认掩码展示） */
  studentPhone: string;
  /** 学生照片文件 ID */
  photoFileId?: string;
}

/** 学生新增 / 编辑表单（本批交付「基础信息 + 教育信息」两组；证件与联系、监护人属下一批） */
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
}
