package org.dromara.edu.service;

import org.dromara.edu.domain.bo.EduActivationCodeBo;
import org.dromara.edu.domain.vo.EduActivationCodeVo;

/**
 * 学生激活码服务层
 *
 * 覆盖 student 模块 3 / 4 个激活相关 operationId：getStudentActivationCode /
 * printStudentActivationSlip / activateStudentAccount。
 * `exportStudentActivationCode` 需要导入导出引擎，放到后续批次。
 *
 * @author Codex
 */
public interface IEduActivationService {

    /** 查看 / 重置学生的激活码（未使用时直接返回；已使用时由班主任重置，D-039） */
    EduActivationCodeVo getStudentActivationCode(Long studentId, String reason);

    /** 打印激活密码条（一个批次一份，写 print_time 与批次号） */
    EduActivationCodeVo printStudentActivationSlip(Long studentId, String issueBatchNo);

    /** 学生首登激活（校验一次性激活码 + 设置密码，用完即废） */
    Boolean activateStudentAccount(EduActivationCodeBo activation);

}
