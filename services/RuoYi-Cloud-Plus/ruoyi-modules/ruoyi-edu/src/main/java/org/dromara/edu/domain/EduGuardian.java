package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 监护人主体 edu_guardian
 *
 * 平台级实体，**不设 tenant_id**（D-030 / GAP-015）——因此继承 BaseEntity 而非 TenantEntity。
 * 手机号是家长登录名候选且平台唯一，一个家长可对应多个孩子（跨租户、多对多）；账号关联 sys_user。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_guardian")
public class EduGuardian extends BaseEntity {

    /** 监护人 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long guardianId;

    /** 监护人姓名 */
    private String guardianName;

    /** 手机号（平台唯一；家长登录名；敏感字段） */
    private String guardianPhone;

    /** 证件号码（可选；敏感字段） */
    private String idCardNo;

    /** 关联系统账号 */
    private Long userId;

    /** 1 正常 / 0 停用 */
    private String status;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
