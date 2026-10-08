package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAuditLog;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 操作日志视图对象
 *
 * 详情展示完整字段并以 diff 形式展示变更前后值（`REQ-AUD-022`）；
 * 变更前后值中的证件号 / 手机号按掩码展示（`BR-AUDIT-007` / `NFR-SEC-06`）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAuditLog.class)
public class EduAuditLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long logId;

    private Long schoolId;

    private String actionType;

    private String moduleCode;

    private String objectType;

    private String objectId;

    private String objectName;

    private Long operatorId;

    /** 操作人姓名（展示用，连接 sys_user 补） */
    private String operatorName;

    private String operatorRole;

    private String clientIp;

    private String requestId;

    private String batchNo;

    private String actionResult;

    private String source;

    private String detail;

    private Date logTime;

    /** 变更明细（详情与时间线返回，`REQ-AUD-003`） */
    private List<EduAuditChangeVo> changeItems;

}
