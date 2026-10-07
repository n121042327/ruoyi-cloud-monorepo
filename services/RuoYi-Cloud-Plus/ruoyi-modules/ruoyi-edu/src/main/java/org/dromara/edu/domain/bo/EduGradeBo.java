package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduGrade;

import java.util.List;

/**
 * 年级业务对象
 *
 * 查询参数与年级 PRD 6.1 的查询区对齐；写入字段与 schema.yaml 的 edu_grade 列一一对应。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduGrade.class, reverseConvertGenerate = false)
public class EduGradeBo extends BaseEntity {

    /** 年级 ID */
    private Long gradeId;

    /** 学校 */
    private Long schoolId;

    /** 学段 */
    @NotBlank(message = "学段不能为空")
    private String stageCode;

    /** 入学年份 */
    private Integer enrollYear;

    /** 学段内序号 */
    private Integer gradeLevel;

    /** 年级名称 */
    private String gradeName;

    /** 年级状态：normal / archived */
    private String gradeStatus;

    /** 关键字：年级名称 */
    private String keyword;

    /** 归档原因（archiveGrade 必填） */
    private String reason;

    /** 批量新增的年级行 */
    private List<EduGradeBo> gradeList;

    /** 年级主任任职：学年学期 */
    private Long termId;

    /** 年级主任任职：系统账号 */
    private Long userId;

    /** 年级主任任职：教师 */
    private Long teacherId;

    /** 年级主任任职：是否主要负责人 */
    private String isPrimary;

}
