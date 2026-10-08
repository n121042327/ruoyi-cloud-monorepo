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
  /** 状态：正常 / 已停用 */
  status?: string;
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
