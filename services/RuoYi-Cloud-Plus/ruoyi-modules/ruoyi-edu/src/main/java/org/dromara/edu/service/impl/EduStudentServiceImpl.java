package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.utils.SequenceUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.bo.EduStudentBo;
import org.dromara.edu.domain.vo.EduStudentVo;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.service.IEduStudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学生服务层处理
 *
 * 数据范围：列表查询先拼 DataScopeResolver 给出的范围条件再取数（学生 PRD 5.2 / DS-DENY-07）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduStudentServiceImpl implements IEduStudentService {

    /** 学籍状态：在读 */
    private static final String STATUS_ENROLLED = "enrolled";

    /** 在班关系标记 */
    private static final String MEMBER_IN = "1";

    private final EduStudentMapper baseMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final EduClassMemberMapper classMemberMapper;
    private final EduClassMapper classMapper;
    private final EduGradeMapper gradeMapper;

    @Override
    public TableDataInfo<EduStudentVo> queryPageList(EduStudentBo student, PageQuery pageQuery) {
        LambdaQueryWrapper<EduStudent> wrapper = buildQueryWrapper(student);
        Page<EduStudentVo> result = baseMapper.selectPageStudentList(pageQuery.build(), wrapper);
        fillListFields(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 填充学生列表的派生列：学段 / 年级 / 班级 / 学籍状态。
     *
     * 学生主体是平台级实体，学校侧一律经「在校记录 + 班级关系」两段式取数（AGENTS 第 7 节）；
     * 阶段 8 验收缺陷 CR-171：列表此前这四列为空（页面列存在但没有任何取数）。
     */
    private void fillListFields(List<EduStudentVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> studentIds = rows.stream().map(EduStudentVo::getStudentId)
            .filter(Objects::nonNull).distinct().toList();
        Map<Long, EduStudentEnrollment> enrollmentMap = new HashMap<>();
        Map<Long, Long> classByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            for (EduStudentEnrollment item : enrollmentMapper.selectList(
                new LambdaQueryWrapper<EduStudentEnrollment>()
                    .in(EduStudentEnrollment::getStudentId, studentIds))) {
                if (item.getStudentId() != null) {
                    enrollmentMap.putIfAbsent(item.getStudentId(), item);
                }
            }
            for (EduClassMember member : classMemberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
                .in(EduClassMember::getStudentId, studentIds)
                .eq(EduClassMember::getStatus, MEMBER_IN))) {
                if (member.getStudentId() != null && member.getClassId() != null) {
                    classByStudent.putIfAbsent(member.getStudentId(), member.getClassId());
                }
            }
        }
        List<Long> classIds = classByStudent.values().stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, EduClass> classes = new HashMap<>();
        if (!classIds.isEmpty()) {
            for (EduClass item : classMapper.selectByIds(classIds)) {
                classes.put(item.getClassId(), item);
            }
        }
        List<Long> gradeIds = classes.values().stream().map(EduClass::getGradeId)
            .filter(Objects::nonNull).distinct().toList();
        Map<Long, EduGrade> grades = new HashMap<>();
        if (!gradeIds.isEmpty()) {
            for (EduGrade item : gradeMapper.selectByIds(gradeIds)) {
                grades.put(item.getGradeId(), item);
            }
        }
        for (EduStudentVo row : rows) {
            EduStudentEnrollment enrollment = row.getStudentId() == null
                ? null : enrollmentMap.get(row.getStudentId());
            if (enrollment != null) {
                row.setEnrollmentStatus(enrollment.getEnrollmentStatus());
            }
            Long classId = row.getStudentId() == null ? null : classByStudent.get(row.getStudentId());
            EduClass clazz = classId == null ? null : classes.get(classId);
            if (clazz != null) {
                row.setClassId(clazz.getClassId());
                row.setClassName(clazz.getClassName());
                EduGrade grade = clazz.getGradeId() == null ? null : grades.get(clazz.getGradeId());
                if (grade != null) {
                    row.setGradeId(grade.getGradeId());
                    row.setGradeName(grade.getGradeName());
                    row.setStageCode(grade.getStageCode());
                }
            }
        }
    }

    @Override
    public EduStudentVo queryById(Long studentId) {
        EduStudentVo vo = baseMapper.selectVoById(studentId);
        if (vo == null) {
            throw new ServiceException("学生不存在或不在当前数据范围内");
        }
        fillListFields(List.of(vo));
        return vo;
    }

    @Override
    public Boolean insertByBo(EduStudentBo student) {
        return insertWithEnrollment(student) != null;
    }

    /**
     * 新增学生：发号 + 建档 + 写在校记录（同一事务）。
     *
     * 口径（学生 PRD 第 1.3 节「学号发号器本身是平台级公共能力，在本模块调用，不在本模块实现」）：
     * 学号由平台级发号器 `SequenceUtils` 按「入学年份 + 6 位全局流水」生成，平台唯一且永不回收（BR-STU-001 / 019）；
     * 在校记录带学校上下（school_id / term 对应学年）—— 学校侧读学生一律经在校记录两段式取数。
     *
     * @return 新学生 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertWithEnrollment(EduStudentBo student) {
        if (student.getSchoolId() == null) {
            throw new ServiceException("新增学生必须带学校上下文（DS-DENY-02）");
        }
        EduStudent add = new EduStudent();
        add.setStudentName(student.getStudentName());
        add.setGender(student.getGender());
        add.setNationalStudentNo(student.getNationalStudentNo());
        add.setIdType(student.getIdType());
        add.setIdCardNo(student.getIdCardNo());
        add.setBirthDate(student.getBirthDate());
        add.setEnrollYear(student.getEnrollYear());
        add.setGraduationDate(student.getGraduationDate());
        add.setRemark(student.getRemark());
        // 平台级发号器：按入学年份分段，6 位流水；学号永不回收（BR-STU-001）
        String year = student.getEnrollYear() == null
            ? String.valueOf(java.time.LocalDate.now().getYear()) : String.valueOf(student.getEnrollYear());
        long next = SequenceUtils.getNextId("edu:student:no:" + year,
            Duration.ofDays(365), 1L, 1L);
        add.setStudentNo(year + String.format("%06d", next));
        if (baseMapper.insert(add) <= 0) {
            throw new ServiceException("学生建档失败");
        }
        EduStudentEnrollment enrollment = new EduStudentEnrollment();
        enrollment.setSchoolId(student.getSchoolId());
        enrollment.setStudentId(add.getStudentId());
        enrollment.setEnrollDate(DateUtils.getNowDate());
        enrollment.setEnrollmentStatus(StringUtils.isBlank(student.getEnrollmentStatus())
            ? STATUS_ENROLLED : student.getEnrollmentStatus());
        enrollment.setEntryGradeId(student.getGradeId());
        enrollmentMapper.insert(enrollment);
        return add.getStudentId();
    }

    @Override
    public Boolean updateByBo(EduStudentBo student) {
        EduStudent update = baseMapper.selectById(student.getStudentId());
        if (update == null) {
            throw new ServiceException("学生不存在或不在当前数据范围内");
        }
        // 学号一经发出不可改（REQ-STU-027 / GAP-030 裁决 B）：此处不接收学号变更
        update.setStudentName(student.getStudentName());
        update.setGender(student.getGender());
        update.setNationalStudentNo(student.getNationalStudentNo());
        update.setIdType(student.getIdType());
        update.setIdCardNo(student.getIdCardNo());
        update.setBirthDate(student.getBirthDate());
        update.setEnrollYear(student.getEnrollYear());
        update.setGraduationDate(student.getGraduationDate());
        update.setRemark(student.getRemark());
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 组装查询条件。
     *
     * 数据范围条件在阶段 8 联调时由 DataScopeResolver 注入（见 30-architecture/09-permission-architecture.md）；
     * 此处只拼业务筛选条件，避免先取数再判断（DS-DENY-07）。
     */
    private LambdaQueryWrapper<EduStudent> buildQueryWrapper(EduStudentBo bo) {
        LambdaQueryWrapper<EduStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(bo.getStudentNo()), EduStudent::getStudentNo, bo.getStudentNo());
        wrapper.like(StringUtils.isNotBlank(bo.getStudentName()), EduStudent::getStudentName, bo.getStudentName());
        wrapper.like(StringUtils.isNotBlank(bo.getNationalStudentNo()), EduStudent::getNationalStudentNo, bo.getNationalStudentNo());
        wrapper.eq(StringUtils.isNotBlank(bo.getGender()), EduStudent::getGender, bo.getGender());
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            wrapper.and(w -> w.like(EduStudent::getStudentNo, bo.getKeyword())
                .or().like(EduStudent::getStudentName, bo.getKeyword())
                .or().like(EduStudent::getNationalStudentNo, bo.getKeyword()));
        }
        wrapper.orderByDesc(EduStudent::getUpdateTime);
        return wrapper;
    }

    /** 供导入等场景批量写入（阶段 7 后续批次接入导入引擎） */
    public Boolean saveBatch(List<EduStudent> students) {
        return students.stream().allMatch(item -> baseMapper.insert(item) > 0);
    }

}
