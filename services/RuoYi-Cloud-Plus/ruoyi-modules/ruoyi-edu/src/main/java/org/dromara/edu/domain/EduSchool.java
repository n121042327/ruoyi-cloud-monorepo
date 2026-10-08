package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 学校 edu_school
 *
 * 「一个学校对应一个租户」（BR-ORG-002）：本表 tenantId 唯一，学校与租户的绑定在接入时写入；
 * 层级不超过三级（运营方 → 集团 → 学校，BR-ORG-003），上级与根租户写在 parentTenantId / rootTenantId。
 *
 * 字段以唯一事实源 docs/40-detailed-design/database/schema.yaml 的 edu_school 为准；
 * 物理主键是 `id`，业务名按接口契约叫 schoolId。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_school")
public class EduSchool extends TenantEntity {

    /** 学校 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long schoolId;

    /** 学校编码（父租户内唯一，BR-ORG-011） */
    private String schoolCode;

    /** 学校名称 */
    private String schoolName;

    /** 简称 */
    private String shortName;

    /** 上级租户（集团 / 运营方） */
    private String parentTenantId;

    /** 根租户（运营方） */
    private String rootTenantId;

    /** 学校类型（公办 / 民办 / 其他） */
    private String schoolType;

    /** 地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 学校状态：active 正常 / disabled 已停用 */
    private String schoolStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
