package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAuditChange;
import org.dromara.edu.domain.vo.EduAuditChangeVo;

/**
 * 日志变更明细数据层（只追加，不提供更新 / 删除）
 *
 * @author Codex
 */
public interface EduAuditChangeMapper extends BaseMapperPlus<EduAuditChange, EduAuditChangeVo> {
}
