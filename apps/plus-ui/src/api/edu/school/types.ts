/** 学校（学校与租户模块） */
export interface SchoolVO {
  schoolId: string;
  schoolName: string;
  schoolCode?: string;
  /** 是否为当前登录用户所属学校 */
  current?: boolean;
}

/** 校区（不参与数据权限判定，可被班级引用） */
export interface CampusVO {
  campusId: string;
  campusName: string;
  schoolId: string;
  status?: string;
}
