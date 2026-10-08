package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStreamHistory;
import org.dromara.edu.domain.vo.EduStreamHistoryVo;

/**
 * 选科历史数据层
 *
 * 追加式，不可删除不可修改（REQ-STR-043）；跨学年学期保留（REQ-STR-044）。
 *
 * @author Codex
 */
public interface EduStreamHistoryMapper extends BaseMapperPlus<EduStreamHistory, EduStreamHistoryVo> {

}
