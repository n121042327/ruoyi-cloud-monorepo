package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduTeacherBo;
import org.dromara.edu.domain.bo.EduTeachingAssignmentBo;
import org.dromara.edu.domain.bo.EduUserRoleBo;
import org.dromara.edu.domain.vo.EduTeacherVo;
import org.dromara.edu.domain.vo.EduTeachingAssignmentVo;
import org.dromara.edu.domain.vo.EduUserRoleVo;

import java.util.List;

/**
 * 教师服务层
 *
 * 覆盖 teacher 模块的 15 个 operationId：listTeacher / getTeacher / addTeacher / updateTeacher /
 * updateTeacherNo / listTeacherRole / saveTeacherRole / removeTeacherRole / listTeachingAssignment /
 * saveTeachingAssignment / batchSaveTeachingAssignment / removeTeachingAssignment /
 * copyTeachingAssignment / leaveTeacher / revokeTeacherLeave。
 *
 * 未落地的 7 个：resetTeacherPassword / disableTeacherAccount / enableTeacherAccount 需要
 * RemoteUserService 新增账号操作方法（见 GAP-091）；importTeacherValidate / importTeacherExecute /
 * downloadTeacherImportTemplate / exportTeacher 需要导入导出引擎（后续批次）。
 *
 * @author Codex
 */
public interface IEduTeacherService {

    /** 分页查询教师列表 */
    TableDataInfo<EduTeacherVo> queryPageList(EduTeacherBo teacher, PageQuery pageQuery);

    /** 查询教师详情 */
    EduTeacherVo queryById(Long teacherId);

    /** 新增教师（保存成功后自动创建登录账号，账号信息不落 edu_teacher） */
    Boolean insertByBo(EduTeacherBo teacher);

    /** 修改教师（工号修改走 updateTeacherNo，需校级管理员权限并留审计，REQ-TCH-022） */
    Boolean updateByBo(EduTeacherBo teacher);

    /** 修改工号（学校租户内唯一，BR-TEACHER-007） */
    Boolean updateTeacherNo(Long teacherId, String teacherNo, String reason);

    /** 查询教师的教育角色（学校级） */
    List<EduUserRoleVo> listRole(Long teacherId);

    /** 保存教师教育角色（同一用户同一角色唯一） */
    Boolean saveRole(Long teacherId, EduUserRoleBo role);

    /** 移除教育角色（置 status=0，不物理删除） */
    Boolean removeRole(Long userRoleId);

    /** 分页查询任教关系（按班级或按教师视角） */
    TableDataInfo<EduTeachingAssignmentVo> queryAssignmentPageList(EduTeachingAssignmentBo assignment, PageQuery pageQuery);

    /** 新增 / 编辑任教关系（权限 person.teaching_assignment:create） */
    Boolean saveAssignment(EduTeachingAssignmentBo assignment);

    /** 批量保存任教关系（同一班级一次挂多门学科） */
    Boolean batchSaveAssignment(EduTeachingAssignmentBo assignment);

    /** 删除（失效）任教关系：写 status=0，不物理删除 */
    Boolean removeAssignment(Long assignmentId, String reason);

    /** 复制上一学年任教关系（目标学期已有同一「班级 + 学科」时跳过，不覆盖） */
    Boolean copyAssignment(EduTeachingAssignmentBo assignment);

    /** 离职 / 调离登记（非在职后只保留查看与撤销，编辑与角色入口隐藏，GAP-075） */
    Boolean leaveTeacher(Long teacherId, String employmentStatus, String leaveDate, String reason);

    /** 撤销离职登记（误操作纠正，写审计） */
    Boolean revokeTeacherLeave(Long teacherId, String reason);

}
