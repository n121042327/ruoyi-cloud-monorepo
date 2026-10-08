package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduImportErrorBo;
import org.dromara.edu.domain.bo.EduImportExecuteBo;
import org.dromara.edu.domain.bo.EduImportTemplateBo;
import org.dromara.edu.domain.bo.EduImportValidateBo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.domain.vo.EduImportErrorVo;
import org.dromara.edu.domain.vo.EduImportExecuteResultVo;
import org.dromara.edu.domain.vo.EduImportTemplateVo;
import org.dromara.edu.domain.vo.EduImportValidateResultVo;
import org.dromara.edu.service.IEduImportExportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导入引擎（统一导入向导）
 *
 * 覆盖 import-export 模块的导入 6 个 operationId：listImportTemplate / downloadImportTemplate /
 * validateImportFile / executeImport / downloadImportFailedRows / downloadImportResult；
 * 另加 1 个**辅助端点** `GET /{batchNo}/rows`（校验结果分页展示，REQ-IMP-006 要求「分页展示」，
 * 契约里只有文件下载，因此按前几批的既有做法登记辅助端点并在 CR-091 说明）。
 *
 * 权限依据 `docs/10-prd/05-permission-matrix.yaml`：`data.import`
 * （读清单与下载用 `read`，发起导入用 `import`）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/import")
public class EduImportController extends BaseController {

    private final IEduImportExportService importExportService;

    /** 模板清单与当前版本 */
    @SaCheckPermission("data.import:read")
    @GetMapping("/template")
    public TableDataInfo<EduImportTemplateVo> listTemplate(EduImportTemplateBo template, PageQuery pageQuery) {
        return importExportService.queryTemplatePageList(template, pageQuery);
    }

    /** 模板下载（旧版本仍可下载，过期给强提示） */
    @SaCheckPermission("data.import:read")
    @GetMapping("/template/{module}")
    public R<EduFileRefVo> downloadTemplate(@PathVariable String module,
                                            @RequestParam(required = false) String version) {
        return R.ok(importExportService.resolveTemplateDownload(module, version));
    }

    /** 上传文件后同步校验（校验阶段不写业务数据） */
    @SaCheckPermission("data.import:import")
    @Log(title = "数据导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/validate")
    public R<EduImportValidateResultVo> validate(@Validated @RequestBody EduImportValidateBo validate) {
        return R.ok(importExportService.validateImportFile(validate));
    }

    /** 确认执行（异步），按批次号幂等 */
    @SaCheckPermission("data.import:import")
    @Log(title = "数据导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/execute")
    public R<EduImportExecuteResultVo> execute(@Validated @RequestBody EduImportExecuteBo execute) {
        return R.ok(importExportService.executeImport(execute));
    }

    /** 失败行下载（CSV，含原始行号、原始内容与失败原因） */
    @SaCheckPermission("data.import:read")
    @Log(title = "数据导入", businessType = BusinessType.EXPORT)
    @GetMapping("/{batchNo}/failed-rows")
    public R<EduFileRefVo> downloadFailedRows(@PathVariable String batchNo) {
        return R.ok(importExportService.resolveBatchFile(batchNo, "failed_rows"));
    }

    /** 结果摘要与学号对照表下载 */
    @SaCheckPermission("data.import:read")
    @Log(title = "数据导入", businessType = BusinessType.EXPORT)
    @GetMapping("/{batchNo}/result")
    public R<EduFileRefVo> downloadResult(@PathVariable String batchNo) {
        return R.ok(importExportService.resolveBatchFile(batchNo, "result"));
    }

    /** 【辅助端点】校验 / 执行结果按行分页查询（REQ-IMP-006 要求分页展示） */
    @SaCheckPermission("data.import:read")
    @GetMapping("/{batchNo}/rows")
    public TableDataInfo<EduImportErrorVo> listRows(@PathVariable String batchNo, EduImportErrorBo rowQuery,
                                                    PageQuery pageQuery) {
        rowQuery.setBatchNo(batchNo);
        return importExportService.queryBatchRowPageList(rowQuery, pageQuery);
    }

}
