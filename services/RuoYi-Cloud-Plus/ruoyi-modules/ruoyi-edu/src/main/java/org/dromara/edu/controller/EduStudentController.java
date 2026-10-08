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
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.bo.EduStudentBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduStudentVo;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.edu.service.IEduStudentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生管理
 *
 * 对应 operationId：listStudent / getStudent / addStudent / updateStudent
 * （docs/30-architecture/06-api-catalog.md 的 student 模块）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/student")
public class EduStudentController extends BaseController {

    private final IEduStudentService studentService;
    private final IEduImportExportService importExportService;

    /**
     * 查询学生列表
     *
     * 查询参数与 PRD「8.1 listStudent 查询参数」一致；数据范围在服务层拼进查询条件。
     */
    @SaCheckPermission("person.student:read")
    @GetMapping("/list")
    public TableDataInfo<EduStudentVo> list(EduStudentBo student, PageQuery pageQuery) {
        return studentService.queryPageList(student, pageQuery);
    }

    /**
     * 查询学生详情
     */
    @SaCheckPermission("person.student:read")
    @GetMapping("/{studentId}")
    public R<EduStudentVo> getInfo(@PathVariable Long studentId) {
        return R.ok(studentService.queryById(studentId));
    }

    /**
     * 新增学生（学号由系统统一发号，前端不提交）
     */
    @SaCheckPermission("person.student:create")
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduStudentBo student) {
        return toAjax(studentService.insertByBo(student));
    }

    /**
     * 修改学生（学号不可修改，REQ-STU-027 / GAP-030 裁决 B）
     */
    @SaCheckPermission("person.student:update")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduStudentBo student) {
        return toAjax(studentService.updateByBo(student));
    }

    /** 学生列表导出（敏感字段默认掩码，明文需 read_sensitive，REQ-IMP-028 / BR-IMP-012） */
    @SaCheckPermission("person.student:export")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> export(@RequestBody EduExportBo export) {
        export.setModuleCode("student");
        return R.ok(importExportService.exportData(export));
    }

}
