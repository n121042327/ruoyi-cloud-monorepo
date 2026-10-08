package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.service.IEduImportExportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出引擎（通用导出入口）
 *
 * 覆盖 import-export 模块的 `exportData`（POST /edu/export）；各业务模块的导出端点
 * （exportStudent / exportTeacher / exportClass / exportGrade / exportSchool /
 * exportStreamSelection / exportClassRoster / exportPromotionTask 等）都委托到本引擎，
 * 避免每个模块各写一套导出与审计口径。
 *
 * 权限依据 `docs/10-prd/05-permission-matrix.yaml`：`data.export` 的 `export`。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu")
public class EduExportController extends BaseController {

    private final IEduImportExportService importExportService;

    /** 导出（同步或异步）；导出前重新解析数据范围，明文导出需 read_sensitive */
    @SaCheckPermission("data.export:export")
    @Log(title = "数据导出", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> exportData(@Validated @RequestBody EduExportBo export) {
        return R.ok(importExportService.exportData(export));
    }

}
