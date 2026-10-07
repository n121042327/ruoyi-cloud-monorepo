/**
 * 选科与教学班模块的接口类型（3+1+2）。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 stream 模块；
 * 固定规则：首选科目只能是物理 / 历史（BV-STREAM-001 / BR-STREAM-001），
 * 再选科目从化学 / 生物 / 思想政治 / 地理中选满 2 门（BR-STREAM-002），共 12 种组合。
 */

/** 选科配置（PAGE-STR-CONFIG） */
export interface StreamConfigVO {
  termId?: string;
  termName?: string;
  /** 开放日期 */
  openFrom?: string;
  /** 截止时间（截止后学生只能提交变更申请，BR-STREAM-005） */
  deadline?: string;
  /** 逾期变更是否需校级管理员审批 */
  overdueRequiresApproval?: boolean;
  /** 开放期状态：未开始 / 进行中 / 已截止 */
  periodStatus?: string;
}

/** 保存选科配置（PUT /edu/stream/config，权限 stream.config:update） */
export interface StreamConfigForm {
  termId?: string;
  openFrom: string;
  deadline: string;
  overdueRequiresApproval: boolean;
}

/** 科目选项 */
export interface StreamSubjectOption {
  code: string;
  name: string;
}

/** 选科可选科目（固定集合，学校不可增减） */
export interface StreamOptionVO {
  primarySubjects: StreamSubjectOption[];
  secondarySubjects: StreamSubjectOption[];
  /** 再选需要选几门，固定 2 */
  secondaryRequired?: number;
}

/** 学生本人的选科结果（PAGE-STR-STUDENT） */
export interface MyStreamVO {
  termId?: string;
  termName?: string;
  primarySubjectCode?: string;
  primarySubjectName?: string;
  secondarySubjectCodes?: string[];
  secondarySubjectNames?: string[];
  /** 组合展示名，如「物理 + 化学 + 生物」 */
  combination?: string;
  /** 状态：已生效 / 待审批 / 未选择 */
  status?: string;
  effectiveDate?: string;
  /** 教学班归属（展示用；行政班不变） */
  teachingClassNames?: string[];
}

/** 提交 / 更新我的选科（POST/PUT /edu/stream/my） */
export interface MyStreamForm {
  primarySubjectCode: string;
  secondarySubjectCodes: string[];
  /** 变更原因（截止后提交或变更时必填） */
  reason?: string;
}

/** 未选科学生（催办清单） */
export interface UnselectedStudentVO {
  studentId: string;
  studentNo?: string;
  studentName?: string;
  gradeName?: string;
  className?: string;
}

/** 选科历史记录 */
export interface StreamHistoryVO {
  historyId: string;
  /** 变更类型：初次提交 / 变更 / 审批通过 等 */
  changeType?: string;
  beforeCombination?: string;
  afterCombination?: string;
  effectiveDate?: string;
  operator?: string;
  reason?: string;
}
