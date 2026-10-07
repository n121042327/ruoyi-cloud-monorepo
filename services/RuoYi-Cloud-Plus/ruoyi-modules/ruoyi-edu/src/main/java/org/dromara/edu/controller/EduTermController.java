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
import org.dromara.edu.domain.bo.EduAcademicYearBo;
import org.dromara.edu.domain.bo.EduTermBo;
import org.dromara.edu.domain.vo.EduAcademicYearVo;
import org.dromara.edu.domain.vo.EduTermVo;
import org.dromara.edu.domain.vo.TermReferenceVo;
import org.dromara.edu.service.IEduTermService;
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
 * 学年学期管理
 *
 * 对应 operationId：listAcademicYear / getAcademicYear / addAcademicYear / updateAcademicYear /
 * archiveAcademicYear / revokeArchiveAcademicYear / listTerm / saveTerm / removeTerm /
 * getCurrentTerm / setCurrentTerm / checkTermReference
 * （docs/30-architecture/06-api-catalog.md 的 term 模块，全部 12 个）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/term")
public class EduTermController extends BaseController {

    private final IEduTermService termService;

    // ==================== 学年 ====================

    /** 查询学年列表（按学校维度分组，按学年编码倒序） */
    @SaCheckPermission("org.term:read")
    @GetMapping("/year/list")
    public TableDataInfo<EduAcademicYearVo> listYear(EduAcademicYearBo year, PageQuery pageQuery) {
        return termService.queryYearPageList(year, pageQuery);
    }

    /** 查询学年详情 */
    @SaCheckPermission("org.term:read")
    @GetMapping("/year/{academicYearId}")
    public R<EduAcademicYearVo> getYear(@PathVariable Long academicYearId) {
        return R.ok(termService.queryYearById(academicYearId));
    }

    /** 新建学年（编码 YYYY-YYYY 连续两年；同步创建默认学期结构，REQ-TERM-012） */
    @SaCheckPermission("org.term:create")
    @Log(title = "学年学期", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/year")
    public R<Void> addYear(@Validated @RequestBody EduAcademicYearBo year) {
        return toAjax(termService.insertYear(year));
    }

    /** 编辑学年 */
    @SaCheckPermission("org.term:update")
    @Log(title = "学年学期", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/year")
    public R<Void> editYear(@Validated @RequestBody EduAcademicYearBo year) {
        return toAjax(termService.updateYear(year));
    }

    /** 归档学年（有引用时只允许归档，不允许删除，REQ-TERM-028） */
    @SaCheckPermission("org.term:update")
    @Log(title = "学年学期", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/year/{academicYearId}/archive")
    public R<Void> archiveYear(@PathVariable Long academicYearId, @RequestParam String reason) {
        return toAjax(termService.archiveYear(academicYearId, reason));
    }

    /** 撤销归档（误操作纠正，写审计，REQ-TERM-033） */
    @SaCheckPermission("org.term:update")
    @Log(title = "学年学期", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/year/{academicYearId}/archive/revoke")
    public R<Void> revokeArchiveYear(@PathVariable Long academicYearId, @RequestParam String reason) {
        return toAjax(termService.revokeArchiveYear(academicYearId, reason));
    }

    // ==================== 学期 ====================

    /** 查询学期列表 */
    @SaCheckPermission("org.term:read")
    @GetMapping("/list")
    public TableDataInfo<EduTermVo> listTerm(EduTermBo term, PageQuery pageQuery) {
        return termService.queryTermPageList(term, pageQuery);
    }

    /** 新建 / 编辑学期（有 termId 为编辑） */
    @SaCheckPermission("org.term:create")
    @Log(title = "学年学期", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> saveTerm(@Validated @RequestBody EduTermBo term) {
        return toAjax(termService.saveTerm(term));
    }

    /** 删除学期（已被班级 / 任教关系 / 花名册引用的学期不允许删除，REQ-TERM-019） */
    @SaCheckPermission("org.term:remove")
    @Log(title = "学年学期", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{termId}")
    public R<Void> removeTerm(@PathVariable Long termId, @RequestParam(required = false) String reason) {
        return toAjax(termService.removeTerm(termId, reason));
    }

    /** 查询当前学年学期（各模块默认学期上下文的权威来源） */
    @SaCheckPermission("org.term:read")
    @GetMapping("/current")
    public R<EduTermVo> getCurrentTerm(@RequestParam(required = false) Long schoolId) {
        return R.ok(termService.getCurrentTerm(schoolId));
    }

    /** 设为当前学年学期（同一学校唯一；已归档的学年 / 学期不允许设为当前） */
    @SaCheckPermission("org.term:update")
    @Log(title = "学年学期", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{termId}/set-current")
    public R<Void> setCurrentTerm(@PathVariable Long termId) {
        return toAjax(termService.setCurrentTerm(termId));
    }

    /** 引用检查（四类引用：班级 / 任教关系 / 学生班级关系 / 学生选科，REQ-TERM-029 / 034） */
    @SaCheckPermission("org.term:read")
    @GetMapping("/{academicYearId}/reference")
    public R<TermReferenceVo> checkReference(@PathVariable Long academicYearId) {
        return R.ok(termService.checkReference(academicYearId));
    }

}
