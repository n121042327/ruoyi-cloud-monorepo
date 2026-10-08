package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduTeachingClass;

import java.util.List;

/**
 * 教学班业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduTeachingClass.class, reverseConvertGenerate = false)
public class EduTeachingClassBo extends BaseEntity {

    /** 教学班 ID */
    private Long teachingClassId;

    /** 学校 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 年级 */
    private Long gradeId;

    /** 教学班名称 */
    @NotBlank(message = "教学班名称不能为空")
    private String className;

    /** 组合 / 单学科标识 */
    @NotBlank(message = "组合 / 单学科标识不能为空")
    private String combination;

    /** 成员数 */
    private Integer memberCount;

    /** active 正常 / disabled 已停用 */
    private String teachingClassStatus;

    /** 关键字：教学班名称 / 组合 */
    private String keyword;

    /** 组合 / 学科筛选（列表查询） */
    private String filterCombination;

    /** 停用原因（disableTeachingClass 必填，至少 5 个字） */
    private String reason;

    /** 生成：成员学生 ID 列表（按组合生成时写入成员） */
    private List<Long> studentIds;

    /** 生成任务的幂等键（REQ-STR-057） */
    private String generateTaskNo;

}
