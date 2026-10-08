package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTeachingClass;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 教学班视图对象（与前端 TeachingClassVO / TeachingClassVO 对齐）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTeachingClass.class)
public class EduTeachingClassVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long classId;

    private Long teachingClassId;

    private Long schoolId;

    private Long termId;

    private String termName;

    private Long gradeId;

    private String gradeName;

    private String className;

    /** 组合 / 学科（前端字段名 subjectCombination） */
    private String subjectCombination;

    private String combination;

    private Integer memberCount;

    /** 任课教师（只读展示，来自任教关系） */
    private String teacherName;

    /** active 正常 / disabled 已停用 */
    private String status;

    private String teachingClassStatus;

    private Date updateTime;

}
