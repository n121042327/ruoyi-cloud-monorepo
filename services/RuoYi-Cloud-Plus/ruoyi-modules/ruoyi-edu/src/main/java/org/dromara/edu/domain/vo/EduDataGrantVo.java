package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduDataGrant;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据共享授权视图对象
 *
 * 本批只为数据层提供转换落点（授权管理端点不在 openapi 的 181 个 operationId 内）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduDataGrant.class)
public class EduDataGrantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long grantId;

    private String grantNo;

    private Long grantorUserId;

    private String granteeType;

    private String granteeId;

    private String title;

    private String reason;

    private String resourceTypes;

    private String resourceScope;

    private Date effectiveStart;

    private Date effectiveEnd;

    private String grantStatus;

    private Long revokeBy;

    private Date revokeTime;

    private String revokeReason;

}
