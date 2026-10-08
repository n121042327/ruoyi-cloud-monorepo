package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 教学班 edu_teaching_class
 *
 * 与行政班完全独立（BR-CLASS-001）；不设班主任、不参与 DS-06 解析。
 * 成员由「按组合生成」触发写入（REQ-STR-056），手工增删成员不开放。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_teaching_class")
public class EduTeachingClass extends TenantEntity {

    /** 教学班 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long teachingClassId;

    /** 学校归属 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 年级 */
    private Long gradeId;

    /** 教学班名称（如 高一 · 物化生 A 层） */
    private String className;

    /** 组合或单学科标识（如 物理+化学+生物 或 单学科：物理） */
    private String combination;

    /** 成员数（冗余统计） */
    private Integer memberCount;

    /** active 正常 / disabled 已停用 */
    private String teachingClassStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
