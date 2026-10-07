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
