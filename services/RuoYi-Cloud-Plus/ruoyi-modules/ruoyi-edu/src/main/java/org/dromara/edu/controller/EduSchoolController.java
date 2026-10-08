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
import org.dromara.edu.domain.bo.EduCampusBo;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.bo.EduSchoolBo;
import org.dromara.edu.domain.bo.EduSchoolStageBo;
import org.dromara.edu.domain.vo.EduCampusVo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduSchoolStageVo;
import org.dromara.edu.domain.vo.EduSchoolVo;
import org.dromara.edu.domain.vo.SchoolSummaryVo;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.edu.service.IEduSchoolService;
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
 * 学校与校区管理
 *
 * 对应 operationId：listSchool / getSchool / getCurrentSchool / addSchool / updateSchool /
 * updateSchoolCode / disableSchool / enableSchool / listCampus / saveCampus / removeCampus /
 * listSchoolStage / saveSchoolStage / getSchoolSummary / initSchoolBaseline
 * （school 模块 15 / 16，exportSchool 归入导入导出引擎批次）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/school")
public class EduSchoolController extends BaseController {

    private final IEduSchoolService schoolService;
    private final IEduImportExportService importExportService;

    /** 查询学校列表（平台运营看全平台；学校用户只看本校） */
    @SaCheckPermission("org.school:read")
    @GetMapping("/list")
    public TableDataInfo<EduSchoolVo> list(EduSchoolBo school, PageQuery pageQuery) {
        return schoolService.queryPageList(school, pageQuery);
    }

    /** 查询学校详情 */
    @SaCheckPermission("org.school:read")
    @GetMapping("/{schoolId}")
    public R<EduSchoolVo> getInfo(@PathVariable Long schoolId) {
        return R.ok(schoolService.queryById(schoolId));
    }

    /** 查询当前租户对应的学校（学校用户固定本校） */
    @SaCheckPermission("org.school:read")
    @GetMapping("/current")
    public R<EduSchoolVo> getCurrentSchool() {
        return R.ok(schoolService.getCurrentSchool());
    }

    /** 新增学校（学校与租户一一对应，BR-ORG-002） */
    @SaCheckPermission("org.school:create")
    @Log(title = "学校管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduSchoolBo school) {
        return toAjax(schoolService.insertByBo(school));
    }

    /** 修改学校（学校与租户的绑定关系不可修改，REQ-SCH-022） */
    @SaCheckPermission("org.school:update")
    @Log(title = "学校管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduSchoolBo school) {
        return toAjax(schoolService.updateByBo(school));
    }

    /** 修改学校编码（父租户内唯一，BR-ORG-011） */
    @SaCheckPermission("org.school:update")
    @Log(title = "学校管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{schoolId}/school-code")
    public R<Void> updateSchoolCode(@PathVariable Long schoolId, @RequestParam String schoolCode,
                                    @RequestParam String reason) {
        return toAjax(schoolService.updateSchoolCode(schoolId, schoolCode, reason));
    }

    /** 停用学校 */
    @SaCheckPermission("org.school:update")
    @Log(title = "学校管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{schoolId}/disable")
    public R<Void> disable(@PathVariable Long schoolId, @RequestParam String reason) {
        return toAjax(schoolService.disableSchool(schoolId, reason));
    }

    /** 启用学校 */
    @SaCheckPermission("org.school:update")
    @Log(title = "学校管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{schoolId}/enable")
    public R<Void> enable(@PathVariable Long schoolId, @RequestParam(required = false) String reason) {
        return toAjax(schoolService.enableSchool(schoolId, reason));
    }

    /** 查询校区列表（校区不参与数据权限判定，BR-ORG-009） */
    @SaCheckPermission("org.school:read")
    @GetMapping("/{schoolId}/campus")
    public TableDataInfo<EduCampusVo> listCampus(@PathVariable Long schoolId, EduCampusBo campus, PageQuery pageQuery) {
        return schoolService.queryCampusPageList(schoolId, campus, pageQuery);
    }

    /** 新增 / 编辑校区 */
    @SaCheckPermission("org.school:update")
    @Log(title = "校区管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{schoolId}/campus")
    public R<Void> saveCampus(@PathVariable Long schoolId, @Validated @RequestBody EduCampusBo campus) {
        campus.setSchoolId(schoolId);
        return toAjax(schoolService.saveCampus(campus));
    }

    /** 停用校区（写 campusStatus=disabled 并留原因） */
    @SaCheckPermission("org.school:update")
    @Log(title = "校区管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/campus/{campusId}")
    public R<Void> removeCampus(@PathVariable Long campusId, @RequestParam String reason) {
        return toAjax(schoolService.removeCampus(campusId, reason));
    }

    /** 查询学校开设学段（未开设的学段在学科与年级配置里置灰，REQ-SUB-026） */
    @SaCheckPermission("org.school:read")
    @GetMapping("/{schoolId}/stage")
    public R<List<EduSchoolStageVo>> listSchoolStage(@PathVariable Long schoolId) {
        return R.ok(schoolService.listSchoolStage(schoolId));
    }

    /** 保存学校开设学段（批量） */
    @SaCheckPermission("org.school:update")
    @Log(title = "学校学段配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{schoolId}/stage")
    public R<Void> saveSchoolStage(@PathVariable Long schoolId, @Validated @RequestBody EduSchoolStageBo stage) {
        return toAjax(schoolService.saveSchoolStage(schoolId, stage));
    }

    /** 学校数据摘要 */
    @SaCheckPermission("org.school:read")
    @GetMapping("/{schoolId}/summary")
    public R<SchoolSummaryVo> getSchoolSummary(@PathVariable Long schoolId) {
        return R.ok(schoolService.getSchoolSummary(schoolId));
    }

    /** 开通初始化（学段 + 学年与默认学期，幂等，REQ-SCH-019 / 045） */
    @SaCheckPermission("org.school:update")
    @Log(title = "开通初始化", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{schoolId}/init")
    public R<SchoolSummaryVo> initSchoolBaseline(@PathVariable Long schoolId, @RequestBody EduSchoolBo school) {
        school.setSchoolId(schoolId);
        return R.ok(schoolService.initSchoolBaseline(school));
    }

    /** 学校列表导出（统一走导出引擎） */
    @SaCheckPermission("org.school:export")
    @Log(title = "学校管理", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> export(@RequestBody EduExportBo export) {
        export.setModuleCode("school");
        return R.ok(importExportService.exportData(export));
    }

}
