package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStreamHistory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 选科历史视图对象（与前端 StreamHistoryVO 对齐）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStreamHistory.class)
public class EduStreamHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long historyId;

    private Long studentId;

    private Long termId;

    /** 首次提交 / 开放期内自助变更 / 变更申请通过 */
    private String changeType;

    private String beforeCombination;

    private String afterCombination;

    private Date effectiveDate;

    private Date operateTime;

    private String operator;

    private String reason;

}
