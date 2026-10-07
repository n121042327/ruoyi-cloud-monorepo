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
