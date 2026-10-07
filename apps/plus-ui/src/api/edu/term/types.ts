/** 学年学期（学年学期模块） */
export interface TermVO {
  termId: string;
  termName: string;
  academicYearId?: string;
  academicYearName?: string;
  /** 学期开始日期（调班生效日期默认取当前学年学期开始日） */
  startDate?: string;
  endDate?: string;
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
