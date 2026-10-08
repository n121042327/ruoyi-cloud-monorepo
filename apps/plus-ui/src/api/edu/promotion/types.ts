/**
 * 升班与学籍异动模块的接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 promotion 模块；
 * 跨校转学（发起 / 接收 / 报到 / 撤销）属同一组接口，字段名取自 docs/10-prd/06-field-dictionary.yaml。
 */

/** 转学单列表行 */
export interface TransferOrderVO extends BaseEntity {
  transferId: string;
  /** 转学单号 */
  transferNo?: string;
  studentId: string;
  studentNo?: string;
  studentName?: string;
  gender?: string;
  /** 原学校与年级（转学单只暴露必要字段，REQ-PRM-055） */
  fromSchoolId?: string;
  fromSchoolName?: string;
  fromGradeName?: string;
  toSchoolId?: string;
  toSchoolName?: string;
  toGradeId?: string;
  toGradeName?: string;
  toClassId?: string;
  toClassName?: string;
  /** 状态：pending_receive（待接收）/ received（已接收）/ checked_in（已报到）/ canceled（已撤销） */
  status?: string;
  effectiveDate?: string;
  remark?: string;
}

/** 发起转出（POST /edu/enrollment/transfer） */
export interface TransferForm {
  /** 学生 ID */
  studentId: string;
  /** 转入校 */
  toSchoolId: string;
  /** 目标年级 */
  toGradeId: string;
  /** 目标班级；留空表示报到时再分班 */
  toClassId?: string;
  /** 申请日期（effective_date） */
  effectiveDate: string;
  /** 备注 */
  remark?: string;
}

/** 转入校接收（POST /edu/enrollment/transfer/{id}/accept） */
export interface TransferAcceptForm {
  transferId: string;
  /** 接收时指定的目标年级 */
  toGradeId: string;
  /** 接收时指定的目标班级；留空表示报到时再分班 */
  toClassId?: string;
}

/** 学籍异动记录（异动历史页） */
export interface EnrollmentChangeVO extends BaseEntity {
  changeId: string;
  studentId: string;
  studentNo?: string;
  studentName?: string;
  /** 异动类型：休学 / 复学 / 转出 / 退学 等 */
  changeType?: string;
  /** 生效日期 */
  effectiveDate?: string;
  beforeStatus?: string;
  afterStatus?: string;
  operator?: string;
  reason?: string;
  termId?: string;
  gradeId?: string;
}

/** 异动记录查询参数 */
export interface EnrollmentChangeQuery extends Partial<PageQuery> {
  schoolId?: string;
  termId?: string;
  gradeId?: string;
  changeType?: string;
  effectiveDate?: string;
}

/**
 * 升班任务列表行（PAGE-PRM-LIST）。
 *
 * 状态取自字典 `FD-promotion_task_status`：draft / previewed / validating / running /
 * succeeded / partial_failed / failed / cancelled（REQ-PRM-003 / 011）。
 */
export interface PromotionTaskVO extends BaseEntity {
  taskId: string;
  /** 任务编号，如 PRM-20260620-0001 */
  taskNo?: string;
  schoolId?: string;
  /** 源学年学期 ID 与展示名 */
  sourceTermId?: string;
  sourceTermName?: string;
  /** 目标学年学期 ID 与展示名 */
  targetTermId?: string;
  targetTermName?: string;
  /** 任务状态码（draft / previewed / validating / running / succeeded / partial_failed / failed / cancelled） */
  status?: string;
  /** 草稿没有升班明细，三个计数字段为空 */
  totalCount?: number;
  successCount?: number;
  failedCount?: number;
  /** 范围说明（选填，最长 500 字） */
  remark?: string;
  /** 失败原因摘要（部分失败时） */
  failReason?: string;
}

/** 升班任务查询参数（PAGE-PRM-LIST 查询区） */
export interface PromotionTaskQuery extends Partial<PageQuery> {
  schoolId?: string;
  sourceTermId?: string;
  targetTermId?: string;
  status?: string;
  createBy?: string;
  taskNo?: string;
}

/** 新建升班任务（POST /edu/promotion/task，REQ-PRM-007 / 008） */
export interface PromotionTaskForm {
  schoolId?: string;
  /** 源学年学期（决定从哪一批在读学生升班） */
  sourceTermId: string;
  /** 目标学年学期（年级与班级必须已建好） */
  targetTermId: string;
  /** 范围说明（选填，最长 500 字，随台账导出） */
  remark?: string;
}

/** 目标学年学期的年级与班级齐备性检查结果（PAGE-PRM-CREATE，REQ-PRM-009） */
export interface PromotionReadiness {
  /** 结论：ready（可以创建）/ blocked（阻塞）/ warning（可创建但需确认） */
  level?: string;
  /** 结论说明 */
  message?: string;
  /** 缺失的年级名称 */
  missingGrades?: string[];
  /** 缺失的班级数量 */
  missingClassCount?: number;
  /** 同一源 → 目标学期是否已有未结束任务 */
  hasUnfinishedTask?: boolean;
  /** 源学年学期在读人数（REQ-PRM-005） */
  enrolledCount?: number;
}

/**
 * 升班明细行（PAGE-PRM-PREVIEW / PAGE-PRM-VALIDATE / PAGE-PRM-RESULT 共用）。
 *
 * `status` 为明细状态：pending（待处理）/ adjusted（已调整）/ valid（校验通过）/
 * error（校验不通过）/ success（已升班）/ failed（失败）。
 */
export interface PromotionItemVO {
  itemId: string;
  studentId: string;
  studentNo?: string;
  studentName?: string;
  sourceClassId?: string;
  sourceClassName?: string;
  /** 结果类型：promote（升班）/ repeat（留级）/ graduate（毕业）/ complete（结业） */
  resultType?: string;
  targetClassId?: string;
  targetClassName?: string;
  status?: string;
  /** 校验说明 / 失败原因 */
  errorMsg?: string;
  remark?: string;
}

/** 逐条调整升班去向（PAGE-PRM-ADJUST，updatePromotionItem） */
export interface PromotionItemAdjustForm {
  itemId: string;
  studentNo?: string;
  studentName?: string;
  resultType: string;
  targetClassId?: string;
  remark?: string;
}

/** 按源班级批量指定目标班级（batchUpdatePromotionItem，REQ-PRM-018） */
export interface PromotionBatchAdjustForm {
  /** 源班级 */
  sourceClassId: string;
  /** 目标班级 */
  targetClassId: string;
  /** 结果类型，默认升班 */
  resultType?: string;
}

/** 升班明细查询参数 */
export interface PromotionItemQuery extends Partial<PageQuery> {
  /** 按源班级过滤（左侧源班级列表点击后只看该班明细） */
  sourceClassId?: string;
  /** 明细状态 */
  status?: string;
  /** 只看已调整项 */
  adjustedOnly?: boolean;
}
