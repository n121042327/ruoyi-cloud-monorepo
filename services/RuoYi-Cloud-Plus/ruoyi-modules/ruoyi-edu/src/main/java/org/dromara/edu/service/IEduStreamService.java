package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduStreamChangeRequestBo;
import org.dromara.edu.domain.bo.EduStreamConfigBo;
import org.dromara.edu.domain.bo.EduStreamHistoryBo;
import org.dromara.edu.domain.bo.EduStreamSelectionBo;
import org.dromara.edu.domain.vo.EduMyStreamVo;
import org.dromara.edu.domain.vo.EduStreamChangeRequestVo;
import org.dromara.edu.domain.vo.EduStreamConfigVo;
import org.dromara.edu.domain.vo.EduStreamHistoryVo;
import org.dromara.edu.domain.vo.EduStreamOptionVo;
import org.dromara.edu.domain.vo.EduStreamSelectionVo;
import org.dromara.edu.domain.vo.EduStreamStatVo;
import org.dromara.edu.domain.vo.EduTeachingClassGenerateVo;
import org.dromara.edu.domain.vo.EduUnselectedStudentVo;

import java.util.List;

/**
 * 选科与教学班生成服务层
 *
 * 覆盖 stream 模块 16 / 17 个 operationId：getStreamConfig / saveStreamConfig / getStreamOption /
 * getMyStream / submitMyStream / updateMyStream / listStreamSelection / getStreamStat /
 * listUnselectedStudent / listStreamChangeRequest / addStreamChangeRequest / cancelStreamChangeRequest /
 * approveStreamChangeRequest / listStreamHistory / previewTeachingClassGenerate / executeTeachingClassGenerate。
 * `exportStreamSelection` 归入导入导出引擎批次。
 *
 * @author Codex
 */
public interface IEduStreamService {

    /** 选科配置（含按当前时间实时比较的开放期状态） */
    EduStreamConfigVo getStreamConfig(Long termId);

    /** 保存选科配置（同校同学期唯一，REQ-STR-004） */
    EduStreamConfigVo saveStreamConfig(EduStreamConfigBo config);

    /** 选科可选科目（首选物理 / 历史，再选 4 选 2，学校不可增减） */
    EduStreamOptionVo getStreamOption();

    /** 我的选科（当前生效值 + 是否有待审批变更） */
    EduMyStreamVo getMyStream(Long termId, Long studentId);

    /** 首次提交选科（开放期内直接生效） */
    EduMyStreamVo submitMyStream(EduStreamSelectionBo stream);

    /** 更新选科（开放期内直接生效；截止后必须走变更申请，BR-STREAM-006） */
    EduMyStreamVo updateMyStream(EduStreamSelectionBo stream);

    /** 选科清单 */
    TableDataInfo<EduStreamSelectionVo> querySelectionPageList(EduStreamSelectionBo query, PageQuery pageQuery);

    /** 组合分布统计（图表与明细同源） */
    EduStreamStatVo getStreamStat(Long termId, Long gradeId);

    /** 未选科学生（催办清单） */
    List<EduUnselectedStudentVo> listUnselectedStudent(Long termId);

    /** 变更申请待办列表 */
    TableDataInfo<EduStreamChangeRequestVo> queryChangeRequestPageList(EduStreamChangeRequestBo query, PageQuery pageQuery);

    /** 提交变更申请（截止后；同一学生同一学期只允许一条待审批，REQ-STR-029） */
    EduStreamChangeRequestVo addStreamChangeRequest(EduStreamChangeRequestBo request);

    /** 撤销变更申请 */
    Boolean cancelStreamChangeRequest(Long requestId, String reason);

    /** 审批变更申请（通过即生效；驳回必填意见，REQ-STR-032 / 034 / 037） */
    Boolean approveStreamChangeRequest(EduStreamChangeRequestBo request);

    /** 选科历史（追加式，不可删除不可修改，REQ-STR-043） */
    List<EduStreamHistoryVo> listStreamHistory(EduStreamHistoryBo query);

    /** 教学班生成预览（只读不写，REQ-STR-056） */
    EduTeachingClassGenerateVo previewTeachingClassGenerate(EduStreamSelectionBo generate);

    /** 执行教学班生成（唯一键幂等，REQ-STR-057） */
    EduTeachingClassGenerateVo executeTeachingClassGenerate(EduStreamSelectionBo generate);

}
