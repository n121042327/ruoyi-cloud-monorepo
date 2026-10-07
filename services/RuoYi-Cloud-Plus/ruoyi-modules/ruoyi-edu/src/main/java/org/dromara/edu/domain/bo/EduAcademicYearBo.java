package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduAcademicYear;

/**
 * 学年业务对象
 *
 * 日期用字符串接收（前端 el-date-picker 的 YYYY-MM-DD），服务层统一解析后落库（DateUtils.parseDate）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduAcademicYear.class, reverseConvertGenerate = false)
public class EduAcademicYearBo extends BaseEntity {

    /** 学年 ID */
    private Long academicYearId;

    /** 学校 */
    private Long schoolId;

    /** 学年编码（YYYY-YYYY，连续两个自然年） */
    @NotBlank(message = "学年编码不能为空")
    private String academicYearCode;

    /** 开始日期（YYYY-MM-DD） */
    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    /** 结束日期（YYYY-MM-DD） */
    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    /** 学年状态 */
    private String academicYearStatus;

    /** 关键字：学年编码 */
    private String keyword;

    /** 归档 / 撤销归档原因（至少 5 个字） */
    private String reason;

}
