/** 学科下拉项（供各模块高频调用，带缓存） */
export interface SubjectOptionVO {
  subjectCode: string;
  subjectName: string;
  stageCode?: string;
  /** 是否参与 3+1+2：none / primary / secondary */
  streamRole?: string;
  status?: string;
}

/** 学科列表行 */
export interface SubjectVO {
  subjectId: string;
  subjectCode: string;
  subjectName: string;
  /** 启用学段（多个以逗号分隔，如 primary,junior,senior） */
  enabledStages?: string;
  /** 是否参与 3+1+2 */
  streamEnabled?: boolean;
  /** 选科角色：none / primary（首选）/ secondary（再选） */
  streamRole?: string;
  /** 排序号 */
  sortNo?: number;
  status?: string;
}

/** 学科查询参数 */
export interface SubjectQuery extends Partial<PageQuery> {
  /** 学科名称 / 编码关键字 */
  keyword?: string;
  stageCode?: string;
  streamRole?: string;
  status?: string;
}
