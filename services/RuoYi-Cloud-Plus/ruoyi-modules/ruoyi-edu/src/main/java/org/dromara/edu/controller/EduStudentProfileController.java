package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduGuardianBo;
import org.dromara.edu.domain.bo.EduStudentEnrollmentBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduEnrollmentStatusOptionVo;
import org.dromara.edu.domain.vo.EduGuardianVo;
import org.dromara.edu.domain.vo.EduStudentEnrollmentVo;
import org.dromara.edu.service.IEduStudentProfileService;
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
 * 学生档案（在校记录 / 学籍异动 / 监护人 / 敏感字段）
 *
 * 对应 operationId：listEnrollmentStatusOption / changeEnrollmentStatus / listStudentChangeLog /
 * listStudentGuardian / saveStudentGuardian / unbindStudentGuardian / viewStudentIdCard /
 * updateStudentNo / removeStudent。
 *
 * 未落地的学生接口：viewStudentPhone（GAP-090）、uploadStudentPhoto / getStudentPhoto（需 OSS 依赖）、
 * resetStudentPassword（GAP-091）、激活码 4 个与 exportStudent（导入导出引擎）——见 CR-086 的边界说明。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/student")
public class EduStudentProfileController extends BaseController {

    private final IEduStudentProfileService profileService;

    /** 学籍异动动作选项（按当前状态给出可选流转，含禁用原因） */
    @SaCheckPermission("person.student:read")
    @GetMapping("/{studentId}/status-options")
    public R<List<EduEnrollmentStatusOptionVo>> listStatusOption(@PathVariable Long studentId) {
        return R.ok(profileService.listStatusOption(studentId));
    }

    /** 学生变更记录（学籍异动 + 资料变更申请，只读聚合） */
    @SaCheckPermission("person.student:read")
    @GetMapping("/{studentId}/change-log")
    public R<List<EduEnrollmentChangeVo>> listChangeLog(@PathVariable Long studentId) {
        return R.ok(profileService.listChangeLog(studentId));
    }

    /** 查询学生的在校记录 */
    @SaCheckPermission("person.student:read")
    @GetMapping("/{studentId}/enrollment")
    public R<EduStudentEnrollmentVo> queryEnrollment(@PathVariable Long studentId) {
        return R.ok(profileService.queryEnrollment(studentId));
    }

    /** 学籍异动登记（唯一流转入口 DP-01；开除在义务教育阶段被后端拒绝，REQ-PRM-043） */
    @SaCheckPermission("enrollment.status:update")
    @Log(title = "学籍异动", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{studentId}/enrollment-change")
    public R<Void> changeEnrollmentStatus(@PathVariable Long studentId,
                                          @Validated @RequestBody EduStudentEnrollmentBo change) {
        return toAjax(profileService.changeEnrollmentStatus(studentId, change));
    }

    /** 查询学生监护人（手机号默认掩码） */
    @SaCheckPermission("person.student:read")
    @GetMapping("/{studentId}/guardian")
    public R<List<EduGuardianVo>> listGuardian(@PathVariable Long studentId) {
        return R.ok(profileService.listGuardian(studentId));
    }

    /** 新增 / 修改监护人（监护人主体平台唯一，按手机号复用） */
    @SaCheckPermission("person.student:update")
    @Log(title = "学生监护人", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{studentId}/guardian")
    public R<Void> saveGuardian(@PathVariable Long studentId, @Validated @RequestBody EduGuardianBo guardian) {
        return toAjax(profileService.saveGuardian(studentId, guardian));
    }

    /** 解绑监护人（需班主任确认口径，写解绑时间与原因，GAP-015） */
    @SaCheckPermission("person.student:update")
    @Log(title = "学生监护人", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @PostMapping("/{studentId}/guardian/{guardianId}/unbind")
    public R<Void> unbindGuardian(@PathVariable Long studentId, @PathVariable Long guardianId,
                                  @RequestParam String reason) {
        return toAjax(profileService.unbindGuardian(studentId, guardianId, reason));
    }

    /** 查看证件号明文（需 person.student:read_sensitive，写审计，NFR-AUDIT-02） */
    @SaCheckPermission("person.student:read_sensitive")
    @Log(title = "学生敏感字段", businessType = BusinessType.OTHER)
    @GetMapping("/{studentId}/id-card")
    public R<String> viewIdCard(@PathVariable Long studentId) {
        return R.ok(profileService.viewIdCard(studentId));
    }

    /** 修改学号（需校级管理员权限并留审计，学号永不回收 BR-STU-001） */
    @SaCheckPermission("person.student:update")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{studentId}/student-no")
    public R<Void> updateStudentNo(@PathVariable Long studentId, @RequestParam String studentNo,
                                   @RequestParam String reason) {
        return toAjax(profileService.updateStudentNo(studentId, studentNo, reason));
    }

    /** 删除学生（有花名册关系或异动记录时拒绝，历史与学籍类数据禁止物理删除） */
    @SaCheckPermission("person.student:remove")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{studentId}")
    public R<Void> remove(@PathVariable Long studentId, @RequestParam(required = false) String reason) {
        return toAjax(profileService.removeStudent(studentId, reason));
    }

}
