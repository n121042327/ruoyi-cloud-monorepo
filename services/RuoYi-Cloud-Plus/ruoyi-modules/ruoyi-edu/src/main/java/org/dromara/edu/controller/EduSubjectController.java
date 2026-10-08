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
import org.dromara.edu.domain.bo.EduSubjectBo;
import org.dromara.edu.domain.bo.EduSubjectStageBo;
import org.dromara.edu.domain.vo.EduSubjectOptionVo;
import org.dromara.edu.domain.vo.EduSubjectVo;
import org.dromara.edu.domain.vo.SubjectReferenceVo;
import org.dromara.edu.service.IEduSubjectService;
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

import java.util.List;

/**
 * 学科与配置
 *
 * 对应 operationId：listSubject / getSubject / removeSubject / addSubject / updateSubject /
 * batchInitSubject / saveSubjectStreamRole / saveSubjectStage / disableSubject / enableSubject /
 * checkSubjectReference / listSubjectOption（subject 模块全部 12 个）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/subject")
public class EduSubjectController extends BaseController {

    private final IEduSubjectService subjectService;

    /** 查询学科列表（含各学科已启用学段） */
    @SaCheckPermission("org.subject:read")
    @GetMapping("/list")
    public TableDataInfo<EduSubjectVo> list(EduSubjectBo subject, PageQuery pageQuery) {
        return subjectService.queryPageList(subject, pageQuery);
    }

    /** 学科下拉选项（按学段过滤，未开设的学段不返回） */
    @SaCheckPermission("org.subject:read")
    @GetMapping("/option")
    public R<List<EduSubjectOptionVo>> listOption(@RequestParam(required = false) String stageCode) {
        return R.ok(subjectService.listSubjectOption(stageCode));
    }

    /** 查询学科详情 */
    @SaCheckPermission("org.subject:read")
    @GetMapping("/{subjectId}")
    public R<EduSubjectVo> getInfo(@PathVariable Long subjectId) {
        return R.ok(subjectService.queryById(subjectId));
    }

    /** 新增学科（编码校内唯一，BR-SUBJECT-001） */
    @SaCheckPermission("org.subject:create")
    @Log(title = "学科配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduSubjectBo subject) {
        return toAjax(subjectService.insertByBo(subject));
    }

    /** 修改学科（学科编码不提供修改入口） */
    @SaCheckPermission("org.subject:update")
    @Log(title = "学科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduSubjectBo subject) {
        return toAjax(subjectService.updateByBo(subject));
    }

    /** 删除学科（有引用时只允许停用） */
    @SaCheckPermission("org.subject:remove")
    @Log(title = "学科配置", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{subjectId}")
    public R<Void> remove(@PathVariable Long subjectId, @RequestParam(required = false) String reason) {
        return toAjax(subjectService.removeSubject(subjectId, reason));
    }

    /** 批量初始化标准学科模板（幂等：已存在的编码跳过） */
    @SaCheckPermission("org.subject:create")
    @Log(title = "学科配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/batch-init")
    public R<Void> batchInit(@Validated @RequestBody EduSubjectBo subject) {
        return toAjax(subjectService.batchInitSubject(subject));
    }

    /** 设置学科的选科角色（primary / secondary / none） */
    @SaCheckPermission("org.subject:update")
    @Log(title = "学科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{subjectId}/stream-role")
    public R<Void> saveStreamRole(@PathVariable Long subjectId, @Validated @RequestBody EduSubjectBo subject) {
        return toAjax(subjectService.saveStreamRole(subjectId, subject));
    }

    /** 设置学科启用的学段（未开设的学段不允许启用，REQ-SUB-026） */
    @SaCheckPermission("org.subject:update")
    @Log(title = "学科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{subjectId}/stage")
    public R<Void> saveSubjectStage(@PathVariable Long subjectId, @Validated @RequestBody EduSubjectStageBo stage) {
        return toAjax(subjectService.saveSubjectStage(subjectId, stage));
    }

    /** 停用学科 */
    @SaCheckPermission("org.subject:update")
    @Log(title = "学科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{subjectId}/disable")
    public R<Void> disable(@PathVariable Long subjectId, @RequestParam String reason) {
        return toAjax(subjectService.disableSubject(subjectId, reason));
    }

    /** 启用学科 */
    @SaCheckPermission("org.subject:update")
    @Log(title = "学科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{subjectId}/enable")
    public R<Void> enable(@PathVariable Long subjectId, @RequestParam(required = false) String reason) {
        return toAjax(subjectService.enableSubject(subjectId, reason));
    }

    /** 引用检查（任教关系 / 教学班 / 学生选科） */
    @SaCheckPermission("org.subject:read")
    @GetMapping("/{subjectId}/reference")
    public R<SubjectReferenceVo> checkReference(@PathVariable Long subjectId) {
        return R.ok(subjectService.checkReference(subjectId));
    }

}
