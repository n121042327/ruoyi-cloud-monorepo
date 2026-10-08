package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduEnrollmentChangeBo;
import org.dromara.edu.domain.bo.EduTransferOrderBo;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;
import org.dromara.edu.domain.vo.EduTransferOrderVo;

/**
 * 学籍异动与跨校转学服务层
 *
 * 覆盖 enrollment 模块全部 8 个 operationId：listEnrollmentChange / addEnrollmentChange /
 * approveEnrollmentChange / listTransfer / addTransfer / acceptTransfer / checkInTransfer / cancelTransfer。
 *
 * @author Codex
 */
public interface IEduEnrollmentService {

    /** 分页查询学籍异动记录（异动历史页） */
    TableDataInfo<EduEnrollmentChangeVo> queryChangePageList(EduEnrollmentChangeBo change, PageQuery pageQuery);

    /** 异动登记（追加式，不更新不删除，BR-PROMO-012） */
    Boolean addEnrollmentChange(EduEnrollmentChangeBo change);

    /** 异动审批（退学 / 开除 / 死亡需校级管理员审批，REQ-PRM-043 口径） */
    Boolean approveEnrollmentChange(EduEnrollmentChangeBo change);

    /** 分页查询转学单（转入校待接收清单 / 转出校清单） */
    TableDataInfo<EduTransferOrderVo> queryTransferPageList(EduTransferOrderBo transfer, PageQuery pageQuery);

    /** 发起转出（跨校转学的唯一发起入口） */
    Boolean addTransfer(EduTransferOrderBo transfer);

    /** 转入校接收（接收动作本身即审批；接收前不计入在读数，REQ-PRM-053） */
    Boolean acceptTransfer(EduTransferOrderBo transfer);

    /** 办理报到（学生到校报到，状态转为在读；班级关系仍由班级管理写入，DP-01） */
    Boolean checkInTransfer(EduTransferOrderBo transfer);

    /** 撤销（未报到前可撤销接收 / 撤销申请，REQ-PRM-054） */
    Boolean cancelTransfer(Long transferId, String reason);

}
