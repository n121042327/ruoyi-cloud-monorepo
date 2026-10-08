package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduDataGrantScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 授权范围明细视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduDataGrantScope.class)
public class EduDataGrantScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long grantScopeId;

    private Long grantId;

    private String scopeType;

    private String scopeId;

    private String resourceCode;

    /** 首轮仅 read / export（BR-DATA-015） */
    private String accessLevel;

    private Date createTime;

}
