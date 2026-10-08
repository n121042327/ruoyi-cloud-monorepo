/** 学年学期（学年学期模块） */
export interface TermVO {
  termId: string;
  termName: string;
  academicYearId?: string;
  academicYearName?: string;
  /** 学期开始日期（调班生效日期默认取当前学年学期开始日） */
  startDate?: string;
  endDate?: string;
  /** 学期状态：进行中 / 已结束 / 已归档 */
  status?: string;
  /** 班级数与在读学生数（学期管理页展示） */
  classCount?: number;
  studentCount?: number;
  /** 是否为当前学年学期 */
  current?: boolean;
}

/** 学年列表行 */
export interface AcademicYearVO {
  academicYearId: string;
  /** 学年编码，格式 YYYY-YYYY（连续两个自然年） */
  academicYearCode: string;
  startDate?: string;
  endDate?: string;
  /** 学期数 */
  termCount?: number;
  /** 当前学年学期名称（同一学校唯一） */
  currentTermName?: string;
  /** 状态：进行中 / 已结束 / 已归档 */
  status?: string;
}

/** 学年查询参数 */
export interface AcademicYearQuery extends Partial<PageQuery> {
  schoolId?: string;
  academicYearCode?: string;
  status?: string;
}

/** 新建 / 编辑学年的表单体（PAGE-TERM-CREATE） */
export interface AcademicYearForm {
  academicYearId?: string;
  schoolId?: string;
  /** 学年编码，格式 YYYY-YYYY（连续两个自然年，校内唯一，REQ-TERM-008 / 009） */
  academicYearCode: string;
  startDate?: string;
  endDate?: string;
  /** 创建时一并生成的学期数：2（默认）或 1（REQ-TERM-012） */
  termCount?: number;
}

/** 学年引用检查结果（归档前展示，REQ-TERM-029 / 034） */
export interface AcademicYearReference {
  /** 班级数 */
  classCount?: number;
  /** 任教关系数 */
  teachingRelationCount?: number;
  /** 花名册人数 */
  rosterCount?: number;
  /** 选科人数 */
  subjectChoiceCount?: number;
  /** 是否已产生引用（有引用时只允许归档，不允许删除，REQ-TERM-028） */
  referenced?: boolean;
}
