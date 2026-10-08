package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 授权范围明细 edu_data_grant_scope
 *
 * 一份授权可覆盖多个学校 / 年级 / 班级；**首轮只有 read / export，不开放 write**（`BR-DATA-015`）。
 *
 * **本表 scope 为 tenant、with_audit 为 false、soft_delete 为 false**（schema.yaml）：
 * 只有 `create_time`，不继承 BaseEntity / TenantEntity。
 *
 * @author Codex
 */
@Data
@TableName("edu_data_grant_scope")
public class EduDataGrantScope {

    /** 范围明细 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long grantScopeId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 所属授权 */
    private Long grantId;

    /** school_tenant / grade / class */
    private String scopeType;

    /** 范围对象 ID */
    private String scopeId;

    /** 资源编码（取自权限矩阵，如 question_bank_item） */
    private String resourceCode;

    /** 首轮仅 read / export，不开放 write（BR-DATA-015） */
    private String accessLevel;

    /** 创建时间 */
    private Date createTime;

}
