package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduActivationCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 激活码视图对象
 *
 * 激活码只在「查看 / 打印」时下发一次（D-039），列表与详情默认不回显完整码值。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduActivationCode.class)
public class EduActivationCodeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long activationId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    /** 一次性激活码（仅打印 / 查看时下发） */
    private String code;

    /** 未使用 / 已使用 / 已作废 */
    private String status;

    private String issueBatchNo;

    private Date printTime;

    private Date usedTime;

    private Date resetTime;

    private Date createTime;

    /** 打印用：班级名称（班主任分发密码条时打印） */
    private String className;

    /** 打印用：年级名称 */
    private String gradeName;

}
