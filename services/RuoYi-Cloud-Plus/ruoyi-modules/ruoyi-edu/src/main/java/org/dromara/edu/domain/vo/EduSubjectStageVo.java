package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduSubjectStage;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学科与学段启用视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduSubjectStage.class)
public class EduSubjectStageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectStageId;

    private Long subjectId;

    private String stageCode;

    /** 1 启用 / 0 停用 */
    private String status;

}
