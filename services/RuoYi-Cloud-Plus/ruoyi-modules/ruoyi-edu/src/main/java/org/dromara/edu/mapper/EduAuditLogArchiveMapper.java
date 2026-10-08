package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAuditLogArchive;
import org.dromara.edu.domain.vo.EduAuditLogVo;

/**
 * 归档操作日志数据层
 *
 * 归档表与在线表同构，查询结果在服务层手工投影为 `EduAuditLogVo`
 * （不依赖 MapStruct 的跨表转换，避免归档实体与在线 VO 之间出现隐式映射）。
 *
 * @author Codex
 */
public interface EduAuditLogArchiveMapper extends BaseMapperPlus<EduAuditLogArchive, EduAuditLogVo> {
}
