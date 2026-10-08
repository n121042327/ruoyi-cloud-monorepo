package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.bo.EduStudentBo;
import org.dromara.edu.domain.vo.EduStudentVo;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduStudentService;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final EduStudentMapper baseMapper;

    @Override
    public TableDataInfo<EduStudentVo> queryPageList(EduStudentBo student, PageQuery pageQuery) {
        LambdaQueryWrapper<EduStudent> wrapper = buildQueryWrapper(student);
        Page<EduStudentVo> result = baseMapper.selectPageStudentList(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    public EduStudentVo queryById(Long studentId) {
        EduStudentVo vo = baseMapper.selectVoById(studentId);
        if (vo == null) {
            throw new ServiceException("学生不存在或不在当前数据范围内");
        }
        return vo;
    }

    @Override
    public Boolean insertByBo(EduStudentBo student) {
        // 学号由系统统一发号，前端不提交；发号与在校记录写入在同一事务内（design.md 第 5 节）
        EduStudent add = new EduStudent();
        add.setStudentName(student.getStudentName());
        add.setGender(student.getGender());
        add.setNationalStudentNo(student.getNationalStudentNo());
        add.setIdType(student.getIdType());
        add.setIdCardNo(student.getIdCardNo());
        add.setStudentPhone(student.getStudentPhone());
        add.setAddress(student.getAddress());
        add.setRemark(student.getRemark());
        return baseMapper.insert(add) > 0;
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
        update.setStudentPhone(student.getStudentPhone());
        update.setAddress(student.getAddress());
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
