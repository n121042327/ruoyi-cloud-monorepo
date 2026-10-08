package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduSchoolStage;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学校开设学段视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduSchoolStage.class)
public class EduSchoolStageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long schoolStageId;

    private Long schoolId;

    private String stageCode;

    private String stageName;

    /** 1 开设 / 0 停开 */
    private String status;

}
