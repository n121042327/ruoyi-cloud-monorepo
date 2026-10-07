package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 学年 edu_academic_year
 *
 * 学年日期必须连续不重叠：前一年结束日 = 后一年开始日 − 1 天（RV-TERM-08）。归档后仍可按时间范围检索历史。
 *
 * 本表没有 del_flag（schema.yaml 的 soft_delete 为 false）——学年只归档、不物理删除，
 * 因此**不加** @TableLogic。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_academic_year")
public class EduAcademicYear extends TenantEntity {

    /** 学年 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long academicYearId;

    /** 学校归属（scope: school） */
    private Long schoolId;

    /** 学年编码（如 2026-2027，连续两个自然年，BR-TERM-003） */
    private String academicYearCode;

    /** 开始日期 */
    private Date startDate;

    /** 结束日期 */
    private Date endDate;

    /** 学年状态：normal 未开始 / 进行中 / archived 已归档 */
    private String academicYearStatus;

}
