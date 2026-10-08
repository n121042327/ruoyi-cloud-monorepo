package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTeachingAssignment;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任教关系视图对象
 *
 * 教师姓名 / 学科名称 / 班级名称与是否跨校为展示字段，由自定义 SQL 连接填充
 * （EduTeachingAssignmentMapper.xml 在后续批次补齐）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTeachingAssignment.class)
public class EduTeachingAssignmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long assignmentId;

    private Long schoolId;

    private String schoolName;

    private Long termId;

    private String termName;

    private Long teacherId;

    private String teacherName;

    private Long subjectId;

    private String subjectName;

    /** administrative / teaching */
    private String classType;

    private Long classId;

    private String className;

    /** 1 有效 / 0 失效 */
    private String status;

    /** 是否跨校任教：任教学校 ≠ 教师所属学校 */
    private Boolean crossSchool;

}
