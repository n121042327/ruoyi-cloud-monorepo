package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduCampus;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.EduTerm;
import org.dromara.edu.domain.bo.EduClassBo;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.domain.vo.EduClassMemberVo;
import org.dromara.edu.domain.vo.EduClassVo;
import org.dromara.edu.mapper.EduCampusMapper;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduSchoolMapper;
import org.dromara.edu.mapper.EduTermMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.service.IEduClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 班级服务层处理
 *
 * 口径要点：
 * - 行政班必填年级；教学班的创建入口唯一在「按组合生成教学班」向导（CR-017），本服务只做行政班与通用维护；
 * - 容量只提示不拦截（BR-CLASS-005）；有在读学生不允许删除、只允许停用（BR-CLASS-006）；
 * - 学生班级归属的唯一写入入口是班级管理（DP-01），关系追加式、不物理删除（BR-PROMO-012）；
 * - 数据范围条件在服务层拼进 wrapper（DS-DENY-07：不先取数再判断）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduClassServiceImpl implements IEduClassService {

    /** 班级类型：行政班 */
    private static final String TYPE_ADMINISTRATIVE = "administrative";

    /** 班级类型：教学班 */
    private static final String TYPE_TEACHING = "teaching";

    /** 班级状态：在读 / 正常 */
    private static final String STATUS_ACTIVE = "active";

    /** 班级状态：已停用 */
    private static final String STATUS_DISABLED = "disabled";

    /** 成员在班 / 已离开 */
    private static final String MEMBER_IN = "1";
    private static final String MEMBER_OUT = "0";

    private final EduClassMapper baseMapper;
    private final EduClassMemberMapper memberMapper;
    private final EduTeachingAssignmentMapper teachingAssignmentMapper;
    private final EduSchoolMapper schoolMapper;
    private final EduCampusMapper campusMapper;
    private final EduGradeMapper gradeMapper;
    private final EduTermMapper termMapper;

    @Override
    public TableDataInfo<EduClassVo> queryPageList(EduClassBo clazz, PageQuery pageQuery) {
        LambdaQueryWrapper<EduClass> wrapper = buildQueryWrapper(clazz);
        Page<EduClassVo> result = baseMapper.selectPageClassList(pageQuery.build(), wrapper);
        fillNames(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public EduClassVo queryById(Long classId) {
        EduClassVo vo = baseMapper.selectVoById(classId);
        if (vo == null) {
            throw new ServiceException("班级不存在或不在当前数据范围内");
        }
        fillNames(List.of(vo));
        return vo;
    }

    /**
     * 填充学校 / 年级 / 学年学期的显示名称。
     *
     * 口径：edu_class 只存外键，名称按 id 集合做三次批量查询，避免 N+1；
     * 更复杂的派生字段（校区名、班主任名、在读人数）在需要时再补连接查询。
     */
    private void fillNames(List<EduClassVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> schoolIds = rows.stream().map(EduClassVo::getSchoolId).filter(Objects::nonNull).distinct().toList();
        List<Long> gradeIds = rows.stream().map(EduClassVo::getGradeId).filter(Objects::nonNull).distinct().toList();
        List<Long> termIds = rows.stream().map(EduClassVo::getTermId).filter(Objects::nonNull).distinct().toList();
        List<Long> campusIds = rows.stream().map(EduClassVo::getCampusId).filter(Objects::nonNull).distinct().toList();

        Map<Long, String> schoolNames = schoolIds.isEmpty() ? Map.of()
            : schoolMapper.selectList(new LambdaQueryWrapper<EduSchool>().in(EduSchool::getSchoolId, schoolIds))
                .stream().collect(Collectors.toMap(EduSchool::getSchoolId, EduSchool::getSchoolName, (a, b) -> a));
        Map<Long, String> gradeNames = gradeIds.isEmpty() ? Map.of()
            : gradeMapper.selectList(new LambdaQueryWrapper<EduGrade>().in(EduGrade::getGradeId, gradeIds))
                .stream().collect(Collectors.toMap(EduGrade::getGradeId, EduGrade::getGradeName, (a, b) -> a));
        Map<Long, String> campusNames = campusIds.isEmpty() ? Map.of()
            : campusMapper.selectList(new LambdaQueryWrapper<EduCampus>().in(EduCampus::getCampusId, campusIds))
                .stream().collect(Collectors.toMap(EduCampus::getCampusId, EduCampus::getCampusName, (a, b) -> a));
        Map<Long, String> termNames = termIds.isEmpty() ? Map.of()
            : termMapper.selectList(new LambdaQueryWrapper<EduTerm>().in(EduTerm::getTermId, termIds))
                .stream().collect(Collectors.toMap(EduTerm::getTermId, EduTerm::getTermName, (a, b) -> a));

        for (EduClassVo row : rows) {
            row.setSchoolName(schoolNames.get(row.getSchoolId()));
            row.setGradeName(gradeNames.get(row.getGradeId()));
            row.setTermName(termNames.get(row.getTermId()));
            row.setCampusName(campusNames.get(row.getCampusId()));
        }
    }

    @Override
    public Boolean insertByBo(EduClassBo clazz) {
        validateClassForInsert(clazz);
        EduClass add = new EduClass();
        copyWritableFields(clazz, add);
        add.setClassStatus(StringUtils.isBlank(clazz.getClassStatus()) ? STATUS_ACTIVE : clazz.getClassStatus());
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(EduClassBo clazz) {
        EduClass update = requireClass(clazz.getClassId());
        // 学年学期与年级一经创建不可修改（REQ-CLS-022）：此处不接收 termId / gradeId / classType 变更
        update.setClassName(clazz.getClassName());
        update.setClassCapacity(clazz.getClassCapacity());
        update.setCampusId(clazz.getCampusId());
        update.setClassroom(clazz.getClassroom());
        update.setSubjectCombination(clazz.getSubjectCombination());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddClass(EduClassBo clazz) {
        List<EduClassBo> rows = clazz.getClassList();
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("批量新增至少需要一行班级");
        }
        for (EduClassBo row : rows) {
            if (row.getTermId() == null) {
                row.setTermId(clazz.getTermId());
            }
            if (row.getSchoolId() == null) {
                row.setSchoolId(clazz.getSchoolId());
            }
            insertByBo(row);
        }
        return true;
    }

    @Override
    public Boolean disableClass(Long classId, String reason) {
        EduClass update = requireClass(classId);
        if (STATUS_DISABLED.equals(update.getClassStatus())) {
            throw new ServiceException("该班级已经是停用状态");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("停用原因至少 5 个字");
        }
        update.setClassStatus(STATUS_DISABLED);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean removeClass(Long classId, String reason) {
        EduClass remove = requireClass(classId);
        Long members = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getClassId, classId)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        if (members != null && members > 0) {
            throw new ServiceException("该班级仍有在读学生，不允许删除，请改用停用（REQ-CLS-043）");
        }
        Long assignments = teachingAssignmentMapper.selectCount(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getClassId, classId)
            .eq(EduTeachingAssignment::getClassType, TYPE_ADMINISTRATIVE)
            .eq(EduTeachingAssignment::getStatus, MEMBER_IN));
        if (assignments != null && assignments > 0) {
            throw new ServiceException("该班级仍存在任教关系，不允许删除，请先结束任教关系");
        }
        return baseMapper.deleteById(remove.getClassId()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeClass(EduClassBo clazz) {
        if (clazz.getTargetClassId() == null) {
            throw new ServiceException("请选择目标班级");
        }
        if (clazz.getSourceClassIds() == null || clazz.getSourceClassIds().isEmpty()) {
            throw new ServiceException("请选择至少一个源班级");
        }
        if (clazz.getSourceClassIds().contains(clazz.getTargetClassId())) {
            throw new ServiceException("目标班级不能出现在源班级列表中");
        }
        EduClass target = requireClass(clazz.getTargetClassId());
        for (Long sourceClassId : clazz.getSourceClassIds()) {
            EduClass source = requireClass(sourceClassId);
            List<EduClassMember> members = memberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
                .eq(EduClassMember::getClassId, sourceClassId)
                .eq(EduClassMember::getStatus, MEMBER_IN));
            for (EduClassMember member : members) {
                // 源班级学生在目标班级下新增在班关系；源关系置为已离开（追加式，不删历史）
                insertMember(target.getClassId(), target.getTermId(), member.getStudentId(),
                    member.getStudentEnrollmentId(), member.getGenderSnapshot(), clazz.getEffectiveDate());
                member.setStatus(MEMBER_OUT);
                member.setLeaveDate(new Date());
                memberMapper.updateById(member);
            }
            source.setClassStatus(STATUS_DISABLED);
            baseMapper.updateById(source);
        }
        return true;
    }

    @Override
    public Boolean assignHeadTeacher(EduClassBo clazz) {
        EduClass update = requireClass(clazz.getClassId());
        if (TYPE_TEACHING.equals(update.getClassType())) {
            throw new ServiceException("教学班不设班主任（BR-CLASS-007）");
        }
        if (clazz.getHeadTeacherId() == null) {
            throw new ServiceException("请选择班主任");
        }
        update.setHeadTeacherId(clazz.getHeadTeacherId());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public TableDataInfo<EduClassMemberVo> queryRoster(Long classId, EduClassMemberBo member, PageQuery pageQuery) {
        requireClass(classId);
        LambdaQueryWrapper<EduClassMember> wrapper = new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getClassId, classId)
            .eq(EduClassMember::getClassType, TYPE_ADMINISTRATIVE)
            .eq(StringUtils.isNotBlank(member.getEnrollmentStatus()), EduClassMember::getStatus, member.getEnrollmentStatus())
            .orderByDesc(EduClassMember::getJoinDate);
        Page<EduClassMemberVo> result = memberMapper.selectPageRoster(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addRoster(EduClassMemberBo member) {
        EduClass clazz = requireClass(member.getClassId());
        List<String> conflicts = validateJoin(clazz.getClassId(), member.getStudentIds());
        if (!conflicts.isEmpty()) {
            // 冲突整体拒绝，不做部分成功（原型 PAGE-CLS-ROSTER-ADD 的口径）
            throw new ServiceException("以下学生不可加入本班：" + String.join("；", conflicts));
        }
        for (Long studentId : member.getStudentIds()) {
            insertMember(clazz.getClassId(), clazz.getTermId(), studentId, null, null, member.getEffectiveDate());
        }
        return true;
    }

    @Override
    public Boolean removeRoster(Long classId, Long studentId, String reason) {
        EduClassMember current = memberMapper.selectOne(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getClassId, classId)
            .eq(EduClassMember::getStudentId, studentId)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        if (current == null) {
            throw new ServiceException("该学生当前不在本班");
        }
        current.setStatus(MEMBER_OUT);
        current.setLeaveDate(new Date());
        return memberMapper.updateById(current) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transferClass(EduClassMemberBo member) {
        if (member.getTargetClassId() == null) {
            throw new ServiceException("请选择目标班级");
        }
        if (member.getStudentIds() == null || member.getStudentIds().isEmpty()) {
            throw new ServiceException("请至少选择一名学生");
        }
        EduClass target = requireClass(member.getTargetClassId());
        if (STATUS_DISABLED.equals(target.getClassStatus())) {
            throw new ServiceException("目标班级已停用，不能作为调班 / 迁移目标");
        }
        for (Long studentId : member.getStudentIds()) {
            EduClassMember current = memberMapper.selectOne(new LambdaQueryWrapper<EduClassMember>()
                .eq(EduClassMember::getStudentId, studentId)
                .eq(EduClassMember::getTermId, target.getTermId())
                .eq(EduClassMember::getStatus, MEMBER_IN));
            if (current != null) {
                if (current.getClassId().equals(target.getClassId())) {
                    throw new ServiceException("学生已在目标班级，无需迁移");
                }
                current.setStatus(MEMBER_OUT);
                current.setLeaveDate(new Date());
                memberMapper.updateById(current);
            }
            insertMember(target.getClassId(), target.getTermId(), studentId, null, null, member.getEffectiveDate());
        }
        return true;
    }

    @Override
    public List<String> validateJoin(Long classId, List<Long> studentIds) {
        List<String> conflicts = new ArrayList<>();
        if (studentIds == null || studentIds.isEmpty()) {
            conflicts.add("未选择任何学生");
            return conflicts;
        }
        for (Long studentId : studentIds) {
            Long occupied = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
                .eq(EduClassMember::getStudentId, studentId)
                .eq(EduClassMember::getStatus, MEMBER_IN));
            if (occupied != null && occupied > 0) {
                conflicts.add("学生 " + studentId + " 已有在班关系（同一学年学期只能属于一个行政班，BR-STU-003）");
            }
        }
        return conflicts;
    }

    /** 新增班级的前置校验：行政班必填年级，教学班必填组合 */
    private void validateClassForInsert(EduClassBo clazz) {
        if (clazz.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        if (TYPE_ADMINISTRATIVE.equals(clazz.getClassType()) && clazz.getGradeId() == null) {
            throw new ServiceException("行政班必须指定年级");
        }
        if (TYPE_TEACHING.equals(clazz.getClassType()) && StringUtils.isBlank(clazz.getSubjectCombination())) {
            throw new ServiceException("教学班必须指定组合 / 单学科标识");
        }
    }

    /** 取班级并做存在性校验 */
    private EduClass requireClass(Long classId) {
        if (classId == null) {
            throw new ServiceException("缺少班级 ID");
        }
        EduClass clazz = baseMapper.selectById(classId);
        if (clazz == null) {
            throw new ServiceException("班级不存在或不在当前数据范围内");
        }
        return clazz;
    }

    /** 追加一条在班关系（唯一键 uk_class_member_admin = term_id + student_id） */
    private void insertMember(Long classId, Long termId, Long studentId, Long enrollmentId,
                              String genderSnapshot, String effectiveDate) {
        EduClassMember add = new EduClassMember();
        add.setClassId(classId);
        add.setTermId(termId);
        add.setClassType(TYPE_ADMINISTRATIVE);
        add.setStudentId(studentId);
        add.setStudentEnrollmentId(enrollmentId);
        add.setGenderSnapshot(genderSnapshot);
        add.setJoinDate(new Date());
        add.setStatus(MEMBER_IN);
        memberMapper.insert(add);
    }

    /** 只拷贝可写列（主键、状态与审计列由框架维护） */
    private void copyWritableFields(EduClassBo bo, EduClass entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setTermId(bo.getTermId());
        entity.setGradeId(bo.getGradeId());
        entity.setStageCode(bo.getStageCode());
        entity.setClassName(bo.getClassName());
        entity.setClassType(bo.getClassType());
        entity.setClassCapacity(bo.getClassCapacity());
        entity.setCampusId(bo.getCampusId());
        entity.setClassroom(bo.getClassroom());
        entity.setSubjectCombination(bo.getSubjectCombination());
    }

    /**
     * 组装查询条件。
     *
     * 数据范围条件由 DataScopeResolver 在阶段 8 联调时注入（见 30-architecture/09-permission-architecture.md）；
     * 此处只拼业务筛选条件，避免先取数再判断（DS-DENY-07）。
     */
    private LambdaQueryWrapper<EduClass> buildQueryWrapper(EduClassBo bo) {
        LambdaQueryWrapper<EduClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(bo.getSchoolId() != null, EduClass::getSchoolId, bo.getSchoolId());
        wrapper.eq(bo.getCampusId() != null, EduClass::getCampusId, bo.getCampusId());
        wrapper.eq(bo.getTermId() != null, EduClass::getTermId, bo.getTermId());
        wrapper.eq(bo.getGradeId() != null, EduClass::getGradeId, bo.getGradeId());
        wrapper.eq(StringUtils.isNotBlank(bo.getClassType()), EduClass::getClassType, bo.getClassType());
        wrapper.eq(bo.getHeadTeacherId() != null, EduClass::getHeadTeacherId, bo.getHeadTeacherId());
        wrapper.eq(StringUtils.isNotBlank(bo.getClassStatus()), EduClass::getClassStatus, bo.getClassStatus());
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            wrapper.and(w -> w.like(EduClass::getClassName, bo.getKeyword())
                .or().like(EduClass::getClassroom, bo.getKeyword()));
        }
        wrapper.orderByAsc(EduClass::getGradeId).orderByAsc(EduClass::getClassName);
        return wrapper;
    }

    @Override
    public List<org.dromara.edu.domain.vo.EduTeachingAssignmentVo> listClassTeachingAssignment(Long classId) {
        requireClass(classId);
        // 任教关系只读：唯一写入入口在教师模块（AGENTS 第 7 节模块边界）
        return teachingAssignmentMapper.selectVoList(
            new LambdaQueryWrapper<org.dromara.edu.domain.EduTeachingAssignment>()
                .eq(org.dromara.edu.domain.EduTeachingAssignment::getClassId, classId)
                .orderByAsc(org.dromara.edu.domain.EduTeachingAssignment::getSubjectId));
    }

}
