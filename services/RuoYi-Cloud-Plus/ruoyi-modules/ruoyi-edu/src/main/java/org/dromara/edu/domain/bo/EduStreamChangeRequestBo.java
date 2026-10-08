package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduStreamChangeRequest;

import java.util.List;

/**
 * 选科变更申请业务对象
 *
 * 覆盖 addStreamChangeRequest / listStreamChangeRequest / approveStreamChangeRequest / cancelStreamChangeRequest。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduStreamChangeRequest.class, reverseConvertGenerate = false)
public class EduStreamChangeRequestBo extends BaseEntity {

    /** 申请 ID */
    private Long requestId;

    /** 申请单号 */
    private String requestNo;

    /** 学校 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 学生主体 ID */
    private Long studentId;

    /** 新的首选科目 */
    private String primarySubjectCode;

    /** 新的再选科目集合 */
    @AutoMapping(target = "secondarySubjectCodes", ignore = true)
    private List<String> secondarySubjectCodes;

    /** 申请原因（必填，至少 5 个字） */
    private String reason;

    /** 状态筛选：草稿 / 待审批 / 已通过 / 已驳回 / 已撤销 */
    private String requestStatus;

    /** 年级筛选 */
    private Long gradeId;

    /** 关键字：申请单号 / 姓名 / 学号 */
    private String keyword;

    /** 审批结果：true 通过 / false 驳回 */
    private Boolean approved;

    /** 审批意见（驳回必填，至少 5 个字） */
    private String approveOpinion;

}
