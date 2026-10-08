package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 导入模板 edu_import_template
 *
 * 模板携带版本号，字段字典变更必须升版本；旧版本模板**仍可下载**但页面标注「已过期」并强提示
 * （REQ-IMP-003 / BR-IMP-007）；`(module_code, template_version)` 唯一。
 *
 * **本表 scope 为 tenant、with_audit 为 true、soft_delete 为 false**（schema.yaml）：
 * 继承 TenantEntity 拿到租户键与审计列，**无 school_id**，且**不加** `@TableLogic delFlag`。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_import_template")
public class EduImportTemplate extends TenantEntity {

    /** 模板 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long templateId;

    /** 模块（student / teacher / class_roster） */
    private String moduleCode;

    /** 模板版本（如 student-v3） */
    private String templateVersion;

    /** 列数 */
    private Integer columnCount;

    /** 模板文件引用 */
    private Long fileId;

    /** 版本过期时间（过期后强提示） */
    private Date expireTime;

    /** 1 当前版本 / 0 历史版本 */
    private String status;

}
