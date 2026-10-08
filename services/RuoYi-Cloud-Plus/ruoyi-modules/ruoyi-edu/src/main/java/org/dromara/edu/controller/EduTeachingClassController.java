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
import org.dromara.edu.domain.bo.EduTeachingClassBo;
import org.dromara.edu.domain.vo.EduTeachingClassMemberVo;
import org.dromara.edu.domain.vo.EduTeachingClassVo;
import org.dromara.edu.service.IEduTeachingClassService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教学班管理
 *
 * 覆盖 teaching-class 全部 5 个 operationId：listTeachingClass / addTeachingClass /
 * getTeachingClass / disableTeachingClass / listTeachingClassRoster。
 *
 * 口径：教学班与行政班完全独立（BR-CLASS-001），不设班主任、不参与 DS-06；
 * 手工增删成员不开放，成员由「按组合生成」触发写入（REQ-STR-056）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/teaching-class")
public class EduTeachingClassController extends BaseController {

    private final IEduTeachingClassService teachingClassService;

    /** 分页查询教学班列表 */
    @SaCheckPermission("org.teaching_class:read")
    @GetMapping("/list")
    public TableDataInfo<EduTeachingClassVo> list(EduTeachingClassBo teachingClass, PageQuery pageQuery) {
        return teachingClassService.queryPageList(teachingClass, pageQuery);
    }

    /** 新增 / 幂等生成教学班（同一学期同一组合同一名称只建一次，REQ-STR-057） */
    @SaCheckPermission("org.teaching_class:create")
    @Log(title = "教学班管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<EduTeachingClassVo> add(@Validated @RequestBody EduTeachingClassBo teachingClass) {
        return R.ok(teachingClassService.addTeachingClass(teachingClass));
    }

    /** 查询教学班详情 */
    @SaCheckPermission("org.teaching_class:read")
    @GetMapping("/{teachingClassId}")
    public R<EduTeachingClassVo> getInfo(@PathVariable Long teachingClassId) {
        return R.ok(teachingClassService.queryById(teachingClassId));
    }

    /** 停用教学班（必填原因；历史成员保留） */
    @SaCheckPermission("org.teaching_class:update")
    @Log(title = "教学班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teachingClassId}/disable")
    public R<Void> disable(@PathVariable Long teachingClassId, @RequestParam String reason) {
        return toAjax(teachingClassService.disableTeachingClass(teachingClassId, reason));
    }

    /** 查询教学班成员清单 */
    @SaCheckPermission("org.teaching_class:read")
    @GetMapping("/{teachingClassId}/roster")
    public R<List<EduTeachingClassMemberVo>> roster(@PathVariable Long teachingClassId) {
        return R.ok(teachingClassService.listRoster(teachingClassId));
    }

}
