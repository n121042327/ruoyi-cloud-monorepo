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
import org.dromara.edu.domain.bo.EduStreamChangeRequestBo;
import org.dromara.edu.domain.bo.EduStreamConfigBo;
import org.dromara.edu.domain.bo.EduStreamHistoryBo;
import org.dromara.edu.domain.bo.EduStreamSelectionBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduMyStreamVo;
import org.dromara.edu.domain.vo.EduStreamChangeRequestVo;
import org.dromara.edu.domain.vo.EduStreamConfigVo;
import org.dromara.edu.domain.vo.EduStreamHistoryVo;
import org.dromara.edu.domain.vo.EduStreamOptionVo;
import org.dromara.edu.domain.vo.EduStreamSelectionVo;
import org.dromara.edu.domain.vo.EduStreamStatVo;
import org.dromara.edu.domain.vo.EduTeachingClassGenerateVo;
import org.dromara.edu.domain.vo.EduUnselectedStudentVo;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.edu.service.IEduStreamService;
import org.springframework.validation.annotation.Validated;
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
 * 选科与教学班生成
 *
 * 覆盖 stream 模块 16 / 17 个 operationId（`exportStreamSelection` 归入导入导出引擎批次）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/stream")
public class EduStreamController extends BaseController {

    private final IEduStreamService streamService;
    private final IEduImportExportService importExportService;

    /** 选科配置（含开放期实时状态，REQ-STR-005） */
    @SaCheckPermission("stream.config:read")
    @GetMapping("/config")
    public R<EduStreamConfigVo> getConfig(@RequestParam(required = false) Long termId) {
        return R.ok(streamService.getStreamConfig(termId));
    }

    /** 保存选科配置（同校同学期唯一，REQ-STR-004） */
    @SaCheckPermission("stream.config:update")
    @Log(title = "选科配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/config")
    public R<EduStreamConfigVo> saveConfig(@Validated @RequestBody EduStreamConfigBo config) {
        return R.ok(streamService.saveStreamConfig(config));
    }

    /** 选科可选科目（首选物理 / 历史，再选 4 选 2，学校不可增减） */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/option")
    public R<EduStreamOptionVo> option() {
        return R.ok(streamService.getStreamOption());
    }

    /** 我的选科 */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/my")
    public R<EduMyStreamVo> my(@RequestParam(required = false) Long termId,
                               @RequestParam(required = false) Long studentId) {
        return R.ok(streamService.getMyStream(termId, studentId));
    }

    /** 首次提交选科（开放期内直接生效） */
    @SaCheckPermission("stream.selection:update")
    @Log(title = "学生选科", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/my")
    public R<EduMyStreamVo> submitMy(@Validated @RequestBody EduStreamSelectionBo stream) {
        return R.ok(streamService.submitMyStream(stream));
    }

    /** 更新选科（开放期内直接生效；截止后转变更申请，BR-STREAM-006） */
    @SaCheckPermission("stream.selection:update")
    @Log(title = "学生选科", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/my")
    public R<EduMyStreamVo> updateMy(@Validated @RequestBody EduStreamSelectionBo stream) {
        return R.ok(streamService.updateMyStream(stream));
    }

    /** 选科清单 */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/selection/list")
    public TableDataInfo<EduStreamSelectionVo> selectionList(EduStreamSelectionBo query, PageQuery pageQuery) {
        return streamService.querySelectionPageList(query, pageQuery);
    }

    /** 组合分布统计（图表与明细同源） */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/stat")
    public R<EduStreamStatVo> stat(@RequestParam(required = false) Long termId,
                                   @RequestParam(required = false) Long gradeId) {
        return R.ok(streamService.getStreamStat(termId, gradeId));
    }

    /** 未选科学生（催办清单） */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/unselected")
    public R<List<EduUnselectedStudentVo>> unselected(@RequestParam(required = false) Long termId) {
        return R.ok(streamService.listUnselectedStudent(termId));
    }

    /** 变更申请待办列表（按提交时间升序，REQ-STR-035） */
    @SaCheckPermission("stream.change_request:read")
    @GetMapping("/change/list")
    public TableDataInfo<EduStreamChangeRequestVo> changeList(EduStreamChangeRequestBo query, PageQuery pageQuery) {
        return streamService.queryChangeRequestPageList(query, pageQuery);
    }

    /** 提交变更申请（同一学生同一学期只允许一条待审批，REQ-STR-029） */
    @SaCheckPermission("stream.change_request:create")
    @Log(title = "选科变更申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/change")
    public R<EduStreamChangeRequestVo> addChange(@Validated @RequestBody EduStreamChangeRequestBo request) {
        return R.ok(streamService.addStreamChangeRequest(request));
    }

    /** 撤销变更申请 */
    @SaCheckPermission("stream.change_request:create")
    @Log(title = "选科变更申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/change/{requestId}/cancel")
    public R<Void> cancelChange(@PathVariable Long requestId, @RequestParam(required = false) String reason) {
        return toAjax(streamService.cancelStreamChangeRequest(requestId, reason));
    }

    /** 审批变更申请（通过即生效；驳回必填意见，REQ-STR-037） */
    @SaCheckPermission("stream.change_request:approve")
    @Log(title = "选科变更审批", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/change/{requestId}/approve")
    public R<Void> approveChange(@PathVariable Long requestId, @RequestBody EduStreamChangeRequestBo request) {
        request.setRequestId(requestId);
        return toAjax(streamService.approveStreamChangeRequest(request));
    }

    /** 选科历史（追加式，不可删除不可修改，REQ-STR-043） */
    @SaCheckPermission("stream.selection:read")
    @GetMapping("/history")
    public R<List<EduStreamHistoryVo>> history(EduStreamHistoryBo query) {
        return R.ok(streamService.listStreamHistory(query));
    }

    /** 教学班生成预览（只读不写，REQ-STR-056） */
    @SaCheckPermission("stream.selection:read")
    @PostMapping("/teaching-class/preview")
    public R<EduTeachingClassGenerateVo> previewTeachingClass(@Validated @RequestBody EduStreamSelectionBo generate) {
        return R.ok(streamService.previewTeachingClassGenerate(generate));
    }

    /** 执行教学班生成（唯一键幂等，REQ-STR-057） */
    @SaCheckPermission("stream.selection:read")
    @Log(title = "教学班生成", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/teaching-class/generate")
    public R<EduTeachingClassGenerateVo> generateTeachingClass(@Validated @RequestBody EduStreamSelectionBo generate) {
        return R.ok(streamService.executeTeachingClassGenerate(generate));
    }

    /** 选科清单导出（统一走导出引擎，导出前重新解析数据范围） */
    @SaCheckPermission("stream.selection:export")
    @Log(title = "学生选科", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> exportSelection(@RequestBody EduExportBo export) {
        export.setModuleCode("stream");
        return R.ok(importExportService.exportData(export));
    }

}
