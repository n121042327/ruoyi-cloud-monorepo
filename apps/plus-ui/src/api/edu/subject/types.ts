/** 学科下拉项（供各模块高频调用，带缓存） */
export interface SubjectOptionVO {
  subjectCode: string;
  subjectName: string;
  stageCode?: string;
  /** 是否参与 3+1+2：none / primary / secondary */
  streamRole?: string;
  status?: string;
}
