package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 学期 edu_term
 *
 * 同一学校同一学年只能有一个当前学期（BR-TERM-002）；学年日期连续不重叠（RV-TERM-08）。
 * 已被班级、任教关系或花名册引用的学期不允许删除（REQ-TERM-019）。
 *
 * 本表没有 del_flag（soft_delete 为 false），不加 @TableLogic。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_term")
public class EduTerm extends TenantEntity {

    /** 学期 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long termId;

    /** 学校归属 */
    private Long schoolId;

    /** 学年 */
    private Long academicYearId;

    /** 学期编码（如 1 / 2） */
    private String termCode;

    /** 学期名称（第一学期 / 第二学期） */
    private String termName;

    /** 开始日期 */
    private Date startDate;

    /** 结束日期 */
    private Date endDate;

    /** 是否当前学期：1 是 / 0 否 */
    private String isCurrent;

    /** 学期状态：normal 正常 / archived 已归档 */
    private String termStatus;

}
