package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduSchoolStage;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.EduSubjectStage;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.bo.EduSubjectBo;
import org.dromara.edu.domain.bo.EduSubjectStageBo;
import org.dromara.edu.domain.vo.EduSubjectOptionVo;
import org.dromara.edu.domain.vo.EduSubjectStageVo;
import org.dromara.edu.domain.vo.EduSubjectVo;
import org.dromara.edu.domain.vo.SubjectReferenceVo;
import org.dromara.edu.mapper.EduSchoolStageMapper;
import org.dromara.edu.mapper.EduSubjectMapper;
import org.dromara.edu.mapper.EduSubjectStageMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.service.IEduSubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学科服务层处理
 *
 * 口径要点：
 * - 「一条学科主体 + 学段启用表」（RV-SUB-04）：编码校内唯一（BR-SUBJECT-001），
 *   同一学科在不同学段启用只加启用记录、不拆多条主体；
 * - 未开设的学段（edu_school_stage.status=0）不允许启用学科（REQ-SUB-026 同口径）；
 * - 删除前检查三类引用（任教关系 / 教学班 / 学生选科），有引用时只允许停用；
 * - 排序号决定再选科目的展示顺序（REQ-STR-017）；选科角色 primary / secondary / none。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduSubjectServiceImpl implements IEduSubjectService {

    /** 学科状态 */
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";

    /** 启用标记 */
    private static final String FLAG_ON = "1";
    private static final String FLAG_OFF = "0";

    /** 标准学科模板：编码 / 名称 / 排序 / 选科角色（3+1+2 的固定集合，BR-STREAM-001 / 002） */
    private static final String[][] SUBJECT_TEMPLATE = {
        {"chinese", "语文", "10", "none"},
        {"math", "数学", "20", "none"},
        {"english", "外语", "30", "none"},
        {"physics", "物理", "40", "primary"},
        {"history", "历史", "50", "primary"},
        {"chemistry", "化学", "60", "secondary"},
        {"biology", "生物", "70", "secondary"},
        {"politics", "思想政治", "80", "secondary"},
        {"geography", "地理", "90", "secondary"},
    };

    private final EduSubjectMapper baseMapper;
    private final EduSubjectStageMapper subjectStageMapper;
    private final EduSchoolStageMapper schoolStageMapper;
    private final EduTeachingAssignmentMapper assignmentMapper;

    @Override
    public TableDataInfo<EduSubjectVo> queryPageList(EduSubjectBo subject, PageQuery pageQuery) {
        LambdaQueryWrapper<EduSubject> wrapper = new LambdaQueryWrapper<EduSubject>()
            .eq(subject.getSchoolId() != null, EduSubject::getSchoolId, subject.getSchoolId())
            .eq(StringUtils.isNotBlank(subject.getFilterStatus()), EduSubject::getSubjectStatus, subject.getFilterStatus())
            .eq(StringUtils.isNotBlank(subject.getFilterStreamRole()), EduSubject::getStreamRole, subject.getFilterStreamRole());
        if (StringUtils.isNotBlank(subject.getKeyword())) {
            wrapper.and(w -> w.like(EduSubject::getSubjectName, subject.getKeyword())
                .or().like(EduSubject::getSubjectCode, subject.getKeyword()));
        }
        if (StringUtils.isNotBlank(subject.getStageCode())) {
            wrapper.inSql(EduSubject::getSubjectId,
                "select subject_id from edu_subject_stage where status = '1' and stage_code = '" + subject.getStageCode() + "'");
        }
        wrapper.orderByAsc(EduSubject::getSortNo);
        Page<EduSubjectVo> result = baseMapper.selectPageSubjectList(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillStageCodes);
        return TableDataInfo.build(result);
    }

    @Override
    public EduSubjectVo queryById(Long subjectId) {
        EduSubjectVo vo = baseMapper.selectVoById(subjectId);
        if (vo == null) {
            throw new ServiceException("学科不存在或不在当前数据范围内");
        }
        fillStageCodes(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(EduSubjectBo subject) {
        validateSubjectCodeUnique(subject.getSchoolId(), subject.getSubjectCode(), null);
        EduSubject add = new EduSubject();
        copyWritableFields(subject, add);
        add.setSubjectStatus(StringUtils.isBlank(subject.getSubjectStatus()) ? STATUS_ACTIVE : subject.getSubjectStatus());
        if (add.getSortNo() == null) {
            add.setSortNo(0);
        }
        if (add.getStreamEnabled() == null) {
            add.setStreamEnabled(FLAG_OFF);
        }
        if (StringUtils.isBlank(add.getStreamRole())) {
            add.setStreamRole("none");
        }
        if (baseMapper.insert(add) <= 0) {
            return false;
        }
        if (subject.getStageCodes() != null && !subject.getStageCodes().isEmpty()) {
            EduSubjectStageBo stageBo = new EduSubjectStageBo();
            stageBo.setStageCodes(subject.getStageCodes());
            saveSubjectStage(add.getSubjectId(), stageBo);
        }
        return true;
    }

    @Override
    public Boolean updateByBo(EduSubjectBo subject) {
        EduSubject update = requireSubject(subject.getSubjectId());
        // 学科编码校内唯一（BR-SUBJECT-001）且不提供修改入口：改名与排序可变，编码不变
        update.setSubjectName(subject.getSubjectName());
        if (subject.getSortNo() != null) {
            update.setSortNo(subject.getSortNo());
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean removeSubject(Long subjectId, String reason) {
        EduSubject update = requireSubject(subjectId);
        SubjectReferenceVo reference = checkReference(subjectId);
        if (Boolean.TRUE.equals(reference.getReferenced())) {
            throw new ServiceException("该学科已被任教关系 / 教学班 / 学生选科引用，只允许停用，不允许删除");
        }
        return baseMapper.deleteById(update.getSubjectId()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchInitSubject(EduSubjectBo subject) {
        if (subject.getSchoolId() == null) {
            throw new ServiceException("请选择所属学校");
        }
        List<String> stages = subject.getInitStageCodes();
        for (String[] row : SUBJECT_TEMPLATE) {
            Long exist = baseMapper.selectCount(new LambdaQueryWrapper<EduSubject>()
                .eq(EduSubject::getSchoolId, subject.getSchoolId())
                .eq(EduSubject::getSubjectCode, row[0]));
            if (exist != null && exist > 0) {
                // 幂等：已存在的编码跳过，不覆盖已有配置
                continue;
            }
            EduSubject add = new EduSubject();
            add.setSchoolId(subject.getSchoolId());
            add.setSubjectCode(row[0]);
            add.setSubjectName(row[1]);
            add.setSortNo(Integer.valueOf(row[2]));
            add.setStreamRole(row[3]);
            add.setStreamEnabled("none".equals(row[3]) ? FLAG_OFF : FLAG_ON);
            add.setSubjectStatus(STATUS_ACTIVE);
            baseMapper.insert(add);
            if (stages != null && !stages.isEmpty()) {
                EduSubjectStageBo stageBo = new EduSubjectStageBo();
                stageBo.setStageCodes(stages);
                saveSubjectStage(add.getSubjectId(), stageBo);
            }
        }
        return true;
    }

    @Override
    public Boolean saveStreamRole(Long subjectId, EduSubjectBo subject) {
        EduSubject update = requireSubject(subjectId);
        if (StringUtils.isBlank(subject.getStreamRole())) {
            throw new ServiceException("请选择选科角色");
        }
        if (!List.of("primary", "secondary", "none").contains(subject.getStreamRole())) {
            throw new ServiceException("选科角色只能是 primary / secondary / none");
        }
        update.setStreamRole(subject.getStreamRole());
        update.setStreamEnabled("none".equals(subject.getStreamRole()) ? FLAG_OFF : FLAG_ON);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSubjectStage(Long subjectId, EduSubjectStageBo stage) {
        EduSubject subject = requireSubject(subjectId);
        List<String> codes = stage.getStageCodes();
        if (codes == null || codes.isEmpty()) {
            if (StringUtils.isNotBlank(stage.getStageCode())) {
                codes = List.of(stage.getStageCode());
            } else {
                throw new ServiceException("请至少选择一个学段");
            }
        }
        for (String code : codes) {
            // 未开设的学段不允许启用（REQ-SUB-026）
            Long opened = schoolStageMapper.selectCount(new LambdaQueryWrapper<EduSchoolStage>()
                .eq(EduSchoolStage::getSchoolId, subject.getSchoolId())
                .eq(EduSchoolStage::getStageCode, code)
                .eq(EduSchoolStage::getStatus, FLAG_ON));
            if (opened == null || opened == 0) {
                throw new ServiceException("学校尚未开设学段「" + code + "」，不能在学科里启用（REQ-SUB-026）");
            }
            EduSubjectStage exist = subjectStageMapper.selectOne(new LambdaQueryWrapper<EduSubjectStage>()
                .eq(EduSubjectStage::getSubjectId, subjectId)
                .eq(EduSubjectStage::getStageCode, code));
            if (exist != null) {
                exist.setStatus(FLAG_ON);
                subjectStageMapper.updateById(exist);
                continue;
            }
            EduSubjectStage add = new EduSubjectStage();
            add.setSchoolId(subject.getSchoolId());
            add.setSubjectId(subjectId);
            add.setStageCode(code);
            add.setStatus(FLAG_ON);
            subjectStageMapper.insert(add);
        }
        return true;
    }

    @Override
    public Boolean disableSubject(Long subjectId, String reason) {
        EduSubject update = requireSubject(subjectId);
        if (STATUS_DISABLED.equals(update.getSubjectStatus())) {
            throw new ServiceException("该学科已经是停用状态");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("停用原因至少 5 个字");
        }
        update.setSubjectStatus(STATUS_DISABLED);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean enableSubject(Long subjectId, String reason) {
        EduSubject update = requireSubject(subjectId);
        if (STATUS_ACTIVE.equals(update.getSubjectStatus())) {
            throw new ServiceException("该学科已经是正常状态");
        }
        update.setSubjectStatus(STATUS_ACTIVE);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public SubjectReferenceVo checkReference(Long subjectId) {
        requireSubject(subjectId);
        SubjectReferenceVo vo = new SubjectReferenceVo();
        vo.setTeachingAssignmentCount(assignmentMapper.selectCount(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getSubjectId, subjectId)));
        // 教学班引用与学生选科人数：分别在教学班模块、选科模块交付后填充
        vo.setTeachingClassCount(null);
        vo.setStudentStreamCount(null);
        vo.setReferenced(vo.getTeachingAssignmentCount() != null && vo.getTeachingAssignmentCount() > 0);
        return vo;
    }

    @Override
    public List<EduSubjectOptionVo> listSubjectOption(String stageCode) {
        List<EduSubject> subjects = baseMapper.selectList(new LambdaQueryWrapper<EduSubject>()
            .eq(EduSubject::getSubjectStatus, STATUS_ACTIVE)
            .inSql(StringUtils.isNotBlank(stageCode), EduSubject::getSubjectId,
                "select subject_id from edu_subject_stage where status = '1' and stage_code = '" + stageCode + "'")
            .orderByAsc(EduSubject::getSortNo));
        List<EduSubjectOptionVo> options = new ArrayList<>();
        for (EduSubject subject : subjects) {
            EduSubjectOptionVo vo = new EduSubjectOptionVo();
            vo.setSubjectId(subject.getSubjectId());
            vo.setSubjectCode(subject.getSubjectCode());
            vo.setSubjectName(subject.getSubjectName());
            vo.setStreamRole(subject.getStreamRole());
            vo.setStageCodes(subjectStageMapper.selectVoList(new LambdaQueryWrapper<EduSubjectStage>()
                    .eq(EduSubjectStage::getSubjectId, subject.getSubjectId())
                    .eq(EduSubjectStage::getStatus, FLAG_ON))
                .stream().map(EduSubjectStageVo::getStageCode).collect(Collectors.toList()));
            options.add(vo);
        }
        return options;
    }

    // ==================== 内部方法 ====================

    private void fillStageCodes(EduSubjectVo vo) {
        vo.setStageCodes(subjectStageMapper.selectVoList(new LambdaQueryWrapper<EduSubjectStage>()
                .eq(EduSubjectStage::getSubjectId, vo.getSubjectId())
                .eq(EduSubjectStage::getStatus, FLAG_ON))
            .stream().map(EduSubjectStageVo::getStageCode).collect(Collectors.toList()));
    }

    private void validateSubjectCodeUnique(Long schoolId, String subjectCode, Long excludeId) {
        if (StringUtils.isBlank(subjectCode)) {
            throw new ServiceException("学科编码不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<EduSubject>()
            .eq(schoolId != null, EduSubject::getSchoolId, schoolId)
            .eq(EduSubject::getSubjectCode, subjectCode)
            .ne(excludeId != null, EduSubject::getSubjectId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException("学科编码在该学校内已存在：" + subjectCode);
        }
    }

    private void copyWritableFields(EduSubjectBo bo, EduSubject entity) {
        entity.setSchoolId(bo.getSchoolId());
        entity.setSubjectCode(bo.getSubjectCode());
        entity.setSubjectName(bo.getSubjectName());
        entity.setSortNo(bo.getSortNo());
        entity.setStreamEnabled(bo.getStreamEnabled());
        entity.setStreamRole(bo.getStreamRole());
    }

    private EduSubject requireSubject(Long subjectId) {
        if (subjectId == null) {
            throw new ServiceException("缺少学科 ID");
        }
        EduSubject subject = baseMapper.selectById(subjectId);
        if (subject == null) {
            throw new ServiceException("学科不存在或不在当前数据范围内");
        }
        return subject;
    }

}
