package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAuditArchiveBatch;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 日志归档批次视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAuditArchiveBatch.class)
public class EduAuditArchiveBatchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long archiveId;

    private String archiveNo;

    private Date rangeStart;

    private Date rangeEnd;

    private Long rowCount;

    private Long fileId;

    private String archiveStatus;

    private Long operatorId;

    private Date archiveTime;

    private String errorMsg;

}
