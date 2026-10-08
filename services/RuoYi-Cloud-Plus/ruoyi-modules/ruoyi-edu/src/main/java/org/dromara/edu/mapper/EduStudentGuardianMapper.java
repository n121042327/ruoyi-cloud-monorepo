package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStudentGuardian;
import org.dromara.edu.domain.vo.EduGuardianVo;

/**
 * 监护人与学生关联数据层
 *
 * 绑定上限 3（BR-ACCOUNT-018）；解绑需班主任确认（GAP-015）。
 *
 * @author Codex
 */
public interface EduStudentGuardianMapper extends BaseMapperPlus<EduStudentGuardian, EduGuardianVo> {

}
