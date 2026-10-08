package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 一次性激活凭据 edu_activation_code
 *
 * 班主任打印密码条分发 → 学生首登即设密码 → 激活码用完即废 → 丢码由班主任重置（D-039 / GAP-021）。
 * 同一学生同一时刻只允许一个未使用的激活码，由生成列 `active_guard` + `uk_activation_active`
 * 在数据库层强制。
 *
 * **本表 with_audit 为 false**（schema.yaml）：只有 `create_time`，没有 create_by / update_by /
 * update_time / del_flag，因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 字段
 * （租户值由租户拦截器写入）。`active_guard` 是生成列，不映射（写生成列会报错）。
 *
 * @author Codex
 */
@Data
@TableName("edu_activation_code")
public class EduActivationCode {

    /** 激活码 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long activationId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 一次性激活码（全局唯一） */
    private String code;

    /** 未使用 / 已使用 / 已作废 */
    private String status;

    /** 打印批次号（班主任一次打印一个批次） */
    private String issueBatchNo;

    /** 打印（查看）时间 */
    private Date printTime;

    /** 使用时间 */
    private Date usedTime;

    /** 使用时来源 IP */
    private String usedIp;

    /** 重置人（班主任） */
    private Long resetBy;

    /** 重置时间 */
    private Date resetTime;

    /** 创建时间 */
    private Date createTime;

}
