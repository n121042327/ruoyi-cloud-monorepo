package org.dromara.edu.service;

import org.dromara.edu.domain.bo.EduGuardianBo;
import org.dromara.edu.domain.bo.EduStudentEnrollmentBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduEnrollmentStatusOptionVo;
import org.dromara.edu.domain.vo.EduGuardianVo;
import org.dromara.edu.domain.vo.EduStudentEnrollmentVo;

import java.util.List;

/**
 * 学生档案服务层（在校记录 / 学籍异动 / 监护人 / 敏感字段）
 *
 * 覆盖 student 模块的 9 个 operationId：listEnrollmentStatusOption / changeEnrollmentStatus /
 * listStudentChangeLog / listStudentGuardian / saveStudentGuardian / unbindStudentGuardian /
 * viewStudentIdCard / updateStudentNo / removeStudent。
 *
 * 未落地：viewStudentPhone（GAP-090：edu_student 无电话列，落点待定）、
 * uploadStudentPhoto / getStudentPhoto（需 ruoyi-common-oss 依赖与文件服务远程接口）、
 * resetStudentPassword（同 GAP-091）、激活码 4 个与 exportStudent（导入导出引擎）。
 *
 * @author Codex
 */
public interface IEduStudentProfileService {

    /** 学籍异动动作选项（按当前状态给出可选流转，含禁用原因） */
    List<EduEnrollmentStatusOptionVo> listStatusOption(Long studentId);

    /** 学籍异动登记（唯一流转入口 DP-01：写在校记录 + 追加异动记录） */
    Boolean changeEnrollmentStatus(Long studentId, EduStudentEnrollmentBo change);

    /** 查询学生变更记录（学籍异动 + 资料变更申请，只读聚合） */
    List<EduEnrollmentChangeVo> listChangeLog(Long studentId);

    /** 查询学生的在校记录 */
    EduStudentEnrollmentVo queryEnrollment(Long studentId);

    /** 查询学生监护人（手机号默认掩码） */
    List<EduGuardianVo> listGuardian(Long studentId);

    /** 新增 / 修改监护人（监护人主体平台唯一，按手机号复用） */
    Boolean saveGuardian(Long studentId, EduGuardianBo guardian);

    /** 解绑监护人（需班主任确认口径，写 unbind_time 与原因） */
    Boolean unbindGuardian(Long studentId, Long guardianId, String reason);

    /** 查看证件号明文（需 person.student:read_sensitive，写审计） */
    String viewIdCard(Long studentId);

    /** 修改学号（需校级管理员权限并留审计，REQ-STU-027 口径：学号系统发号、可纠错但不可复用） */
    Boolean updateStudentNo(Long studentId, String studentNo, String reason);

    /** 删除学生（有在校记录 / 班级关系时只允许通过学籍异动处理，不允许物理删除） */
    Boolean removeStudent(Long studentId, String reason);

    /**
     * 重置学生登录账号密码（GAP-091）
     *
     * 学生账号按登录名约定关联：`REQ-STU-023` / `BR-ACCOUNT-002` 规定登录名为 `s` + 学号，
     * `edu_student` 本身没有 `user_id` 列，因此这里先拼登录名再经 `RemoteUserService.getUserInfo`
     * 取 `userId`，最后调 `resetPassword`。
     *
     * @param studentId 学生主体 ID
     * @param password  明文新密码
     * @return 是否成功
     */
    Boolean resetStudentPassword(Long studentId, String password);

    /**
     * 查看学生完整联系电话（`viewStudentPhone`，`REQ-STU-013` / `CR-046`）
     *
     * 默认掩码是列表页的展示口径；本接口只对持有 `person.student_contact` 的 `read_contact`
     * 的调用方开放，并在服务层写一条敏感数据访问日志（`REQ-AUD-008` / `BR-AUDIT-002`）。
     *
     * @param studentId 学生主体 ID
     * @return 完整联系电话
     */
    String viewStudentPhone(Long studentId);

    /**
     * 上传 / 更换学生照片（`uploadStudentPhoto`）
     *
     * 走统一文件服务：字节交给文件服务落对象存储，`edu_student.photo_url` 只保存返回的文件地址
     * （`BR-ACCOUNT-002` 同族的照片口径见 `GAP-027`；文件服务见 `CR-095`）。
     *
     * @param studentId 学生主体 ID
     * @param file      图片文件
     * @return 文件地址
     */
    String uploadStudentPhoto(Long studentId, org.springframework.web.multipart.MultipartFile file);

    /**
     * 查看学生照片原图（`getStudentPhoto`，返回二进制）
     *
     * 需 `person.student:read_sensitive`，并写敏感数据访问日志（`student PRD 4.3` / `GAP-027`）。
     *
     * @param studentId 学生主体 ID
     * @return 图片字节
     */
    byte[] getStudentPhoto(Long studentId);

}
