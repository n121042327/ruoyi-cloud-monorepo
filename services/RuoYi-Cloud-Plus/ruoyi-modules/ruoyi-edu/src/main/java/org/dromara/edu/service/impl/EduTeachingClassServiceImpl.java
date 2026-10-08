package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduTeachingClass;
import org.dromara.edu.domain.EduTeachingClassMember;
import org.dromara.edu.domain.bo.EduTeachingClassBo;
import org.dromara.edu.domain.vo.EduTeachingClassMemberVo;
import org.dromara.edu.domain.vo.EduTeachingClassVo;
import org.dromara.edu.mapper.EduTeachingClassMapper;
import org.dromara.edu.mapper.EduTeachingClassMemberMapper;
import org.dromara.edu.service.IEduTeachingClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 教学班服务层处理
 *
 * 口径：教学班与行政班完全独立（BR-CLASS-001），不设班主任、不参与 DS-06 数据范围；
 * 唯一键 uk_teaching_class = (school_id, term_id, combination, class_name) 是「幂等生成」的前提
 * （同一学期同一组合同一名称只建一次，REQ-STR-056 / 057）；手工增删成员不开放，
 * 成员只能由「按组合生成」或本服务在生成时批量写入。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduTeachingClassServiceImpl implements IEduTeachingClassService {

    /** 教学班状态 */
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";

    /** 成员在班 / 已离开 */
    private static final String MEMBER_IN = "1";

    private final EduTeachingClassMapper baseMapper;
    private final EduTeachingClassMemberMapper memberMapper;

    @Override
    public TableDataInfo<EduTeachingClassVo> queryPageList(EduTeachingClassBo teachingClass, PageQuery pageQuery) {
        LambdaQueryWrapper<EduTeachingClass> wrapper = new LambdaQueryWrapper<EduTeachingClass>()
            .eq(teachingClass.getSchoolId() != null, EduTeachingClass::getSchoolId, teachingClass.getSchoolId())
            .eq(teachingClass.getTermId() != null, EduTeachingClass::getTermId, teachingClass.getTermId())
            .eq(teachingClass.getGradeId() != null, EduTeachingClass::getGradeId, teachingClass.getGradeId())
            .eq(StringUtils.isNotBlank(teachingClass.getTeachingClassStatus()),
                EduTeachingClass::getTeachingClassStatus, teachingClass.getTeachingClassStatus())
            .like(StringUtils.isNotBlank(teachingClass.getFilterCombination()),
                EduTeachingClass::getCombination, teachingClass.getFilterCombination());
        if (StringUtils.isNotBlank(teachingClass.getKeyword())) {
            wrapper.and(w -> w.like(EduTeachingClass::getClassName, teachingClass.getKeyword())
                .or().like(EduTeachingClass::getCombination, teachingClass.getKeyword()));
        }
        wrapper.orderByAsc(EduTeachingClass::getClassName);
        Page<EduTeachingClassVo> result = baseMapper.selectPageTeachingClass(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillDisplayFields);
        return TableDataInfo.build(result);
    }

    @Override
    public EduTeachingClassVo queryById(Long teachingClassId) {
        EduTeachingClass entity = requireClass(teachingClassId);
        EduTeachingClassVo vo = baseMapper.selectVoById(entity.getTeachingClassId());
        fillDisplayFields(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduTeachingClassVo addTeachingClass(EduTeachingClassBo teachingClass) {
        if (teachingClass.getTermId() == null) {
            throw new ServiceException("请选择学年学期");
        }
        // 幂等：同一学期同一组合同一名称只建一次（uk_teaching_class）
        EduTeachingClass exist = baseMapper.selectOne(new LambdaQueryWrapper<EduTeachingClass>()
            .eq(teachingClass.getSchoolId() != null, EduTeachingClass::getSchoolId, teachingClass.getSchoolId())
            .eq(EduTeachingClass::getTermId, teachingClass.getTermId())
            .eq(EduTeachingClass::getCombination, teachingClass.getCombination())
            .eq(EduTeachingClass::getClassName, teachingClass.getClassName()));
        EduTeachingClass entity = exist;
        if (entity == null) {
            entity = new EduTeachingClass();
            entity.setSchoolId(teachingClass.getSchoolId());
            entity.setTermId(teachingClass.getTermId());
            entity.setGradeId(teachingClass.getGradeId());
            entity.setClassName(teachingClass.getClassName());
            entity.setCombination(teachingClass.getCombination());
            entity.setMemberCount(0);
            entity.setTeachingClassStatus(STATUS_ACTIVE);
            baseMapper.insert(entity);
        }
        // 生成时批量写入成员（唯一键 uk_tclass_member 保证幂等）
        if (teachingClass.getStudentIds() != null && !teachingClass.getStudentIds().isEmpty()) {
            for (Long studentId : teachingClass.getStudentIds()) {
                EduTeachingClassMember current = memberMapper.selectOne(new LambdaQueryWrapper<EduTeachingClassMember>()
                    .eq(EduTeachingClassMember::getTeachingClassId, entity.getTeachingClassId())
                    .eq(EduTeachingClassMember::getStudentId, studentId));
                if (current != null) {
                    current.setStatus(MEMBER_IN);
                    current.setLeaveDate(null);
                    memberMapper.updateById(current);
                    continue;
                }
                EduTeachingClassMember member = new EduTeachingClassMember();
                member.setSchoolId(entity.getSchoolId());
                member.setTeachingClassId(entity.getTeachingClassId());
                member.setTermId(entity.getTermId());
                member.setStudentId(studentId);
                member.setSource(StringUtils.isBlank(teachingClass.getGenerateTaskNo()) ? "manual" : "generate");
                member.setGenerateTaskNo(teachingClass.getGenerateTaskNo());
                member.setJoinDate(new Date());
                member.setStatus(MEMBER_IN);
                memberMapper.insert(member);
            }
            refreshMemberCount(entity.getTeachingClassId());
        }
        EduTeachingClassVo vo = baseMapper.selectVoById(entity.getTeachingClassId());
        fillDisplayFields(vo);
        return vo;
    }

    @Override
    public Boolean disableTeachingClass(Long teachingClassId, String reason) {
        EduTeachingClass update = requireClass(teachingClassId);
        if (STATUS_DISABLED.equals(update.getTeachingClassStatus())) {
            throw new ServiceException("该教学班已经是停用状态");
        }
        if (StringUtils.isBlank(reason) || reason.trim().length() < 5) {
            throw new ServiceException("停用原因至少 5 个字");
        }
        update.setTeachingClassStatus(STATUS_DISABLED);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public List<EduTeachingClassMemberVo> listRoster(Long teachingClassId) {
        requireClass(teachingClassId);
        return memberMapper.selectVoList(new LambdaQueryWrapper<EduTeachingClassMember>()
            .eq(EduTeachingClassMember::getTeachingClassId, teachingClassId)
            .eq(EduTeachingClassMember::getStatus, MEMBER_IN)
            .orderByAsc(EduTeachingClassMember::getStudentId));
    }

    // ==================== 内部方法 ====================

    /** 刷新成员数（冗余统计，写入时维护） */
    private void refreshMemberCount(Long teachingClassId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<EduTeachingClassMember>()
            .eq(EduTeachingClassMember::getTeachingClassId, teachingClassId)
            .eq(EduTeachingClassMember::getStatus, MEMBER_IN));
        EduTeachingClass update = baseMapper.selectById(teachingClassId);
        if (update != null) {
            update.setMemberCount(count == null ? 0 : count.intValue());
            baseMapper.updateById(update);
        }
    }

    /** 前端字段映射：classId / subjectCombination / status */
    private void fillDisplayFields(EduTeachingClassVo vo) {
        if (vo == null) {
            return;
        }
        vo.setClassId(vo.getTeachingClassId());
        vo.setSubjectCombination(vo.getCombination());
        vo.setStatus(vo.getTeachingClassStatus());
    }

    private EduTeachingClass requireClass(Long teachingClassId) {
        if (teachingClassId == null) {
            throw new ServiceException("缺少教学班 ID");
        }
        EduTeachingClass entity = baseMapper.selectById(teachingClassId);
        if (entity == null) {
            throw new ServiceException("教学班不存在或不在当前数据范围内");
        }
        return entity;
    }

}
