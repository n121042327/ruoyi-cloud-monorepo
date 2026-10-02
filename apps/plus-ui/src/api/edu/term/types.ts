/** 学年学期（学年学期模块） */
export interface TermVO {
  termId: string;
  termName: string;
  academicYearId?: string;
  academicYearName?: string;
  /** 是否为当前学年学期 */
  current?: boolean;
}
