package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 年级 edu_grade
 *
 * 年级与学段固定映射（学段内序号：小学 1–6、初中 / 高中 1–3，RV-GRD-03）；年级不能跨学段改名
 * （BR-GRADE-006）。有班级或学生关系时不允许删除、只允许归档（BR-GRADE-004）。
 *
 * 字段以唯一事实源 docs/40-detailed-design/database/schema.yaml 的 edu_grade 为准；
 * 物理主键是 `id`，业务名按接口契约叫 gradeId。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_grade")
public class EduGrade extends TenantEntity {

    /** 年级 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long gradeId;

    /** 学校归属（scope: school） */
    private Long schoolId;

    /** 学段 */
    private String stageCode;

    /** 入学年份（如 2026） */
    private Integer enrollYear;

    /** 学段内序号（小学 1–6、初中 / 高中 1–3） */
    private Integer gradeLevel;

    /** 年级名称（如 2026 级 高一） */
    private String gradeName;

    /** 班级数（冗余统计，写入时维护） */
    private Integer classCount;

    /** 在读学生数（冗余统计） */
    private Integer studentCount;

    /** 年级状态：normal 正常 / archived 已归档 */
    private String gradeStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
