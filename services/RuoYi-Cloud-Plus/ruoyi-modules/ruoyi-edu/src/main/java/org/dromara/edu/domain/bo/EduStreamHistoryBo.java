package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 选科历史查询对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduStreamHistoryBo extends BaseEntity {

    /** 学生主体 ID */
    private Long studentId;

    /** 学年学期 */
    private Long termId;

}
