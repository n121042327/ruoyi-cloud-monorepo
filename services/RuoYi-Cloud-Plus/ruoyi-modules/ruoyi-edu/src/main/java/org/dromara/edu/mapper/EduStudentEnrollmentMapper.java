package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.vo.EduStudentEnrollmentVo;

/**
 * 在校记录数据层
 *
 * 学校侧读学生一律经本表两段式取数（DS-DENY-09）；学籍状态流转只由升班与学籍异动模块写（DP-01）。
 *
 * @author Codex
 */
public interface EduStudentEnrollmentMapper extends BaseMapperPlus<EduStudentEnrollment, EduStudentEnrollmentVo> {

}
