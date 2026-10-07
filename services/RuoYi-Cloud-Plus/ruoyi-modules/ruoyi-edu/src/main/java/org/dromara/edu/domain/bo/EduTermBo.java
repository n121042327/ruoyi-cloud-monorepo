package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduTerm;

/**
 * 学期业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduTerm.class, reverseConvertGenerate = false)
public class EduTermBo extends BaseEntity {

    /** 学期 ID */
    private Long termId;

    /** 学校 */
    private Long schoolId;

    /** 学年 */
    private Long academicYearId;

    /** 学期编码（1 / 2） */
    @NotBlank(message = "学期编码不能为空")
    private String termCode;

    /** 学期名称 */
    @NotBlank(message = "学期名称不能为空")
    private String termName;

    /** 开始日期（YYYY-MM-DD） */
    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    /** 结束日期（YYYY-MM-DD） */
    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    /** 是否当前学期：1 / 0 */
    private String isCurrent;

    /** 学期状态 */
    private String termStatus;

    /** 关键字：学期名称 */
    private String keyword;

    /** 删除原因（已被引用时不返回给前端展示，仅审计用） */
    private String reason;

}
