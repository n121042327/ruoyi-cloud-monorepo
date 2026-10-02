/** 学校（学校与租户模块） */
export interface SchoolVO {
  schoolId: string;
  schoolName: string;
  schoolCode?: string;
  /** 是否为当前登录用户所属学校 */
  current?: boolean;
}
