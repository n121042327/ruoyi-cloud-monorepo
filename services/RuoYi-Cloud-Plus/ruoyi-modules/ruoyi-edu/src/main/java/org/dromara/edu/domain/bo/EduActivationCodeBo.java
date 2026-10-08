package org.dromara.edu.domain.bo;

import lombok.Data;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 激活码业务对象
 *
 * @author Codex
 */
@Data
public class EduActivationCodeBo {

    /** 激活码 ID */
    private Long activationId;

    /** 学校 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 一次性激活码 */
    private String code;

    /** 打印批次号 */
    private String issueBatchNo;

    /** 重置原因（丢码重置时必填） */
    private String reason;

    /** 激活时设置的密码 */
    private String password;

    /** 激活时来源 IP */
    private String ip;

}
