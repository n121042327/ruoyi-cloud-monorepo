package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 数据共享授权 edu_data_grant
 *
 * **授权对象仅限教学资源**（题库习题、试卷等），学生 / 班级 / 年级 / 教师 / 成绩等业务数据
 * 一律不跨校（`BR-DATA-018`）；授权由运营方直接创建即生效、免审批（`BR-DATA-017`）；
 * 可设置生效有效期（`BR-DATA-013`）；每次访问额外审计（`BR-DATA-012`）；
 * 过期与撤销保留历史（`BR-DATA-016`）。
 *
 * **本表 scope 为 tenant、with_audit 为 true、soft_delete 为 false**（schema.yaml）：
 * 继承 TenantEntity，**不加** `@TableLogic delFlag`。
 *
 * 本批只落实体与数据层（为 `DataScopeResolver` 的「叠加共享授权」提供来源表），
 * 授权管理端点不在 openapi 的 181 个 operationId 内，不另造接口。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_data_grant")
public class EduDataGrant extends TenantEntity {

    /** 授权 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long grantId;

    /** 业务编号（幂等键，唯一） */
    private String grantNo;

    /** 授权人（运营方用户） */
    private Long grantorUserId;

    /** tenant / user / role */
    private String granteeType;

    /** 被授权对象 ID */
    private String granteeId;

    /** 事由标题 */
    private String title;

    /** 共享原因 */
    private String reason;

    /** 资源类型集合（逗号分隔，如 question_bank_item,exam_paper） */
    private String resourceTypes;

    /** 资源范围细化（哪些题库 / 试卷；为空表示该类型全部），物理列为 json，按文本读写 */
    private String resourceScope;

    /** 生效时间 */
    private Date effectiveStart;

    /** 失效时间（为空表示长期有效） */
    private Date effectiveEnd;

    /** 草稿 / 生效 / 已撤销 / 已过期 */
    private String grantStatus;

    /** 撤销人 */
    private Long revokeBy;

    /** 撤销时间 */
    private Date revokeTime;

    /** 撤销原因 */
    private String revokeReason;

}
