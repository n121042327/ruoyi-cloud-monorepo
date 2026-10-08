package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 跨校转学单 edu_transfer_order
 *
 * 接收动作本身即审批（已确认 3）；接收前不计入在读数（REQ-PRM-053）；未报到前可撤销接收（REQ-PRM-054）；
 * 同一学生未完成转学单唯一（REQ-PRM-057）。转学单只暴露必要字段给转入校
 * （学号 / 姓名 / 性别 / 原学校 / 原年级，REQ-PRM-055）。
 *
 * 本表没有 del_flag（soft_delete 为 false）：转学单只按状态流转，不物理删除。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_transfer_order")
public class EduTransferOrder extends TenantEntity {

    /** 转学单 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long transferId;

    /** 当前租户对应的学校归属 */
    private Long schoolId;

    /** 转学单号（唯一） */
    private String transferNo;

    /** 学生主体 ID */
    private Long studentId;

    /** 转出学校租户 */
    private String fromTenantId;

    /** 转出学校 */
    private Long fromSchoolId;

    /** 原年级 */
    private Long fromGradeId;

    /** 转入学校租户 */
    private String toTenantId;

    /** 转入学校 */
    private Long toSchoolId;

    /** 目标年级 */
    private Long toGradeId;

    /** 目标班级 */
    private Long toClassId;

    /** 待接收 / 已接收 / 已报到 / 已撤销 / 已驳回 */
    private String transferStatus;

    /** 申请人（转出校） */
    private Long applyBy;

    /** 申请时间 */
    private Date applyTime;

    /** 接收人（转入校，接收即审批） */
    private Long acceptBy;

    /** 接收时间 */
    private Date acceptTime;

    /** 报到时间 */
    private Date checkInTime;

    /** 撤销人 */
    private Long cancelBy;

    /** 撤销时间 */
    private Date cancelTime;

    /** 备注 */
    private String remark;

}
