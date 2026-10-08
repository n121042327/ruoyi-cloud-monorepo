package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduUserRole;
import org.dromara.edu.domain.vo.EduUserRoleVo;

/**
 * 用户教育角色数据层
 *
 * 数据范围解析的入口之一：校领导（school_leader）看本校全部、教务主任（academic_director）
 * 看本校全部，判定依据是本表的 user_id + school_id + edu_role + status。
 *
 * @author Codex
 */
public interface EduUserRoleMapper extends BaseMapperPlus<EduUserRole, EduUserRoleVo> {

}
