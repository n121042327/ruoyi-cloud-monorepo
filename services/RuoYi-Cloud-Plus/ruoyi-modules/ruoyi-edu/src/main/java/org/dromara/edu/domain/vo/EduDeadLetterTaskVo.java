package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduDeadLetterTask;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 死信任务视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduDeadLetterTask.class)
public class EduDeadLetterTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deadLetterId;

    private String taskNo;

    private String taskType;

    private Date deadTime;

    private Integer retryCount;

    private String lastError;

    private String batchNo;

    /** replayable 待重放 / replayed 已重放 */
    private String replayStatus;

    private Long replayBy;

    private Date replayTime;

    private String replayReason;

    private String replayTaskStatus;

}
