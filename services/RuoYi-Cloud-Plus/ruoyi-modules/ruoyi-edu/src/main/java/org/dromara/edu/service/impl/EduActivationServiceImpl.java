package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduActivationCode;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.bo.EduActivationCodeBo;
import org.dromara.edu.domain.vo.EduActivationCodeVo;
import org.dromara.edu.mapper.EduActivationCodeMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduActivationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;

/**
 * 学生激活码服务层处理
 *
 * 口径（D-039 / GAP-021）：班主任打印密码条分发 → 学生首登即设密码 → 激活码用完即废 →
 * 丢码由班主任重置。同一学生同一时刻只允许一个未使用激活码，由生成列 active_guard +
 * uk_activation_active 在数据库层强制，因此重置时必须先把旧码置为「已作废」。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduActivationServiceImpl implements IEduActivationService {

    /** 激活码状态 */
    private static final String STATUS_UNUSED = "unused";
    private static final String STATUS_USED = "used";
    private static final String STATUS_REVOKED = "revoked";

    /** 激活码字符集（去掉易混字符 0/O/1/I） */
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    /** 激活码长度 */
    private static final int CODE_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EduActivationCodeMapper baseMapper;
    private final EduStudentMapper studentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduActivationCodeVo getStudentActivationCode(Long studentId, String reason) {
        EduStudent student = requireStudent(studentId);
        EduActivationCode exist = findUnused(studentId);
        if (exist == null) {
            if (StringUtils.isBlank(reason)) {
                // 首次下发不需要原因；已有用过的码再取即视为重置，必须填原因
                reason = "首次下发激活码";
            }
            exist = issue(student, reason);
        }
        return toVo(exist, student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduActivationCodeVo printStudentActivationSlip(Long studentId, String issueBatchNo) {
        EduStudent student = requireStudent(studentId);
        EduActivationCode code = findUnused(studentId);
        if (code == null) {
            code = issue(student, "打印密码条时下发");
        }
        code.setIssueBatchNo(issueBatchNo);
        // 打印（查看）时间：密码条只在打印时下发一次（D-039）
        code.setPrintTime(new Date());
        baseMapper.updateById(code);
        return toVo(code, student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activateStudentAccount(EduActivationCodeBo activation) {
        if (StringUtils.isBlank(activation.getCode())) {
            throw new ServiceException("请输入激活码");
        }
        if (StringUtils.isBlank(activation.getPassword())) {
            throw new ServiceException("请设置登录密码");
        }
        EduActivationCode code = baseMapper.selectOne(new LambdaQueryWrapper<EduActivationCode>()
            .eq(EduActivationCode::getCode, activation.getCode()));
        if (code == null) {
            throw new ServiceException("激活码无效");
        }
        if (!STATUS_UNUSED.equals(code.getStatus())) {
            throw new ServiceException("激活码已使用或已作废，请联系班主任重置");
        }
        code.setStatus(STATUS_USED);
        code.setUsedTime(new Date());
        code.setUsedIp(activation.getIp());
        baseMapper.updateById(code);
        // 账号创建与密码设置走系统模块的远程契约（GAP-091 落地后接入）；本批只完成激活码核销
        return true;
    }

    // ==================== 内部方法 ====================

    private EduActivationCode issue(EduStudent student, String reason) {
        // 重置前先把旧码置为已作废，避免生成列 active_guard 唯一键冲突
        EduActivationCode old = findUnused(student.getStudentId());
        if (old != null) {
            if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
                throw new ServiceException("重置激活码必须填写原因（至少 5 个字）");
            }
            old.setStatus(STATUS_REVOKED);
            old.setResetTime(new Date());
            baseMapper.updateById(old);
        }
        EduActivationCode add = new EduActivationCode();
        add.setStudentId(student.getStudentId());
        add.setCode(randomCode());
        add.setStatus(STATUS_UNUSED);
        add.setCreateTime(new Date());
        baseMapper.insert(add);
        return add;
    }

    private EduActivationCode findUnused(Long studentId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<EduActivationCode>()
            .eq(EduActivationCode::getStudentId, studentId)
            .eq(EduActivationCode::getStatus, STATUS_UNUSED)
            .last("limit 1"));
    }

    private EduActivationCodeVo toVo(EduActivationCode code, EduStudent student) {
        EduActivationCodeVo vo = new EduActivationCodeVo();
        vo.setActivationId(code.getActivationId());
        vo.setStudentId(code.getStudentId());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getStudentName());
        vo.setCode(code.getCode());
        vo.setStatus(code.getStatus());
        vo.setIssueBatchNo(code.getIssueBatchNo());
        vo.setPrintTime(code.getPrintTime());
        vo.setUsedTime(code.getUsedTime());
        vo.setResetTime(code.getResetTime());
        vo.setCreateTime(code.getCreateTime());
        return vo;
    }

    /** 生成一次性激活码（去掉易混字符） */
    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET[RANDOM.nextInt(CODE_ALPHABET.length)]);
        }
        return builder.toString();
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

}
