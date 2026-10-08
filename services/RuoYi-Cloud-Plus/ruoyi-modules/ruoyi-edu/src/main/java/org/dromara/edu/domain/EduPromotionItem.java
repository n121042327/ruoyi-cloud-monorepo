package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 升班明细 edu_promotion_item
 *
 * 逐学生结果。幂等键 = (task_id, student_id)（REQ-PRM-029）；重试只处理失败 / 跳过项，
 * 不重复写已成功的行（REQ-PRM-032 / 037）。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：只有 `create_time`，
 * 没有 create_by / update_by / update_time / del_flag，因此**不继承** BaseEntity / TenantEntity，
 * 只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_promotion_item")
public class EduPromotionItem {

    /** 明细 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long itemId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 升班任务 */
    private Long taskId;

    /** 学生主体 ID */
    private Long studentId;

    /** 源班级 */
    private Long sourceClassId;

    /** 目标班级 */
    private Long targetClassId;

    /** 结果类型：promote 升级 / repeat 留级 / transfer 转班 / graduate 毕业 / skip 跳过 */
    private String resultType;

    /** 明细状态：pending 待处理 / success 成功 / failed 失败 / skipped 已跳过 */
    private String itemStatus;

    /** 失败原因 */
    private String errorMsg;

    /** 调整方式（手工指定目标班级时写入） */
    private String adjustMode;

    /** 创建时间 */
    private Date createTime;

}
