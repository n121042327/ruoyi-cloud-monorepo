package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 学生选科 edu_student_stream
 *
 * 学生当前生效的选科组合。同一学生同一学期唯一（BR-STREAM-007）。
 * 组合不在库里拼字符串：由 primarySubjectCode + secondarySubjectCodes 派生展示（CR-016）。
 * 截止前自助修改立即生效；截止后只能走变更申请，审批通过前保持原值（BR-STREAM-006）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student_stream")
public class EduStudentStream extends TenantEntity {

    /** 选科记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long streamId;

    /** 学校归属 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 学生主体 ID */
    private Long studentId;

    /** 首选科目（物理 / 历史） */
    private String primarySubjectCode;

    /** 再选科目（两个编码，逗号分隔，按学科排序号排序） */
    private String secondarySubjectCodes;

    /** 生效时间 */
    private Date effectiveTime;

    /** 生效 / 待审批 */
    private String streamStatus;

}
