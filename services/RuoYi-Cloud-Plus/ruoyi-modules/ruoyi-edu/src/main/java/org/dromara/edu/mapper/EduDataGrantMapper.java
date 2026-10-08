package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduDataGrant;
import org.dromara.edu.domain.vo.EduDataGrantVo;

/**
 * 数据共享授权数据层
 *
 * 为 `DataScopeResolver` 的「叠加共享授权」提供来源表（架构文档 09-permission-architecture 第 3 节：
 * 范围片段 = edu_user_role / edu_grade_leader / edu_class.head_teacher_id / edu_teaching_assignment
 * 叠加 edu_data_grant + scope 中生效的授权）。
 *
 * @author Codex
 */
public interface EduDataGrantMapper extends BaseMapperPlus<EduDataGrant, EduDataGrantVo> {
}
