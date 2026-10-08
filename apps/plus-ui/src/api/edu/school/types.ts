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
  /** 所属租户：只用于编辑态展示（学校与租户一一对应，绑定关系不可修改，REQ-SCH-022） */
  tenantId?: string;
}
