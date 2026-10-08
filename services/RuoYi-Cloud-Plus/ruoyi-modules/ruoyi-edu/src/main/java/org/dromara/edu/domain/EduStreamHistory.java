package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 选科历史 edu_stream_history
 *
 * 追加式，不可删除不可修改（REQ-STR-043）；跨学年学期保留（REQ-STR-044）。
 *
 * **本表 with_audit 为 false**（schema.yaml）：只有 operate_time，没有 create_by / update_time / del_flag，
 * 因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_stream_history")
public class EduStreamHistory {

    /** 历史 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long historyId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 学年学期 */
    private Long termId;

    /** 原组合 */
    private String beforeCombination;

    /** 新组合 */
    private String afterCombination;

    /** 首次提交 / 开放期内自助变更 / 变更申请通过 */
    private String changeType;

    /** 关联变更申请单号 */
    private String requestNo;

    /** 变更原因 */
    private String reason;

    /** 操作人 */
    private Long operator;

    /** 操作时间 */
    private Date operateTime;

}
