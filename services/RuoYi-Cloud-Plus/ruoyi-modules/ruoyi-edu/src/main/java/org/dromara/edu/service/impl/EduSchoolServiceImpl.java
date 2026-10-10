package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.edu.domain.EduAcademicYear;
import org.dromara.edu.domain.EduCampus;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.EduSchoolStage;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.EduTerm;
import org.dromara.edu.domain.bo.EduAcademicYearBo;
import org.dromara.edu.domain.bo.EduCampusBo;
import org.dromara.edu.domain.bo.EduSchoolBo;
import org.dromara.edu.domain.bo.EduSchoolStageBo;
import org.dromara.edu.domain.bo.EduSubjectBo;
import org.dromara.edu.domain.vo.EduCampusVo;
import org.dromara.edu.domain.vo.EduSchoolStageVo;
import org.dromara.edu.domain.vo.EduSchoolVo;
import org.dromara.edu.domain.vo.SchoolSummaryVo;
import org.dromara.edu.mapper.EduAcademicYearMapper;
import org.dromara.edu.mapper.EduCampusMapper;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduSchoolMapper;
import org.dromara.edu.mapper.EduSchoolStageMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduSubjectMapper;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.mapper.EduTermMapper;
import org.dromara.edu.service.IEduSchoolService;
import org.dromara.edu.service.IEduSubjectService;
import org.dromara.edu.service.IEduTermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学校与校区服务层处理
 *
 * 口径要点：
 * - 一个学校对应一个租户（BR-ORG-002），学校与租户的绑定关系一经创建不可修改；
 * - 层级不超过三级（运营方 → 集团 → 学校，BR-ORG-003）；学校编码在父租户内唯一（BR-ORG-011）；
 * - 校区**不参与数据权限判定**，只用于组织与统计（BR-ORG-009 / GAP-045）；
 * - 未开设的学段在学科与年级配置里置灰（REQ-SUB-026 同口径）；
 * - 开通初始化（initSchoolBaseline）一次性完成学段 + 学年与默认学期，**幂等**：重复执行不产生重复数据（REQ-SCH-019 / 045）。
 *
 * 学生数等统计依赖对应模块的 mapper，随模块交付逐项补齐（当前 subjectCount 待学科模块交付后填充）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduSchoolServiceImpl implements IEduSchoolService {

    /** 学校 / 校区状态 */
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";

    /** 学段开设标记 */
    private static final String STAGE_ON = "1";

    /** 学籍状态：在读（`enrollment_status.enrolled`，GAP-101 的「在读学生」口径） */
    private static final String ENROLLMENT_IN_SCHOOL = "enrolled";
    private static final String STAGE_OFF = "0";

    private final EduSchoolMapper baseMapper;
    private final EduCampusMapper campusMapper;
    private final EduSchoolStageMapper schoolStageMapper;
    private final EduAcademicYearMapper academicYearMapper;
    private final EduTermMapper termMapper;
    private final EduGradeMapper gradeMapper;
    private final EduClassMapper classMapper;
    private final EduTeacherMapper teacherMapper;
    private final EduSubjectMapper subjectMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final IEduTermService termService;
    private final IEduSubjectService subjectService;

    // ==================== 学校 ====================

    @Override
    public TableDataInfo<EduSchoolVo> queryPageList(EduSchoolBo school, PageQuery pageQuery) {
        LambdaQueryWrapper<EduSchool> wrapper = new LambdaQueryWrapper<EduSchool>()
            .eq(StringUtils.isNotBlank(school.getSchoolStatus()), EduSchool::getSchoolStatus, school.getSchoolStatus())
            .eq(StringUtils.isNotBlank(school.getSchoolCode()), EduSchool::getSchoolCode, school.getSchoolCode());
        if (StringUtils.isNotBlank(school.getKeyword())) {
            wrapper.and(w -> w.like(EduSchool::getSchoolName, school.getKeyword())
                .or().like(EduSchool::getSchoolCode, school.getKeyword()));
        }
        // 按开设学段筛选：命中 edu_school_stage 的启用记录（REQ-SCH-002 / GAP-101，CR-159）
        if (StringUtils.isNotBlank(school.getStageCode())) {
            List<Long> stageSchoolIds = schoolStageMapper.selectList(new LambdaQueryWrapper<EduSchoolStage>()
                    .select(EduSchoolStage::getSchoolId)
                    .eq(EduSchoolStage::getStatus, STAGE_ON)
                    .eq(EduSchoolStage::getStageCode, school.getStageCode()))
                .stream().map(EduSchoolStage::getSchoolId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
            if (stageSchoolIds.isEmpty()) {
                // 没有学校开设该学段：返回空页，不退回全量（DS-DENY-03 同口径）
                Page<EduSchoolVo> empty = pageQuery.build();
                empty.setRecords(List.of());
                empty.setTotal(0);
                return TableDataInfo.build(empty);
            }
            wrapper.in(EduSchool::getSchoolId, stageSchoolIds);
        }
        wrapper.orderByAsc(EduSchool::getSchoolCode);
        Page<EduSchoolVo> result = baseMapper.selectPageSchoolList(pageQuery.build(), wrapper);
        fillListAggregates(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 补齐列表的派生列：开设学段 / 校区数 / 班级数 / 在读学生数
     * <p>
     * 口径与 {@link #getSchoolSummary(Long)} 保持一致（学段取 status=1 的记录；校区、班级按 school_id 计数，
     * 逻辑删除由 MyBatis-Plus 自动过滤），不另立第二套口径（CR-107）。
     * 「在读学生」= 本校在校记录中 `enrollment_status = 'enrolled'` 的**去重学生数**、不限学年（GAP-101 / CR-159）。
     */
    private void fillListAggregates(List<EduSchoolVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> schoolIds = records.stream().map(EduSchoolVo::getSchoolId).collect(Collectors.toList());
        Map<Long, String> stageMap = schoolStageMapper.selectList(new LambdaQueryWrapper<EduSchoolStage>()
                .in(EduSchoolStage::getSchoolId, schoolIds)
                .eq(EduSchoolStage::getStatus, STAGE_ON))
            .stream()
            .collect(Collectors.groupingBy(EduSchoolStage::getSchoolId,
                Collectors.mapping(EduSchoolStage::getStageCode, Collectors.joining(","))));
        Map<Long, Long> campusMap = campusMapper.selectList(new LambdaQueryWrapper<EduCampus>()
                .select(EduCampus::getSchoolId)
                .in(EduCampus::getSchoolId, schoolIds))
            .stream()
            .collect(Collectors.groupingBy(EduCampus::getSchoolId, Collectors.counting()));
        Map<Long, Long> classMap = classMapper.selectList(new LambdaQueryWrapper<EduClass>()
                .select(EduClass::getSchoolId)
                .in(EduClass::getSchoolId, schoolIds))
            .stream()
            .collect(Collectors.groupingBy(EduClass::getSchoolId, Collectors.counting()));
        // 在读学生：同一学生在同一学校只算一次（学籍状态可能有多条历史记录）
        Map<Long, Set<Long>> studentMap = new HashMap<>();
        for (EduStudentEnrollment enrollment : enrollmentMapper.selectList(
            new LambdaQueryWrapper<EduStudentEnrollment>()
                .select(EduStudentEnrollment::getSchoolId, EduStudentEnrollment::getStudentId)
                .in(EduStudentEnrollment::getSchoolId, schoolIds)
                .eq(EduStudentEnrollment::getEnrollmentStatus, ENROLLMENT_IN_SCHOOL))) {
            if (enrollment.getSchoolId() != null && enrollment.getStudentId() != null) {
                studentMap.computeIfAbsent(enrollment.getSchoolId(), key -> new HashSet<>())
                    .add(enrollment.getStudentId());
            }
        }
        for (EduSchoolVo vo : records) {
            vo.setStageCodes(stageMap.get(vo.getSchoolId()));
            vo.setCampusCount(campusMap.getOrDefault(vo.getSchoolId(), 0L).intValue());
            vo.setClassCount(classMap.getOrDefault(vo.getSchoolId(), 0L).intValue());
            vo.setStudentCount(studentMap.getOrDefault(vo.getSchoolId(), Set.of()).size());
        }
    }

    @Override
    public EduSchoolVo queryById(Long schoolId) {
        EduSchoolVo vo = baseMapper.selectVoById(schoolId);
        if (vo == null) {
            throw new ServiceException("学校不存在或不在当前数据范围内");
        }
        return vo;
    }

    @Override
    public EduSchoolVo getCurrentSchool() {
        String tenantId = TenantHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("缺少租户上下文，无法解析当前学校");
        }
        EduSchool school = baseMapper.selectOne(new LambdaQueryWrapper<EduSchool>()
            .eq(EduSchool::getTenantId, tenantId)
            .last("limit 1"));
        if (school == null) {
            throw new ServiceException("当前租户尚未接入学校");
        }
        EduSchoolVo vo = baseMapper.selectVoById(school.getSchoolId());
        if (vo != null) {
            vo.setCurrent(Boolean.TRUE);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(EduSchoolBo school) {
        validateSchoolCodeUnique(school.getParentTenantId(), school.getSchoolCode(), null);
        EduSchool add = new EduSchool();
        copyWritableFields(school, add);
        add.setSchoolStatus(StringUtils.isBlank(school.getSchoolStatus()) ? STATUS_ACTIVE : school.getSchoolStatus());
        boolean ok = baseMapper.insert(add) > 0;
        // 建校时一并落「开设学段」（PAGE-SCH-CREATE 的必填项，CR-107）：与 saveSchoolStage 共用同一套 upsert，
        // 避免前端建校后再发一次请求、也避免出现第二套学段写入逻辑（BR-GRADE-006 的学段决定可建年级与升学路径）
        if (ok && school.getStageCodes() != null && !school.getStageCodes().isEmpty()) {
            EduSchoolStageBo stage = new EduSchoolStageBo();
            stage.setStageCodes(school.getStageCodes());
            saveSchoolStage(add.getSchoolId(), stage);
        }
        return ok;
    }

    @Override
    public Boolean updateByBo(EduSchoolBo school) {
        EduSchool update = requireSchool(school.getSchoolId());
        // 学校与租户的绑定关系一经创建不可修改（REQ-SCH-022）：此处不接收 tenantId / parentTenantId 变更
        update.setSchoolName(school.getSchoolName());
        update.setShortName(school.getShortName());
        update.setSchoolType(school.getSchoolType());
        update.setAddress(school.getAddress());
        update.setPhone(school.getPhone());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateSchoolCode(Long schoolId, String schoolCode, String reason) {
        EduSchool update = requireSchool(schoolId);
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("修改学校编码必须填写原因（至少 5 个字）");
        }
        validateSchoolCodeUnique(update.getParentTenantId(), schoolCode, schoolId);
        update.setSchoolCode(schoolCode);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean disableSchool(Long schoolId, String reason) {
        EduSchool update = requireSchool(schoolId);
        if (STATUS_DISABLED.equals(update.getSchoolStatus())) {
            throw new ServiceException("该学校已经是停用状态");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("停用原因至少 5 个字");
        }
        update.setSchoolStatus(STATUS_DISABLED);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean enableSchool(Long schoolId, String reason) {
        EduSchool update = requireSchool(schoolId);
        if (STATUS_ACTIVE.equals(update.getSchoolStatus())) {
            throw new ServiceException("该学校已经是正常状态");
        }
        update.setSchoolStatus(STATUS_ACTIVE);
        return baseMapper.updateById(update) > 0;
    }

    // ==================== 校区 ====================

    @Override
    public TableDataInfo<EduCampusVo> queryCampusPageList(Long schoolId, EduCampusBo campus, PageQuery pageQuery) {
        LambdaQueryWrapper<EduCampus> wrapper = new LambdaQueryWrapper<EduCampus>()
            .eq(schoolId != null, EduCampus::getSchoolId, schoolId)
            .eq(StringUtils.isNotBlank(campus.getCampusStatus()), EduCampus::getCampusStatus, campus.getCampusStatus());
        if (StringUtils.isNotBlank(campus.getKeyword())) {
            wrapper.and(w -> w.like(EduCampus::getCampusName, campus.getKeyword())
                .or().like(EduCampus::getCampusCode, campus.getKeyword()));
        }
        wrapper.orderByAsc(EduCampus::getCampusCode);
        Page<EduCampusVo> result = campusMapper.selectPageCampusList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    public Boolean saveCampus(EduCampusBo campus) {
        if (campus.getSchoolId() == null) {
            throw new ServiceException("请选择所属学校");
        }
        validateCampusUnique(campus.getSchoolId(), campus.getCampusCode(), campus.getCampusName(), campus.getCampusId());
        EduCampus entity = campus.getCampusId() == null ? new EduCampus() : requireCampus(campus.getCampusId());
        entity.setSchoolId(campus.getSchoolId());
        entity.setCampusCode(campus.getCampusCode());
        entity.setCampusName(campus.getCampusName());
        entity.setAddress(campus.getAddress());
        entity.setLeaderName(campus.getLeaderName());
        entity.setLeaderPhone(campus.getLeaderPhone());
        if (StringUtils.isNotBlank(campus.getCampusStatus())) {
            entity.setCampusStatus(campus.getCampusStatus());
        } else if (entity.getCampusStatus() == null) {
            entity.setCampusStatus(STATUS_ACTIVE);
        }
        return entity.getCampusId() == null ? campusMapper.insert(entity) > 0 : campusMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean removeCampus(Long campusId, String reason) {
        EduCampus update = requireCampus(campusId);
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("停用校区必须填写原因（至少 5 个字）");
        }
        // 校区不参与数据权限判定（BR-ORG-009），但仍有班级挂在其上，因此只停用不删除
        update.setCampusStatus(STATUS_DISABLED);
        return campusMapper.updateById(update) > 0;
    }

    // ==================== 学段 ====================

    @Override
    public List<EduSchoolStageVo> listSchoolStage(Long schoolId) {
        List<EduSchoolStageVo> list = schoolStageMapper.selectVoList(new LambdaQueryWrapper<EduSchoolStage>()
            .eq(schoolId != null, EduSchoolStage::getSchoolId, schoolId)
            .orderByAsc(EduSchoolStage::getStageCode));
        fillStageGradeCount(list);
        return list;
    }

    /**
     * 补齐每个学段下的年级数。
     * <p>
     * REQ-SCH-034：学段一旦有年级引用就不允许移除；PAGE-SCH-STAGE 的交互要求「已被年级引用的学段置灰并说明原因」，
     * 因此列表接口要把年级数带给前端。
     */
    private void fillStageGradeCount(List<EduSchoolStageVo> stages) {
        if (stages == null || stages.isEmpty()) {
            return;
        }
        Long schoolId = stages.get(0).getSchoolId();
        Map<String, Long> gradeMap = gradeMapper.selectList(new LambdaQueryWrapper<EduGrade>()
                .select(EduGrade::getStageCode)
                .eq(EduGrade::getSchoolId, schoolId))
            .stream()
            .collect(Collectors.groupingBy(EduGrade::getStageCode, Collectors.counting()));
        for (EduSchoolStageVo vo : stages) {
            vo.setGradeCount(gradeMap.getOrDefault(vo.getStageCode(), 0L).intValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSchoolStage(Long schoolId, EduSchoolStageBo stage) {
        requireSchool(schoolId);
        List<String> codes = stage.getStageCodes();
        if (codes == null || codes.isEmpty()) {
            if (StringUtils.isNotBlank(stage.getStageCode())) {
                codes = List.of(stage.getStageCode());
            } else {
                throw new ServiceException("请至少选择一个学段");
            }
        }
        for (String code : codes) {
            EduSchoolStage exist = schoolStageMapper.selectOne(new LambdaQueryWrapper<EduSchoolStage>()
                .eq(EduSchoolStage::getSchoolId, schoolId)
                .eq(EduSchoolStage::getStageCode, code));
            if (exist != null) {
                exist.setStatus(STAGE_ON);
                schoolStageMapper.updateById(exist);
                continue;
            }
            EduSchoolStage add = new EduSchoolStage();
            add.setSchoolId(schoolId);
            add.setStageCode(code);
            add.setStatus(STAGE_ON);
            schoolStageMapper.insert(add);
        }
        // REQ-SCH-034：未被选中的其他学段置为「停开」；但该学段下已有年级时不允许移除，直接拒绝并说明原因
        List<EduSchoolStage> current = schoolStageMapper.selectList(new LambdaQueryWrapper<EduSchoolStage>()
            .eq(EduSchoolStage::getSchoolId, schoolId)
            .eq(EduSchoolStage::getStatus, STAGE_ON));
        for (EduSchoolStage exist : current) {
            if (codes.contains(exist.getStageCode())) {
                continue;
            }
            Long gradeCount = gradeMapper.selectCount(new LambdaQueryWrapper<EduGrade>()
                .eq(EduGrade::getSchoolId, schoolId)
                .eq(EduGrade::getStageCode, exist.getStageCode()));
            if (gradeCount != null && gradeCount > 0) {
                throw new ServiceException("该学段下已有年级，不能移除该学段（REQ-SCH-034）");
            }
            exist.setStatus(STAGE_OFF);
            schoolStageMapper.updateById(exist);
        }
        return true;
    }

    // ==================== 摘要与初始化 ====================

    @Override
    public SchoolSummaryVo getSchoolSummary(Long schoolId) {
        EduSchool school = requireSchool(schoolId);
        SchoolSummaryVo vo = new SchoolSummaryVo();
        vo.setSchoolId(schoolId);
        vo.setSchoolName(school.getSchoolName());
        List<String> stages = schoolStageMapper.selectVoList(new LambdaQueryWrapper<EduSchoolStage>()
                .eq(EduSchoolStage::getSchoolId, schoolId)
                .eq(EduSchoolStage::getStatus, STAGE_ON))
            .stream().map(EduSchoolStageVo::getStageCode).collect(Collectors.toList());
        vo.setStageCodes(stages);
        vo.setCampusCount(campusMapper.selectCount(new LambdaQueryWrapper<EduCampus>().eq(EduCampus::getSchoolId, schoolId)));
        vo.setAcademicYearCount(academicYearMapper.selectCount(new LambdaQueryWrapper<EduAcademicYear>()
            .eq(EduAcademicYear::getSchoolId, schoolId)));
        vo.setTermCount(termMapper.selectCount(new LambdaQueryWrapper<EduTerm>().eq(EduTerm::getSchoolId, schoolId)));
        vo.setGradeCount(gradeMapper.selectCount(new LambdaQueryWrapper<EduGrade>().eq(EduGrade::getSchoolId, schoolId)));
        vo.setClassCount(classMapper.selectCount(new LambdaQueryWrapper<EduClass>().eq(EduClass::getSchoolId, schoolId)));
        vo.setStudentCount(countStudentsInSchool(schoolId));
        vo.setTeacherCount(teacherMapper.selectCount(new LambdaQueryWrapper<EduTeacher>().eq(EduTeacher::getSchoolId, schoolId)));
        vo.setSubjectCount(subjectMapper.selectCount(new LambdaQueryWrapper<EduSubject>()
            .eq(EduSubject::getSchoolId, schoolId)));
        vo.setInitialized(!stages.isEmpty()
            && vo.getAcademicYearCount() != null && vo.getAcademicYearCount() > 0);
        return vo;
    }

    /** 在读学生数：本校在校记录中 `enrollment_status = 'enrolled'` 的去重学生数、不限学年（GAP-101 / CR-159） */
    private long countStudentsInSchool(Long schoolId) {
        return enrollmentMapper.selectList(new LambdaQueryWrapper<EduStudentEnrollment>()
                .select(EduStudentEnrollment::getStudentId)
                .eq(EduStudentEnrollment::getSchoolId, schoolId)
                .eq(EduStudentEnrollment::getEnrollmentStatus, ENROLLMENT_IN_SCHOOL))
            .stream()
            .map(EduStudentEnrollment::getStudentId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolSummaryVo initSchoolBaseline(EduSchoolBo school) {
        EduSchool target = requireSchool(school.getSchoolId());
        // ① 学段：幂等 —— 已有记录只置 status=1
        if (school.getStageCodes() != null && !school.getStageCodes().isEmpty()) {
            EduSchoolStageBo stageBo = new EduSchoolStageBo();
            stageBo.setStageCodes(school.getStageCodes());
            saveSchoolStage(target.getSchoolId(), stageBo);
        }
        // ② 学年与默认学期：同一学校同一编码只建一次（幂等）
        if (StringUtils.isNotBlank(school.getAcademicYearCode())) {
            Long exist = academicYearMapper.selectCount(new LambdaQueryWrapper<EduAcademicYear>()
                .eq(EduAcademicYear::getSchoolId, target.getSchoolId())
                .eq(EduAcademicYear::getAcademicYearCode, school.getAcademicYearCode()));
            if (exist == null || exist == 0) {
                EduAcademicYearBo yearBo = new EduAcademicYearBo();
                yearBo.setSchoolId(target.getSchoolId());
                yearBo.setAcademicYearCode(school.getAcademicYearCode());
                yearBo.setStartDate(school.getStartDate());
                yearBo.setEndDate(school.getEndDate());
                termService.insertYear(yearBo);
            }
        }
        // ③ 学科模板：默认初始化（幂等，已存在的编码跳过），在已开设的学段上启用
        boolean initSubject = school.getInitSubject() == null || Boolean.TRUE.equals(school.getInitSubject());
        if (initSubject) {
            EduSubjectBo subjectBo = new EduSubjectBo();
            subjectBo.setSchoolId(target.getSchoolId());
            subjectBo.setInitStageCodes(school.getStageCodes());
            subjectService.batchInitSubject(subjectBo);
        }
        // ④ 基础角色：教育角色模板随教育角色配置批次接入
        return getSchoolSummary(target.getSchoolId());
    }

    // ==================== 内部方法 ====================

    private void validateSchoolCodeUnique(String parentTenantId, String schoolCode, Long excludeId) {
        if (StringUtils.isBlank(schoolCode)) {
            throw new ServiceException("学校编码不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<EduSchool>()
            .eq(EduSchool::getSchoolCode, schoolCode)
            .eq(parentTenantId != null, EduSchool::getParentTenantId, parentTenantId)
            .ne(excludeId != null, EduSchool::getSchoolId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException("学校编码在上级租户内已存在：" + schoolCode);
        }
    }

    private void validateCampusUnique(Long schoolId, String campusCode, String campusName, Long excludeId) {
        Long codeCount = campusMapper.selectCount(new LambdaQueryWrapper<EduCampus>()
            .eq(EduCampus::getSchoolId, schoolId)
            .eq(EduCampus::getCampusCode, campusCode)
            .ne(excludeId != null, EduCampus::getCampusId, excludeId));
        if (codeCount != null && codeCount > 0) {
            throw new ServiceException("校区编码在该学校内已存在：" + campusCode);
        }
        Long nameCount = campusMapper.selectCount(new LambdaQueryWrapper<EduCampus>()
            .eq(EduCampus::getSchoolId, schoolId)
            .eq(EduCampus::getCampusName, campusName)
            .ne(excludeId != null, EduCampus::getCampusId, excludeId));
        if (nameCount != null && nameCount > 0) {
            throw new ServiceException("校区名称在该学校内已存在：" + campusName);
        }
    }

    private void copyWritableFields(EduSchoolBo bo, EduSchool entity) {
        entity.setSchoolCode(bo.getSchoolCode());
        entity.setSchoolName(bo.getSchoolName());
        entity.setShortName(bo.getShortName());
        entity.setParentTenantId(bo.getParentTenantId());
        entity.setRootTenantId(bo.getRootTenantId());
        entity.setSchoolType(bo.getSchoolType());
        entity.setAddress(bo.getAddress());
        entity.setPhone(bo.getPhone());
    }

    private EduSchool requireSchool(Long schoolId) {
        if (schoolId == null) {
            throw new ServiceException("缺少学校 ID");
        }
        EduSchool school = baseMapper.selectById(schoolId);
        if (school == null) {
            throw new ServiceException("学校不存在或不在当前数据范围内");
        }
        return school;
    }

    private EduCampus requireCampus(Long campusId) {
        if (campusId == null) {
            throw new ServiceException("缺少校区 ID");
        }
        EduCampus campus = campusMapper.selectById(campusId);
        if (campus == null) {
            throw new ServiceException("校区不存在或不在当前数据范围内");
        }
        return campus;
    }

}
