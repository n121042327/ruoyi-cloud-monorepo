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
import org.dromara.edu.domain.bo.EduPromotionTaskBo;
import org.dromara.edu.domain.vo.EduPromotionItemVo;
import org.dromara.edu.domain.vo.EduPromotionReadinessVo;
import org.dromara.edu.domain.vo.EduPromotionTaskVo;
import org.dromara.edu.service.IEduPromotionService;
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
 * 升班任务与向导
 *
 * 覆盖 promotion 模块 10 / 13 个 operationId：listPromotionTask / getPromotionTask / addPromotionTask /
 * previewPromotionTask / updatePromotionItem / batchUpdatePromotionItem / validatePromotionTask /
 * executePromotionTask / retryPromotionTask / cancelPromotionTask。
 * 导出类 3 个归入导入导出引擎批次。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/promotion/task")
public class EduPromotionController extends BaseController {

    private final IEduPromotionService promotionService;

    /** 分页查询升班任务列表 */
    @SaCheckPermission("promotion.batch:read")
    @GetMapping("/list")
    public TableDataInfo<EduPromotionTaskVo> list(EduPromotionTaskBo task, PageQuery pageQuery) {
        return promotionService.queryPageList(task, pageQuery);
    }

    /** 目标学期年级与班级齐备性检查（创建任务前的前置校验，REQ-PRM-009） */
    @SaCheckPermission("promotion.batch:read")
    @GetMapping("/readiness")
    public R<EduPromotionReadinessVo> readiness(@RequestParam(required = false) Long sourceTermId,
                                                @RequestParam(required = false) Long targetTermId) {
        return R.ok(promotionService.readiness(sourceTermId, targetTermId));
    }

    /** 查询升班任务详情（带明细） */
    @SaCheckPermission("promotion.batch:read")
    @GetMapping("/{taskId}")
    public R<EduPromotionTaskVo> getInfo(@PathVariable Long taskId) {
        return R.ok(promotionService.queryById(taskId));
    }

    /** 查询升班明细（预览 / 校验 / 结果页共用） */
    @SaCheckPermission("promotion.batch:read")
    @GetMapping("/{taskId}/item")
    public R<List<EduPromotionItemVo>> listItem(@PathVariable Long taskId, EduPromotionTaskBo query) {
        return R.ok(promotionService.queryItems(taskId, query));
    }

    /** 创建升班任务（草稿；目标学期年级与班级必须齐备） */
    @SaCheckPermission("promotion.batch:create")
    @Log(title = "升班管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<EduPromotionTaskVo> add(@Validated @RequestBody EduPromotionTaskBo task) {
        return R.ok(promotionService.addTask(task));
    }

    /** 生成预览（不写任何学生数据，REQ-PRM-020） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskId}/preview")
    public R<EduPromotionTaskVo> preview(@PathVariable Long taskId) {
        return R.ok(promotionService.previewTask(taskId));
    }

    /** 调整单条明细的去向 */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{taskId}/item")
    public R<Void> updateItem(@PathVariable Long taskId, @RequestBody EduPromotionTaskBo item) {
        return toAjax(promotionService.updateItem(taskId, item));
    }

    /** 按源班级或明细列表批量调整目标班级（REQ-PRM-018） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{taskId}/item/batch")
    public R<Void> batchUpdateItem(@PathVariable Long taskId, @RequestBody EduPromotionTaskBo item) {
        return toAjax(promotionService.batchUpdateItem(taskId, item));
    }

    /** 升班校验（校验通过才允许执行，REQ-PRM-027） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskId}/validate")
    public R<EduPromotionTaskVo> validate(@PathVariable Long taskId) {
        return R.ok(promotionService.validateTask(taskId));
    }

    /** 执行升班（按学年追加、不改写历史，BR-PROMO-001） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskId}/execute")
    public R<EduPromotionTaskVo> execute(@PathVariable Long taskId) {
        return R.ok(promotionService.executeTask(taskId));
    }

    /** 重试失败项（只处理失败 / 跳过项，REQ-PRM-032） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskId}/retry")
    public R<EduPromotionTaskVo> retry(@PathVariable Long taskId, @RequestParam(required = false) String reason) {
        return R.ok(promotionService.retryTask(taskId, reason));
    }

    /** 取消任务（保留已完成部分，不做整批回滚，REQ-PRM-036） */
    @SaCheckPermission("promotion.batch:update")
    @Log(title = "升班管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskId}/cancel")
    public R<Void> cancel(@PathVariable Long taskId, @RequestParam String reason) {
        return toAjax(promotionService.cancelTask(taskId, reason));
    }

}
