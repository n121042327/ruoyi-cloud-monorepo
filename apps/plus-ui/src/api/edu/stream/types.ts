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

/** 选科清单行（PAGE-STR-LIST） */
export interface StreamSelectionVO {
  studentId: string;
  studentNo?: string;
  studentName?: string;
  gradeId?: string;
  gradeName?: string;
  className?: string;
  primarySubjectCode?: string;
  primarySubjectName?: string;
  secondarySubjectCodes?: string[];
  secondarySubjectNames?: string[];
  /** 组合展示名，如「物理 + 化学 + 生物」 */
  combination?: string;
  /** 状态：已生效 / 待审批 / 未选择 */
  status?: string;
}

/** 选科清单查询参数 */
export interface StreamSelectionQuery extends Partial<PageQuery> {
  termId?: string;
  gradeId?: string;
  classId?: string;
  primarySubjectCode?: string;
  combination?: string;
  status?: string;
  keyword?: string;
}

/** 组合分布统计行（PAGE-STR-STAT 第一张表） */
export interface StreamStatCombinationVO {
  subjectCombination?: string;
  primarySubjectCode?: string;
  primarySubjectName?: string;
  secondarySubjectCodes?: string[];
  secondarySubjectNames?: string[];
  memberCount?: number;
  /** 占比（按已选科人数为分母） */
  ratio?: string;
}

/** 学科选择人数行（PAGE-STR-STAT 第二张表） */
export interface StreamStatSubjectVO {
  subjectName?: string;
  /** 选科角色：首选 / 再选 */
  streamRole?: string;
  memberCount?: number;
  ratio?: string;
  /** 占比分母说明 */
  ratioBase?: string;
}

/** 组合分布统计（PAGE-STR-STAT） */
export interface StreamStatVO {
  termId?: string;
  termName?: string;
  /** 已选科人数 */
  selectedCount?: number;
  /** 未选科人数 */
  unselectedCount?: number;
  /** 首选分布：物理 / 历史 */
  primaryDistribution?: Array<{ subjectName?: string; memberCount?: number; ratio?: string }>;
  /** 组合明细 */
  combinations?: StreamStatCombinationVO[];
  /** 学科选择人数 */
  subjects?: StreamStatSubjectVO[];
}

/** 选科变更申请（PAGE-STR-CHANGE） */
export interface StreamChangeForm {
  studentId: string;
  primarySubjectCode: string;
  secondarySubjectCodes: string[];
  reason: string;
}

/**
 * 选科变更申请单（PAGE-STR-APPROVE）
 *
 * 审批通过后学生选科立即生效，教学班名单不会自动同步，需人工触发增量生成并核对差异（REQ-STR-060）。
 */
export interface StreamChangeRequestVO {
  requestId: string;
  /** 申请单号，如 SC-2026-0031 */
  requestNo?: string;
  studentId?: string;
  studentNo?: string;
  studentName?: string;
  gradeName?: string;
  className?: string;
  /** 原组合（当前生效） */
  beforeCombination?: string;
  /** 新组合（申请） */
  afterCombination?: string;
  /** 申请原因 */
  reason?: string;
  /** 提交时间 */
  applyTime?: string;
  /** 状态：待审批 / 已通过 / 已驳回 / 已撤销 */
  status?: string;
}

/** 变更申请查询参数 */
export interface StreamChangeRequestQuery extends Partial<PageQuery> {
  status?: string;
  gradeId?: string;
  keyword?: string;
  termId?: string;
}

/** 审批动作（approveStreamChangeRequest） */
export interface StreamApproveForm {
  requestId: string;
  /** 审批结果：true 通过并生效 / false 驳回 */
  approved: boolean;
  /** 审批意见：驳回必填（≥ 5 字，REQ-STR-037） */
  opinion?: string;
}

/** 教学班生成预览行（PAGE-STR-GEN-CLASS 主表） */
export interface TeachingClassGenerateRowVO {
  subjectCombination?: string;
  className?: string;
  memberCount?: number;
  /** 是否已存在同组合教学班 */
  existing?: boolean;
  /** 将执行的动作：新建 / 增量并入 / 跳过 */
  action?: string;
}

/** 教学班生成预览（previewTeachingClassGenerate） */
export interface TeachingClassGeneratePreviewVO {
  termId?: string;
  termName?: string;
  gradeId?: string;
  gradeName?: string;
  /** 生成方式：combination（按完整组合）/ subject（按单学科） */
  generateMode?: string;
  /** 预计新增与并入的教学班数 */
  planCount?: number;
  rows?: TeachingClassGenerateRowVO[];
}

/** 生成后的核对行（教学班人数 vs 选科统计人数） */
export interface TeachingClassCheckRowVO {
  className?: string;
  memberCount?: number;
  statCount?: number;
  diff?: number;
  /** 核对结果：一致 / 存在差异 */
  result?: string;
}
