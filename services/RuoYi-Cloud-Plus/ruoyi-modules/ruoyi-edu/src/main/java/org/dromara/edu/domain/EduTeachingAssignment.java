package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 任教关系 edu_teaching_assignment
 *
 * 「某学期某学科某班由某教师授课」的稳定事实（不含上课时间与教室）。任课教师的数据范围 DS-07
 * 与字段裁剪（本人所授学科）都依据本表（BR-TEACHER-003）。`schoolId` 是**任教学校**，
 * 可与教师所属学校不同，用于表达跨校任教。
 *
 * 本表没有 del_flag（soft_delete 为 false）：任教关系失效写 status='0'，不物理删除。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_teaching_assignment")
public class EduTeachingAssignment extends TenantEntity {

    /** 任教关系 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long assignmentId;

    /** 任教学校（可与教师所属学校不同，用于跨校任教） */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 教师 */
    private Long teacherId;

    /** 学科 */
    private Long subjectId;

    /** 班级类型：administrative 行政班 / teaching 教学班 */
    private String classType;

    /** 行政班或教学班 ID */
    private Long classId;

    /** 1 有效 / 0 失效 */
    private String status;

}
