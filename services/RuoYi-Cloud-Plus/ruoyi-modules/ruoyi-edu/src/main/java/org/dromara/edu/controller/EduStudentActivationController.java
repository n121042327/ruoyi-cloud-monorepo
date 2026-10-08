package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduActivationCodeBo;
import org.dromara.edu.domain.vo.EduActivationCodeVo;
import org.dromara.edu.service.IEduActivationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生激活码
 *
 * 覆盖 student 模块 3 个激活相关 operationId：getStudentActivationCode /
 * printStudentActivationSlip / activateStudentAccount（exportStudentActivationCode 归入导入导出引擎批次）。
 *
 * 口径（D-039 / GAP-021）：班主任打印密码条分发 → 学生首登即设密码 → 激活码用完即废 → 丢码由班主任重置。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/student")
public class EduStudentActivationController extends BaseController {

    private final IEduActivationService activationService;

    /** 查看 / 重置学生的激活码（未使用时直接返回；已使用时由班主任重置并填原因） */
    @SaCheckPermission("person.student:update")
    @Log(title = "学生激活码", businessType = BusinessType.UPDATE)
    @GetMapping("/{studentId}/activation-code")
    public R<EduActivationCodeVo> getActivationCode(@PathVariable Long studentId,
                                                    @RequestParam(required = false) String reason) {
        return R.ok(activationService.getStudentActivationCode(studentId, reason));
    }

    /** 打印激活密码条（一个批次一份，写 print_time 与批次号） */
    @SaCheckPermission("person.student:read")
    @Log(title = "学生激活码", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/activation-slip/print")
    public R<EduActivationCodeVo> printActivationSlip(@RequestParam Long studentId,
                                                      @RequestParam(required = false) String issueBatchNo) {
        return R.ok(activationService.printStudentActivationSlip(studentId, issueBatchNo));
    }

    /** 学生首登激活（校验一次性激活码 + 设置密码，用完即废） */
    @RepeatSubmit()
    @PostMapping("/{studentId}/activate")
    public R<Void> activate(@PathVariable Long studentId, @Validated @RequestBody EduActivationCodeBo activation) {
        activation.setStudentId(studentId);
        return toAjax(activationService.activateStudentAccount(activation));
    }

}
