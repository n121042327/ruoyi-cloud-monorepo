package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduSchoolStage;

import java.util.List;

/**
 * 学校开设学段业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduSchoolStage.class, reverseConvertGenerate = false)
public class EduSchoolStageBo extends BaseEntity {

    /** 记录 ID */
    private Long schoolStageId;

    /** 学校 */
    private Long schoolId;

    /** 单个学段（单条保存用） */
    private String stageCode;

    /** 1 开设 / 0 停开 */
    private String status;

    /** 批量保存的学段集合（saveSchoolStage） */
    private List<String> stageCodes;

}
