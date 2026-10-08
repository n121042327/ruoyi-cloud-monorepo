package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduSubjectStage;

/**
 * 学科与学段启用业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduSubjectStage.class, reverseConvertGenerate = false)
public class EduSubjectStageBo extends BaseEntity {

    /** 记录 ID */
    private Long subjectStageId;

    /** 学校 */
    private Long schoolId;

    /** 学科 */
    private Long subjectId;

    /** 学段 */
    private String stageCode;

    /** 1 启用 / 0 停用 */
    private String status;

    /** 批量设置的学段集合 */
    private java.util.List<String> stageCodes;

}
