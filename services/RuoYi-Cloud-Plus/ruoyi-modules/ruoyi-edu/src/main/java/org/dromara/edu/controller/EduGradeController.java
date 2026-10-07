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
import org.dromara.edu.domain.bo.EduGradeBo;
import org.dromara.edu.domain.vo.EduGradeLeaderVo;
import org.dromara.edu.domain.vo.EduGradeVo;
import org.dromara.edu.service.IEduGradeService;
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
 * 年级管理
 *
 * 对应 operationId：listGrade / getGrade / addGrade / updateGrade / batchAddGrade / removeGrade /
 * archiveGrade / listGradeLeader / saveGradeLeader / removeGradeLeader / getGradePromotionView
 * （docs/30-architecture/06-api-catalog.md 的 grade 模块）。
 *
 * exportGrade 需要导入导出引擎，放在阶段 7 后续批次。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/grade")
public class EduGradeController extends BaseController {

    private final IEduGradeService gradeService;

    /** 查询年级列表 */
    @SaCheckPermission("org.grade:read")
    @GetMapping("/list")
    public TableDataInfo<EduGradeVo> list(EduGradeBo grade, PageQuery pageQuery) {
        return gradeService.queryPageList(grade, pageQuery);
    }

    /** 查询年级详情 */
    @SaCheckPermission("org.grade:read")
    @GetMapping("/{gradeId}")
    public R<EduGradeVo> getInfo(@PathVariable Long gradeId) {
        return R.ok(gradeService.queryById(gradeId));
    }

    /** 新增年级（学段与学段内序号一经创建不可修改，REQ-GRD-018） */
    @SaCheckPermission("org.grade:create")
    @Log(title = "年级管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduGradeBo grade) {
        return toAjax(gradeService.insertByBo(grade));
    }

    /** 修改年级（不允许跨学段改名，BR-GRADE-006） */
    @SaCheckPermission("org.grade:update")
    @Log(title = "年级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduGradeBo grade) {
        return toAjax(gradeService.updateByBo(grade));
    }

    /** 批量新增年级（一个学段一次建多个年级） */
    @SaCheckPermission("org.grade:create")
    @Log(title = "年级管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/batch")
    public R<Void> batchAdd(@Validated @RequestBody EduGradeBo grade) {
        return toAjax(gradeService.batchAddGrade(grade));
    }

    /** 删除年级（有班级或学生关系时不允许删除，BR-GRADE-004） */
    @SaCheckPermission("org.grade:remove")
    @Log(title = "年级管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{gradeId}")
    public R<Void> remove(@PathVariable Long gradeId) {
        return toAjax(gradeService.removeGrade(gradeId));
    }

    /** 归档年级（有班级或学生关系时只允许归档） */
    @SaCheckPermission("org.grade:update")
    @Log(title = "年级管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{gradeId}/archive")
    public R<Void> archive(@PathVariable Long gradeId, @RequestParam String reason) {
        return toAjax(gradeService.archiveGrade(gradeId, reason));
    }

    /** 查询年级主任任职 */
    @SaCheckPermission("org.grade:read")
    @GetMapping("/{gradeId}/leader")
    public R<List<EduGradeLeaderVo>> listLeader(@PathVariable Long gradeId,
                                                @RequestParam(required = false) Long termId) {
        return R.ok(gradeService.listLeader(gradeId, termId));
    }

    /** 保存 / 变更年级主任任职（DS-05 的写入入口） */
    @SaCheckPermission("org.grade:update")
    @Log(title = "年级主任任职", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{gradeId}/leader")
    public R<Void> saveLeader(@PathVariable Long gradeId, @Validated @RequestBody EduGradeBo grade) {
        grade.setGradeId(gradeId);
        return toAjax(gradeService.saveLeader(grade));
    }

    /** 年级主任离任（置 status=0，不物理删除） */
    @SaCheckPermission("org.grade:update")
    @Log(title = "年级主任任职", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{gradeId}/leader/{leaderId}")
    public R<Void> removeLeader(@PathVariable Long gradeId, @PathVariable Long leaderId) {
        return toAjax(gradeService.removeLeader(leaderId));
    }

    /**
     * 年级升班只读视图
     *
     * 升班的唯一执行入口是升班模块（AGENTS 第 7 节模块边界），本接口只提供「学段内序号 +1」的只读参考。
     */
    @SaCheckPermission("org.grade:read")
    @GetMapping("/promotion-view")
    public R<List<EduGradeVo>> promotionView(@RequestParam(required = false) Long schoolId,
                                             @RequestParam(required = false) Long termId) {
        return R.ok(gradeService.promotionView(schoolId, termId));
    }

}
