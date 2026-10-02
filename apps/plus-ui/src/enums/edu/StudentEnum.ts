/**
 * 学生域枚举与选项。
 *
 * 取值来源：docs/10-prd/06-field-dictionary.yaml（stage_code / enrollment_status）。
 * 前端不自行发明码值；字典变更时先改字段字典，再改本文件。
 */

/** 学段（`stage_code`） */
export enum StageCodeEnum {
  /** 小学：年级范围 1-6 */
  PRIMARY = 'primary',
  /** 初中：年级范围 1-3 */
  JUNIOR = 'junior',
  /** 高中：年级范围 1-3 */
  SENIOR = 'senior'
}

/** 学段选项；顺序按原型 student-list.html 的展示顺序（高中 → 初中 → 小学） */
export const STAGE_CODE_OPTIONS = [
  { value: StageCodeEnum.SENIOR, label: '高中' },
  { value: StageCodeEnum.JUNIOR, label: '初中' },
  { value: StageCodeEnum.PRIMARY, label: '小学' }
];

/** 学段名称映射 */
export const STAGE_CODE_LABEL: Record<string, string> = {
  [StageCodeEnum.PRIMARY]: '小学',
  [StageCodeEnum.JUNIOR]: '初中',
  [StageCodeEnum.SENIOR]: '高中'
};

/** 性别 */
export enum GenderEnum {
  MALE = '男',
  FEMALE = '女'
}

export const GENDER_OPTIONS = [
  { value: GenderEnum.MALE, label: '男' },
  { value: GenderEnum.FEMALE, label: '女' }
];

/** 学籍状态（`enrollment_status`，共 12 个，其中 7 个终态） */
export enum EnrollmentStatusEnum {
  PENDING_ENROLL = 'pending_enroll',
  ENROLLED = 'enrolled',
  SUSPENDED = 'suspended',
  STUDYING_ABROAD = 'studying_abroad',
  MISSING = 'missing',
  TRANSFERRED_OUT = 'transferred_out',
  GRADUATED = 'graduated',
  COURSE_COMPLETED = 'course_completed',
  COURSE_INCOMPLETE = 'course_incomplete',
  EXPELLED = 'expelled',
  WITHDRAWN = 'withdrawn',
  DECEASED = 'deceased'
}

/** 学籍状态全量选项（详情、表单、导出列使用） */
export const ENROLLMENT_STATUS_OPTIONS = [
  { value: EnrollmentStatusEnum.PENDING_ENROLL, label: '转入未报到' },
  { value: EnrollmentStatusEnum.ENROLLED, label: '在读' },
  { value: EnrollmentStatusEnum.SUSPENDED, label: '休学' },
  { value: EnrollmentStatusEnum.STUDYING_ABROAD, label: '出国' },
  { value: EnrollmentStatusEnum.MISSING, label: '失踪' },
  { value: EnrollmentStatusEnum.TRANSFERRED_OUT, label: '已转出' },
  { value: EnrollmentStatusEnum.GRADUATED, label: '毕业' },
  { value: EnrollmentStatusEnum.COURSE_COMPLETED, label: '结业' },
  { value: EnrollmentStatusEnum.COURSE_INCOMPLETE, label: '肄业' },
  { value: EnrollmentStatusEnum.EXPELLED, label: '开除' },
  { value: EnrollmentStatusEnum.WITHDRAWN, label: '退学' },
  { value: EnrollmentStatusEnum.DECEASED, label: '死亡' }
];

/**
 * 学生列表查询区使用的学籍状态选项。
 *
 * 与原型 `prototypes/functional/v2/pages/student-list.html` 的 chip 组一致（在读 / 休学 /
 * 转入未报到 / 出国 / 已转出 / 退学），不是全量 12 个；全量用于详情与表单。
 */
export const ENROLLMENT_STATUS_FILTER_OPTIONS = ENROLLMENT_STATUS_OPTIONS.filter((item) =>
  [
    EnrollmentStatusEnum.ENROLLED,
    EnrollmentStatusEnum.SUSPENDED,
    EnrollmentStatusEnum.PENDING_ENROLL,
    EnrollmentStatusEnum.STUDYING_ABROAD,
    EnrollmentStatusEnum.TRANSFERRED_OUT,
    EnrollmentStatusEnum.WITHDRAWN
  ].includes(item.value)
);

/** 学籍状态名称映射 */
export const ENROLLMENT_STATUS_LABEL: Record<string, string> = ENROLLMENT_STATUS_OPTIONS.reduce(
  (acc, item) => {
    acc[item.value] = item.label;
    return acc;
  },
  {} as Record<string, string>
);
