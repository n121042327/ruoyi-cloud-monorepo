package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduImportTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 导入模板视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduImportTemplate.class)
public class EduImportTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long templateId;

    private String moduleCode;

    private String templateVersion;

    private Integer columnCount;

    private Long fileId;

    private Date expireTime;

    /** 1 当前版本 / 0 历史版本 */
    private String status;

    /** 版本是否已过期（过期仍可下载，但页面强提示，REQ-IMP-003） */
    private Boolean expired;

}
