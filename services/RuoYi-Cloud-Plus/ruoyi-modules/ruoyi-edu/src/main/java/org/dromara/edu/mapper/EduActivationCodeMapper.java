package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduActivationCode;
import org.dromara.edu.domain.vo.EduActivationCodeVo;

/**
 * 激活码数据层
 *
 * 同一学生同一时刻只允许一个未使用激活码：由生成列 active_guard + uk_activation_active 在数据库层强制，
 * 服务层只需在重置前把旧码置为「已作废」。
 *
 * @author Codex
 */
public interface EduActivationCodeMapper extends BaseMapperPlus<EduActivationCode, EduActivationCodeVo> {

}
