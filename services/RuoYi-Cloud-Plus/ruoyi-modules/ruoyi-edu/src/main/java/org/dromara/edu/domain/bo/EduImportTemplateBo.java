package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduImportTemplate;

/**
 * 导入模板业务对象
 *
 * 覆盖模板清单与模板下载两个 operationId 的入参。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduImportTemplate.class, reverseConvertGenerate = false)
public class EduImportTemplateBo extends BaseEntity {

    /** 模板 ID */
    private Long templateId;

    /** 模块（student / teacher / class_roster） */
    private String moduleCode;

    /** 模板版本，留空表示当前版本 */
    private String templateVersion;

    /** 1 当前版本 / 0 历史版本 */
    private String status;

    /** 关键字：模块或版本模糊匹配 */
    private String keyword;

}
