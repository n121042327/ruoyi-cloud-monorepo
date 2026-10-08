package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduPromotionTaskBo;
import org.dromara.edu.domain.vo.EduPromotionItemVo;
import org.dromara.edu.domain.vo.EduPromotionReadinessVo;
import org.dromara.edu.domain.vo.EduPromotionTaskVo;

import java.util.List;

/**
 * 升班服务层
 *
 * 覆盖 promotion 模块 10 / 13 个 operationId：listPromotionTask / getPromotionTask /
 * addPromotionTask / previewPromotionTask / updatePromotionItem / batchUpdatePromotionItem /
 * validatePromotionTask / executePromotionTask / retryPromotionTask / cancelPromotionTask。
 *
 * 导出类 3 个（exportPromotionTask / exportPromotionPreview / exportPromotionResult）归入导入导出引擎批次。
 *
 * @author Codex
 */
public interface IEduPromotionService {

    /** 分页查询升班任务列表 */
    TableDataInfo<EduPromotionTaskVo> queryPageList(EduPromotionTaskBo task, PageQuery pageQuery);

    /** 查询升班任务详情（带明细） */
    EduPromotionTaskVo queryById(Long taskId);

    /** 查询升班明细 */
    List<EduPromotionItemVo> queryItems(Long taskId, EduPromotionTaskBo query);

    /** 创建升班任务（草稿；目标学期年级与班级必须齐备，REQ-PRM-009） */
    EduPromotionTaskVo addTask(EduPromotionTaskBo task);

    /** 目标学期年级与班级齐备性检查 */
    EduPromotionReadinessVo readiness(Long sourceTermId, Long targetTermId);

    /** 生成预览（不写任何学生数据，REQ-PRM-020；重新预览覆盖旧明细，REQ-PRM-021） */
    EduPromotionTaskVo previewTask(Long taskId);

    /** 调整单条明细的去向 */
    Boolean updateItem(Long taskId, EduPromotionTaskBo item);

    /** 按源班级或明细列表批量调整目标班级（REQ-PRM-018） */
    Boolean batchUpdateItem(Long taskId, EduPromotionTaskBo item);

    /** 升班校验（校验通过才允许执行，REQ-PRM-027） */
    EduPromotionTaskVo validateTask(Long taskId);

    /** 执行升班（按学年追加、不改写历史，BR-PROMO-001） */
    EduPromotionTaskVo executeTask(Long taskId);

    /** 重试失败项（只处理失败 / 跳过项，REQ-PRM-032 / 037） */
    EduPromotionTaskVo retryTask(Long taskId, String reason);

    /** 取消任务（保留已完成部分，不做整批回滚，REQ-PRM-036） */
    Boolean cancelTask(Long taskId, String reason);

}
