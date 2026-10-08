package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduSubject;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学科视图对象
 *
 * stageCodes 为该学科已启用的学段集合（来自 edu_subject_stage），由服务层或自定义 SQL 组装。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduSubject.class)
public class EduSubjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private Long schoolId;

    private String subjectCode;

    private String subjectName;

    private Integer sortNo;

    private String streamEnabled;

    /** primary 首选 / secondary 再选 / none 不参与 */
    private String streamRole;

    /** active 正常 / disabled 已停用 */
    private String subjectStatus;

    /** 已启用的学段集合 */
    private List<String> stageCodes;

    private Date updateTime;

}
