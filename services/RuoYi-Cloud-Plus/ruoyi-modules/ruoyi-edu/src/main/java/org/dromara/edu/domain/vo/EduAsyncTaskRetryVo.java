package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAsyncTaskRetry;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务重试记录视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAsyncTaskRetry.class)
public class EduAsyncTaskRetryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long retryId;

    private String taskNo;

    private Integer retryNo;

    /** success / failed / timeout */
    private String result;

    private String errorMsg;

    private Date createTime;

}
