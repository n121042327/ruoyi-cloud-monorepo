package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduSchool;

import java.util.List;

/**
 * 学校业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduSchool.class, reverseConvertGenerate = false)
public class EduSchoolBo extends BaseEntity {

    /** 学校 ID */
    private Long schoolId;

    /** 学校编码（父租户内唯一） */
    @NotBlank(message = "学校编码不能为空")
    private String schoolCode;

    /** 学校名称 */
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    /** 简称 */
    private String shortName;

    /** 上级租户 */
    private String parentTenantId;

    /** 根租户 */
    private String rootTenantId;

    /** 学校类型 */
    private String schoolType;

    /** 地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 学校状态 */
    private String schoolStatus;

    /** 关键字：学校名称 / 编码 */
    private String keyword;

    /** 停用 / 启用原因（至少 5 个字） */
    private String reason;

    /** 开通初始化：学校开设学段集合（initSchoolBaseline） */
    private List<String> stageCodes;

    /** 开通初始化：学年编码（YYYY-YYYY） */
    private String academicYearCode;

    /** 开通初始化：学年开始日期 */
    private String startDate;

    /** 开通初始化：学年结束日期 */
    private String endDate;

    /** 开通初始化：是否同时初始化学科模板与基础角色（默认 true） */
    private Boolean initSubject;

}
