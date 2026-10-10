/** 学校（学校与租户模块） */
export interface SchoolVO {
  schoolId: string;
  schoolName: string;
  schoolCode?: string;
  /** 开设学段（多个以逗号分隔） */
  stageCodes?: string;
  campusCount?: number;
  classCount?: number;
  studentCount?: number;
  /** 学校类型：public 公办 / private 民办 / other 其他 */
  schoolType?: string;
  /** 状态：active 正常 / disabled 已停用（后端 VO 字段名是 schoolStatus） */
  schoolStatus?: string;
  /** 所属租户 */
  tenantId?: string;
  /** 上级集团（无集团时为空） */
  parentTenantId?: string;
  address?: string;
  contactPhone?: string;
  /** 是否为当前登录用户所属学校 */
  current?: boolean;
}

/** 校区（不参与数据权限判定，可被班级引用） */
export interface CampusVO {
  campusId: string;
  campusName: string;
  schoolId: string;
  /** 校区编码 */
  campusCode?: string;
  address?: string;
  /** 负责人 */
  leader?: string;
  /** 班级数 */
  classCount?: number;
  status?: string;
}

/** 新建 / 编辑学校表单（字段依据 docs/10-prd/06-field-dictionary.yaml 与 school_Request） */
export interface SchoolForm {
  /** 学校 ID：编辑态必填，新增态为空 */
  schoolId?: string;
  /** 学校编码（父租户内唯一，BR-ORG-011）；编辑态只读，变更走 updateSchoolCode */
  schoolCode: string;
  /** 学校名称 */
  schoolName: string;
  /** 学校类型：public 公办 / private 民办 / other 其他 */
  schoolType?: string;
  /** 开设学段（新建必填；建校时一并落 edu_school_stage，CR-107） */
  stageCodes?: string[];
  /** 所属租户：只用于编辑态展示（学校与租户一一对应，绑定关系不可修改，REQ-SCH-022） */
  tenantId?: string;
}

/** 学校开设学段（listSchoolStage） */
export interface SchoolStageVO {
  schoolStageId?: string;
  schoolId?: string;
  /** 学段编码：primary / junior / senior */
  stageCode?: string;
  stageName?: string;
  /** 1 开设 / 0 停开 */
  status?: string;
  /** 该学段下的年级数：> 0 时不允许移除该学段（REQ-SCH-034） */
  gradeCount?: number;
}

/** 校区新增 / 编辑表单（POST /edu/school/{id}/campus，字段与 EduCampusBo 对齐） */
export interface CampusForm {
  campusId?: string;
  schoolId?: string;
  campusCode: string;
  campusName: string;
  address?: string;
  leaderName?: string;
  leaderPhone?: string;
  campusStatus?: string;
}

/** 学校数据摘要（GET /edu/school/{id}/summary） */
export interface SchoolSummaryVO {
  schoolId: string;
  schoolName?: string;
  stageCodes?: string[];
  campusCount?: number;
  academicYearCount?: number;
  termCount?: number;
  gradeCount?: number;
  classCount?: number;
  /** 在读学生数（本校在校记录中 enrollment_status = 'enrolled' 的去重学生数，不限学年） */
  studentCount?: number;
  teacherCount?: number;
  subjectCount?: number;
  initialized?: boolean;
}
