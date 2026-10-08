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
import org.dromara.edu.domain.bo.EduImportExecuteBo;
import org.dromara.edu.domain.bo.EduImportValidateBo;
import org.dromara.edu.domain.bo.EduTeachingAssignmentBo;
import org.dromara.edu.domain.bo.EduTeacherBo;
import org.dromara.edu.domain.bo.EduUserRoleBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.domain.vo.EduImportExecuteResultVo;
import org.dromara.edu.domain.vo.EduImportValidateResultVo;
import org.dromara.edu.domain.vo.EduTeacherVo;
import org.dromara.edu.domain.vo.EduTeachingAssignmentVo;
import org.dromara.edu.domain.vo.EduUserRoleVo;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.edu.service.IEduTeacherService;
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
 * 教师管理（含教育角色与任教关系）
 *
 * 对应 operationId：listTeacher / getTeacher / addTeacher / updateTeacher / updateTeacherNo /
 * listTeacherRole / saveTeacherRole / removeTeacherRole / listTeachingAssignment /
 * saveTeachingAssignment / batchSaveTeachingAssignment / removeTeachingAssignment /
 * copyTeachingAssignment / leaveTeacher / revokeTeacherLeave（teacher 模块 15 / 22）。
 *
 * 未落地的 7 个：resetTeacherPassword / disableTeacherAccount / enableTeacherAccount（需 RemoteUserService
 * 新增账号操作方法，见 GAP-091）；importTeacherValidate / importTeacherExecute /
 * downloadTeacherImportTemplate / exportTeacher（需导入导出引擎，后续批次）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/teacher")
public class EduTeacherController extends BaseController {

    private final IEduTeacherService teacherService;
    private final IEduImportExportService importExportService;

    /** 查询教师列表 */
    @SaCheckPermission("person.teacher:read")
    @GetMapping("/list")
    public TableDataInfo<EduTeacherVo> list(EduTeacherBo teacher, PageQuery pageQuery) {
        return teacherService.queryPageList(teacher, pageQuery);
    }

    /** 查询教师详情 */
    @SaCheckPermission("person.teacher:read")
    @GetMapping("/{teacherId}")
    public R<EduTeacherVo> getInfo(@PathVariable Long teacherId) {
        return R.ok(teacherService.queryById(teacherId));
    }

    /** 新增教师（保存成功后自动创建登录账号） */
    @SaCheckPermission("person.teacher:create")
    @Log(title = "教师管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody EduTeacherBo teacher) {
        return toAjax(teacherService.insertByBo(teacher));
    }

    /** 修改教师（工号修改走 updateTeacherNo，需校级管理员权限并留审计，REQ-TCH-022） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody EduTeacherBo teacher) {
        return toAjax(teacherService.updateByBo(teacher));
    }

    /** 修改工号（学校租户内唯一，BR-TEACHER-007） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{teacherId}/teacher-no")
    public R<Void> updateTeacherNo(@PathVariable Long teacherId, @RequestParam String teacherNo,
                                   @RequestParam String reason) {
        return toAjax(teacherService.updateTeacherNo(teacherId, teacherNo, reason));
    }

    /** 离职 / 调离登记（非在职后只保留查看与撤销，GAP-075） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/leave")
    public R<Void> leave(@PathVariable Long teacherId, @RequestParam String employmentStatus,
                         @RequestParam(required = false) String leaveDate, @RequestParam String reason) {
        return toAjax(teacherService.leaveTeacher(teacherId, employmentStatus, leaveDate, reason));
    }

    /** 撤销离职登记（误操作纠正，写审计） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/leave/revoke")
    public R<Void> revokeLeave(@PathVariable Long teacherId, @RequestParam String reason) {
        return toAjax(teacherService.revokeTeacherLeave(teacherId, reason));
    }

    /** 查询教师的教育角色（学校级：校领导 / 教务主任） */
    @SaCheckPermission("person.teacher:read")
    @GetMapping("/{teacherId}/role")
    public R<List<EduUserRoleVo>> listRole(@PathVariable Long teacherId) {
        return R.ok(teacherService.listRole(teacherId));
    }

    /** 保存教师教育角色 */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师教育角色", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/role")
    public R<Void> saveRole(@PathVariable Long teacherId, @Validated @RequestBody EduUserRoleBo role) {
        return toAjax(teacherService.saveRole(teacherId, role));
    }

    /** 移除教育角色（置 status=0，不物理删除） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师教育角色", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/{teacherId}/role/{userRoleId}")
    public R<Void> removeRole(@PathVariable Long teacherId, @PathVariable Long userRoleId) {
        return toAjax(teacherService.removeRole(userRoleId));
    }

    /** 查询任教关系（按班级或按教师视角，DS-07 的判定入口） */
    @SaCheckPermission("person.teaching_assignment:read")
    @GetMapping("/assignment/list")
    public TableDataInfo<EduTeachingAssignmentVo> listAssignment(EduTeachingAssignmentBo assignment, PageQuery pageQuery) {
        return teacherService.queryAssignmentPageList(assignment, pageQuery);
    }

    /** 新增 / 编辑任教关系 */
    @SaCheckPermission("person.teaching_assignment:create")
    @Log(title = "任教关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/assignment")
    public R<Void> saveAssignment(@Validated @RequestBody EduTeachingAssignmentBo assignment) {
        return toAjax(teacherService.saveAssignment(assignment));
    }

    /** 批量保存任教关系（同一班级一次挂多门学科） */
    @SaCheckPermission("person.teaching_assignment:create")
    @Log(title = "任教关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/assignment/batch")
    public R<Void> batchSaveAssignment(@Validated @RequestBody EduTeachingAssignmentBo assignment) {
        return toAjax(teacherService.batchSaveAssignment(assignment));
    }

    /** 结束任教关系（写 status=0，不物理删除） */
    @SaCheckPermission("person.teaching_assignment:create")
    @Log(title = "任教关系", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @DeleteMapping("/assignment/{assignmentId}")
    public R<Void> removeAssignment(@PathVariable Long assignmentId, @RequestParam String reason) {
        return toAjax(teacherService.removeAssignment(assignmentId, reason));
    }

    /** 复制上一学年任教关系（目标学期已有同一「班级 + 学科」时跳过，不覆盖） */
    @SaCheckPermission("person.teaching_assignment:create")
    @Log(title = "任教关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/assignment/copy")
    public R<Void> copyAssignment(@Validated @RequestBody EduTeachingAssignmentBo assignment) {
        return toAjax(teacherService.copyAssignment(assignment));
    }

    /** 教师导入模板下载（统一走导入引擎的模板清单，REQ-IMP-002 / 003） */
    @SaCheckPermission("person.teacher:import")
    @GetMapping("/import/template")
    public R<EduFileRefVo> downloadImportTemplate(@RequestParam(required = false) String version) {
        return R.ok(importExportService.resolveTemplateDownload("teacher", version));
    }

    /** 教师导入校验（统一走导入引擎，BR-IMP-001） */
    @SaCheckPermission("person.teacher:import")
    @Log(title = "教师导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/import/validate")
    public R<EduImportValidateResultVo> importValidate(@Validated @RequestBody EduImportValidateBo validate) {
        validate.setModuleCode("teacher");
        return R.ok(importExportService.validateImportFile(validate));
    }

    /** 教师导入执行（异步，按批次号幂等） */
    @SaCheckPermission("person.teacher:import")
    @Log(title = "教师导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping("/import/execute")
    public R<EduImportExecuteResultVo> importExecute(@Validated @RequestBody EduImportExecuteBo execute) {
        return R.ok(importExportService.executeImport(execute));
    }

    /** 教师列表导出（手机号等敏感字段默认掩码，REQ-IMP-028 / BR-TEACHER-005） */
    @SaCheckPermission("person.teacher:export")
    @Log(title = "教师管理", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/export")
    public R<EduExportResultVo> export(@RequestBody EduExportBo export) {
        export.setModuleCode("teacher");
        return R.ok(importExportService.exportData(export));
    }

    /** 重置教师登录账号密码（明文密码交给 sys_user 侧加密落库，GAP-091） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师账号", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/reset-password")
    public R<Void> resetPassword(@PathVariable Long teacherId, @Validated @RequestBody EduTeacherBo teacher) {
        return toAjax(teacherService.resetTeacherPassword(teacherId, teacher.getPassword()));
    }

    /** 停用教师登录账号（原因必填，停用同时让已签发 token 失效） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师账号", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/account/disable")
    public R<Void> disableAccount(@PathVariable Long teacherId, @Validated @RequestBody EduTeacherBo teacher) {
        return toAjax(teacherService.disableTeacherAccount(teacherId, teacher.getReason()));
    }

    /** 启用教师登录账号（原因必填） */
    @SaCheckPermission("person.teacher:update")
    @Log(title = "教师账号", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{teacherId}/account/enable")
    public R<Void> enableAccount(@PathVariable Long teacherId, @Validated @RequestBody EduTeacherBo teacher) {
        return toAjax(teacherService.enableTeacherAccount(teacherId, teacher.getReason()));
    }

}
