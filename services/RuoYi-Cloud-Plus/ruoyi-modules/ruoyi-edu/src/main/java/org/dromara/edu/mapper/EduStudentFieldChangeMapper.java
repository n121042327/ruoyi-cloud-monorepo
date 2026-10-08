package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStudentFieldChange;
import org.dromara.edu.domain.vo.EduStudentFieldChangeVo;

/**
 * 学生资料变更申请数据层
 *
 * 同一学生同一字段同时只允许一条待审核（GAP-018）：由数据库生成列 pending_guard + 唯一键强制，
 * 因此本实体不映射该生成列（避免写生成列报错），唯一性冲突由数据库抛错、服务层转成业务提示。
 *
 * @author Codex
 */
public interface EduStudentFieldChangeMapper extends BaseMapperPlus<EduStudentFieldChange, EduStudentFieldChangeVo> {

}
