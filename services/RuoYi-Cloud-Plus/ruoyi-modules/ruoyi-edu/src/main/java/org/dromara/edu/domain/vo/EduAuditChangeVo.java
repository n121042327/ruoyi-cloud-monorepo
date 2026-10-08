package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAuditChange;

import java.io.Serial;
import java.io.Serializable;

/**
 * 日志变更明细视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAuditChange.class)
public class EduAuditChangeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long changeId;

    private Long logId;

    private String fieldName;

    /** 变更前值（敏感字段掩码） */
    private String beforeValue;

    /** 变更后值（敏感字段掩码） */
    private String afterValue;

}
