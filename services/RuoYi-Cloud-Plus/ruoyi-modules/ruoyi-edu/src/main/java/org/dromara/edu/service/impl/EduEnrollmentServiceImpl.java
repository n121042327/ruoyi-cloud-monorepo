package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.edu.domain.EduEnrollmentChange;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.EduTransferOrder;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.domain.bo.EduEnrollmentChangeBo;
import org.dromara.edu.domain.bo.EduTransferOrderBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduTransferOrderVo;
import org.dromara.edu.mapper.EduEnrollmentChangeMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.mapper.EduTransferOrderMapper;
import org.dromara.edu.service.IEduClassService;
import org.dromara.edu.service.IEduEnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 学籍异动与跨校转学服务层处理
 *
 * 口径要点：
 * - 异动记录**追加式、不更新不删除**（BR-PROMO-012）；
 * - 退学 / 开除 / 死亡需校级管理员审批（REQ-PRM-043 口径）：登记时置「待审批」，审批通过后才改在校记录状态；
 * - 跨校转学：接收动作本身即审批，**接收前不计入在读数**（REQ-PRM-053）；未报到前可撤销接收（REQ-PRM-054）；
 *   同一学生未完成转学单唯一（REQ-PRM-057）；转学单只暴露必要字段（REQ-PRM-055）；
 * - 报到后的班级关系仍由班级管理写入（DP-01），本服务只是触发方；
 * - 转出 / 转出未报到都不复制教师、班级等业务数据。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduEnrollmentServiceImpl implements IEduEnrollmentService {

    /** 异动审批状态 */
    private static final String APPROVAL_PENDING = "待审批";
    private static final String APPROVAL_APPROVED = "已通过";
    private static final String APPROVAL_REJECTED = "已驳回";

    /** 转学单状态 */
    private static final String TRANSFER_PENDING = "pending";
    private static final String TRANSFER_RECEIVED = "received";
    private static final String TRANSFER_CHECKED_IN = "checked_in";
    private static final String TRANSFER_CANCELED = "canceled";

    /** 需要审批的异动类型 */
    private static final List<String> APPROVAL_REQUIRED = List.of("退学", "开除", "死亡");

    /** 终态学籍状态（写入离校日期） */
    private static final List<String> FINAL_STATUS = List.of("已毕业", "转出", "退学", "开除", "死亡");

    private static final String STATUS_ENROLLED = "在读";

    private final EduEnrollmentChangeMapper changeMapper;
    private final EduTransferOrderMapper transferMapper;
    private final EduStudentMapper studentMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final IEduClassService classService;

    // ==================== 学籍异动 ====================

    @Override
    public TableDataInfo<EduEnrollmentChangeVo> queryChangePageList(EduEnrollmentChangeBo change, PageQuery pageQuery) {
        LambdaQueryWrapper<EduEnrollmentChange> wrapper = new LambdaQueryWrapper<EduEnrollmentChange>()
            .eq(change.getStudentId() != null, EduEnrollmentChange::getStudentId, change.getStudentId())
            .eq(StringUtils.isNotBlank(change.getChangeType()), EduEnrollmentChange::getChangeType, change.getChangeType())
            .eq(StringUtils.isNotBlank(change.getApprovalStatus()), EduEnrollmentChange::getApprovalStatus, change.getApprovalStatus())
            .ge(StringUtils.isNotBlank(change.getEffectiveDateFrom()), EduEnrollmentChange::getEffectiveDate,
                DateUtils.parseDate(change.getEffectiveDateFrom()))
            .le(StringUtils.isNotBlank(change.getEffectiveDateTo()), EduEnrollmentChange::getEffectiveDate,
                DateUtils.parseDate(change.getEffectiveDateTo()))
            .orderByDesc(EduEnrollmentChange::getOperateTime);
        Page<EduEnrollmentChangeVo> result = changeMapper.selectPageChangeList(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillStudentInfo);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addEnrollmentChange(EduEnrollmentChangeBo change) {
        if (change.getStudentId() == null) {
            throw new ServiceException("请选择学生");
        }
        if (StringUtils.isBlank(change.getChangeType())) {
            throw new ServiceException("请选择异动类型");
        }
        if (StringUtils.isBlank(change.getReason()) || change.getReason().trim().length() < 5) {
            throw new ServiceException("异动原因至少 5 个字");
        }
        EduStudent student = requireStudent(change.getStudentId());
        EduStudentEnrollment enrollment = findEnrollment(change.getStudentId());
        boolean needApproval = APPROVAL_REQUIRED.contains(change.getChangeType());

        EduEnrollmentChange record = new EduEnrollmentChange();
        record.setStudentId(change.getStudentId());
        record.setSchoolRecordId(enrollment == null ? null : enrollment.getEnrollmentId());
        record.setChangeType(change.getChangeType());
        record.setBeforeStatus(enrollment == null ? null : enrollment.getEnrollmentStatus());
        // 异动登记只登记「变更为在读」这类可推导的状态；具体目标状态由 changeType 决定（与学籍异动登记同一口径）
        record.setAfterStatus(mapAfterStatus(change.getChangeType()));
        record.setEffectiveDate(DateUtils.parseDate(change.getEffectiveDate()));
        record.setReason(change.getReason());
        record.setApprovalStatus(needApproval ? APPROVAL_PENDING : null);
        record.setOperateTime(new Date());
        changeMapper.insert(record);

        // 不需要审批的异动立即生效；需要审批的等 approveEnrollmentChange
        if (!needApproval && enrollment != null) {
            applyStatus(enrollment, record.getAfterStatus(), record.getEffectiveDate());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnrollmentChange(EduEnrollmentChangeBo change) {
        if (change.getChangeId() == null) {
            throw new ServiceException("缺少异动记录 ID");
        }
        EduEnrollmentChange record = changeMapper.selectById(change.getChangeId());
        if (record == null) {
            throw new ServiceException("异动记录不存在");
        }
        if (!APPROVAL_PENDING.equals(record.getApprovalStatus())) {
            throw new ServiceException("该异动记录不在待审批状态");
        }
        boolean approved = change.getApproved() == null || Boolean.TRUE.equals(change.getApproved());
        if (!approved && (StringUtils.isBlank(change.getApproveOpinion()) || change.getApproveOpinion().trim().length() < 5)) {
            throw new ServiceException("驳回时必须填写审批意见（至少 5 个字）");
        }
        record.setApprovalStatus(approved ? APPROVAL_APPROVED : APPROVAL_REJECTED);
        record.setApproveOpinion(change.getApproveOpinion());
        record.setApproveTime(new Date());
        changeMapper.updateById(record);
        if (approved) {
            EduStudentEnrollment enrollment = findEnrollment(record.getStudentId());
            if (enrollment != null) {
                applyStatus(enrollment, record.getAfterStatus(), record.getEffectiveDate());
            }
        }
        return true;
    }

    // ==================== 跨校转学 ====================

    @Override
    public TableDataInfo<EduTransferOrderVo> queryTransferPageList(EduTransferOrderBo transfer, PageQuery pageQuery) {
        LambdaQueryWrapper<EduTransferOrder> wrapper = new LambdaQueryWrapper<EduTransferOrder>()
            .eq(transfer.getStudentId() != null, EduTransferOrder::getStudentId, transfer.getStudentId())
            .eq(transfer.getToSchoolId() != null, EduTransferOrder::getToSchoolId, transfer.getToSchoolId())
            .eq(transfer.getFromSchoolId() != null, EduTransferOrder::getFromSchoolId, transfer.getFromSchoolId())
            .eq(StringUtils.isNotBlank(transfer.getTransferStatus()), EduTransferOrder::getTransferStatus, transfer.getTransferStatus())
            .orderByDesc(EduTransferOrder::getApplyTime);
        Page<EduTransferOrderVo> result = transferMapper.selectPageTransferList(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillTransferStudent);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTransfer(EduTransferOrderBo transfer) {
        if (transfer.getStudentId() == null) {
            throw new ServiceException("请选择学生");
        }
        if (transfer.getToSchoolId() == null) {
            throw new ServiceException("请选择转入学校");
        }
        // 同一学生未完成转学单唯一（REQ-PRM-057）
        Long unfinished = transferMapper.selectCount(new LambdaQueryWrapper<EduTransferOrder>()
            .eq(EduTransferOrder::getStudentId, transfer.getStudentId())
            .in(EduTransferOrder::getTransferStatus, TRANSFER_PENDING, TRANSFER_RECEIVED));
        if (unfinished != null && unfinished > 0) {
            throw new ServiceException("该学生已有未完成的转学单，请先撤销或完成（REQ-PRM-057）");
        }
        requireStudent(transfer.getStudentId());
        EduStudentEnrollment enrollment = findEnrollment(transfer.getStudentId());

        EduTransferOrder order = new EduTransferOrder();
        order.setSchoolId(transfer.getFromSchoolId());
        order.setTransferNo(generateTransferNo());
        order.setStudentId(transfer.getStudentId());
        order.setFromTenantId(TenantHelper.getTenantId());
        order.setFromSchoolId(transfer.getFromSchoolId());
        order.setFromGradeId(transfer.getFromGradeId() != null ? transfer.getFromGradeId()
            : (enrollment == null ? null : enrollment.getEntryGradeId()));
        order.setToSchoolId(transfer.getToSchoolId());
        order.setToGradeId(transfer.getToGradeId());
        order.setToClassId(transfer.getToClassId());
        order.setTransferStatus(TRANSFER_PENDING);
        order.setApplyTime(new Date());
        order.setRemark(transfer.getRemark());
        return transferMapper.insert(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean acceptTransfer(EduTransferOrderBo transfer) {
        EduTransferOrder order = requireTransfer(transfer.getTransferId());
        if (!TRANSFER_PENDING.equals(order.getTransferStatus())) {
            throw new ServiceException("该转学单不在待接收状态");
        }
        // 接收动作本身即审批（已确认 3）：接收前不计入在读数（REQ-PRM-053），因此此处只落接收信息
        order.setTransferStatus(TRANSFER_RECEIVED);
        order.setAcceptTime(new Date());
        if (transfer.getToGradeId() != null) {
            order.setToGradeId(transfer.getToGradeId());
        }
        if (transfer.getToClassId() != null) {
            order.setToClassId(transfer.getToClassId());
        }
        return transferMapper.updateById(order) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkInTransfer(EduTransferOrderBo transfer) {
        EduTransferOrder order = requireTransfer(transfer.getTransferId());
        if (!TRANSFER_RECEIVED.equals(order.getTransferStatus())) {
            throw new ServiceException("只有已接收的转学单才能办理报到");
        }
        order.setTransferStatus(TRANSFER_CHECKED_IN);
        order.setCheckInTime(new Date());
        if (transfer.getToClassId() != null) {
            order.setToClassId(transfer.getToClassId());
        }
        transferMapper.updateById(order);

        // 报到即在本校建立在校记录（在读）——若已有记录则只改状态
        EduStudentEnrollment enrollment = findEnrollment(order.getStudentId());
        if (enrollment == null) {
            enrollment = new EduStudentEnrollment();
            enrollment.setSchoolId(order.getToSchoolId());
            enrollment.setStudentId(order.getStudentId());
            enrollment.setEnrollDate(new Date());
            enrollment.setEnrollmentStatus(STATUS_ENROLLED);
            enrollment.setStatusEffectiveDate(new Date());
            enrollment.setEntryGradeId(order.getToGradeId());
            enrollmentMapper.insert(enrollment);
        } else {
            applyStatus(enrollment, STATUS_ENROLLED, new Date());
        }

        // 班级关系仍由班级管理写入（DP-01）：报到时指定了班级则调班级服务的加入接口
        if (order.getToClassId() != null) {
            EduClassMemberBo member = new EduClassMemberBo();
            member.setClassId(order.getToClassId());
            member.setStudentIds(List.of(order.getStudentId()));
            member.setEffectiveDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            member.setRemark("跨校转学报到，由学籍异动模块触发");
            classService.addRoster(member);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelTransfer(Long transferId, String reason) {
        EduTransferOrder order = requireTransfer(transferId);
        if (TRANSFER_CHECKED_IN.equals(order.getTransferStatus())) {
            throw new ServiceException("已报到的转学单不可撤销（REQ-PRM-054）");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("撤销原因至少 5 个字");
        }
        order.setTransferStatus(TRANSFER_CANCELED);
        order.setCancelTime(new Date());
        order.setRemark(reason);
        return transferMapper.updateById(order) > 0;
    }

    // ==================== 内部方法 ====================

    /** 异动类型 → 变更后学籍状态 */
    private String mapAfterStatus(String changeType) {
        return switch (changeType) {
            case "休学", "suspend" -> "休学";
            case "复学", "resume" -> STATUS_ENROLLED;
            case "转学", "转出", "transfer_out" -> "转出";
            case "退学", "withdraw" -> "退学";
            case "开除", "expel" -> "开除";
            case "出国", "abroad" -> "出国保留学籍";
            case "失踪", "missing" -> "失踪";
            case "死亡", "death" -> "死亡";
            case "毕业", "graduate" -> "已毕业";
            case "转入未报到" -> "转入未报到";
            case "报到", "check_in" -> STATUS_ENROLLED;
            case "升班", "promote" -> STATUS_ENROLLED;
            default -> throw new ServiceException("未知的异动类型：" + changeType);
        };
    }

    /** 改在校记录状态（终态写离校日期） */
    private void applyStatus(EduStudentEnrollment enrollment, String status, Date effectiveDate) {
        enrollment.setEnrollmentStatus(status);
        enrollment.setStatusEffectiveDate(effectiveDate);
        if (FINAL_STATUS.contains(status)) {
            enrollment.setLeaveDate(effectiveDate);
        }
        enrollmentMapper.updateById(enrollment);
    }

    private void fillStudentInfo(EduEnrollmentChangeVo vo) {
        if (vo.getStudentId() == null) {
            return;
        }
        EduStudent student = studentMapper.selectById(vo.getStudentId());
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
        }
        vo.setType(vo.getChangeType());
        if (vo.getChangeTime() == null) {
            vo.setChangeTime(vo.getOperateTime());
        }
    }

    private void fillTransferStudent(EduTransferOrderVo vo) {
        if (vo.getStudentId() == null) {
            return;
        }
        EduStudent student = studentMapper.selectById(vo.getStudentId());
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
            vo.setGender(student.getGender());
        }
    }

    /** 生成转学单号：日期 + 4 位随机（唯一键 uk_transfer_no 兜底） */
    private String generateTransferNo() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "TR-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private EduTransferOrder requireTransfer(Long transferId) {
        if (transferId == null) {
            throw new ServiceException("缺少转学单 ID");
        }
        EduTransferOrder order = transferMapper.selectById(transferId);
        if (order == null) {
            throw new ServiceException("转学单不存在或不在当前数据范围内");
        }
        return order;
    }

    private EduStudentEnrollment findEnrollment(Long studentId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<EduStudentEnrollment>()
            .eq(EduStudentEnrollment::getStudentId, studentId)
            .last("limit 1"));
    }

    private EduStudent requireStudent(Long studentId) {
        EduStudent student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在或不在当前数据范围内");
        }
        return student;
    }

}
