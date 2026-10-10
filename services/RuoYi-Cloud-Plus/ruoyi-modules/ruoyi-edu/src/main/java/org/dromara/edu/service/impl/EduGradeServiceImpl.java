package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduGradeLeader;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.bo.EduGradeBo;
import org.dromara.edu.domain.vo.EduGradeLeaderVo;
import org.dromara.edu.domain.vo.EduGradeVo;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduGradeLeaderMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduSchoolMapper;
import org.dromara.edu.service.IEduGradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 年级服务层处理
 *
 * 口径要点：
 * - 年级与学段固定映射，学段与学段内序号一经创建不可修改（REQ-GRD-018 / RV-GRD-03）；
 * - 年级不能跨学段改名（BR-GRADE-006）；
 * - 有班级或学生关系时不允许删除、只允许归档（BR-GRADE-004）；
 * - 升班的唯一执行入口是升班模块，本服务只提供「学段内序号 +1」的只读视图（REQ-GRD-032）；
 * - 年级主任任职是数据范围 DS-05 的权威来源，离任置 status=0、不物理删除。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduGradeServiceImpl implements IEduGradeService {

    /** 年级状态：正常 / 已归档 */
    private static final String GRADE_NORMAL = "normal";
    private static final String GRADE_ARCHIVED = "archived";

    /** 任职状态：在职 / 离任 */
    private static final String LEADER_ON = "1";
    private static final String LEADER_OFF = "0";

    private final EduGradeMapper baseMapper;
    private final EduGradeLeaderMapper leaderMapper;
    private final EduClassMapper classMapper;
    private final EduClassMemberMapper classMemberMapper;
    private final EduSchoolMapper schoolMapper;

    @Override
    public TableDataInfo<EduGradeVo> queryPageList(EduGradeBo grade, PageQuery pageQuery) {
        LambdaQueryWrapper<EduGrade> wrapper = buildQueryWrapper(grade);
        Page<EduGradeVo> result = baseMapper.selectPageGradeList(pageQuery.build(), wrapper);
        fillListCounts(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 填充年级列表的班级数与在读学生数（阶段 8 验收缺陷 CR-169）。
     *
     * `edu_grade.class_count / student_count` 是建年级时写死的 0，从未维护（编班后仍显示 0），
     * 因此列表改为实时统计：班级数按 `edu_class.grade_id` 计数，
     * 在读学生数按这些班级下 `edu_class_member.status = '1'`（在班）的记录数统计；
     * `school_name` 由 `edu_school` 回填（阶段 8 验收缺陷 GAP-118：年级详情抽屉的「所属学校」此前是「—」）。
     */
    private void fillListCounts(List<EduGradeVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> gradeIds = rows.stream().map(EduGradeVo::getGradeId)
            .filter(Objects::nonNull).distinct().toList();
        Map<Long, Integer> classCounts = new HashMap<>();
        Map<Long, Integer> studentCounts = new HashMap<>();
        if (!gradeIds.isEmpty()) {
            Map<Long, Long> classToGrade = new HashMap<>();
            for (EduClass clazz : classMapper.selectList(new LambdaQueryWrapper<EduClass>()
                .in(EduClass::getGradeId, gradeIds))) {
                if (clazz.getClassId() == null || clazz.getGradeId() == null) {
                    continue;
                }
                classToGrade.put(clazz.getClassId(), clazz.getGradeId());
                classCounts.merge(clazz.getGradeId(), 1, Integer::sum);
            }
            if (!classToGrade.isEmpty()) {
                for (EduClassMember member : classMemberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
                    .in(EduClassMember::getClassId, classToGrade.keySet())
                    .eq(EduClassMember::getStatus, "1"))) {
                    Long gradeId = member.getClassId() == null ? null : classToGrade.get(member.getClassId());
                    if (gradeId != null) {
                        studentCounts.merge(gradeId, 1, Integer::sum);
                    }
                }
            }
        }
        List<Long> schoolIds = rows.stream().map(EduGradeVo::getSchoolId)
            .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> schoolNames = new HashMap<>();
        if (!schoolIds.isEmpty()) {
            for (EduSchool school : schoolMapper.selectList(new LambdaQueryWrapper<EduSchool>()
                .in(EduSchool::getSchoolId, schoolIds))) {
                schoolNames.put(school.getSchoolId(), school.getSchoolName());
            }
        }
        for (EduGradeVo row : rows) {
            row.setClassCount(classCounts.getOrDefault(row.getGradeId(), 0));
            row.setStudentCount(studentCounts.getOrDefault(row.getGradeId(), 0));
            row.setSchoolName(row.getSchoolId() == null ? null : schoolNames.get(row.getSchoolId()));
        }
    }

    @Override
    public EduGradeVo queryById(Long gradeId) {
        EduGradeVo vo = baseMapper.selectVoById(gradeId);
        if (vo == null) {
            throw new ServiceException("年级不存在或不在当前数据范围内");
        }
        fillListCounts(List.of(vo));
        return vo;
    }

    @Override
    public Boolean insertByBo(EduGradeBo grade) {
        validateSequence(grade);
        EduGrade add = new EduGrade();
        copyWritableFields(grade, add);
        add.setClassCount(0);
        add.setStudentCount(0);
        add.setGradeStatus(StringUtils.isBlank(grade.getGradeStatus()) ? GRADE_NORMAL : grade.getGradeStatus());
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(EduGradeBo grade) {
        EduGrade update = requireGrade(grade.getGradeId());
        // 学段与学段内序号一经创建不可修改（REQ-GRD-018）；改名不允许跨学段（BR-GRADE-006）
        if (StringUtils.isNotBlank(grade.getStageCode()) && !grade.getStageCode().equals(update.getStageCode())) {
            throw new ServiceException("年级不允许跨学段改名（BR-GRADE-006）");
        }
        if (StringUtils.isNotBlank(grade.getGradeName())) {
            update.setGradeName(grade.getGradeName());
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddGrade(EduGradeBo grade) {
        List<EduGradeBo> rows = grade.getGradeList();
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("批量新增至少需要一行年级");
        }
        for (EduGradeBo row : rows) {
            if (row.getSchoolId() == null) {
                row.setSchoolId(grade.getSchoolId());
            }
            insertByBo(row);
        }
        return true;
    }

    @Override
    public Boolean removeGrade(Long gradeId, String reason) {
        EduGrade grade = requireGrade(gradeId);
        Long classCount = classMapper.selectCount(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getGradeId, gradeId));
        if (classCount != null && classCount > 0) {
            throw new ServiceException("该年级下仍有班级，不允许删除，请改用归档（BR-GRADE-004）");
        }
        return baseMapper.deleteById(grade.getGradeId()) > 0;
    }

    @Override
    public Boolean archiveGrade(Long gradeId, String reason) {
        EduGrade update = requireGrade(gradeId);
        if (GRADE_ARCHIVED.equals(update.getGradeStatus())) {
            throw new ServiceException("该年级已经归档");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("归档原因至少 5 个字");
        }
        update.setGradeStatus(GRADE_ARCHIVED);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public List<EduGradeLeaderVo> listLeader(Long gradeId, Long termId) {
        return leaderMapper.selectVoList(new LambdaQueryWrapper<EduGradeLeader>()
            .eq(gradeId != null, EduGradeLeader::getGradeId, gradeId)
            .eq(termId != null, EduGradeLeader::getTermId, termId)
            .orderByDesc(EduGradeLeader::getIsPrimary));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveLeader(EduGradeBo grade) {
        requireGrade(grade.getGradeId());
        if (grade.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        if (grade.getUserId() == null) {
            throw new ServiceException("请选择年级主任账号");
        }
        // 同一学年学期同一人只允许一条任职（uk_grade_leader = term_id + grade_id + user_id）
        EduGradeLeader exist = leaderMapper.selectOne(new LambdaQueryWrapper<EduGradeLeader>()
            .eq(EduGradeLeader::getTermId, grade.getTermId())
            .eq(EduGradeLeader::getGradeId, grade.getGradeId())
            .eq(EduGradeLeader::getUserId, grade.getUserId()));
        if (exist != null) {
            exist.setStatus(LEADER_ON);
            exist.setIsPrimary(StringUtils.isBlank(grade.getIsPrimary()) ? "0" : grade.getIsPrimary());
            exist.setTeacherId(grade.getTeacherId());
            return leaderMapper.updateById(exist) > 0;
        }
        EduGradeLeader add = new EduGradeLeader();
        add.setSchoolId(grade.getSchoolId());
        add.setGradeId(grade.getGradeId());
        add.setTermId(grade.getTermId());
        add.setUserId(grade.getUserId());
        add.setTeacherId(grade.getTeacherId());
        add.setIsPrimary(StringUtils.isBlank(grade.getIsPrimary()) ? "0" : grade.getIsPrimary());
        add.setStatus(LEADER_ON);
        return leaderMapper.insert(add) > 0;
    }

    @Override
    public Boolean removeLeader(Long leaderId) {
        EduGradeLeader leader = leaderMapper.selectById(leaderId);
        if (leader == null) {
            throw new ServiceException("任职记录不存在");
        }
        leader.setStatus(LEADER_OFF);
        return leaderMapper.updateById(leader) > 0;
    }

    @Override
    public List<EduGradeVo> promotionView(Long schoolId, Long termId) {
        // 只读视图：按学段内序号倒序给出「学段内序号 +1」的参考，不写任何数据（REQ-GRD-032）
        return baseMapper.selectVoList(new LambdaQueryWrapper<EduGrade>()
            .eq(schoolId != null, EduGrade::getSchoolId, schoolId)
            .eq(EduGrade::getGradeStatus, GRADE_NORMAL)
            .orderByAsc(EduGrade::getStageCode)
            .orderByAsc(EduGrade::getGradeLevel));
    }

    /** 取年级并做存在性校验 */
    private EduGrade requireGrade(Long gradeId) {
        if (gradeId == null) {
            throw new ServiceException("缺少年级 ID");
        }
        EduGrade grade = baseMapper.selectById(gradeId);
        if (grade == null) {
            throw new ServiceException("年级不存在或不在当前数据范围内");
        }
        return grade;
    }

    /** 学段与学段内序号必填校验（RV-GRD-03） */
    private void validateSequence(EduGradeBo grade) {
        if (StringUtils.isBlank(grade.getStageCode())) {
            throw new ServiceException("请选择学段");
        }
        if (grade.getEnrollYear() == null) {
            throw new ServiceException("请填写入学年份");
        }
        if (grade.getGradeLevel() == null) {
            throw new ServiceException("请填写学段内序号");
        }
    }

    /** 只拷贝可写列（主键、状态与审计列由框架维护） */
    private void copyWritableFields(EduGradeBo bo, EduGrade entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setStageCode(bo.getStageCode());
        entity.setEnrollYear(bo.getEnrollYear());
        entity.setGradeLevel(bo.getGradeLevel());
        entity.setGradeName(bo.getGradeName());
    }

    /**
     * 组装查询条件。
     *
     * 数据范围条件由 DataScopeResolver 在阶段 7 后续批次注入（09-permission-architecture.md）；
     * 此处只拼业务筛选条件，避免先取数再判断（DS-DENY-07）。
     */
    private LambdaQueryWrapper<EduGrade> buildQueryWrapper(EduGradeBo bo) {
        LambdaQueryWrapper<EduGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(bo.getSchoolId() != null, EduGrade::getSchoolId, bo.getSchoolId());
        wrapper.eq(StringUtils.isNotBlank(bo.getStageCode()), EduGrade::getStageCode, bo.getStageCode());
        wrapper.eq(bo.getEnrollYear() != null, EduGrade::getEnrollYear, bo.getEnrollYear());
        wrapper.eq(StringUtils.isNotBlank(bo.getGradeStatus()), EduGrade::getGradeStatus, bo.getGradeStatus());
        wrapper.like(StringUtils.isNotBlank(bo.getKeyword()), EduGrade::getGradeName, bo.getKeyword());
        wrapper.orderByAsc(EduGrade::getStageCode).orderByAsc(EduGrade::getGradeLevel);
        return wrapper;
    }

}
