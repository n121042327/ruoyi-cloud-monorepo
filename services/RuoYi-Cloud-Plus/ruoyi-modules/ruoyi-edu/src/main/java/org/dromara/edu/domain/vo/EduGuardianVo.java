package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduGuardian;

import java.io.Serial;
import java.io.Serializable;

/**
 * 监护人视图对象（对应前端 GuardianVO）
 *
 * 手机号默认只返回掩码 `guardianPhoneMasked`；明文需 `person.student:read_sensitive` 并写审计。
 * relation / isPrimary / bindStatus 来自 edu_student_guardian（关系表）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduGuardian.class)
public class EduGuardianVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 关系记录 ID（edu_student_guardian.id） */
    private Long relationId;

    private Long guardianId;

    private Long studentId;

    private String guardianName;

    /** 手机号（明文，仅在有 read_sensitive 时返回） */
    private String guardianPhone;

    /** 手机号掩码（默认返回） */
    private String guardianPhoneMasked;

    /** father / mother / other */
    private String relation;

    /** 是否主要联系人 */
    private Boolean isPrimary;

    /** pending / approved / rejected / unbinding */
    private String bindStatus;

    /** qrcode / teacher / import */
    private String source;

    /** 1 正常 / 0 停用 */
    private String status;

}
