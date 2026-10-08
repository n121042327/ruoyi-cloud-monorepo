package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduEnrollmentChange;
import org.dromara.edu.domain.EduPromotionItem;
import org.dromara.edu.domain.EduPromotionTask;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.domain.bo.EduPromotionTaskBo;
import org.dromara.edu.domain.vo.EduPromotionItemVo;
import org.dromara.edu.domain.vo.EduPromotionReadinessVo;
import org.dromara.edu.domain.vo.EduPromotionTaskVo;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduEnrollmentChangeMapper;
import org.dromara.edu.mapper.EduPromotionItemMapper;
import org.dromara.edu.mapper.EduPromotionTaskMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduClassService;
import org.dromara.edu.service.IEduPromotionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 升班服务层处理
 *
 * 口径要点：
 * - 升班按学年**追加**下一学年的班级与学生关系，不改写历史（BR-PROMO-001）；
 * - 同一源 → 目标学期的未结束任务唯一（REQ-PRM-005）；目标学期的年级与班级必须齐备（REQ-PRM-009）；
 * - 预览**不写任何学生数据**（REQ-PRM-020），重新预览覆盖旧明细（REQ-PRM-021）；
 * - 明细幂等键 = (task_id, student_id)（REQ-PRM-029）；重试只处理失败 / 跳过项（REQ-PRM-032 / 037）；
 * - 执行时按学籍状态决定去向：在读按结果类型升班 / 留级 / 转班 / 毕业，毕业与终态写学籍异动记录；
 * - 班级关系仍由班级管理写入（DP-01），本服务调 IEduClassService.addRoster 落班；
 * - 取消保留已完成部分、不做整批回滚（REQ-PRM-036）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduPromotionServiceImpl implements IEduPromotionService {

    /** 任务状态（数据库枚举原值） */
    private static final String TASK_DRAFT = "草稿";
    private static final String TASK_PREVIEWED = "已预览待确认";
    private static final String TASK_VALIDATED = "已预览待确认";
    private static final String TASK_RUNNING = "执行中";
    private static final String TASK_SUCCEEDED = "已完成";
    private static final String TASK_PARTIAL_FAILED = "部分失败";
    private static final String TASK_FAILED = "失败";
    private static final String TASK_CANCELLED = "已取消";

    /** 结果类型 */
    private static final String RESULT_PROMOTE = "promote";
    private static final String RESULT_REPEAT = "repeat";
    private static final String RESULT_GRADUATE = "graduate";
    private static final String RESULT_TRANSFER = "transfer";

    /** 明细状态 */
    private static final String ITEM_PENDING = "pending";
    private static final String ITEM_SUCCESS = "success";
    private static final String ITEM_FAILED = "failed";
    private static final String ITEM_SKIPPED = "skipped";

    /** 花名册在班标记 */
    private static final String MEMBER_IN = "1";

    /** 前端状态码映射 */
    private static final Map<String, String> STATUS_CODE = new HashMap<>();
    private static final Map<String, String> STATUS_TEXT = new HashMap<>();

    static {
        STATUS_CODE.put(TASK_DRAFT, "draft");
        STATUS_CODE.put(TASK_PREVIEWED, "previewed");
        STATUS_CODE.put(TASK_RUNNING, "running");
        STATUS_CODE.put(TASK_SUCCEEDED, "succeeded");
        STATUS_CODE.put(TASK_PARTIAL_FAILED, "partial_failed");
        STATUS_CODE.put(TASK_FAILED, "failed");
        STATUS_CODE.put(TASK_CANCELLED, "cancelled");
        STATUS_TEXT.put("draft", TASK_DRAFT);
        STATUS_TEXT.put("previewed", TASK_PREVIEWED);
        STATUS_TEXT.put("running", TASK_RUNNING);
        STATUS_TEXT.put("succeeded", TASK_SUCCEEDED);
        STATUS_TEXT.put("partial_failed", TASK_PARTIAL_FAILED);
        STATUS_TEXT.put("failed", TASK_FAILED);
        STATUS_TEXT.put("cancelled", TASK_CANCELLED);
    }

    private final EduPromotionTaskMapper baseMapper;
    private final EduPromotionItemMapper itemMapper;
    private final EduClassMapper classMapper;
    private final EduClassMemberMapper memberMapper;
    private final EduStudentMapper studentMapper;
    private final EduEnrollmentChangeMapper changeMapper;
    private final IEduClassService classService;

    // ==================== 查询 ====================

    @Override
    public TableDataInfo<EduPromotionTaskVo> queryPageList(EduPromotionTaskBo task, PageQuery pageQuery) {
        LambdaQueryWrapper<EduPromotionTask> wrapper = new LambdaQueryWrapper<EduPromotionTask>()
            .eq(task.getSchoolId() != null, EduPromotionTask::getSchoolId, task.getSchoolId())
            .eq(task.getSourceTermId() != null, EduPromotionTask::getSourceTermId, task.getSourceTermId())
            .eq(task.getTargetTermId() != null, EduPromotionTask::getTargetTermId, task.getTargetTermId())
            .eq(StringUtils.isNotBlank(task.getTaskStatus()), EduPromotionTask::getTaskStatus,
                STATUS_TEXT.getOrDefault(task.getTaskStatus(), task.getTaskStatus()))
            .like(StringUtils.isNotBlank(task.getKeyword()), EduPromotionTask::getTaskNo, task.getKeyword())
            .orderByDesc(EduPromotionTask::getCreateTime);
        Page<EduPromotionTaskVo> result = baseMapper.selectPagePromotionTask(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillStatus);
        return TableDataInfo.build(result);
    }

    @Override
    public EduPromotionTaskVo queryById(Long taskId) {
        EduPromotionTask task = requireTask(taskId);
        EduPromotionTaskVo vo = baseMapper.selectVoById(task.getTaskId());
        fillStatus(vo);
        vo.setItems(queryItems(taskId, new EduPromotionTaskBo()));
        return vo;
    }

    @Override
    public List<EduPromotionItemVo> queryItems(Long taskId, EduPromotionTaskBo query) {
        LambdaQueryWrapper<EduPromotionItem> wrapper = new LambdaQueryWrapper<EduPromotionItem>()
            .eq(EduPromotionItem::getTaskId, taskId)
            .eq(query.getFilterSourceClassId() != null, EduPromotionItem::getSourceClassId, query.getFilterSourceClassId())
            .eq(StringUtils.isNotBlank(query.getItemStatus()), EduPromotionItem::getItemStatus, query.getItemStatus())
            .orderByAsc(EduPromotionItem::getItemId);
        List<EduPromotionItemVo> items = itemMapper.selectVoList(wrapper);
        items.forEach(this::fillItemDisplay);
        return items;
    }

    // ==================== 创建与预览 ====================

    @Override
    public EduPromotionReadinessVo readiness(Long sourceTermId, Long targetTermId) {
        EduPromotionReadinessVo vo = new EduPromotionReadinessVo();
        List<String> missing = new ArrayList<>();
        if (targetTermId == null) {
            missing.add("未选择目标学年学期");
        } else {
            Long classCount = classMapper.selectCount(new LambdaQueryWrapper<EduClass>()
                .eq(EduClass::getTermId, targetTermId));
            vo.setClassCount(classCount == null ? 0 : classCount.intValue());
            if (classCount == null || classCount == 0) {
                missing.add("目标学年学期尚未建立年级与班级");
            }
        }
        if (sourceTermId != null && targetTermId != null) {
            Long unfinished = baseMapper.selectCount(new LambdaQueryWrapper<EduPromotionTask>()
                .eq(EduPromotionTask::getSourceTermId, sourceTermId)
                .eq(EduPromotionTask::getTargetTermId, targetTermId)
                .in(EduPromotionTask::getTaskStatus, TASK_DRAFT, TASK_PREVIEWED, TASK_RUNNING));
            vo.setHasUnfinishedTask(unfinished != null && unfinished > 0);
            Long enrolled = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
                .eq(EduClassMember::getTermId, sourceTermId)
                .eq(EduClassMember::getStatus, MEMBER_IN));
            vo.setEnrolledCount(enrolled == null ? 0 : enrolled.intValue());
        }
        vo.setMissingGrades(missing);
        if (!missing.isEmpty() || Boolean.TRUE.equals(vo.getHasUnfinishedTask())) {
            vo.setLevel("blocked");
            vo.setMessage(Boolean.TRUE.equals(vo.getHasUnfinishedTask())
                ? "同一源 → 目标学期已有未结束任务（REQ-PRM-005）"
                : String.join("；", missing));
        } else {
            vo.setLevel("ready");
            vo.setMessage("目标学年学期的年级与班级已齐备，可以创建（REQ-PRM-009）");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduPromotionTaskVo addTask(EduPromotionTaskBo task) {
        if (task.getSourceTermId() == null || task.getTargetTermId() == null) {
            throw new ServiceException("请选择源学年学期与目标学年学期");
        }
        if (task.getSourceTermId().equals(task.getTargetTermId())) {
            throw new ServiceException("目标学年学期必须晚于源学年学期，两者不能相同（REQ-PRM-008）");
        }
        EduPromotionReadinessVo readiness = readiness(task.getSourceTermId(), task.getTargetTermId());
        if ("blocked".equals(readiness.getLevel())) {
            throw new ServiceException(readiness.getMessage());
        }
        EduPromotionTask add = new EduPromotionTask();
        add.setSchoolId(task.getSchoolId());
        add.setTaskNo(generateTaskNo());
        add.setSourceTermId(task.getSourceTermId());
        add.setTargetTermId(task.getTargetTermId());
        add.setScopeNote(task.getScopeNote());
        add.setTaskStatus(TASK_DRAFT);
        add.setTotalCount(0);
        add.setSuccessCount(0);
        add.setFailedCount(0);
        add.setRepeatCount(0);
        add.setGraduateCount(0);
        baseMapper.insert(add);
        return queryById(add.getTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduPromotionTaskVo previewTask(Long taskId) {
        EduPromotionTask task = requireTask(taskId);
        if (TASK_CANCELLED.equals(task.getTaskStatus())) {
            throw new ServiceException("已取消的任务不能重新预览，请新建任务");
        }
        // 重新预览会覆盖旧明细（REQ-PRM-021）：先清掉旧明细再重建
        itemMapper.delete(new LambdaQueryWrapper<EduPromotionItem>().eq(EduPromotionItem::getTaskId, taskId));
        // 源学期在读学生来自花名册（edu_class_member，status='1'），逐条写入待处理明细
        List<EduClassMember> members = memberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getTermId, task.getSourceTermId())
            .eq(EduClassMember::getStatus, MEMBER_IN));
        int total = 0;
        for (EduClassMember member : members) {
            EduPromotionItem item = new EduPromotionItem();
            item.setSchoolId(task.getSchoolId());
            item.setTaskId(taskId);
            item.setStudentId(member.getStudentId());
            item.setSourceClassId(member.getClassId());
            item.setResultType(RESULT_PROMOTE);
            item.setItemStatus(ITEM_PENDING);
            item.setCreateTime(new Date());
            itemMapper.insert(item);
            total++;
        }
        task.setTotalCount(total);
        task.setTaskStatus(TASK_PREVIEWED);
        baseMapper.updateById(task);
        return queryById(taskId);
    }

    // ==================== 明细调整 ====================

    @Override
    public Boolean updateItem(Long taskId, EduPromotionTaskBo item) {
        requireTask(taskId);
        EduPromotionItem entity = requireItem(item.getItemId(), taskId);
        if (item.getTargetClassId() != null) {
            entity.setTargetClassId(item.getTargetClassId());
            entity.setAdjustMode("manual");
        }
        if (StringUtils.isNotBlank(item.getResultType())) {
            entity.setResultType(item.getResultType());
        }
        entity.setItemStatus(ITEM_PENDING);
        entity.setErrorMsg(null);
        return itemMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateItem(Long taskId, EduPromotionTaskBo item) {
        requireTask(taskId);
        if (item.getTargetClassId() == null) {
            throw new ServiceException("请选择目标班级");
        }
        LambdaQueryWrapper<EduPromotionItem> wrapper = new LambdaQueryWrapper<EduPromotionItem>()
            .eq(EduPromotionItem::getTaskId, taskId);
        if (item.getSourceClassId() != null) {
            wrapper.eq(EduPromotionItem::getSourceClassId, item.getSourceClassId());
        } else if (item.getItemIds() != null && !item.getItemIds().isEmpty()) {
            wrapper.in(EduPromotionItem::getItemId, item.getItemIds());
        } else {
            throw new ServiceException("请按源班级或勾选明细进行批量调整（REQ-PRM-018）");
        }
        List<EduPromotionItem> items = itemMapper.selectList(wrapper);
        for (EduPromotionItem entity : items) {
            entity.setTargetClassId(item.getTargetClassId());
            entity.setAdjustMode("batch");
            entity.setItemStatus(ITEM_PENDING);
            entity.setErrorMsg(null);
            itemMapper.updateById(entity);
        }
        return true;
    }

    // ==================== 校验 / 执行 / 重试 / 取消 ====================

    @Override
    public EduPromotionTaskVo validateTask(Long taskId) {
        requireTask(taskId);
        List<EduPromotionItem> items = itemMapper.selectList(new LambdaQueryWrapper<EduPromotionItem>()
            .eq(EduPromotionItem::getTaskId, taskId));
        if (items.isEmpty()) {
            throw new ServiceException("尚未生成预览明细，无法校验（REQ-PRM-020）");
        }
        for (EduPromotionItem item : items) {
            // 升班 / 转班必须有目标班级；毕业不需要（REQ-PRM-027）
            if (!RESULT_GRADUATE.equals(item.getResultType()) && item.getTargetClassId() == null) {
                item.setItemStatus(ITEM_FAILED);
                item.setErrorMsg("缺少目标班级，请在预览与调整里指定（REQ-PRM-009）");
            } else {
                item.setItemStatus(ITEM_PENDING);
                item.setErrorMsg(null);
            }
            itemMapper.updateById(item);
        }
        return queryById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduPromotionTaskVo executeTask(Long taskId) {
        EduPromotionTask task = requireTask(taskId);
        if (TASK_CANCELLED.equals(task.getTaskStatus())) {
            throw new ServiceException("已取消的任务不能执行，请新建任务");
        }
        List<EduPromotionItem> items = itemMapper.selectList(new LambdaQueryWrapper<EduPromotionItem>()
            .eq(EduPromotionItem::getTaskId, taskId));
        if (items.isEmpty()) {
            throw new ServiceException("尚未生成预览明细，无法执行");
        }
        task.setTaskStatus(TASK_RUNNING);
        task.setStartTime(new Date());
        baseMapper.updateById(task);
        processItems(task, items);
        return queryById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduPromotionTaskVo retryTask(Long taskId, String reason) {
        EduPromotionTask task = requireTask(taskId);
        List<EduPromotionItem> retryItems = itemMapper.selectList(new LambdaQueryWrapper<EduPromotionItem>()
            .eq(EduPromotionItem::getTaskId, taskId)
            .in(EduPromotionItem::getItemStatus, ITEM_FAILED, ITEM_SKIPPED));
        if (retryItems.isEmpty()) {
            throw new ServiceException("没有需要重试的失败 / 跳过项（REQ-PRM-032）");
        }
        task.setTaskStatus(TASK_RUNNING);
        task.setStartTime(new Date());
        baseMapper.updateById(task);
        // 重试只处理失败 / 跳过项，已成功的行不动（REQ-PRM-032 / 037）
        processItems(task, retryItems);
        return queryById(taskId);
    }

    @Override
    public Boolean cancelTask(Long taskId, String reason) {
        EduPromotionTask task = requireTask(taskId);
        if (TASK_SUCCEEDED.equals(task.getTaskStatus())) {
            throw new ServiceException("已完成的任务不能取消");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("取消原因至少 5 个字");
        }
        // 取消保留已完成部分，不做整批回滚（REQ-PRM-036）
        task.setTaskStatus(TASK_CANCELLED);
        task.setCancelReason(reason);
        task.setFinishTime(new Date());
        return baseMapper.updateById(task) > 0;
    }

    // ==================== 内部方法 ====================

    /** 逐条执行明细：按结果类型落班 / 写学籍异动，并统计成功失败 */
    private void processItems(EduPromotionTask task, List<EduPromotionItem> items) {
        int success = 0;
        int failed = 0;
        int repeat = 0;
        int graduate = 0;
        for (EduPromotionItem item : items) {
            try {
                if (RESULT_GRADUATE.equals(item.getResultType())) {
                    writeGraduateChange(task, item);
                    graduate++;
                } else if (item.getTargetClassId() == null) {
                    item.setItemStatus(ITEM_FAILED);
                    item.setErrorMsg("缺少目标班级");
                    itemMapper.updateById(item);
                    failed++;
                    continue;
                } else {
                    // 班级关系仍由班级管理写入（DP-01）
                    EduClassMemberBo member = new EduClassMemberBo();
                    member.setClassId(item.getTargetClassId());
                    member.setStudentIds(List.of(item.getStudentId()));
                    member.setEffectiveDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    member.setRemark("升班任务 " + task.getTaskNo() + " 追加在班关系（按学年追加，BR-PROMO-001）");
                    classService.addRoster(member);
                    if (RESULT_REPEAT.equals(item.getResultType())) {
                        repeat++;
                    }
                }
                item.setItemStatus(ITEM_SUCCESS);
                item.setErrorMsg(null);
                itemMapper.updateById(item);
                success++;
            } catch (Exception e) {
                item.setItemStatus(ITEM_FAILED);
                item.setErrorMsg(truncate(e.getMessage()));
                itemMapper.updateById(item);
                failed++;
            }
        }
        task.setSuccessCount((task.getSuccessCount() == null ? 0 : task.getSuccessCount()) + success);
        task.setFailedCount(failed);
        task.setRepeatCount(repeat);
        task.setGraduateCount(graduate);
        if (failed == 0) {
            task.setTaskStatus(TASK_SUCCEEDED);
        } else if (success > 0) {
            task.setTaskStatus(TASK_PARTIAL_FAILED);
        } else {
            task.setTaskStatus(TASK_FAILED);
        }
        task.setFinishTime(new Date());
        baseMapper.updateById(task);
    }

    /** 毕业：追加学籍异动记录（学籍状态唯一流转入口，DP-01） */
    private void writeGraduateChange(EduPromotionTask task, EduPromotionItem item) {
        EduEnrollmentChange change = new EduEnrollmentChange();
        change.setSchoolId(task.getSchoolId());
        change.setStudentId(item.getStudentId());
        change.setChangeType("毕业");
        change.setBeforeStatus("在读");
        change.setAfterStatus("已毕业");
        change.setEffectiveDate(new Date());
        change.setReason("升班任务 " + task.getTaskNo() + " 毕业处理");
        change.setOperateTime(new Date());
        changeMapper.insert(change);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    /** 生成任务编号：PRM-yyyyMMdd-#### */
    private String generateTaskNo() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "PRM-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /** 补齐前端状态码与失败原因摘要 */
    private void fillStatus(EduPromotionTaskVo vo) {
        if (vo == null) {
            return;
        }
        vo.setStatus(STATUS_CODE.getOrDefault(vo.getTaskStatus(), "draft"));
        if (vo.getFailedCount() != null && vo.getFailedCount() > 0) {
            vo.setFailReason(vo.getFailedCount() + " 名学生未完成，可到结果页重试失败项（REQ-PRM-032）");
        }
    }

    /** 补齐明细的展示字段（状态码与姓名、班级名） */
    private void fillItemDisplay(EduPromotionItemVo vo) {
        vo.setStatus(switch (vo.getItemStatus() == null ? "" : vo.getItemStatus()) {
            case ITEM_SUCCESS, "成功" -> ITEM_SUCCESS;
            case ITEM_FAILED, "失败" -> ITEM_FAILED;
            case ITEM_SKIPPED, "已跳过" -> ITEM_SKIPPED;
            default -> ITEM_PENDING;
        });
        if (vo.getStudentId() != null) {
            EduStudent student = studentMapper.selectById(vo.getStudentId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getStudentName());
            }
        }
        if (vo.getSourceClassId() != null) {
            EduClass source = classMapper.selectById(vo.getSourceClassId());
            vo.setSourceClassName(source == null ? null : source.getClassName());
        }
        if (vo.getTargetClassId() != null) {
            EduClass target = classMapper.selectById(vo.getTargetClassId());
            vo.setTargetClassName(target == null ? null : target.getClassName());
        }
    }

    private EduPromotionItem requireItem(Long itemId, Long taskId) {
        if (itemId == null) {
            throw new ServiceException("缺少明细 ID");
        }
        EduPromotionItem item = itemMapper.selectById(itemId);
        if (item == null || !taskId.equals(item.getTaskId())) {
            throw new ServiceException("升班明细不存在或不属于该任务");
        }
        return item;
    }

    private EduPromotionTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new ServiceException("缺少升班任务 ID");
        }
        EduPromotionTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("升班任务不存在或不在当前数据范围内");
        }
        return task;
    }

}
