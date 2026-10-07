package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 年级主任任职 edu_grade_leader
 *
 * 按学年学期生效；一个年级可有多名主任（isPrimary 标主要负责人）。这是数据范围 DS-05 的权威来源：
 * 年级主任只看负责年级的数据，判定入口就是本表（见 30-architecture/09-permission-architecture.md）。
 *
 * 本表没有 del_flag（schema.yaml 的 soft_delete 为 false），离任时把 status 置 0 而不是删除。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_grade_leader")
public class EduGradeLeader extends TenantEntity {

    /** 任职记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long leaderId;

    /** 学校归属 */
    private Long schoolId;

    /** 年级 */
    private Long gradeId;

    /** 学年学期 */
    private Long termId;

    /** 年级主任（系统账号） */
    private Long userId;

    /** 教师（可空：纯管理岗账号可能不是教师） */
    private Long teacherId;

    /** 是否主要负责人：1 是 / 0 否 */
    private String isPrimary;

    /** 1 在职 / 0 离任 */
    private String status;

}
