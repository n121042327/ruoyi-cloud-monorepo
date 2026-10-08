package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduSubjectBo;
import org.dromara.edu.domain.bo.EduSubjectStageBo;
import org.dromara.edu.domain.vo.EduSubjectOptionVo;
import org.dromara.edu.domain.vo.EduSubjectVo;
import org.dromara.edu.domain.vo.SubjectReferenceVo;

import java.util.List;

/**
 * 学科服务层
 *
 * 覆盖 subject 模块全部 12 个 operationId：listSubject / getSubject / removeSubject /
 * addSubject / updateSubject / batchInitSubject / saveSubjectStreamRole / saveSubjectStage /
 * disableSubject / enableSubject / checkSubjectReference / listSubjectOption。
 *
 * @author Codex
 */
public interface IEduSubjectService {

    /** 分页查询学科列表（含各学科已启用学段） */
    TableDataInfo<EduSubjectVo> queryPageList(EduSubjectBo subject, PageQuery pageQuery);

    /** 查询学科详情 */
    EduSubjectVo queryById(Long subjectId);

    /** 新增学科（编码校内唯一，BR-SUBJECT-001；可一并启用在指定学段） */
    Boolean insertByBo(EduSubjectBo subject);

    /** 修改学科（编码不可改，改编码走停用后重建） */
    Boolean updateByBo(EduSubjectBo subject);

    /** 删除学科（有引用时只允许停用：任教关系 / 教学班 / 学生选科） */
    Boolean removeSubject(Long subjectId, String reason);

    /** 批量初始化标准学科模板（幂等：已存在的编码跳过） */
    Boolean batchInitSubject(EduSubjectBo subject);

    /** 设置学科的选科角色（是否参与 3+1+2 与首选 / 再选） */
    Boolean saveStreamRole(Long subjectId, EduSubjectBo subject);

    /** 设置学科启用的学段（未开设的学段不允许启用，REQ-SUB-026） */
    Boolean saveSubjectStage(Long subjectId, EduSubjectStageBo stage);

    /** 停用学科 */
    Boolean disableSubject(Long subjectId, String reason);

    /** 启用学科 */
    Boolean enableSubject(Long subjectId, String reason);

    /** 引用检查（任教关系 / 教学班 / 学生选科） */
    SubjectReferenceVo checkReference(Long subjectId);

    /** 学科下拉选项（按学段过滤，只返回启用中的学科） */
    List<EduSubjectOptionVo> listSubjectOption(String stageCode);

}
