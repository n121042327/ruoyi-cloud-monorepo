package org.dromara.edu.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduGradeLeader;
import org.dromara.edu.domain.vo.EduGradeLeaderVo;

/**
 * 年级主任任职数据层
 *
 * 该表是数据范围 DS-05（年级主任只看负责年级）的判定入口，解析器按 user_id + term_id + status
 * 反查负责年级集合（见 30-architecture/09-permission-architecture.md）。
 *
 * @author Codex
 */
public interface EduGradeLeaderMapper extends BaseMapperPlus<EduGradeLeader, EduGradeLeaderVo> {

}
