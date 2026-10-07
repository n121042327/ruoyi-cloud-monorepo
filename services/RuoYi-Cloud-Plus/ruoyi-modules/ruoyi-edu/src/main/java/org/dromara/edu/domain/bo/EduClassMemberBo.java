package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduClassMember;

import java.util.List;

/**
 * 班级成员（花名册）业务对象
 *
 * addClassRoster / removeClassRoster / transferClass 三个 operationId 共用。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduClassMember.class, reverseConvertGenerate = false)
public class EduClassMemberBo extends BaseEntity {

    /** 成员关系 ID */
    private Long memberId;

    /** 学校 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 班级 */
    private Long classId;

    /** 学生 ID */
    private Long studentId;

    /** 在校记录 ID */
    private Long studentEnrollmentId;

    /** 关键字：学号 / 姓名 */
    private String keyword;

    /** 学籍状态过滤（在读 / 休学 …） */
    private String enrollmentStatus;

    /** 加入 / 调班的批量学生 ID 列表 */
    @NotEmpty(message = "请至少选择一名学生")
    private List<Long> studentIds;

    /** 调班 / 迁移的目标班级 */
    private Long targetClassId;

    /** 生效日期（join_date / 调班生效日期，字段字典 effective_date） */
    private String effectiveDate;

    /** 加入 / 调班说明 */
    private String remark;

}
