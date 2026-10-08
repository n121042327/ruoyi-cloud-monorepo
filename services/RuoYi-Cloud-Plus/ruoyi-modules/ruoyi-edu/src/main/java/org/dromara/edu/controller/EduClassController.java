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
import org.dromara.edu.domain.bo.EduClassBo;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.bo.EduImportExecuteBo;
import org.dromara.edu.domain.bo.EduImportValidateBo;
import org.dromara.edu.domain.vo.EduClassMemberVo;
import org.dromara.edu.domain.vo.EduClassVo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduImportExecuteResultVo;
import org.dromara.edu.domain.vo.EduImportValidateResultVo;
import org.dromara.edu.service.IEduClassService;
import org.dromara.edu.service.IEduImportExportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 班级管理
 *
 * 对应 operationId：listClass / getClass / addClass / updateClass / batchAddClass /
 * disableClass / mergeClass / assignHeadTeacher / listClassRoster / addClassRoster /
 * removeClassRoster / transferClass（docs/30-architecture/06-api-catalog.md 的 class 模块）。
 *
 * 导入导出四个 operationId（importRosterValidate / importRosterExecute / exportClass /
 * exportClassRoster）与教学班任教关系（listClassTeachingAssignment）在阶段 7 后续批次补齐。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/class")
public class EduClassController extends BaseController {

    private final IEduClassService classService;
    private final IEduImportExportService importExportService;

    /** 查询班级列表 */
    @SaCheckPermission("org.class:read")
    @GetMapping("/list")
    public TableDataInfo<EduClassVo> list(EduClassBo clazz, PageQuery pageQuery) {
        return classService.queryPageList(clazz, pageQuery);
    }

    /** 查询班级详情 */
    @SaCheckPermission("org.class:read")
    @GetMapping("/{classId}")
    public R<EduClassVo> getInfo(@PathVariable Long classId) {
        return R.ok(classService.queryById(classId));
    }

    /** 新增班级（行政班必填年级；教学班创建入口唯一在生成向导，CR-017） */
    @SaCheckPermission("org.class:create")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduClassBo clazz) {
        return toAjax(classService.insertByBo(clazz));
    }

    /** 修改班级（学年学期与年级一经创建不可修改，REQ-CLS-022） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduClassBo clazz) {
        return toAjax(classService.updateByBo(clazz));
    }

    /** 批量新增班级（同一年级一次建多班） */
    @SaCheckPermission("org.class:create")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/batch")
    public R<Void> batchAdd(@Validated @RequestBody EduClassBo clazz) {
        return toAjax(classService.batchAddClass(clazz));
    }

    /** 停用班级（有在读学生不允许删除、只允许停用，BR-CLASS-006） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{classId}/disable")
    public R<Void> disable(@PathVariable Long classId, @RequestParam String reason) {
        return toAjax(classService.disableClass(classId, reason));
    }

    /** 班级合并（源班级并入目标班级，源班级置停用、保留历史花名册） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/merge")
    public R<Void> merge(@Validated @RequestBody EduClassBo clazz) {
        return toAjax(classService.mergeClass(clazz));
    }

    /** 指定 / 变更班主任（唯一写入入口在班级管理，DP-01） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{classId}/head-teacher")
    public R<Void> assignHeadTeacher(@PathVariable Long classId, @Validated @RequestBody EduClassBo clazz) {
        clazz.setClassId(classId);
        return toAjax(classService.assignHeadTeacher(clazz));
    }

    /** 查询花名册 */
    @SaCheckPermission("org.class:read")
    @GetMapping("/{classId}/roster")
    public TableDataInfo<EduClassMemberVo> roster(@PathVariable Long classId, EduClassMemberBo member, PageQuery pageQuery) {
        return classService.queryRoster(classId, member, pageQuery);
    }

    /** 添加学生到行政班（学生班级归属的唯一写入入口，DP-01） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级花名册", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{classId}/roster")
    public R<Void> addRoster(@PathVariable Long classId, @Validated @RequestBody EduClassMemberBo member) {
        member.setClassId(classId);
        return toAjax(classService.addRoster(member));
    }

    /** 移出学生（追加式结束关系，不物理删除） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级花名册", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{classId}/roster/{studentId}")
    public R<Void> removeRoster(@PathVariable Long classId, @PathVariable Long studentId,
                                @RequestParam(required = false) String reason) {
        return toAjax(classService.removeRoster(classId, studentId, reason));
    }

    /** 调班 / 批量迁学生（单条 = 调班，多条 = 批量迁移，D-067） */
    @SaCheckPermission("org.class:update")
    @Log(title = "班级花名册", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/roster/transfer")
    public R<Void> transfer(@Validated @RequestBody EduClassMemberBo member) {
        return toAjax(classService.transferClass(member));
    }

    /** 班级花名册导入校验（统一走导入引擎，BR-IMP-001 / REQ-IMP-001） */
    @SaCheckPermission("org.class:import")
    @Log(title = "班级花名册", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/roster/import/validate")
    public R<EduImportValidateResultVo> importRosterValidate(
        @Validated @RequestBody EduImportValidateBo validate) {
        validate.setModuleCode("class_roster");
        return R.ok(importExportService.validateImportFile(validate));
    }

    /** 班级花名册导入执行（异步，按批次号幂等，REQ-IMP-015 / 017） */
    @SaCheckPermission("org.class:import")
    @Log(title = "班级花名册", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/roster/import/execute")
    public R<EduImportExecuteResultVo> importRosterExecute(
        @Validated @RequestBody EduImportExecuteBo execute) {
        return R.ok(importExportService.executeImport(execute));
    }

    /** 班级列表导出（统一走导出引擎，导出前重新解析数据范围，REQ-IMP-024 / 025） */
    @SaCheckPermission("org.class:export")
    @Log(title = "班级管理", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> export(@RequestBody EduExportBo export) {
        export.setModuleCode("class");
        return R.ok(importExportService.exportData(export));
    }

    /** 班级花名册导出 */
    @SaCheckPermission("org.class:export")
    @Log(title = "班级花名册", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/{classId}/roster/export")
    public R<EduExportResultVo> exportRoster(@PathVariable Long classId, @RequestBody EduExportBo export) {
        export.setModuleCode("class_roster");
        export.setClassId(classId);
        return R.ok(importExportService.exportData(export));
    }

}
