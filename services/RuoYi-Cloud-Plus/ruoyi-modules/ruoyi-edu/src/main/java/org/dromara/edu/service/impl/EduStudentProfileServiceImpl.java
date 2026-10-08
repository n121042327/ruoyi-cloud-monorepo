package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduEnrollmentChange;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduGuardian;
import org.dromara.edu.domain.EduStudent;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.RemoteUserService;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.edu.service.IEduAuditService;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.bo.EduAuditLogBo;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.EduStudentFieldChange;
import org.dromara.edu.domain.EduStudentGuardian;
import org.dromara.edu.domain.bo.EduGuardianBo;
import org.dromara.edu.domain.bo.EduStudentEnrollmentBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduEnrollmentStatusOptionVo;
import org.dromara.edu.domain.vo.EduGuardianVo;
import org.dromara.edu.domain.vo.EduStudentEnrollmentVo;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduEnrollmentChangeMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduGuardianMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentFieldChangeMapper;
import org.dromara.edu.mapper.EduStudentGuardianMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduStudentProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 学生档案服务层处理
 *
 * 口径要点：
 * - 学籍状态的**唯一流转入口**是升班与学籍异动模块（DP-01）：变更写在校记录 + 追加异动记录（BR-PROMO-012 追加式）；
 * - 开除在义务教育阶段不可用且后端拒绝（REQ-PRM-043）；退学 / 开除 / 死亡需校级管理员审批；
 * - 复学 / 报到必须指定班级，但班级关系仍由班级管理写（DP-01：学生班级归属唯一写入入口在班级管理）；
 * - 监护人主体是平台级实体（不设租户），手机号平台唯一，一个家长可对应多个孩子；
 *   一个学生的监护人绑定上限 3（BR-ACCOUNT-018），解绑需原因（GAP-015）；
 * - 敏感字段：证件号 / 手机号默认掩码，明文只在有 read_sensitive 时返回并写审计。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduStudentProfileServiceImpl implements IEduStudentProfileService {

    /** 学籍状态 */
    private static final String STATUS_ENROLLED = "在读";
    private static final String STATUS_SUSPENDED = "休学";
    private static final String STATUS_ABROAD = "出国保留学籍";
    private static final String STATUS_MISSING = "失踪";
    private static final String STATUS_TRANSFER_OUT = "转出";
    private static final String STATUS_WITHDRAW = "退学";
    private static final String STATUS_EXPEL = "开除";
    private static final String STATUS_GRADUATED = "已毕业";
    private static final String STATUS_NOT_REPORTED = "转入未报到";
    private static final String STATUS_DEATH = "死亡";

    /** 义务教育阶段学段编码（开除在义务教育阶段不可用） */
    private static final List<String> COMPULSORY_STAGES = List.of("primary", "junior");

    /** 监护人绑定上限（BR-ACCOUNT-018 同口径） */
    private static final int GUARDIAN_BIND_LIMIT = 3;

    /** 绑定状态 */
    private static final String BIND_PENDING = "pending";
    private static final String BIND_APPROVED = "approved";
    private static final String BIND_UNBINDING = "unbinding";

    /** 通用标记 */
    private static final String FLAG_ON = "1";

    private final EduStudentMapper studentMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final EduEnrollmentChangeMapper changeMapper;
    private final EduGuardianMapper guardianMapper;
    private final EduStudentGuardianMapper studentGuardianMapper;
    private final EduStudentFieldChangeMapper fieldChangeMapper;
    private final EduGradeMapper gradeMapper;
    private final EduClassMemberMapper classMemberMapper;

    /**
     * 学生登录名前缀：登录名 = s + 学号（REQ-STU-023 / BR-ACCOUNT-002）。
     * edu_student 没有 user_id 列，学生的账号关联靠这个约定，见 D-169。
     */
    private static final String STUDENT_LOGIN_PREFIX = "s";

    /** 照片允许的后缀（拒绝可执行文件与伪装扩展名，NFR-SEC-04） */
    private static final java.util.Set<String> PHOTO_ALLOWED_SUFFIXES =
        java.util.Set.of(".jpg", ".jpeg", ".png", ".webp");

    /** 照片大小上限 2 MB */
    private static final long PHOTO_MAX_SIZE = 2L * 1024 * 1024;

    @DubboReference
    private RemoteUserService remoteUserService;

    /** 统一文件服务：照片上传与按地址取字节（CR-095） */
    @DubboReference
    private RemoteFileService remoteFileService;

    /** 敏感数据访问留痕（REQ-AUD-008） */
    private final IEduAuditService auditService;

    // ==================== 学籍状态 ====================

    @Override
    public List<EduEnrollmentStatusOptionVo> listStatusOption(Long studentId) {
        requireStudent(studentId);
        EduStudentEnrollment enrollment = findEnrollment(studentId);
        String current = enrollment == null ? STATUS_ENROLLED : enrollment.getEnrollmentStatus();
        boolean compulsory = isCompulsory(studentId, enrollment);

        List<EduEnrollmentStatusOptionVo> options = new ArrayList<>();
        options.add(option("suspend", STATUS_ENROLLED, STATUS_SUSPENDED, false, false, false, null));
        options.add(option("resume", STATUS_SUSPENDED, STATUS_ENROLLED, false, true, false, null));
        options.add(option("abroad", STATUS_ENROLLED, STATUS_ABROAD, false, false, false, null));
        options.add(option("missing", STATUS_ENROLLED, STATUS_MISSING, true, false, false, null));
        options.add(option("transfer_out", STATUS_ENROLLED, STATUS_TRANSFER_OUT, false, false, false, null));
        options.add(option("withdraw", STATUS_ENROLLED, STATUS_WITHDRAW, true, false, false, null));
        options.add(option("expel", STATUS_ENROLLED, STATUS_EXPEL, true, false, compulsory,
            compulsory ? "义务教育阶段不可使用开除（REQ-PRM-043）" : null));
        options.add(option("graduate", STATUS_ENROLLED, STATUS_GRADUATED, false, false, false, null));
        options.add(option("check_in", STATUS_NOT_REPORTED, STATUS_ENROLLED, false, true, false, null));
        options.add(option("death", STATUS_ENROLLED, STATUS_DEATH, true, false, false, null));

        // 与当前状态不匹配的流转直接禁用，避免前端误选
        options.forEach(item -> {
            String from = item.getValue() == null ? "" : item.getValue();
            boolean available = matchesCurrent(item, current, from);
            if (!available && !Boolean.TRUE.equals(item.getDisabled())) {
                item.setDisabled(Boolean.TRUE);
                item.setDisabledReason("当前状态为「" + current + "」，该动作不可用");
            }
        });
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeEnrollmentStatus(Long studentId, EduStudentEnrollmentBo change) {
        requireStudent(studentId);
        if (StringUtils.isBlank(change.getChangeType())) {
            throw new ServiceException("请选择异动类型");
        }
        if (StringUtils.isBlank(change.getReason()) || change.getReason().trim().length() < 5) {
            throw new ServiceException("异动原因至少 5 个字");
        }
        String afterStatus = mapChangeTypeToStatus(change.getChangeType());
        EduStudentEnrollment enrollment = findEnrollment(studentId);
        String beforeStatus = enrollment == null ? null : enrollment.getEnrollmentStatus();
        if (enrollment == null) {
            throw new ServiceException("该学生在本校没有在校记录，无法登记学籍异动");
        }
        if ("expel".equals(change.getChangeType()) && isCompulsory(studentId, enrollment)) {
            throw new ServiceException("义务教育阶段不可使用开除（REQ-PRM-043）");
        }
        if (("resume".equals(change.getChangeType()) || "check_in".equals(change.getChangeType()))
            && change.getClassId() == null) {
            throw new ServiceException("复学 / 报到必须指定班级（班级关系仍由班级管理写入，DP-01）");
        }

        Date effective = DateUtils.parseDate(change.getEffectiveDate());
        enrollment.setEnrollmentStatus(afterStatus);
        enrollment.setStatusEffectiveDate(effective);
        if (List.of(STATUS_GRADUATED, STATUS_TRANSFER_OUT, STATUS_WITHDRAW, STATUS_EXPEL, STATUS_DEATH).contains(afterStatus)) {
            enrollment.setLeaveDate(effective);
        }
        enrollmentMapper.updateById(enrollment);

        EduEnrollmentChange record = new EduEnrollmentChange();
        record.setSchoolId(enrollment.getSchoolId());
        record.setStudentId(studentId);
        record.setSchoolRecordId(enrollment.getEnrollmentId());
        record.setChangeType(change.getChangeType());
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        record.setEffectiveDate(effective);
        record.setReason(change.getReason());
        // 退学 / 开除 / 死亡需校级管理员审批（REQ-PRM-043 口径）
        if (List.of(STATUS_WITHDRAW, STATUS_EXPEL, STATUS_DEATH).contains(afterStatus)) {
            record.setApprovalStatus("待审批");
        }
        record.setOperateTime(new Date());
        changeMapper.insert(record);
        return true;
    }

    @Override
    public List<EduEnrollmentChangeVo> listChangeLog(Long studentId) {
        requireStudent(studentId);
        List<EduEnrollmentChangeVo> changes = changeMapper.selectVoList(new LambdaQueryWrapper<EduEnrollmentChange>()
            .eq(EduEnrollmentChange::getStudentId, studentId));
        changes.forEach(item -> {
            item.setChangeTag((item.getBeforeStatus() == null ? "—" : item.getBeforeStatus())
                + " → " + (item.getAfterStatus() == null ? "—" : item.getAfterStatus()));
            item.setSummary(item.getChangeType() + "：" + (item.getReason() == null ? "" : item.getReason()));
            item.setChangeTime(item.getOperateTime() != null ? item.getOperateTime() : item.getEffectiveDate());
        });
        // 资料变更申请也进同一时间线（只读聚合，来源 edu_student_field_change）
        List<EduStudentFieldChange> fieldChanges = fieldChangeMapper.selectList(new LambdaQueryWrapper<EduStudentFieldChange>()
            .eq(EduStudentFieldChange::getStudentId, studentId));
        for (EduStudentFieldChange field : fieldChanges) {
            EduEnrollmentChangeVo vo = new EduEnrollmentChangeVo();
            vo.setChangeId(field.getFieldChangeId());
            vo.setStudentId(studentId);
            vo.setChangeType("资料变更");
            vo.setChangeTag(field.getFieldName() + "：" + (field.getOldValue() == null ? "—" : "已填写")
                + " → " + (field.getNewValue() == null ? "—" : "已申请"));
            vo.setSummary("资料变更申请（" + field.getStatus() + "）："
                + (field.getApplyReason() == null ? "" : field.getApplyReason()));
            vo.setChangeTime(field.getCreateTime());
            changes.add(vo);
        }
        changes.sort(Comparator.comparing(EduEnrollmentChangeVo::getChangeTime,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return changes;
    }

    @Override
    public EduStudentEnrollmentVo queryEnrollment(Long studentId) {
        EduStudentEnrollment enrollment = findEnrollment(studentId);
        if (enrollment == null) {
            return null;
        }
        return enrollmentMapper.selectVoById(enrollment.getEnrollmentId());
    }

    // ==================== 监护人 ====================

    @Override
    public List<EduGuardianVo> listGuardian(Long studentId) {
        requireStudent(studentId);
        List<EduStudentGuardian> relations = studentGuardianMapper.selectList(new LambdaQueryWrapper<EduStudentGuardian>()
            .eq(EduStudentGuardian::getStudentId, studentId));
        List<EduGuardianVo> result = new ArrayList<>();
        for (EduStudentGuardian relation : relations) {
            EduGuardian guardian = guardianMapper.selectById(relation.getGuardianId());
            if (guardian == null) {
                continue;
            }
            EduGuardianVo vo = new EduGuardianVo();
            vo.setRelationId(relation.getRelationId());
            vo.setGuardianId(guardian.getGuardianId());
            vo.setStudentId(studentId);
            vo.setGuardianName(guardian.getGuardianName());
            vo.setGuardianPhoneMasked(maskPhone(guardian.getGuardianPhone()));
            vo.setRelation(relation.getRelation());
            vo.setIsPrimary(FLAG_ON.equals(relation.getIsPrimary()));
            vo.setBindStatus(relation.getBindStatus());
            vo.setSource(relation.getSource());
            vo.setStatus(guardian.getStatus());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveGuardian(Long studentId, EduGuardianBo guardian) {
        requireStudent(studentId);
        // 监护人主体平台唯一：按手机号复用已有主体（一个家长可对应多个孩子，GAP-015）
        EduGuardian entity = guardianMapper.selectOne(new LambdaQueryWrapper<EduGuardian>()
            .eq(EduGuardian::getGuardianPhone, guardian.getGuardianPhone()));
        if (entity == null) {
            entity = new EduGuardian();
            entity.setGuardianName(guardian.getGuardianName());
            entity.setGuardianPhone(guardian.getGuardianPhone());
            entity.setIdCardNo(guardian.getIdCardNo());
            entity.setStatus(FLAG_ON);
            guardianMapper.insert(entity);
        } else {
            entity.setGuardianName(guardian.getGuardianName());
            if (StringUtils.isNotBlank(guardian.getIdCardNo())) {
                entity.setIdCardNo(guardian.getIdCardNo());
            }
            guardianMapper.updateById(entity);
        }
        EduStudentGuardian relation = studentGuardianMapper.selectOne(new LambdaQueryWrapper<EduStudentGuardian>()
            .eq(EduStudentGuardian::getStudentId, studentId)
            .eq(EduStudentGuardian::getGuardianId, entity.getGuardianId()));
        if (relation == null) {
            Long bound = studentGuardianMapper.selectCount(new LambdaQueryWrapper<EduStudentGuardian>()
                .eq(EduStudentGuardian::getStudentId, studentId)
                .in(EduStudentGuardian::getBindStatus, BIND_PENDING, BIND_APPROVED, BIND_UNBINDING));
            if (bound != null && bound >= GUARDIAN_BIND_LIMIT) {
                throw new ServiceException("一个学生最多绑定 " + GUARDIAN_BIND_LIMIT + " 名监护人（BR-ACCOUNT-018）");
            }
            relation = new EduStudentGuardian();
            relation.setStudentId(studentId);
            relation.setGuardianId(entity.getGuardianId());
            relation.setSource(StringUtils.isBlank(guardian.getSource()) ? "teacher" : guardian.getSource());
        }
        relation.setRelation(guardian.getRelation());
        relation.setIsPrimary(Boolean.TRUE.equals(guardian.getIsPrimary()) ? FLAG_ON : "0");
        // 教师录入直接生效；其它来源（扫码 / 导入）走待审核（GAP-015）
        relation.setBindStatus(StringUtils.isBlank(relation.getBindStatus())
            ? ("teacher".equals(relation.getSource()) ? BIND_APPROVED : BIND_PENDING)
            : relation.getBindStatus());
        // 契约里的 remark 在 edu_student_guardian 没有对应列（GAP-092）：按裁决复用 audit_opinion 承载
        if (StringUtils.isNotBlank(guardian.getRemark())) {
            relation.setAuditOpinion(guardian.getRemark());
        }
        if (relation.getBindTime() == null) {
            relation.setBindTime(new Date());
        }
        return relation.getRelationId() == null
            ? studentGuardianMapper.insert(relation) > 0
            : studentGuardianMapper.updateById(relation) > 0;
    }

    @Override
    public Boolean unbindGuardian(Long studentId, Long guardianId, String reason) {
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("解绑监护人必须填写原因（至少 5 个字）");
        }
        EduStudentGuardian relation = studentGuardianMapper.selectOne(new LambdaQueryWrapper<EduStudentGuardian>()
            .eq(EduStudentGuardian::getStudentId, studentId)
            .eq(EduStudentGuardian::getGuardianId, guardianId));
        if (relation == null) {
            throw new ServiceException("该监护人与该学生没有绑定关系");
        }
        // 解绑需班主任确认（GAP-015）：置为待解绑并留审核意见；班级管理侧的审核入口在后续批次补齐
        relation.setBindStatus(BIND_UNBINDING);
        relation.setAuditOpinion(reason);
        relation.setUnbindTime(new Date());
        return studentGuardianMapper.updateById(relation) > 0;
    }

    // ==================== 敏感字段与学生主体 ====================

    @Override
    public String viewIdCard(Long studentId) {
        EduStudent student = requireStudent(studentId);
        if (StringUtils.isBlank(student.getIdCardNo())) {
            throw new ServiceException("该学生未登记证件号");
        }
        // 明文查看需 person.student:read_sensitive（控制器已注解）并写审计（NFR-AUDIT-02）
        return student.getIdCardNo();
    }

    @Override
    public Boolean updateStudentNo(Long studentId, String studentNo, String reason) {
        EduStudent student = requireStudent(studentId);
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("修改学号必须填写原因（至少 5 个字）");
        }
        Long exist = studentMapper.selectCount(new LambdaQueryWrapper<EduStudent>()
            .eq(EduStudent::getStudentNo, studentNo)
            .ne(EduStudent::getStudentId, studentId));
        if (exist != null && exist > 0) {
            throw new ServiceException("学号已被占用，且学号永不回收（BR-STU-001）：" + studentNo);
        }
        student.setStudentNo(studentNo);
        return studentMapper.updateById(student) > 0;
    }

    @Override
    public Boolean removeStudent(Long studentId, String reason) {
        requireStudent(studentId);
        Long enrolled = classMemberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getStudentId, studentId)
            .eq(EduClassMember::getStatus, FLAG_ON));
        if (enrolled != null && enrolled > 0) {
            throw new ServiceException("该学生仍在班级花名册中，请先通过班级管理移出或走学籍异动（DP-01）");
        }
        Long changes = changeMapper.selectCount(new LambdaQueryWrapper<EduEnrollmentChange>()
            .eq(EduEnrollmentChange::getStudentId, studentId));
        if (changes != null && changes > 0) {
            throw new ServiceException("该学生已有学籍异动记录，历史与学籍类数据禁止物理删除，请走学籍异动处理");
        }
        return studentMapper.deleteById(studentId) > 0;
    }

    // ==================== 内部方法 ====================

    /** 组装一个异动动作选项 */
    private EduEnrollmentStatusOptionVo option(String value, String from, String to, boolean needApproval,
                                               boolean needClass, boolean disabled, String disabledReason) {
        EduEnrollmentStatusOptionVo vo = new EduEnrollmentStatusOptionVo();
        vo.setValue(value);
        vo.setLabel(label(value) + "（" + from + " → " + to + "）");
        vo.setNeedApproval(needApproval);
        vo.setNeedClass(needClass);
        vo.setDisabled(disabled);
        vo.setDisabledReason(disabledReason);
        return vo;
    }

    /** 动作码对应的当前状态是否匹配（不匹配则在列表里禁用） */
    private boolean matchesCurrent(EduEnrollmentStatusOptionVo item, String current, String value) {
        String expectFrom = switch (value) {
            case "resume" -> STATUS_SUSPENDED;
            case "check_in" -> STATUS_NOT_REPORTED;
            default -> STATUS_ENROLLED;
        };
        return expectFrom.equals(current);
    }

    private String label(String value) {
        return switch (value) {
            case "suspend" -> "休学";
            case "resume" -> "复学";
            case "abroad" -> "出国保留学籍";
            case "missing" -> "失踪";
            case "transfer_out" -> "转出";
            case "withdraw" -> "退学";
            case "expel" -> "开除";
            case "graduate" -> "毕业";
            case "check_in" -> "报到";
            case "death" -> "死亡";
            default -> value;
        };
    }

    /** 异动动作码 → 变更后的学籍状态 */
    private String mapChangeTypeToStatus(String changeType) {
        return switch (changeType) {
            case "suspend" -> STATUS_SUSPENDED;
            case "resume", "check_in" -> STATUS_ENROLLED;
            case "abroad" -> STATUS_ABROAD;
            case "missing" -> STATUS_MISSING;
            case "transfer_out" -> STATUS_TRANSFER_OUT;
            case "withdraw" -> STATUS_WITHDRAW;
            case "expel" -> STATUS_EXPEL;
            case "graduate" -> STATUS_GRADUATED;
            case "death" -> STATUS_DEATH;
            default -> throw new ServiceException("未知的异动类型：" + changeType);
        };
    }

    /** 是否义务教育阶段（用在校记录的入校年级反查学段） */
    private boolean isCompulsory(Long studentId, EduStudentEnrollment enrollment) {
        if (enrollment == null || enrollment.getEntryGradeId() == null) {
            return false;
        }
        EduGrade grade = gradeMapper.selectById(enrollment.getEntryGradeId());
        return grade != null && COMPULSORY_STAGES.contains(grade.getStageCode());
    }

    private EduStudentEnrollment findEnrollment(Long studentId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<EduStudentEnrollment>()
            .eq(EduStudentEnrollment::getStudentId, studentId)
            .last("limit 1"));
    }

    private EduStudent requireStudent(Long studentId) {
        if (studentId == null) {
            throw new ServiceException("缺少学生 ID");
        }
        EduStudent student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在或不在当前数据范围内");
        }
        return student;
    }

    /** 手机号掩码：保留前 3 后 4（敏感字段默认掩码展示） */
    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public Boolean resetStudentPassword(Long studentId, String password) {
        EduStudent student = requireStudent(studentId);
        if (StringUtils.isBlank(student.getStudentNo())) {
            throw new ServiceException("该学生没有学号，无法定位登录账号");
        }
        if (StringUtils.isBlank(password)) {
            throw new ServiceException("请输入新密码");
        }
        // 学生账号按登录名约定关联：登录名 = s + 学号（REQ-STU-023 / BR-ACCOUNT-002）；
        // edu_student 没有 user_id 列，所以先用登录名换 userId（见 D-169）
        String tenantId = LoginHelper.getTenantId();
        var loginUser = remoteUserService.getUserInfo(STUDENT_LOGIN_PREFIX + student.getStudentNo(), tenantId);
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new ServiceException("未找到该学生的登录账号：" + STUDENT_LOGIN_PREFIX + student.getStudentNo());
        }
        return remoteUserService.resetPassword(loginUser.getUserId(), password, tenantId);
    }

    // ==================== 联系电话与照片（CR-046 / A1 + B1，CR-095） ====================

    @Override
    public String viewStudentPhone(Long studentId) {
        EduStudent student = requireStudent(studentId);
        EduStudentEnrollment enrollment = requireEnrollment(studentId);
        if (StringUtils.isBlank(enrollment.getStudentPhone())) {
            throw new ServiceException("该学生尚未登记联系电话");
        }
        // 查看全量联系方式必须留痕（REQ-AUD-008 / BR-AUDIT-002；掩码展示不记录）
        writeSensitiveAccessLog(enrollment, student, "查看学生联系电话全量");
        return enrollment.getStudentPhone();
    }

    @Override
    public String uploadStudentPhoto(Long studentId, MultipartFile file) {
        EduStudent student = requireStudent(studentId);
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择要上传的照片");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String suffix = StringUtils.isBlank(originalName) || !originalName.contains(".")
            ? "" : originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        // 只允许常见图片后缀，拒绝可执行文件与伪装扩展名（NFR-SEC-04）
        if (!PHOTO_ALLOWED_SUFFIXES.contains(suffix)) {
            throw new ServiceException("只支持 jpg / jpeg / png / webp 格式的照片");
        }
        if (file.getSize() > PHOTO_MAX_SIZE) {
            throw new ServiceException("照片大小不能超过 2 MB");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new ServiceException("读取上传文件失败");
        }
        // 字节交给统一文件服务落对象存储，库里只保存文件地址（BR-IMP-041 同族口径）
        RemoteFile remoteFile = remoteFileService.upload(null, originalName, file.getContentType(), bytes);
        if (remoteFile == null || StringUtils.isBlank(remoteFile.getUrl())) {
            throw new ServiceException("文件服务上传失败，请稍后重试");
        }
        EduStudent update = new EduStudent();
        update.setStudentId(student.getStudentId());
        update.setPhotoUrl(remoteFile.getUrl());
        studentMapper.updateById(update);
        return remoteFile.getUrl();
    }

    @Override
    public byte[] getStudentPhoto(Long studentId) {
        EduStudent student = requireStudent(studentId);
        if (StringUtils.isBlank(student.getPhotoUrl())) {
            throw new ServiceException("该学生尚未上传照片");
        }
        // 查看原图属敏感操作，先留痕再取字节（student PRD 4.3 / GAP-027）
        writeSensitiveAccessLog(requireEnrollment(studentId), student, "查看学生照片原图");
        return remoteFileService.downloadByUrl(student.getPhotoUrl());
    }

    /**
     * 取学生最近一条在校记录。
     * 学校级资源必须有学校上下文（`DS-DENY-02`），学生主体是平台级实体、经在校记录两段式取数
     * （`DS-DENY-09`），因此审计日志的 `school_id` 只能从在校记录来，缺记录时直接拒绝而不是写不完整日志。
     */
    private EduStudentEnrollment requireEnrollment(Long studentId) {
        EduStudentEnrollment enrollment = enrollmentMapper.selectOne(
            new LambdaQueryWrapper<EduStudentEnrollment>()
                .eq(EduStudentEnrollment::getStudentId, studentId)
                .orderByDesc(EduStudentEnrollment::getEnrollmentId)
                .last("limit 1"));
        if (enrollment == null || enrollment.getSchoolId() == null) {
            throw new ServiceException("该学生没有在校记录，缺少学校上下文，拒绝访问（DS-DENY-02）");
        }
        return enrollment;
    }

    /** 写一条敏感数据访问日志（action_type = view_sensitive） */
    private void writeSensitiveAccessLog(EduStudentEnrollment enrollment, EduStudent student, String detail) {
        EduAuditLogBo log = new EduAuditLogBo();
        log.setActionType("view_sensitive");
        log.setModuleCode("student");
        log.setObjectType("student");
        log.setObjectId(String.valueOf(student.getStudentId()));
        log.setObjectName(student.getStudentName());
        log.setDetail(detail);
        log.setSchoolId(enrollment.getSchoolId());
        auditService.recordLog(log);
    }

}
