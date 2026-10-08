package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduImportError;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 导入行结果视图对象
 *
 * 失败行给出原始行号、原始内容与失败原因（REQ-IMP-006 / REQ-IMP-008）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduImportError.class)
public class EduImportErrorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long errorId;

    private String batchNo;

    private Integer rowNo;

    /** invalid 校验失败 / failed 执行失败 / skipped 跳过 */
    private String result;

    private String failReason;

    private String objectName;

    private String rawData;

    private Date createTime;

}
