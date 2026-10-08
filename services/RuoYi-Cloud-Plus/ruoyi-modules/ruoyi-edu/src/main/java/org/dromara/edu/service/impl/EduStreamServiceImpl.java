package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduStudentStream;
import org.dromara.edu.domain.EduStreamChangeRequest;
import org.dromara.edu.domain.EduStreamConfig;
import org.dromara.edu.domain.EduStreamHistory;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.EduTeachingClass;
import org.dromara.edu.domain.EduTeachingClassMember;
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
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.mapper.EduStudentStreamMapper;
import org.dromara.edu.mapper.EduStreamChangeRequestMapper;
import org.dromara.edu.mapper.EduStreamConfigMapper;
import org.dromara.edu.mapper.EduStreamHistoryMapper;
import org.dromara.edu.mapper.EduSubjectMapper;
import org.dromara.edu.mapper.EduTeachingClassMapper;
import org.dromara.edu.mapper.EduTeachingClassMemberMapper;
import org.dromara.edu.service.IEduStreamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 选科与教学班生成服务层处理
 *
 * 口径要点：
 * - 配置按学校 + 学年学期唯一（REQ-STR-004）；开放期状态按当前时间**实时比较**（REQ-STR-005）；
 * - 截止前自助修改立即生效；截止后只能走变更申请，**审批通过前保持原值**（BR-STREAM-006）；
 * - 组合不在库里拼字符串，由 primary + secondary 派生展示（CR-016）；
 * - 同一学生同一学期**同时只允许一条待审批**变更（REQ-STR-029）；驳回必填意见（REQ-STR-037）；
 * - 历史**追加式、不可删除不可修改**，跨学年学期保留（REQ-STR-043 / 044）；
 * - 教学班生成预览**只读不写**（REQ-STR-056），执行按唯一键**幂等**（REQ-STR-057），核对差异只提示不修正（REQ-STR-060）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduStreamServiceImpl implements IEduStreamService {

    /** 配置状态 */
    private static final String CONFIG_ACTIVE = "active";

    /** 选科状态 */
    private static final String STREAM_EFFECTIVE = "effective";

    /** 变更申请状态 */
    private static final String REQUEST_PENDING = "pending";
    private static final String REQUEST_APPROVED = "approved";
    private static final String REQUEST_REJECTED = "rejected";
    private static final String REQUEST_CANCELED = "canceled";

    /** 教学班状态 */
    private static final String TCLASS_ACTIVE = "active";

    /** 花名册在班标记 */
    private static final String MEMBER_IN = "1";

    /** 学科选科角色与再选门数 */
    private static final String ROLE_PRIMARY = "primary";
    private static final String ROLE_SECONDARY = "secondary";
    private static final String ROLE_NONE = "none";
    private static final int SECONDARY_REQUIRED = 2;

    private final EduStreamConfigMapper configMapper;
    private final EduStudentStreamMapper streamMapper;
    private final EduStreamChangeRequestMapper requestMapper;
    private final EduStreamHistoryMapper historyMapper;
    private final EduSubjectMapper subjectMapper;
    private final EduStudentMapper studentMapper;
    private final EduClassMapper classMapper;
    private final EduClassMemberMapper memberMapper;
    private final EduTeachingClassMapper teachingClassMapper;
    private final EduTeachingClassMemberMapper teachingClassMemberMapper;

    @Override
    public EduStreamConfigVo getStreamConfig(Long termId) {
        EduStreamConfig config = findConfig(termId);
        if (config == null) {
            throw new ServiceException("当前学校尚未配置选科开放期");
        }
        return toConfigVo(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduStreamConfigVo saveStreamConfig(EduStreamConfigBo bo) {
        if (bo.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        Date openFrom = DateUtils.parseDate(bo.getOpenFrom());
        Date deadline = DateUtils.parseDate(bo.getDeadline());
        if (openFrom == null || deadline == null) {
            throw new ServiceException("请选择开放日期与截止时间");
        }
        if (!openFrom.before(deadline)) {
            throw new ServiceException("开放日期不能晚于截止时间");
        }
        EduStreamConfig config = findConfig(bo.getTermId());
        if (config == null) {
            config = new EduStreamConfig();
            config.setSchoolId(bo.getSchoolId());
            config.setTermId(bo.getTermId());
            config.setConfigStatus(CONFIG_ACTIVE);
        }
        config.setStreamOpenFrom(openFrom);
        config.setStreamDeadline(deadline);
        config.setOverdueRequiresApproval(bo.getOverdueRequiresApproval() == null || bo.getOverdueRequiresApproval());
        if (config.getConfigId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        return toConfigVo(config);
    }

    @Override
    public EduStreamOptionVo getStreamOption() {
        List<EduSubject> subjects = subjectMapper.selectList(new LambdaQueryWrapper<EduSubject>()
            .eq(EduSubject::getSubjectStatus, "active")
            .ne(EduSubject::getStreamRole, ROLE_NONE)
            .orderByAsc(EduSubject::getSortNo));
        EduStreamOptionVo vo = new EduStreamOptionVo();
        vo.setSecondaryRequired(SECONDARY_REQUIRED);
        vo.setPrimarySubjects(subjects.stream()
            .filter(item -> ROLE_PRIMARY.equals(item.getStreamRole()))
            .map(this::toOption).collect(Collectors.toList()));
        vo.setSecondarySubjects(subjects.stream()
            .filter(item -> ROLE_SECONDARY.equals(item.getStreamRole()))
            .map(this::toOption).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public EduMyStreamVo getMyStream(Long termId, Long studentId) {
        Long targetStudent = requireStudent(studentId).getStudentId();
        EduStudentStream stream = findStream(termId, targetStudent);
        EduStreamChangeRequest pending = requestMapper.selectOne(new LambdaQueryWrapper<EduStreamChangeRequest>()
            .eq(EduStreamChangeRequest::getStudentId, targetStudent)
            .eq(termId != null, EduStreamChangeRequest::getTermId, termId)
            .eq(EduStreamChangeRequest::getRequestStatus, REQUEST_PENDING)
            .last("limit 1"));
        EduMyStreamVo vo = toMyStreamVo(stream);
        if (vo == null) {
            vo = new EduMyStreamVo();
            vo.setStatus("未选择");
            vo.setTermId(termId);
        }
        if (pending != null) {
            vo.setStatus("待审批");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduMyStreamVo submitMyStream(EduStreamSelectionBo stream) {
        return saveMyStream(stream);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduMyStreamVo updateMyStream(EduStreamSelectionBo stream) {
        return saveMyStream(stream);
    }

    @Override
    public TableDataInfo<EduStreamSelectionVo> querySelectionPageList(EduStreamSelectionBo query, PageQuery pageQuery) {
        LambdaQueryWrapper<EduStudentStream> wrapper = new LambdaQueryWrapper<EduStudentStream>()
            .eq(query.getTermId() != null, EduStudentStream::getTermId, query.getTermId())
            .eq(StringUtils.isNotBlank(query.getPrimarySubjectCode()),
                EduStudentStream::getPrimarySubjectCode, query.getPrimarySubjectCode())
            .like(StringUtils.isNotBlank(query.getCombination()),
                EduStudentStream::getSecondarySubjectCodes, query.getCombination());
        if (query.getClassId() != null) {
            wrapper.inSql(EduStudentStream::getStudentId,
                "select student_id from edu_class_member where status = '1' and class_id = " + query.getClassId());
        }
        Page<EduStreamSelectionVo> result = streamMapper.selectPageSelectionList(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillSelectionDisplay);
        return TableDataInfo.build(result);
    }

    @Override
    public EduStreamStatVo getStreamStat(Long termId, Long gradeId) {
        EduStreamStatVo vo = new EduStreamStatVo();
        vo.setTermId(termId);
        List<EduStudentStream> streams = streamMapper.selectList(new LambdaQueryWrapper<EduStudentStream>()
            .eq(termId != null, EduStudentStream::getTermId, termId));
        vo.setSelectedCount(streams.size());
        Long enrolled = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(termId != null, EduClassMember::getTermId, termId)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        vo.setUnselectedCount(Math.max(0, (enrolled == null ? 0 : enrolled.intValue()) - streams.size()));
        Map<String, Integer> primaryMap = new HashMap<>();
        Map<String, Integer> combinationMap = new HashMap<>();
        Map<String, Integer> subjectMap = new HashMap<>();
        for (EduStudentStream item : streams) {
            primaryMap.merge(item.getPrimarySubjectCode(), 1, Integer::sum);
            subjectMap.merge(item.getPrimarySubjectCode(), 1, Integer::sum);
            combinationMap.merge(combination(item), 1, Integer::sum);
            for (String code : splitCodes(item.getSecondarySubjectCodes())) {
                subjectMap.merge(code, 1, Integer::sum);
            }
        }
        Map<String, String> names = subjectNameMap();
        int total = Math.max(1, streams.size());
        List<EduStreamStatVo.PrimaryRow> primaryRows = new ArrayList<>();
        primaryMap.forEach((code, count) -> {
            EduStreamStatVo.PrimaryRow row = new EduStreamStatVo.PrimaryRow();
            row.setSubjectName(names.getOrDefault(code, code));
            row.setMemberCount(count);
            row.setRatio(ratio(count, total));
            primaryRows.add(row);
        });
        primaryRows.sort(Comparator.comparing(EduStreamStatVo.PrimaryRow::getMemberCount).reversed());
        vo.setPrimaryDistribution(primaryRows);

        List<EduStreamStatVo.CombinationRow> combinationRows = new ArrayList<>();
        combinationMap.forEach((key, count) -> {
            EduStreamStatVo.CombinationRow row = new EduStreamStatVo.CombinationRow();
            row.setSubjectCombination(key);
            String[] parts = key.split(" \\+ ");
            row.setPrimarySubjectName(parts.length > 0 ? parts[0] : key);
            List<String> rest = new ArrayList<>(Arrays.asList(parts));
            if (!rest.isEmpty()) {
                rest.remove(0);
            }
            row.setSecondarySubjectNames(rest);
            row.setMemberCount(count);
            row.setRatio(ratio(count, total));
            combinationRows.add(row);
        });
        combinationRows.sort(Comparator.comparing(EduStreamStatVo.CombinationRow::getMemberCount).reversed());
        vo.setCombinations(combinationRows);

        List<EduStreamStatVo.SubjectRow> subjectRows = new ArrayList<>();
        subjectMap.forEach((code, count) -> {
            EduStreamStatVo.SubjectRow row = new EduStreamStatVo.SubjectRow();
            row.setSubjectName(names.getOrDefault(code, code));
            row.setStreamRole(primaryMap.containsKey(code) ? "首选" : "再选");
            row.setMemberCount(count);
            row.setRatio(ratio(count, total));
            row.setRatioBase(primaryMap.containsKey(code) ? "分母：已选科人数" : "分母：已选科人数");
            subjectRows.add(row);
        });
        subjectRows.sort(Comparator.comparing(EduStreamStatVo.SubjectRow::getMemberCount).reversed());
        vo.setSubjects(subjectRows);
        return vo;
    }

    @Override
    public List<EduUnselectedStudentVo> listUnselectedStudent(Long termId) {
        List<EduClassMember> members = memberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
            .eq(termId != null, EduClassMember::getTermId, termId)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        List<Long> selected = streamMapper.selectList(new LambdaQueryWrapper<EduStudentStream>()
                .eq(termId != null, EduStudentStream::getTermId, termId))
            .stream().map(EduStudentStream::getStudentId).collect(Collectors.toList());
        List<EduUnselectedStudentVo> result = new ArrayList<>();
        for (EduClassMember member : members) {
            if (selected.contains(member.getStudentId())) {
                continue;
            }
            EduStudent student = studentMapper.selectById(member.getStudentId());
            EduClass clazz = member.getClassId() == null ? null : classMapper.selectById(member.getClassId());
            EduUnselectedStudentVo vo = new EduUnselectedStudentVo();
            vo.setStudentId(member.getStudentId());
            vo.setStudentNo(student == null ? null : student.getStudentNo());
            vo.setStudentName(student == null ? null : student.getStudentName());
            vo.setClassName(clazz == null ? null : clazz.getClassName());
            result.add(vo);
        }
        return result;
    }

    // ==================== 变更申请 ====================

    @Override
    public TableDataInfo<EduStreamChangeRequestVo> queryChangeRequestPageList(EduStreamChangeRequestBo query, PageQuery pageQuery) {
        LambdaQueryWrapper<EduStreamChangeRequest> wrapper = new LambdaQueryWrapper<EduStreamChangeRequest>()
            .eq(query.getStudentId() != null, EduStreamChangeRequest::getStudentId, query.getStudentId())
            .eq(query.getTermId() != null, EduStreamChangeRequest::getTermId, query.getTermId())
            .eq(StringUtils.isNotBlank(query.getRequestStatus()),
                EduStreamChangeRequest::getRequestStatus, query.getRequestStatus())
            .like(StringUtils.isNotBlank(query.getKeyword()),
                EduStreamChangeRequest::getRequestNo, query.getKeyword())
            // 审批待办按提交时间升序（REQ-STR-035）
            .orderByAsc(EduStreamChangeRequest::getApplyTime);
        Page<EduStreamChangeRequestVo> result = requestMapper.selectPageRequestList(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillRequestDisplay);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduStreamChangeRequestVo addStreamChangeRequest(EduStreamChangeRequestBo request) {
        if (request.getStudentId() == null) {
            throw new ServiceException("请选择学生");
        }
        if (StringUtils.isBlank(request.getReason()) || request.getReason().trim().length() < 5) {
            throw new ServiceException("变更原因至少 5 个字");
        }
        EduStudent student = requireStudent(request.getStudentId());
        // 同一学生同一学期同时只允许一条待审批（REQ-STR-029 / BR-STREAM-007）
        Long pending = requestMapper.selectCount(new LambdaQueryWrapper<EduStreamChangeRequest>()
            .eq(EduStreamChangeRequest::getStudentId, request.getStudentId())
            .eq(request.getTermId() != null, EduStreamChangeRequest::getTermId, request.getTermId())
            .eq(EduStreamChangeRequest::getRequestStatus, REQUEST_PENDING));
        if (pending != null && pending > 0) {
            throw new ServiceException("该学生已有待审批的选科变更申请（REQ-STR-029）");
        }
        EduStudentStream current = findStream(request.getTermId(), request.getStudentId());
        Map<String, String> names = subjectNameMap();
        EduStreamChangeRequest add = new EduStreamChangeRequest();
        add.setSchoolId(request.getSchoolId());
        add.setRequestNo(generateRequestNo());
        add.setStudentId(student.getStudentId());
        add.setTermId(request.getTermId());
        add.setBeforeCombination(current == null ? "未选择" : combination(current));
        add.setAfterCombination(combination(request.getPrimarySubjectCode(),
            request.getSecondarySubjectCodes(), names));
        add.setPrimarySubjectCode(request.getPrimarySubjectCode());
        add.setSecondarySubjectCodes(joinCodes(request.getSecondarySubjectCodes()));
        add.setRequestStatus(REQUEST_PENDING);
        add.setReason(request.getReason());
        add.setApplyByRole("student");
        add.setApplyTime(new Date());
        requestMapper.insert(add);
        EduStreamChangeRequestVo vo = new EduStreamChangeRequestVo();
        vo.setRequestId(add.getRequestId());
        vo.setRequestNo(add.getRequestNo());
        vo.setStudentId(add.getStudentId());
        vo.setBeforeCombination(add.getBeforeCombination());
        vo.setAfterCombination(add.getAfterCombination());
        vo.setReason(add.getReason());
        vo.setApplyTime(add.getApplyTime());
        vo.setStatus(add.getRequestStatus());
        vo.setRequestStatus(add.getRequestStatus());
        fillRequestDisplay(vo);
        return vo;
    }

    @Override
    public Boolean cancelStreamChangeRequest(Long requestId, String reason) {
        EduStreamChangeRequest request = requireRequest(requestId);
        if (!REQUEST_PENDING.equals(request.getRequestStatus())) {
            throw new ServiceException("只有待审批的申请才能撤销");
        }
        request.setRequestStatus(REQUEST_CANCELED);
        request.setCancelTime(new Date());
        return requestMapper.updateById(request) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveStreamChangeRequest(EduStreamChangeRequestBo request) {
        EduStreamChangeRequest entity = requireRequest(request.getRequestId());
        if (!REQUEST_PENDING.equals(entity.getRequestStatus())) {
            throw new ServiceException("该申请不在待审批状态");
        }
        boolean approved = request.getApproved() == null || Boolean.TRUE.equals(request.getApproved());
        if (!approved && (StringUtils.isBlank(request.getApproveOpinion())
            || request.getApproveOpinion().trim().length() < 5)) {
            throw new ServiceException("驳回时必须填写审批意见（至少 5 个字，REQ-STR-037）");
        }
        entity.setRequestStatus(approved ? REQUEST_APPROVED : REQUEST_REJECTED);
        entity.setApproveOpinion(request.getApproveOpinion());
        entity.setApproveTime(new Date());
        requestMapper.updateById(entity);
        if (approved) {
            // 审批通过后新组合立即生效并写历史（REQ-STR-032 / 040）
            EduStudentStream stream = applyStream(entity);
            writeHistory(stream, entity.getBeforeCombination(), entity.getAfterCombination(),
                "变更申请通过", entity.getRequestNo(), entity.getReason(), entity.getApproveBy());
        }
        return true;
    }

    // ==================== 历史 ====================

    @Override
    public List<EduStreamHistoryVo> listStreamHistory(EduStreamHistoryBo query) {
        List<EduStreamHistory> histories = historyMapper.selectList(new LambdaQueryWrapper<EduStreamHistory>()
            .eq(query.getStudentId() != null, EduStreamHistory::getStudentId, query.getStudentId())
            .eq(query.getTermId() != null, EduStreamHistory::getTermId, query.getTermId())
            .orderByDesc(EduStreamHistory::getOperateTime));
        List<EduStreamHistoryVo> result = new ArrayList<>();
        for (EduStreamHistory history : histories) {
            EduStreamHistoryVo vo = new EduStreamHistoryVo();
            vo.setHistoryId(history.getHistoryId());
            vo.setStudentId(history.getStudentId());
            vo.setTermId(history.getTermId());
            vo.setChangeType(history.getChangeType());
            vo.setBeforeCombination(history.getBeforeCombination());
            vo.setAfterCombination(history.getAfterCombination());
            vo.setOperateTime(history.getOperateTime());
            vo.setEffectiveDate(history.getOperateTime());
            vo.setOperator(history.getOperator() == null ? null : String.valueOf(history.getOperator()));
            vo.setReason(history.getReason());
            result.add(vo);
        }
        return result;
    }

    // ==================== 教学班生成 ====================

    @Override
    public EduTeachingClassGenerateVo previewTeachingClassGenerate(EduStreamSelectionBo generate) {
        return generateTeachingClass(generate, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduTeachingClassGenerateVo executeTeachingClassGenerate(EduStreamSelectionBo generate) {
        return generateTeachingClass(generate, true);
    }

    // ==================== 内部方法 ====================

    /** 保存我的选科：开放期内直接生效；截止后转变更申请并保持原值（BR-STREAM-006） */
    private EduMyStreamVo saveMyStream(EduStreamSelectionBo bo) {
        Long studentId = requireStudent(bo.getStudentId()).getStudentId();
        if (bo.getTermId() == null) {
            throw new ServiceException("缺少学年学期上下文");
        }
        validateChoice(bo);
        EduStreamConfig config = findConfig(bo.getTermId());
        boolean overdue = config != null && config.getStreamDeadline() != null
            && new Date().after(config.getStreamDeadline());
        if (overdue) {
            EduStreamChangeRequestBo request = new EduStreamChangeRequestBo();
            request.setSchoolId(bo.getSchoolId());
            request.setTermId(bo.getTermId());
            request.setStudentId(studentId);
            request.setPrimarySubjectCode(bo.getPrimarySubjectCode());
            request.setSecondarySubjectCodes(bo.getSecondarySubjectCodes());
            request.setReason(bo.getReason());
            addStreamChangeRequest(request);
            return getMyStream(bo.getTermId(), studentId);
        }
        EduStudentStream stream = findStream(bo.getTermId(), studentId);
        String before = stream == null ? null : combination(stream);
        if (stream == null) {
            stream = new EduStudentStream();
            stream.setSchoolId(bo.getSchoolId());
            stream.setTermId(bo.getTermId());
            stream.setStudentId(studentId);
            stream.setStreamStatus(STREAM_EFFECTIVE);
        }
        stream.setPrimarySubjectCode(bo.getPrimarySubjectCode());
        stream.setSecondarySubjectCodes(joinCodes(bo.getSecondarySubjectCodes()));
        stream.setEffectiveTime(new Date());
        if (stream.getStreamId() == null) {
            streamMapper.insert(stream);
        } else {
            streamMapper.updateById(stream);
        }
        writeHistory(stream, before, combination(stream), before == null ? "首次提交" : "开放期内自助变更",
            null, bo.getReason(), bo.getCreateBy());
        return toMyStreamVo(stream);
    }

    /** 把变更申请的新组合写入学生选科（审批通过时） */
    private EduStudentStream applyStream(EduStreamChangeRequest request) {
        EduStudentStream stream = findStream(request.getTermId(), request.getStudentId());
        if (stream == null) {
            stream = new EduStudentStream();
            stream.setSchoolId(request.getSchoolId());
            stream.setTermId(request.getTermId());
            stream.setStudentId(request.getStudentId());
            stream.setStreamStatus(STREAM_EFFECTIVE);
        }
        stream.setPrimarySubjectCode(request.getPrimarySubjectCode());
        stream.setSecondarySubjectCodes(request.getSecondarySubjectCodes());
        stream.setEffectiveTime(new Date());
        if (stream.getStreamId() == null) {
            streamMapper.insert(stream);
        } else {
            streamMapper.updateById(stream);
        }
        return stream;
    }

    /** 写选科历史（追加式，不可删除不可修改，REQ-STR-043） */
    private void writeHistory(EduStudentStream stream, String before, String after, String changeType,
                              String requestNo, String reason, Long operator) {
        EduStreamHistory history = new EduStreamHistory();
        history.setSchoolId(stream.getSchoolId());
        history.setStudentId(stream.getStudentId());
        history.setTermId(stream.getTermId());
        history.setBeforeCombination(before);
        history.setAfterCombination(after);
        history.setChangeType(changeType);
        history.setRequestNo(requestNo);
        history.setReason(reason);
        history.setOperator(operator);
        history.setOperateTime(new Date());
        historyMapper.insert(history);
    }

    /** 教学班生成：execute=false 只预览（REQ-STR-056），execute=true 按唯一键幂等写入（REQ-STR-057） */
    private EduTeachingClassGenerateVo generateTeachingClass(EduStreamSelectionBo generate, boolean execute) {
        if (generate.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        List<EduStudentStream> streams = streamMapper.selectList(new LambdaQueryWrapper<EduStudentStream>()
            .eq(EduStudentStream::getTermId, generate.getTermId()));
        boolean bySubject = "subject".equals(generate.getGenerateMode());
        Map<String, List<EduStudentStream>> groups = new HashMap<>();
        for (EduStudentStream stream : streams) {
            if (bySubject) {
                groups.computeIfAbsent(stream.getPrimarySubjectCode(), k -> new ArrayList<>()).add(stream);
                for (String code : splitCodes(stream.getSecondarySubjectCodes())) {
                    groups.computeIfAbsent(code, k -> new ArrayList<>()).add(stream);
                }
            } else {
                groups.computeIfAbsent(combination(stream), k -> new ArrayList<>()).add(stream);
            }
        }
        Map<String, String> names = subjectNameMap();
        EduTeachingClassGenerateVo vo = new EduTeachingClassGenerateVo();
        vo.setTermId(generate.getTermId());
        vo.setGradeId(generate.getGradeId());
        vo.setGenerateMode(bySubject ? "subject" : "combination");
        vo.setBatchNo(generate.getBatchNo());
        List<EduTeachingClassGenerateVo.PlanRow> rows = new ArrayList<>();
        List<EduTeachingClassGenerateVo.CheckRow> checks = new ArrayList<>();
        for (Map.Entry<String, List<EduStudentStream>> entry : groups.entrySet()) {
            String key = entry.getKey();
            String display = key.contains(" + ") ? key : names.getOrDefault(key, key);
            String className = StringUtils.isNotBlank(generate.getClassNameRule())
                ? generate.getClassNameRule().replace("{组合}", display)
                : display + " 教学班";
            EduTeachingClass exist = teachingClassMapper.selectOne(new LambdaQueryWrapper<EduTeachingClass>()
                .eq(EduTeachingClass::getTermId, generate.getTermId())
                .eq(EduTeachingClass::getCombination, key)
                .eq(EduTeachingClass::getClassName, className)
                .last("limit 1"));
            EduTeachingClassGenerateVo.PlanRow row = new EduTeachingClassGenerateVo.PlanRow();
            row.setSubjectCombination(display);
            row.setClassName(className);
            row.setMemberCount(entry.getValue().size());
            row.setExisting(exist != null);
            row.setAction(exist == null ? "新建" : "增量并入（已存在同组合教学班，幂等，REQ-STR-057）");
            rows.add(row);
            if (!execute) {
                continue;
            }
            EduTeachingClass target = exist;
            if (target == null) {
                target = new EduTeachingClass();
                target.setSchoolId(generate.getSchoolId());
                target.setTermId(generate.getTermId());
                target.setGradeId(generate.getGradeId());
                target.setClassName(className);
                target.setCombination(key);
                target.setMemberCount(0);
                target.setTeachingClassStatus(TCLASS_ACTIVE);
                teachingClassMapper.insert(target);
            }
            for (EduStudentStream stream : entry.getValue()) {
                EduTeachingClassMember member = teachingClassMemberMapper.selectOne(
                    new LambdaQueryWrapper<EduTeachingClassMember>()
                        .eq(EduTeachingClassMember::getTeachingClassId, target.getTeachingClassId())
                        .eq(EduTeachingClassMember::getStudentId, stream.getStudentId())
                        .last("limit 1"));
                if (member != null) {
                    member.setStatus(MEMBER_IN);
                    member.setLeaveDate(null);
                    teachingClassMemberMapper.updateById(member);
                    continue;
                }
                EduTeachingClassMember add = new EduTeachingClassMember();
                add.setSchoolId(target.getSchoolId());
                add.setTeachingClassId(target.getTeachingClassId());
                add.setTermId(target.getTermId());
                add.setStudentId(stream.getStudentId());
                add.setSource("generate");
                add.setGenerateTaskNo(generate.getBatchNo());
                add.setJoinDate(new Date());
                add.setStatus(MEMBER_IN);
                teachingClassMemberMapper.insert(add);
            }
            Long count = teachingClassMemberMapper.selectCount(new LambdaQueryWrapper<EduTeachingClassMember>()
                .eq(EduTeachingClassMember::getTeachingClassId, target.getTeachingClassId())
                .eq(EduTeachingClassMember::getStatus, MEMBER_IN));
            target.setMemberCount(count == null ? 0 : count.intValue());
            teachingClassMapper.updateById(target);
            // 核对：教学班人数与选科统计人数比对，差异只提示不自动修正（REQ-STR-060）
            EduTeachingClassGenerateVo.CheckRow check = new EduTeachingClassGenerateVo.CheckRow();
            check.setClassName(className);
            check.setMemberCount(target.getMemberCount());
            check.setStatCount(entry.getValue().size());
            int diff = target.getMemberCount() - entry.getValue().size();
            check.setDiff(diff);
            check.setResult(diff == 0 ? "一致" : "存在差异（差异只提示，不自动修正，REQ-STR-060）");
            checks.add(check);
        }
        vo.setPlanCount(rows.size());
        vo.setRows(rows);
        vo.setCheckRows(checks);
        return vo;
    }

    /** 校验选科合法性：首选必须物理 / 历史，再选必须 4 选 2（BR-STREAM-001 / 002） */
    private void validateChoice(EduStreamSelectionBo bo) {
        if (StringUtils.isBlank(bo.getPrimarySubjectCode())) {
            throw new ServiceException("请选择首选科目（物理 / 历史）");
        }
        List<String> secondary = bo.getSecondarySubjectCodes();
        if (secondary == null || secondary.size() != SECONDARY_REQUIRED) {
            throw new ServiceException("再选科目必须选满 " + SECONDARY_REQUIRED + " 门且不能重复（BR-STREAM-002）");
        }
        if (secondary.stream().distinct().count() != secondary.size()) {
            throw new ServiceException("再选科目不能重复");
        }
        Map<String, String> names = subjectNameMap();
        if (!names.containsKey(bo.getPrimarySubjectCode())) {
            throw new ServiceException("首选科目不在本校已启用学科中：" + bo.getPrimarySubjectCode());
        }
        for (String code : secondary) {
            if (!names.containsKey(code)) {
                throw new ServiceException("再选科目不在本校已启用学科中：" + code);
            }
        }
    }

    private EduStreamConfig findConfig(Long termId) {
        return configMapper.selectOne(new LambdaQueryWrapper<EduStreamConfig>()
            .eq(termId != null, EduStreamConfig::getTermId, termId)
            .eq(EduStreamConfig::getConfigStatus, CONFIG_ACTIVE)
            .last("limit 1"));
    }

    private EduStudentStream findStream(Long termId, Long studentId) {
        return streamMapper.selectOne(new LambdaQueryWrapper<EduStudentStream>()
            .eq(termId != null, EduStudentStream::getTermId, termId)
            .eq(EduStudentStream::getStudentId, studentId)
            .last("limit 1"));
    }

    private EduStreamConfigVo toConfigVo(EduStreamConfig config) {
        EduStreamConfigVo vo = new EduStreamConfigVo();
        vo.setConfigId(config.getConfigId());
        vo.setSchoolId(config.getSchoolId());
        vo.setTermId(config.getTermId());
        vo.setOpenFrom(config.getStreamOpenFrom());
        vo.setDeadline(config.getStreamDeadline());
        vo.setStreamOpenFrom(config.getStreamOpenFrom());
        vo.setStreamDeadline(config.getStreamDeadline());
        vo.setOverdueRequiresApproval(config.getOverdueRequiresApproval());
        vo.setConfigStatus(config.getConfigStatus());
        // 开放期状态按当前时间实时比较（REQ-STR-005）
        Date now = new Date();
        if (config.getStreamOpenFrom() != null && now.before(config.getStreamOpenFrom())) {
            vo.setPeriodStatus("未开始");
        } else if (config.getStreamDeadline() != null && now.after(config.getStreamDeadline())) {
            vo.setPeriodStatus("已截止");
        } else {
            vo.setPeriodStatus("进行中");
        }
        return vo;
    }

    private EduMyStreamVo toMyStreamVo(EduStudentStream stream) {
        if (stream == null) {
            return null;
        }
        Map<String, String> names = subjectNameMap();
        List<String> secondary = splitCodes(stream.getSecondarySubjectCodes());
        EduMyStreamVo vo = new EduMyStreamVo();
        vo.setStreamId(stream.getStreamId());
        vo.setTermId(stream.getTermId());
        vo.setPrimarySubjectCode(stream.getPrimarySubjectCode());
        vo.setPrimarySubjectName(names.getOrDefault(stream.getPrimarySubjectCode(), stream.getPrimarySubjectCode()));
        vo.setSecondarySubjectCodes(secondary);
        vo.setSecondarySubjectNames(secondary.stream()
            .map(code -> names.getOrDefault(code, code)).collect(Collectors.toList()));
        vo.setCombination(combination(stream));
        vo.setStatus(STREAM_EFFECTIVE.equals(stream.getStreamStatus()) ? "已生效" : stream.getStreamStatus());
        vo.setEffectiveDate(stream.getEffectiveTime());
        return vo;
    }

    private void fillSelectionDisplay(EduStreamSelectionVo vo) {
        Map<String, String> names = subjectNameMap();
        vo.setPrimarySubjectName(names.getOrDefault(vo.getPrimarySubjectCode(), vo.getPrimarySubjectCode()));
        vo.setCombination(combination(vo.getPrimarySubjectCode(),
            vo.getSecondarySubjectCodes(), names));
        vo.setStatus(vo.getStatus() == null ? "已生效" : vo.getStatus());
        if (vo.getStudentId() != null) {
            EduStudent student = studentMapper.selectById(vo.getStudentId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setStudentName(student.getStudentName());
            }
            EduClassMember member = memberMapper.selectOne(new LambdaQueryWrapper<EduClassMember>()
                .eq(EduClassMember::getStudentId, vo.getStudentId())
                .eq(EduClassMember::getStatus, MEMBER_IN)
                .last("limit 1"));
            if (member != null) {
                vo.setClassId(member.getClassId());
                EduClass clazz = classMapper.selectById(member.getClassId());
                vo.setClassName(clazz == null ? null : clazz.getClassName());
                vo.setGradeId(clazz == null ? null : clazz.getGradeId());
            }
        }
    }

    private void fillRequestDisplay(EduStreamChangeRequestVo vo) {
        if (vo.getStudentId() == null) {
            return;
        }
        EduStudent student = studentMapper.selectById(vo.getStudentId());
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
        }
    }

    private EduStreamOptionVo.SubjectOption toOption(EduSubject subject) {
        EduStreamOptionVo.SubjectOption option = new EduStreamOptionVo.SubjectOption();
        option.setCode(subject.getSubjectCode());
        option.setName(subject.getSubjectName());
        return option;
    }

    /** 学科编码 → 名称 */
    private Map<String, String> subjectNameMap() {
        return subjectMapper.selectList(new LambdaQueryWrapper<>()).stream()
            .collect(Collectors.toMap(EduSubject::getSubjectCode, EduSubject::getSubjectName, (a, b) -> a));
    }

    private String combination(EduStudentStream stream) {
        return combination(stream.getPrimarySubjectCode(), splitCodes(stream.getSecondarySubjectCodes()),
            subjectNameMap());
    }

    /** 组合展示名（物理 + 化学 + 生物）；入参可能是编码也可能是编码列表（清单页从 VO 反查） */
    private String combination(String primaryCode, List<String> secondaryCodes, Map<String, String> names) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(primaryCode)) {
            parts.add(displayName(primaryCode, names));
        }
        if (secondaryCodes != null) {
            secondaryCodes.forEach(code -> parts.add(displayName(code, names)));
        }
        return String.join(" + ", parts);
    }

    private String displayName(String code, Map<String, String> names) {
        if (code != null && code.contains(" + ")) {
            return code;
        }
        return names.getOrDefault(code, code);
    }

    private String joinCodes(List<String> codes) {
        return codes == null ? null : String.join(",", codes);
    }

    private List<String> splitCodes(String codes) {
        if (StringUtils.isBlank(codes)) {
            return new ArrayList<>();
        }
        return Arrays.stream(codes.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    private String ratio(int count, int total) {
        return String.format("%.1f%%", count * 100.0 / total);
    }

    private String generateRequestNo() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "SC-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
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

    private EduStreamChangeRequest requireRequest(Long requestId) {
        if (requestId == null) {
            throw new ServiceException("缺少申请 ID");
        }
        EduStreamChangeRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new ServiceException("变更申请不存在或不在当前数据范围内");
        }
        return request;
    }

}
