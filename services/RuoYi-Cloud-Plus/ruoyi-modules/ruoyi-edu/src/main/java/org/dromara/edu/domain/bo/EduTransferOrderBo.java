package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduTransferOrder;

/**
 * 跨校转学单业务对象
 *
 * 覆盖 listTransfer / addTransfer / acceptTransfer / checkInTransfer / cancelTransfer 五个 operationId。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduTransferOrder.class, reverseConvertGenerate = false)
public class EduTransferOrderBo extends BaseEntity {

    /** 转学单 ID */
    private Long transferId;

    /** 转学单号 */
    private String transferNo;

    /** 学生主体 ID */
    @NotNull(message = "请选择学生")
    private Long studentId;

    /** 转出学校 */
    private Long fromSchoolId;

    /** 原年级 */
    private Long fromGradeId;

    /** 转入学校 */
    private Long toSchoolId;

    /** 目标年级 */
    private Long toGradeId;

    /** 目标班级（留空表示报到时再分班） */
    private Long toClassId;

    /** 生效日期（申请日期 effective_date） */
    private String effectiveDate;

    /** 备注 / 撤销原因 */
    private String remark;

    /** 转学单状态筛选 */
    private String transferStatus;

    /** 关键字：转学单号 / 学号 / 姓名 */
    private String keyword;

}
