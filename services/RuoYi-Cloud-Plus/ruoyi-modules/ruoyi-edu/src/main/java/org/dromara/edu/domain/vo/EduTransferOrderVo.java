package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTransferOrder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 跨校转学单视图对象
 *
 * 只暴露必要字段给转入校（学号 / 姓名 / 性别 / 原学校 / 原年级，REQ-PRM-055）；
 * 转入校看不到转出校的其它业务数据。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTransferOrder.class)
public class EduTransferOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long transferId;

    private String transferNo;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String gender;

    private Long fromSchoolId;

    private String fromSchoolName;

    private Long fromGradeId;

    private String fromGradeName;

    private Long toSchoolId;

    private String toSchoolName;

    private Long toGradeId;

    private String toGradeName;

    private Long toClassId;

    private String toClassName;

    /** pending 待接收 / received 已接收 / checked_in 已报到 / canceled 已撤销 / rejected 已驳回 */
    private String status;

    private Date effectiveDate;

    private Date applyTime;

    private Date acceptTime;

    private Date checkInTime;

    private String remark;

}
