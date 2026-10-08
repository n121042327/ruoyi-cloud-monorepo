package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduTeachingAssignment;

import java.util.List;

/**
 * 任教关系业务对象
 *
 * 覆盖 listTeachingAssignment / saveTeachingAssignment / batchSaveTeachingAssignment /
 * removeTeachingAssignment / copyTeachingAssignment 五个 operationId。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduTeachingAssignment.class, reverseConvertGenerate = false)
public class EduTeachingAssignmentBo extends BaseEntity {

    /** 任教关系 ID */
    private Long assignmentId;

    /** 任教学校（可切换，用于跨校任教表达） */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 教师 */
    @NotNull(message = "请选择任教教师")
    private Long teacherId;

    /** 学科 */
    private Long subjectId;

    /** 班级类型：administrative / teaching */
    private String classType;

    /** 行政班或教学班 ID */
    private Long classId;

    /** 1 有效 / 0 失效 */
    private String status;

    /** 视角：class 按班级 / teacher 按教师（列表查询用） */
    private String view;

    /** 关键字：教师姓名 / 班级名称 / 学科 */
    private String keyword;

    /** 周课时（展示字段，任课关系表不存，仅作台账展示） */
    private Integer weeklyHours;

    /** 批量保存的任教关系行（batchSaveTeachingAssignment） */
    private List<EduTeachingAssignmentBo> assignmentList;

    /** 复制：源学年学期（copyTeachingAssignment） */
    private Long sourceTermId;

    /** 复制：目标学年学期 */
    private Long targetTermId;

    /** 失效原因（removeTeachingAssignment 必填） */
    private String reason;

}
