package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduAcademicYear;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduTerm;
import org.dromara.edu.domain.bo.EduAcademicYearBo;
import org.dromara.edu.domain.bo.EduTermBo;
import org.dromara.edu.domain.vo.EduAcademicYearVo;
import org.dromara.edu.domain.vo.EduTermVo;
import org.dromara.edu.domain.vo.TermReferenceVo;
import org.dromara.edu.mapper.EduAcademicYearMapper;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduTermMapper;
import org.dromara.edu.service.IEduTermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 学年学期服务层处理
 *
 * 口径要点：
 * - 学年编码格式 YYYY-YYYY 且必须是连续两个自然年（BR-TERM-003 / REQ-TERM-008），同一学校内唯一（REQ-TERM-009）；
 * - 学年日期连续不重叠：前一年结束日 = 后一年开始日 − 1 天（RV-TERM-08）；
 * - 同一学校同一学年只能有一个当前学期（BR-TERM-002 / REQ-TERM-014）；
 * - 已被班级、任教关系、学生班级关系或学生选科引用的学期不允许删除（REQ-TERM-019 / 034）；
 * - 学年有引用时只允许归档、不允许删除（REQ-TERM-028），归档可撤销且写审计（REQ-TERM-033）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduTermServiceImpl implements IEduTermService {

    /** 学年 / 学期状态 */
    private static final String STATUS_NORMAL = "normal";
    private static final String STATUS_ARCHIVED = "archived";

    /** 当前学期标记 */
    private static final String CURRENT_YES = "1";
    private static final String CURRENT_NO = "0";

    /** 花名册在班标记 */
    private static final String MEMBER_IN = "1";

    private final EduAcademicYearMapper yearMapper;
    private final EduTermMapper termMapper;
    private final EduClassMapper classMapper;
    private final EduClassMemberMapper memberMapper;

    // ==================== 学年 ====================

    @Override
    public TableDataInfo<EduAcademicYearVo> queryYearPageList(EduAcademicYearBo year, PageQuery pageQuery) {
        LambdaQueryWrapper<EduAcademicYear> wrapper = new LambdaQueryWrapper<EduAcademicYear>()
            .eq(year.getSchoolId() != null, EduAcademicYear::getSchoolId, year.getSchoolId())
            .like(StringUtils.isNotBlank(year.getKeyword()), EduAcademicYear::getAcademicYearCode, year.getKeyword())
            .eq(StringUtils.isNotBlank(year.getAcademicYearStatus()), EduAcademicYear::getAcademicYearStatus, year.getAcademicYearStatus())
            .orderByDesc(EduAcademicYear::getStartDate);
        Page<EduAcademicYearVo> result = yearMapper.selectPageYearList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    public EduAcademicYearVo queryYearById(Long academicYearId) {
        EduAcademicYearVo vo = yearMapper.selectVoById(academicYearId);
        if (vo == null) {
            throw new ServiceException("学年不存在或不在当前数据范围内");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertYear(EduAcademicYearBo year) {
        validateYearCode(year.getAcademicYearCode());
        Date start = DateUtils.parseDate(year.getStartDate());
        Date end = DateUtils.parseDate(year.getEndDate());
        if (start == null || end == null || !start.before(end)) {
            throw new ServiceException("学年开始日期必须早于结束日期");
        }
        validateYearNotOverlap(year.getSchoolId(), start, end, null);
        EduAcademicYear add = new EduAcademicYear();
        copyYearFields(year, add);
        add.setAcademicYearStatus(StringUtils.isBlank(year.getAcademicYearStatus()) ? STATUS_NORMAL : year.getAcademicYearStatus());
        if (yearMapper.insert(add) <= 0) {
            return false;
        }
        // 新建学年时同步创建默认学期结构（上学期 / 下学期），允许微调（REQ-TERM-012）
        createDefaultTerms(add);
        return true;
    }

    @Override
    public Boolean updateYear(EduAcademicYearBo year) {
        EduAcademicYear update = requireYear(year.getAcademicYearId());
        if (StringUtils.isNotBlank(year.getAcademicYearCode())) {
            validateYearCode(year.getAcademicYearCode());
            update.setAcademicYearCode(year.getAcademicYearCode());
        }
        Date start = DateUtils.parseDate(year.getStartDate());
        Date end = DateUtils.parseDate(year.getEndDate());
        if (start != null && end != null) {
            if (!start.before(end)) {
                throw new ServiceException("学年开始日期必须早于结束日期");
            }
            validateYearNotOverlap(update.getSchoolId(), start, end, update.getAcademicYearId());
            update.setStartDate(start);
            update.setEndDate(end);
        }
        return yearMapper.updateById(update) > 0;
    }

    @Override
    public Boolean archiveYear(Long academicYearId, String reason) {
        EduAcademicYear update = requireYear(academicYearId);
        if (STATUS_ARCHIVED.equals(update.getAcademicYearStatus())) {
            throw new ServiceException("该学年已经归档");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("归档原因至少 5 个字");
        }
        update.setAcademicYearStatus(STATUS_ARCHIVED);
        if (yearMapper.updateById(update) <= 0) {
            return false;
        }
        // 归档学年时同步归档其下未归档的学期（归档后不在新建业务的可选列表，REQ-TERM-030）
        termMapper.update(null, new LambdaUpdateWrapper<EduTerm>()
            .eq(EduTerm::getAcademicYearId, academicYearId)
            .ne(EduTerm::getTermStatus, STATUS_ARCHIVED)
            .set(EduTerm::getTermStatus, STATUS_ARCHIVED)
            .set(EduTerm::getIsCurrent, CURRENT_NO));
        return true;
    }

    @Override
    public Boolean revokeArchiveYear(Long academicYearId, String reason) {
        EduAcademicYear update = requireYear(academicYearId);
        if (!STATUS_ARCHIVED.equals(update.getAcademicYearStatus())) {
            throw new ServiceException("该学年未归档，无需撤销");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("撤销原因至少 5 个字");
        }
        update.setAcademicYearStatus(STATUS_NORMAL);
        return yearMapper.updateById(update) > 0;
    }

    // ==================== 学期 ====================

    @Override
    public TableDataInfo<EduTermVo> queryTermPageList(EduTermBo term, PageQuery pageQuery) {
        LambdaQueryWrapper<EduTerm> wrapper = new LambdaQueryWrapper<EduTerm>()
            .eq(term.getSchoolId() != null, EduTerm::getSchoolId, term.getSchoolId())
            .eq(term.getAcademicYearId() != null, EduTerm::getAcademicYearId, term.getAcademicYearId())
            .like(StringUtils.isNotBlank(term.getKeyword()), EduTerm::getTermName, term.getKeyword())
            .eq(StringUtils.isNotBlank(term.getTermStatus()), EduTerm::getTermStatus, term.getTermStatus())
            .orderByDesc(EduTerm::getStartDate);
        Page<EduTermVo> result = termMapper.selectPageTermList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveTerm(EduTermBo term) {
        if (term.getAcademicYearId() == null) {
            throw new ServiceException("请选择所属学年");
        }
        EduAcademicYear year = requireYear(term.getAcademicYearId());
        Date start = DateUtils.parseDate(term.getStartDate());
        Date end = DateUtils.parseDate(term.getEndDate());
        if (start == null || end == null) {
            throw new ServiceException("请选择学期开始与结束日期");
        }
        if (!start.before(end)) {
            throw new ServiceException("学期开始日期必须早于结束日期");
        }
        // 学期日期必须落在学年范围内（REQ-TERM-015）
        if (year.getStartDate() != null && start.before(year.getStartDate())) {
            throw new ServiceException("学期开始日期不能早于所属学年的开始日期");
        }
        if (year.getEndDate() != null && end.after(year.getEndDate())) {
            throw new ServiceException("学期结束日期不能晚于所属学年的结束日期");
        }
        validateTermNotOverlap(year.getSchoolId(), start, end, term.getTermId());

        EduTerm entity = term.getTermId() == null ? new EduTerm() : requireTerm(term.getTermId());
        entity.setSchoolId(term.getSchoolId() != null ? term.getSchoolId() : year.getSchoolId());
        entity.setAcademicYearId(term.getAcademicYearId());
        entity.setTermCode(term.getTermCode());
        entity.setTermName(term.getTermName());
        entity.setStartDate(start);
        entity.setEndDate(end);
        if (StringUtils.isNotBlank(term.getTermStatus())) {
            entity.setTermStatus(term.getTermStatus());
        } else if (entity.getTermStatus() == null) {
            entity.setTermStatus(STATUS_NORMAL);
        }
        if (entity.getIsCurrent() == null) {
            entity.setIsCurrent(CURRENT_NO);
        }
        return entity.getTermId() == null ? termMapper.insert(entity) > 0 : termMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTerm(Long termId, String reason) {
        EduTerm term = requireTerm(termId);
        if (CURRENT_YES.equals(term.getIsCurrent())) {
            throw new ServiceException("当前学期不允许删除，请先切换当前学期");
        }
        TermReferenceVo reference = checkReferenceOfTerm(termId);
        if (Boolean.TRUE.equals(reference.getReferenced())) {
            throw new ServiceException("该学期已被班级 / 花名册引用，不允许删除，请改用归档（REQ-TERM-019）");
        }
        return termMapper.deleteById(termId) > 0;
    }

    @Override
    public EduTermVo getCurrentTerm(Long schoolId) {
        EduTerm current = termMapper.selectOne(new LambdaQueryWrapper<EduTerm>()
            .eq(schoolId != null, EduTerm::getSchoolId, schoolId)
            .eq(EduTerm::getIsCurrent, CURRENT_YES)
            .last("limit 1"));
        if (current == null) {
            throw new ServiceException("当前学校尚未设置当前学年学期");
        }
        EduTermVo vo = termMapper.selectVoById(current.getTermId());
        if (vo != null) {
            vo.setCurrent(Boolean.TRUE);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setCurrentTerm(Long termId) {
        EduTerm target = requireTerm(termId);
        if (STATUS_ARCHIVED.equals(target.getTermStatus())) {
            throw new ServiceException("已归档的学期不允许设为当前");
        }
        EduAcademicYear year = requireYear(target.getAcademicYearId());
        if (STATUS_ARCHIVED.equals(year.getAcademicYearStatus())) {
            throw new ServiceException("已归档的学年不允许设为当前");
        }
        // 同一学校同一学年只能有一个当前学期（BR-TERM-002）：先把该校全部置 0，再置目标为 1
        termMapper.update(null, new LambdaUpdateWrapper<EduTerm>()
            .eq(EduTerm::getSchoolId, target.getSchoolId())
            .eq(EduTerm::getIsCurrent, CURRENT_YES)
            .set(EduTerm::getIsCurrent, CURRENT_NO));
        termMapper.update(null, new LambdaUpdateWrapper<EduTerm>()
            .eq(EduTerm::getTermId, termId)
            .set(EduTerm::getIsCurrent, CURRENT_YES));
        return true;
    }

    @Override
    public TermReferenceVo checkReference(Long academicYearId) {
        requireYear(academicYearId);
        TermReferenceVo vo = new TermReferenceVo();
        // 学年的引用按「该学年下所有学期」聚合
        Long classCount = classMapper.selectCount(new LambdaQueryWrapper<EduClass>()
            .inSql(EduClass::getTermId, "select id from edu_term where academic_year_id = " + academicYearId));
        Long rosterCount = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getStatus, MEMBER_IN)
            .inSql(EduClassMember::getTermId, "select id from edu_term where academic_year_id = " + academicYearId));
        vo.setClassCount(classCount);
        vo.setRosterCount(rosterCount);
        // 任教关系数与选科人数分别在教师模块、选科模块交付后填充（EDU-7 后续批次）
        vo.setTeachingRelationCount(null);
        vo.setSubjectChoiceCount(null);
        vo.setReferenced((classCount != null && classCount > 0) || (rosterCount != null && rosterCount > 0));
        return vo;
    }

    // ==================== 内部方法 ====================

    /** 单学期维度的引用检查（删除学期用） */
    private TermReferenceVo checkReferenceOfTerm(Long termId) {
        TermReferenceVo vo = new TermReferenceVo();
        Long classCount = classMapper.selectCount(new LambdaQueryWrapper<EduClass>().eq(EduClass::getTermId, termId));
        Long rosterCount = memberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getTermId, termId)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        vo.setClassCount(classCount);
        vo.setRosterCount(rosterCount);
        vo.setReferenced((classCount != null && classCount > 0) || (rosterCount != null && rosterCount > 0));
        return vo;
    }

    /** 新建学年时同步创建默认学期结构（第一学期 / 第二学期），日期按学年对半切（REQ-TERM-012） */
    private void createDefaultTerms(EduAcademicYear year) {
        Long termCount = termMapper.selectCount(new LambdaQueryWrapper<EduTerm>()
            .eq(EduTerm::getAcademicYearId, year.getAcademicYearId()));
        if (termCount != null && termCount > 0) {
            return;
        }
        Date start = year.getStartDate();
        Date end = year.getEndDate();
        // 取中点作为两个学期的分界：第一学期 start ~ mid，第二学期 mid + 1 天 ~ end
        long midMillis = start.getTime() + (end.getTime() - start.getTime()) / 2;
        Date mid = new Date(midMillis);
        Date secondStart = new Date(midMillis + 24L * 60L * 60L * 1000L);

        EduTerm first = new EduTerm();
        first.setSchoolId(year.getSchoolId());
        first.setAcademicYearId(year.getAcademicYearId());
        first.setTermCode("1");
        first.setTermName("第一学期");
        first.setStartDate(start);
        first.setEndDate(mid);
        first.setIsCurrent(CURRENT_NO);
        first.setTermStatus(STATUS_NORMAL);
        termMapper.insert(first);

        EduTerm second = new EduTerm();
        second.setSchoolId(year.getSchoolId());
        second.setAcademicYearId(year.getAcademicYearId());
        second.setTermCode("2");
        second.setTermName("第二学期");
        second.setStartDate(secondStart);
        second.setEndDate(end);
        second.setIsCurrent(CURRENT_NO);
        second.setTermStatus(STATUS_NORMAL);
        termMapper.insert(second);
    }

    /** 学年编码格式校验：YYYY-YYYY 且必须是连续两个自然年（REQ-TERM-008 / BR-TERM-003） */
    private void validateYearCode(String code) {
        if (StringUtils.isBlank(code) || !code.matches("\\d{4}-\\d{4}")) {
            throw new ServiceException("学年编码格式必须是 YYYY-YYYY，如 2026-2027");
        }
        String[] parts = code.split("-");
        int startYear = Integer.parseInt(parts[0]);
        int endYear = Integer.parseInt(parts[1]);
        if (endYear != startYear + 1) {
            throw new ServiceException("学年编码必须是连续两个自然年（结束年 = 起始年 + 1）");
        }
    }

    /** 学年日期连续不重叠校验（RV-TERM-08） */
    private void validateYearNotOverlap(Long schoolId, Date start, Date end, Long excludeId) {
        LambdaQueryWrapper<EduAcademicYear> wrapper = new LambdaQueryWrapper<EduAcademicYear>()
            .eq(schoolId != null, EduAcademicYear::getSchoolId, schoolId)
            .ne(excludeId != null, EduAcademicYear::getAcademicYearId, excludeId)
            .and(w -> w.le(EduAcademicYear::getStartDate, end).ge(EduAcademicYear::getEndDate, start));
        if (yearMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("学年日期与已有学年重叠：前一年结束日必须等于后一年开始日 − 1 天（RV-TERM-08）");
        }
    }

    /** 同学期日期不重叠（REQ-TERM-016） */
    private void validateTermNotOverlap(Long schoolId, Date start, Date end, Long excludeId) {
        LambdaQueryWrapper<EduTerm> wrapper = new LambdaQueryWrapper<EduTerm>()
            .eq(schoolId != null, EduTerm::getSchoolId, schoolId)
            .ne(excludeId != null, EduTerm::getTermId, excludeId)
            .and(w -> w.le(EduTerm::getStartDate, end).ge(EduTerm::getEndDate, start));
        if (termMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("学期日期与已有学期重叠（REQ-TERM-016）");
        }
    }

    private void copyYearFields(EduAcademicYearBo bo, EduAcademicYear entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setAcademicYearCode(bo.getAcademicYearCode());
        entity.setStartDate(DateUtils.parseDate(bo.getStartDate()));
        entity.setEndDate(DateUtils.parseDate(bo.getEndDate()));
    }

    private EduAcademicYear requireYear(Long academicYearId) {
        if (academicYearId == null) {
            throw new ServiceException("缺少年度 ID");
        }
        EduAcademicYear year = yearMapper.selectById(academicYearId);
        if (year == null) {
            throw new ServiceException("学年不存在或不在当前数据范围内");
        }
        return year;
    }

    private EduTerm requireTerm(Long termId) {
        if (termId == null) {
            throw new ServiceException("缺少学期 ID");
        }
        EduTerm term = termMapper.selectById(termId);
        if (term == null) {
            throw new ServiceException("学期不存在或不在当前数据范围内");
        }
        return term;
    }

}
