/**
 * 学科下拉项（供各模块高频调用，带缓存）
 *
 * 对应后端 EduSubjectOptionVo。
 */
export interface SubjectOptionVO {
  subjectId: string;
  subjectCode: string;
  subjectName: string;
  /** 启用学段 */
  stageCodes?: string[];
  /** 选科角色：none / primary / secondary */
  streamRole?: string;
}

/**
 * 学科列表行
 *
 * 对应后端 EduSubjectVo；字段名以 schema.yaml 的 edu_subject 为准
 * （stream_enabled 是 char(1)，subject_status 取 active / disabled）。
 */
export interface SubjectVO {
  subjectId: string;
  schoolId?: string;
  subjectCode: string;
  subjectName: string;
  sortNo?: number;
  /** 是否参与 3+1+2：'1' 参与 / '0' 不参与 */
  streamEnabled?: string;
  /** 选科角色：none / primary / secondary */
  streamRole?: string;
  /** 学科状态：active / disabled */
  subjectStatus?: string;
  /** 启用学段（来自 edu_subject_stage） */
  stageCodes?: string[];
  updateTime?: string;
}

/** 学科新增 / 编辑表单 */
export interface SubjectForm {
  subjectId?: string;
  subjectCode: string;
  subjectName: string;
  sortNo?: number;
  streamEnabled?: string;
  stageCodes?: string[];
}

/** 学科查询参数（过滤字段名与后端 EduSubjectBo 一致） */
export interface SubjectQuery extends Partial<PageQuery> {
  /** 学科名称 / 编码关键字 */
  keyword?: string;
  stageCode?: string;
  filterStreamRole?: string;
  filterStatus?: string;
}

/** 学科引用检查结果（删除前校验：任教关系 / 教学班 / 学生选科） */
export interface SubjectReferenceVO {
  teachingAssignmentCount?: number;
  teachingClassCount?: number;
  studentStreamCount?: number;
  referenced?: boolean;
}
