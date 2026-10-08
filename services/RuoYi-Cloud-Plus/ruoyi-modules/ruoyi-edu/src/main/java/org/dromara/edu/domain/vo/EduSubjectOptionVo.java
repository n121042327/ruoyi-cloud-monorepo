package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 学科下拉选项（listSubjectOption）
 *
 * 供班级列表、任教关系、选科等页面的「学科」下拉使用；未开设的学段不返回（REQ-SUB-026）。
 *
 * @author Codex
 */
@Data
public class EduSubjectOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private String subjectCode;

    private String subjectName;

    /** 该学科启用的学段集合 */
    private List<String> stageCodes;

    /** primary / secondary / none */
    private String streamRole;

}
