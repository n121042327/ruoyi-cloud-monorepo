/**
 * 教师域枚举与选项。
 *
 * 码值来源：`edu_teacher.employment_status`（`docs/40-detailed-design/database/schema.yaml`
 * 默认 `'active'`）与后端 `EduTeacherServiceImpl.STATUS_ACTIVE`；离职 / 调离码值取自
 * `docs/10-prd/06-field-dictionary.yaml` 的 `employment_status` 枚举（`resigned` / `transferred_out`）。
 *
 * 已知上游分歧：字段字典把「在职」记为 `on_duty`，而 schema.yaml 默认值与后端落库都用 `active`。
 * 本文件按后端实际落库码值对齐，分歧已登记 gap-register（CR-128 / D-208）。
 */

/** 在职状态（`employment_status`） */
export enum EmploymentStatusEnum {
  /** 在职 */
  ON_DUTY = 'active',
  /** 离职 */
  RESIGNED = 'resigned',
  /** 调离 */
  TRANSFERRED_OUT = 'transferred_out'
}

/** 在职状态选项 */
export const EMPLOYMENT_STATUS_OPTIONS = [
  { value: EmploymentStatusEnum.ON_DUTY, label: '在职' },
  { value: EmploymentStatusEnum.RESIGNED, label: '离职' },
  { value: EmploymentStatusEnum.TRANSFERRED_OUT, label: '调离' }
];

/** 在职状态名称映射；保留中文键以兼容早期误存中文的历史行 */
export const EMPLOYMENT_STATUS_LABEL: Record<string, string> = {
  [EmploymentStatusEnum.ON_DUTY]: '在职',
  [EmploymentStatusEnum.RESIGNED]: '离职',
  [EmploymentStatusEnum.TRANSFERRED_OUT]: '调离',
  在职: '在职',
  离职: '离职',
  调离: '调离'
};
