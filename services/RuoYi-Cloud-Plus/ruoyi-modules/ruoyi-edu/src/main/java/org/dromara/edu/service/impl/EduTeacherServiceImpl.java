package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.EduUserRole;
import org.dromara.edu.domain.bo.EduTeacherBo;
import org.dromara.edu.domain.bo.EduTeachingAssignmentBo;
import org.dromara.edu.domain.bo.EduUserRoleBo;
import org.dromara.edu.domain.vo.EduTeacherVo;
import org.dromara.edu.domain.vo.EduTeachingAssignmentVo;
import org.dromara.edu.domain.vo.EduUserRoleVo;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.mapper.EduUserRoleMapper;
import org.dromara.edu.service.IEduTeacherService;
import org.dromara.system.api.RemoteUserService;
import org.dromara.system.api.domain.bo.RemoteUserBo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教师服务层处理
 *
 * 口径要点：
 * - 教师主体带所属学校（schoolId）；跨校任教不复制教师记录，由任教关系的 schoolId 表达（BR-TEACHER-001）；
 * - 工号在学校租户内唯一（BR-TEACHER-007），修改工号需校级管理员权限并留审计（REQ-TCH-022）；
 * - 新增教师保存成功后自动创建登录账号，账号信息在 sys_user（Dubbo 调 RemoteUserService.registerUserInfo）；
 * - 学校级教育角色只落 edu_user_role（校领导 / 教务主任）；年级主任在 edu_grade_leader、班主任在 edu_class（DP-01 / DP-02）；
 * - 任教关系是 DS-07（任课教师只看本人所授班级与学科）的权威来源；失效写 status='0'，不物理删除；
 * - 非在职教师只保留查看与撤销离职登记（GAP-075）。
 *
 * @author Codex
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EduTeacherServiceImpl implements IEduTeacherService {

    /** 在职状态 */
    private static final String STATUS_ACTIVE = "active";

    /** 角色 / 任教关系启停标记 */
    private static final String FLAG_ON = "1";
    private static final String FLAG_OFF = "0";

    /** 班级类型：行政班 */
    private static final String CLASS_TYPE_ADMINISTRATIVE = "administrative";

    /** 账号状态：正常 / 停用（UserStatus 的码值） */
    private static final String ACCOUNT_NORMAL = "0";
    private static final String ACCOUNT_DISABLED = "1";

    /** 原因类文案的最小长度（停用 / 启用原因至少 5 个字） */
    private static final int REASON_MIN_LENGTH = 5;

    private final EduTeacherMapper baseMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final EduTeachingAssignmentMapper assignmentMapper;

    @DubboReference
    private RemoteUserService remoteUserService;

    // ==================== 教师主体 ====================

    @Override
    public TableDataInfo<EduTeacherVo> queryPageList(EduTeacherBo teacher, PageQuery pageQuery) {
        LambdaQueryWrapper<EduTeacher> wrapper = new LambdaQueryWrapper<EduTeacher>()
            .eq(teacher.getSchoolId() != null, EduTeacher::getSchoolId, teacher.getSchoolId())
            .eq(StringUtils.isNotBlank(teacher.getEmploymentStatus()), EduTeacher::getEmploymentStatus, teacher.getEmploymentStatus())
            .eq(StringUtils.isNotBlank(teacher.getTeacherNo()), EduTeacher::getTeacherNo, teacher.getTeacherNo())
            .eq(StringUtils.isNotBlank(teacher.getGender()), EduTeacher::getGender, teacher.getGender())
            .orderByAsc(EduTeacher::getTeacherNo);
        if (StringUtils.isNotBlank(teacher.getKeyword())) {
            wrapper.and(w -> w.like(EduTeacher::getTeacherNo, teacher.getKeyword())
                .or().like(EduTeacher::getTeacherName, teacher.getKeyword())
                .or().like(EduTeacher::getPhone, teacher.getKeyword()));
        }
        Page<EduTeacherVo> result = baseMapper.selectPageTeacherList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    public EduTeacherVo queryById(Long teacherId) {
        EduTeacherVo vo = baseMapper.selectVoById(teacherId);
        if (vo == null) {
            throw new ServiceException("教师不存在或不在当前数据范围内");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(EduTeacherBo teacher) {
        validateTeacherNoUnique(teacher.getSchoolId(), teacher.getTeacherNo(), null);
        EduTeacher add = new EduTeacher();
        copyWritableFields(teacher, add);
        add.setEmploymentStatus(StringUtils.isBlank(teacher.getEmploymentStatus()) ? STATUS_ACTIVE : teacher.getEmploymentStatus());
        // 保存成功后自动创建登录账号（账号信息在 sys_user，本表只留 userId 引用）
        if (StringUtils.isNotBlank(teacher.getLoginName())) {
            RemoteUserBo account = new RemoteUserBo();
            account.setUserName(teacher.getLoginName());
            account.setNickName(teacher.getTeacherName());
            account.setPhonenumber(teacher.getPhone());
            account.setEmail(teacher.getEmail());
            account.setSex(teacher.getGender());
            account.setPassword(teacher.getPassword());
            try {
                remoteUserService.registerUserInfo(account);
            } catch (Exception e) {
                throw new ServiceException("创建教师登录账号失败：" + e.getMessage());
            }
            // 账号建好后回查 userId 落库：教师页的「重置密码 / 停用账号」都要用它（GAP-116）
            add.setUserId(queryUserIdByLoginName(teacher.getLoginName()));
        }
        return baseMapper.insert(add) > 0;
    }

    /**
     * 按登录名回查账号 userId。
     *
     * `RemoteUserService.registerUserInfo` 只返回成功标志，账号 ID 需要再查一次（与学生重置密码同一做法）；
     * 查不到时返回 null 且不阻断建档 —— 后续「重置密码」会给出「该教师还没有登录账号」的明确提示。
     */
    private Long queryUserIdByLoginName(String loginName) {
        try {
            var loginUser = remoteUserService.getUserInfo(loginName, LoginHelper.getTenantId());
            return loginUser == null ? null : loginUser.getUserId();
        } catch (Exception e) {
            log.warn("教师账号建好后未回查到 userId：loginName={}，{}", loginName, e.getMessage());
            return null;
        }
    }

    @Override
    public Boolean updateByBo(EduTeacherBo teacher) {
        EduTeacher update = requireTeacher(teacher.getTeacherId());
        if (!STATUS_ACTIVE.equals(update.getEmploymentStatus())) {
            throw new ServiceException("非在职教师只保留查看与撤销离职登记，不支持编辑（GAP-075）");
        }
        update.setTeacherName(teacher.getTeacherName());
        update.setGender(teacher.getGender());
        update.setPhone(teacher.getPhone());
        update.setEmail(teacher.getEmail());
        update.setHireDate(DateUtils.parseDate(teacher.getHireDate()));
        update.setRemark(teacher.getRemark());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateTeacherNo(Long teacherId, String teacherNo, String reason) {
        EduTeacher update = requireTeacher(teacherId);
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("修改工号必须填写原因（至少 5 个字），REQ-TCH-022");
        }
        validateTeacherNoUnique(update.getSchoolId(), teacherNo, teacherId);
        update.setTeacherNo(teacherNo);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean leaveTeacher(Long teacherId, String employmentStatus, String leaveDate, String reason) {
        EduTeacher update = requireTeacher(teacherId);
        if (!STATUS_ACTIVE.equals(update.getEmploymentStatus())) {
            throw new ServiceException("该教师已是非在职状态");
        }
        if (StringUtils.isBlank(employmentStatus) || STATUS_ACTIVE.equals(employmentStatus)) {
            throw new ServiceException("请选择离职或调离");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("离职 / 调离原因至少 5 个字");
        }
        update.setEmploymentStatus(employmentStatus);
        update.setLeaveDate(DateUtils.parseDate(leaveDate));
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean revokeTeacherLeave(Long teacherId, String reason) {
        EduTeacher update = requireTeacher(teacherId);
        if (STATUS_ACTIVE.equals(update.getEmploymentStatus())) {
            throw new ServiceException("该教师是在职状态，无需撤销");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("撤销原因至少 5 个字");
        }
        update.setEmploymentStatus(STATUS_ACTIVE);
        update.setLeaveDate(null);
        return baseMapper.updateById(update) > 0;
    }

    // ==================== 教育角色 ====================

    @Override
    public List<EduUserRoleVo> listRole(Long teacherId) {
        EduTeacher teacher = requireTeacher(teacherId);
        return userRoleMapper.selectVoList(new LambdaQueryWrapper<EduUserRole>()
            .eq(EduUserRole::getTeacherId, teacherId)
            .or()
            .eq(EduUserRole::getUserId, teacher.getUserId()));
    }

    @Override
    public Boolean saveRole(Long teacherId, EduUserRoleBo role) {
        EduTeacher teacher = requireTeacher(teacherId);
        Long userId = role.getUserId() != null ? role.getUserId() : teacher.getUserId();
        if (userId == null) {
            throw new ServiceException("该教师尚未关联系统账号，无法授予教育角色");
        }
        // 唯一键 uk_user_role = tenant_id + school_id + user_id + edu_role
        EduUserRole exist = userRoleMapper.selectOne(new LambdaQueryWrapper<EduUserRole>()
            .eq(EduUserRole::getSchoolId, teacher.getSchoolId())
            .eq(EduUserRole::getUserId, userId)
            .eq(EduUserRole::getEduRole, role.getEduRole()));
        if (exist != null) {
            exist.setStatus(FLAG_ON);
            exist.setTeacherId(teacherId);
            exist.setStartDate(DateUtils.parseDate(role.getStartDate()));
            exist.setEndDate(DateUtils.parseDate(role.getEndDate()));
            return userRoleMapper.updateById(exist) > 0;
        }
        EduUserRole add = new EduUserRole();
        add.setSchoolId(teacher.getSchoolId());
        add.setUserId(userId);
        add.setTeacherId(teacherId);
        add.setEduRole(role.getEduRole());
        add.setStatus(FLAG_ON);
        add.setStartDate(DateUtils.parseDate(role.getStartDate()));
        add.setEndDate(DateUtils.parseDate(role.getEndDate()));
        return userRoleMapper.insert(add) > 0;
    }

    @Override
    public Boolean removeRole(Long userRoleId) {
        EduUserRole role = userRoleMapper.selectById(userRoleId);
        if (role == null) {
            throw new ServiceException("教育角色记录不存在");
        }
        role.setStatus(FLAG_OFF);
        return userRoleMapper.updateById(role) > 0;
    }

    // ==================== 任教关系 ====================

    @Override
    public TableDataInfo<EduTeachingAssignmentVo> queryAssignmentPageList(EduTeachingAssignmentBo assignment, PageQuery pageQuery) {
        LambdaQueryWrapper<EduTeachingAssignment> wrapper = new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(assignment.getSchoolId() != null, EduTeachingAssignment::getSchoolId, assignment.getSchoolId())
            .eq(assignment.getTermId() != null, EduTeachingAssignment::getTermId, assignment.getTermId())
            .eq(assignment.getTeacherId() != null, EduTeachingAssignment::getTeacherId, assignment.getTeacherId())
            .eq(assignment.getSubjectId() != null, EduTeachingAssignment::getSubjectId, assignment.getSubjectId())
            .eq(assignment.getClassId() != null, EduTeachingAssignment::getClassId, assignment.getClassId())
            .eq(StringUtils.isNotBlank(assignment.getClassType()), EduTeachingAssignment::getClassType, assignment.getClassType())
            .eq(StringUtils.isNotBlank(assignment.getStatus()), EduTeachingAssignment::getStatus, assignment.getStatus());
        Page<EduTeachingAssignmentVo> result = assignmentMapper.selectPageAssignmentList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    public Boolean saveAssignment(EduTeachingAssignmentBo assignment) {
        validateAssignment(assignment);
        // 唯一键 uk_assignment = term_id + teacher_id + subject_id + class_type + class_id
        EduTeachingAssignment exist = assignmentMapper.selectOne(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getTermId, assignment.getTermId())
            .eq(EduTeachingAssignment::getTeacherId, assignment.getTeacherId())
            .eq(EduTeachingAssignment::getSubjectId, assignment.getSubjectId())
            .eq(EduTeachingAssignment::getClassType, assignment.getClassType())
            .eq(EduTeachingAssignment::getClassId, assignment.getClassId()));
        if (exist != null) {
            exist.setStatus(FLAG_ON);
            return assignmentMapper.updateById(exist) > 0;
        }
        EduTeachingAssignment add = new EduTeachingAssignment();
        copyAssignmentFields(assignment, add);
        add.setStatus(StringUtils.isBlank(assignment.getStatus()) ? FLAG_ON : assignment.getStatus());
        return assignmentMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSaveAssignment(EduTeachingAssignmentBo assignment) {
        List<EduTeachingAssignmentBo> rows = assignment.getAssignmentList();
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("批量保存至少需要一行任教关系");
        }
        for (EduTeachingAssignmentBo row : rows) {
            if (row.getClassId() == null) {
                row.setClassId(assignment.getClassId());
            }
            if (row.getClassType() == null) {
                row.setClassType(assignment.getClassType());
            }
            if (row.getTermId() == null) {
                row.setTermId(assignment.getTermId());
            }
            if (row.getSchoolId() == null) {
                row.setSchoolId(assignment.getSchoolId());
            }
            saveAssignment(row);
        }
        return true;
    }

    @Override
    public Boolean removeAssignment(Long assignmentId, String reason) {
        EduTeachingAssignment update = assignmentMapper.selectById(assignmentId);
        if (update == null) {
            throw new ServiceException("任教关系不存在");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("结束任教关系必须填写原因（至少 5 个字）");
        }
        update.setStatus(FLAG_OFF);
        return assignmentMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copyAssignment(EduTeachingAssignmentBo assignment) {
        if (assignment.getSourceTermId() == null || assignment.getTargetTermId() == null) {
            throw new ServiceException("请选择源学年学期与目标学年学期");
        }
        if (assignment.getSourceTermId().equals(assignment.getTargetTermId())) {
            throw new ServiceException("源学年学期与目标学年学期不能相同");
        }
        List<EduTeachingAssignment> sources = assignmentMapper.selectList(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getTermId, assignment.getSourceTermId())
            .eq(EduTeachingAssignment::getStatus, FLAG_ON));
        if (sources.isEmpty()) {
            throw new ServiceException("源学年学期没有可复制的任教关系");
        }
        for (EduTeachingAssignment source : sources) {
            // 目标学期已有同一「班级 + 学科」时跳过，不覆盖已有配置
            EduTeachingAssignment exist = assignmentMapper.selectOne(new LambdaQueryWrapper<EduTeachingAssignment>()
                .eq(EduTeachingAssignment::getTermId, assignment.getTargetTermId())
                .eq(EduTeachingAssignment::getClassId, source.getClassId())
                .eq(EduTeachingAssignment::getClassType, source.getClassType())
                .eq(EduTeachingAssignment::getSubjectId, source.getSubjectId()));
            if (exist != null) {
                continue;
            }
            EduTeachingAssignment add = new EduTeachingAssignment();
            add.setSchoolId(source.getSchoolId());
            add.setTermId(assignment.getTargetTermId());
            add.setTeacherId(source.getTeacherId());
            add.setSubjectId(source.getSubjectId());
            add.setClassType(source.getClassType());
            add.setClassId(source.getClassId());
            add.setStatus(FLAG_ON);
            assignmentMapper.insert(add);
        }
        return true;
    }

    // ==================== 内部方法 ====================

    private void validateAssignment(EduTeachingAssignmentBo assignment) {
        if (assignment.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        if (assignment.getTeacherId() == null) {
            throw new ServiceException("请选择任教教师");
        }
        if (assignment.getSubjectId() == null) {
            throw new ServiceException("请选择学科");
        }
        if (StringUtils.isBlank(assignment.getClassType())) {
            assignment.setClassType(CLASS_TYPE_ADMINISTRATIVE);
        }
        if (assignment.getClassId() == null) {
            throw new ServiceException("请选择班级");
        }
        EduTeacher teacher = requireTeacher(assignment.getTeacherId());
        if (!STATUS_ACTIVE.equals(teacher.getEmploymentStatus())) {
            throw new ServiceException("离职 / 调离的教师不可新增任教（GAP-075）");
        }
    }

    private void copyWritableFields(EduTeacherBo bo, EduTeacher entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setTeacherNo(bo.getTeacherNo());
        entity.setTeacherName(bo.getTeacherName());
        entity.setGender(bo.getGender());
        entity.setPhone(bo.getPhone());
        entity.setEmail(bo.getEmail());
        entity.setHireDate(DateUtils.parseDate(bo.getHireDate()));
        entity.setRemark(bo.getRemark());
    }

    private void copyAssignmentFields(EduTeachingAssignmentBo bo, EduTeachingAssignment entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setTermId(bo.getTermId());
        entity.setTeacherId(bo.getTeacherId());
        entity.setSubjectId(bo.getSubjectId());
        entity.setClassType(bo.getClassType());
        entity.setClassId(bo.getClassId());
    }

    /** 工号在学校租户内唯一（BR-TEACHER-007） */
    private void validateTeacherNoUnique(Long schoolId, String teacherNo, Long excludeId) {
        if (StringUtils.isBlank(teacherNo)) {
            throw new ServiceException("工号不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<EduTeacher>()
            .eq(schoolId != null, EduTeacher::getSchoolId, schoolId)
            .eq(EduTeacher::getTeacherNo, teacherNo)
            .ne(excludeId != null, EduTeacher::getTeacherId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException("工号在该学校内已存在：" + teacherNo);
        }
    }

    private EduTeacher requireTeacher(Long teacherId) {
        if (teacherId == null) {
            throw new ServiceException("缺少教师 ID");
        }
        EduTeacher teacher = baseMapper.selectById(teacherId);
        if (teacher == null) {
            throw new ServiceException("教师不存在或不在当前数据范围内");
        }
        return teacher;
    }

    /** 供班级模块 / 教学班模块判断某学期某班是否有任教关系（DS-07 的判定入口） */
    public boolean hasAssignment(Long termId, String classType, Long classId) {
        Long count = assignmentMapper.selectCount(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getTermId, termId)
            .eq(EduTeachingAssignment::getClassType, classType)
            .eq(EduTeachingAssignment::getClassId, classId)
            .eq(EduTeachingAssignment::getStatus, FLAG_ON));
        return count != null && count > 0;
    }

    // ==================== 登录账号（GAP-091） ====================

    @Override
    public Boolean resetTeacherPassword(Long teacherId, String password) {
        EduTeacher teacher = requireTeacher(teacherId);
        if (teacher.getUserId() == null) {
            throw new ServiceException("该教师还没有登录账号，无法重置密码");
        }
        if (StringUtils.isBlank(password)) {
            throw new ServiceException("请输入新密码");
        }
        return remoteUserService.resetPassword(teacher.getUserId(), password, LoginHelper.getTenantId());
    }

    @Override
    public Boolean disableTeacherAccount(Long teacherId, String reason) {
        return changeTeacherAccountStatus(teacherId, reason, ACCOUNT_DISABLED, "停用");
    }

    @Override
    public Boolean enableTeacherAccount(Long teacherId, String reason) {
        return changeTeacherAccountStatus(teacherId, reason, ACCOUNT_NORMAL, "启用");
    }

    /**
     * 教师账号启停用。
     * 与既有「离职 / 停用必须填原因」的口径一致：原因至少 5 个字（便于审计追溯）。
     */
    private Boolean changeTeacherAccountStatus(Long teacherId, String reason, String status, String action) {
        EduTeacher teacher = requireTeacher(teacherId);
        if (teacher.getUserId() == null) {
            throw new ServiceException("该教师还没有登录账号，无法" + action);
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < REASON_MIN_LENGTH) {
            throw new ServiceException(action + "原因必填，且至少 " + REASON_MIN_LENGTH + " 个字");
        }
        return remoteUserService.changeAccountStatus(teacher.getUserId(), status, LoginHelper.getTenantId());
    }

}
