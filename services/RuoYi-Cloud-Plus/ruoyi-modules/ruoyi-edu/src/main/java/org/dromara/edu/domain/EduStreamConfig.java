package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 选科配置 edu_stream_config
 *
 * 按学校 + 学年学期唯一（REQ-STR-004）。截止时间按当前时间**实时比较**，不依赖定时任务刷状态（REQ-STR-005）。
 * 本表没有 del_flag，配置失效写 `config_status='inactive'`。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_stream_config")
public class EduStreamConfig extends TenantEntity {

    /** 配置 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long configId;

    /** 学校归属 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 开放期起点 */
    private Date streamOpenFrom;

    /** 截止时间 */
    private Date streamDeadline;

    /** 逾期变更是否需校级管理员审批（BR-STREAM-005） */
    private Boolean overdueRequiresApproval;

    /** 生效 / 已失效 */
    private String configStatus;

}
