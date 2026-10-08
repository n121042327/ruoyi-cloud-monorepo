package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStreamChangeRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 选科变更申请视图对象（与前端 StreamChangeRequestVO 对齐）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStreamChangeRequest.class)
public class EduStreamChangeRequestVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long requestId;

    private String requestNo;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String gradeName;

    private String className;

    private String beforeCombination;

    private String afterCombination;

    private String reason;

    private Date applyTime;

    /** 草稿 / 待审批 / 已通过 / 已驳回 / 已撤销 */
    private String status;

    /** 数据库原值 */
    private String requestStatus;

    private String approveOpinion;

    private Date approveTime;

}
