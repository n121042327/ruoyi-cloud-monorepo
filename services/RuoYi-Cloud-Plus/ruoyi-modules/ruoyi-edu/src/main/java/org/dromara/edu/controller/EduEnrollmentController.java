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
import org.dromara.edu.domain.bo.EduEnrollmentChangeBo;
import org.dromara.edu.domain.bo.EduTransferOrderBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduTransferOrderVo;
import org.dromara.edu.service.IEduEnrollmentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学籍异动与跨校转学
 *
 * 覆盖 enrollment 模块全部 8 个 operationId：listEnrollmentChange / addEnrollmentChange /
 * approveEnrollmentChange / listTransfer / addTransfer / acceptTransfer / checkInTransfer / cancelTransfer。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/enrollment")
public class EduEnrollmentController extends BaseController {

    private final IEduEnrollmentService enrollmentService;

    /** 分页查询学籍异动记录（异动历史页） */
    @SaCheckPermission("enrollment.status:read")
    @GetMapping("/change/list")
    public TableDataInfo<EduEnrollmentChangeVo> listChange(EduEnrollmentChangeBo change, PageQuery pageQuery) {
        return enrollmentService.queryChangePageList(change, pageQuery);
    }

    /** 异动登记（追加式，不更新不删除，BR-PROMO-012） */
    @SaCheckPermission("enrollment.status:update")
    @Log(title = "学籍异动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/change")
    public R<Void> addChange(@Validated @RequestBody EduEnrollmentChangeBo change) {
        return toAjax(enrollmentService.addEnrollmentChange(change));
    }

    /** 异动审批（退学 / 开除 / 死亡需校级管理员审批，REQ-PRM-043 口径） */
    @SaCheckPermission("enrollment.status:approve")
    @Log(title = "学籍异动审批", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/change/{changeId}/approve")
    public R<Void> approveChange(@PathVariable Long changeId, @RequestBody EduEnrollmentChangeBo change) {
        change.setChangeId(changeId);
        return toAjax(enrollmentService.approveEnrollmentChange(change));
    }

    /** 分页查询转学单（转入校待接收清单 / 转出校清单） */
    @SaCheckPermission("enrollment.transfer:read")
    @GetMapping("/transfer/list")
    public TableDataInfo<EduTransferOrderVo> listTransfer(EduTransferOrderBo transfer, PageQuery pageQuery) {
        return enrollmentService.queryTransferPageList(transfer, pageQuery);
    }

    /** 发起转出（跨校转学的唯一发起入口） */
    @SaCheckPermission("enrollment.transfer:create")
    @Log(title = "跨校转学", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/transfer")
    public R<Void> addTransfer(@Validated @RequestBody EduTransferOrderBo transfer) {
        return toAjax(enrollmentService.addTransfer(transfer));
    }

    /** 转入校接收（接收动作本身即审批；接收前不计入在读数，REQ-PRM-053） */
    @SaCheckPermission("enrollment.transfer:update")
    @Log(title = "跨校转学", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/transfer/{transferId}/accept")
    public R<Void> acceptTransfer(@PathVariable Long transferId, @RequestBody EduTransferOrderBo transfer) {
        transfer.setTransferId(transferId);
        return toAjax(enrollmentService.acceptTransfer(transfer));
    }

    /** 办理报到（状态转为在读；班级关系仍由班级管理写入，DP-01） */
    @SaCheckPermission("enrollment.transfer:update")
    @Log(title = "跨校转学", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/transfer/{transferId}/check-in")
    public R<Void> checkInTransfer(@PathVariable Long transferId, @RequestBody EduTransferOrderBo transfer) {
        transfer.setTransferId(transferId);
        return toAjax(enrollmentService.checkInTransfer(transfer));
    }

    /** 撤销（未报到前可撤销接收 / 撤销申请，REQ-PRM-054） */
    @SaCheckPermission("enrollment.transfer:update")
    @Log(title = "跨校转学", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/transfer/{transferId}/cancel")
    public R<Void> cancelTransfer(@PathVariable Long transferId, @RequestParam String reason) {
        return toAjax(enrollmentService.cancelTransfer(transferId, reason));
    }

}
