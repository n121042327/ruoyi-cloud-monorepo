package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduGuardian;
import org.dromara.edu.domain.vo.EduGuardianVo;

/**
 * 监护人主体数据层
 *
 * 平台级实体（不设 tenant_id）：手机号平台唯一，一个家长可对应多个孩子（跨租户、多对多）。
 *
 * @author Codex
 */
public interface EduGuardianMapper extends BaseMapperPlus<EduGuardian, EduGuardianVo> {

}
