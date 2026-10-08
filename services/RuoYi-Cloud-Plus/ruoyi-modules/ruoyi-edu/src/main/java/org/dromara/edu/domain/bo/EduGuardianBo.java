package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduGuardian;

/**
 * 监护人业务对象（saveStudentGuardian 的入参 ＝ 监护人主体 + 关系）
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduGuardian.class, reverseConvertGenerate = false)
public class EduGuardianBo extends BaseEntity {

    /** 监护人 ID */
    private Long guardianId;

    /** 学生主体 ID（路径参数带入） */
    private Long studentId;

    /** 监护人姓名 */
    @NotBlank(message = "监护人姓名不能为空")
    private String guardianName;

    /** 手机号（平台唯一；家长登录名） */
    @NotBlank(message = "监护人手机号不能为空")
    private String guardianPhone;

    /** 证件号码 */
    private String idCardNo;

    /** 关系：father / mother / other */
    @NotBlank(message = "请选择与学生的关系")
    private String relation;

    /** 是否主要联系人 */
    private Boolean isPrimary;

    /** 备注（接口契约字段；edu_student_guardian 无 remark 列，见 GAP-092） */
    private String remark;

    /** 绑定状态：pending / approved / rejected / unbinding */
    private String bindStatus;

    /** 来源：qrcode / teacher / import */
    private String source;

    /** 解绑原因（unbindStudentGuardian 必填，至少 5 个字） */
    private String reason;

}
