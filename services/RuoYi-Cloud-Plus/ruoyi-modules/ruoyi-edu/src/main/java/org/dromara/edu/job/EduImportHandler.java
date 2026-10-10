package org.dromara.edu.job;

import java.util.List;

/**
 * 模块导入校验器。
 *
 * 模板列由业务模块声明、引擎统一解析与落库（REQ-IMP-002）；实现类只负责「表头是什么」与「逐行怎么校验」。
 * 校验失败写 {@link EduImportRow#setFailReason(String)}，引擎负责写 `edu_import_error` 与批次计数。
 *
 * @author Codex
 */
public interface EduImportHandler {

    /** 模块编码（与 `EduImportValidateBo.moduleCode` 一致） */
    String moduleCode();

    /** 模板列（顺序固定，与 PRD 的「导入模板列」一致） */
    List<String> templateHeaders();

    /** 逐行校验：给 failReason 赋值即视为该行失败 */
    void validateRows(EduImportContext context, List<EduImportRow> rows);
}
