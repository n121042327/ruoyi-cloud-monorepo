package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduUserRole;

/**
 * 用户教育角色业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduUserRole.class, reverseConvertGenerate = false)
public class EduUserRoleBo extends BaseEntity {

    /** 角色记录 ID */
    private Long userRoleId;

    /** 学校 */
    private Long schoolId;

    /** 系统用户 */
    private Long userId;

    /** 教师 */
    private Long teacherId;

    /** 学校级教育角色 */
    @NotBlank(message = "教育角色不能为空")
    private String eduRole;

    /** 1 启用 / 0 停用 */
    private String status;

    /** 任职开始日期（YYYY-MM-DD） */
    private String startDate;

    /** 任职结束日期（YYYY-MM-DD） */
    private String endDate;

}
