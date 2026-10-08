package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduSchoolStage;
import org.dromara.edu.domain.vo.EduSchoolStageVo;

/**
 * 学校开设学段数据层
 *
 * 未开设的学段在学科与年级配置里置灰（REQ-SUB-026），判定依据就是本表 status='1' 的记录。
 *
 * @author Codex
 */
public interface EduSchoolStageMapper extends BaseMapperPlus<EduSchoolStage, EduSchoolStageVo> {

}
