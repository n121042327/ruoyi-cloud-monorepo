package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStreamConfig;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 选科配置视图对象（与前端 StreamConfigVO 对齐）
 *
 * periodStatus 由服务层按当前时间**实时比较**得出（REQ-STR-005），不依赖定时任务刷状态。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStreamConfig.class)
public class EduStreamConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long configId;

    private Long schoolId;

    private Long termId;

    private String termName;

    /** 开放日期（前端字段名 openFrom） */
    private Date openFrom;

    /** 截止时间（前端字段名 deadline） */
    private Date deadline;

    /** 数据库原值 */
    private Date streamOpenFrom;

    private Date streamDeadline;

    /** 逾期变更是否需校级管理员审批 */
    private Boolean overdueRequiresApproval;

    /** 未开始 / 进行中 / 已截止 */
    private String periodStatus;

    /** 生效 / 已失效 */
    private String configStatus;

}
