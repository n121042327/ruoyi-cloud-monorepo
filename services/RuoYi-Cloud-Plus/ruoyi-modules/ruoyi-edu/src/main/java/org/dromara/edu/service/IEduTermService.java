package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduAcademicYearBo;
import org.dromara.edu.domain.bo.EduTermBo;
import org.dromara.edu.domain.vo.EduAcademicYearVo;
import org.dromara.edu.domain.vo.EduTermVo;
import org.dromara.edu.domain.vo.TermReferenceVo;

/**
 * 学年学期服务层
 *
 * 覆盖 06-api-catalog.md 的 term 模块全部 12 个 operationId：
 * listAcademicYear / getAcademicYear / addAcademicYear / updateAcademicYear /
 * archiveAcademicYear / revokeArchiveAcademicYear / listTerm / saveTerm / removeTerm /
 * getCurrentTerm / setCurrentTerm / checkTermReference。
 *
 * @author Codex
 */
public interface IEduTermService {

    /** 分页查询学年列表 */
    TableDataInfo<EduAcademicYearVo> queryYearPageList(EduAcademicYearBo year, PageQuery pageQuery);

    /** 查询学年详情 */
    EduAcademicYearVo queryYearById(Long academicYearId);

    /** 新建学年（编码 YYYY-YYYY 且连续两年；日期不重叠，BR-TERM-003 / RV-TERM-08） */
    Boolean insertYear(EduAcademicYearBo year);

    /** 编辑学年 */
    Boolean updateYear(EduAcademicYearBo year);

    /** 归档学年（有引用时只允许归档，不允许删除） */
    Boolean archiveYear(Long academicYearId, String reason);

    /** 撤销归档（误操作纠正，写审计） */
    Boolean revokeArchiveYear(Long academicYearId, String reason);

    /** 分页查询学期列表 */
    TableDataInfo<EduTermVo> queryTermPageList(EduTermBo term, PageQuery pageQuery);

    /** 学期详情（新建 / 编辑学期共用；有 termId 为编辑） */
    Boolean saveTerm(EduTermBo term);

    /** 删除学期（已被班级、任教关系或花名册引用的学期不允许删除，REQ-TERM-019） */
    Boolean removeTerm(Long termId, String reason);

    /** 查询当前学年学期（各模块默认学期上下文的权威来源） */
    EduTermVo getCurrentTerm(Long schoolId);

    /** 设为当前学年学期（同一学校唯一；已结束 / 已归档的学年不得设为当前） */
    Boolean setCurrentTerm(Long termId);

    /** 引用检查（四类引用：班级 / 任教关系 / 学生班级关系 / 学生选科，REQ-TERM-029 / 034） */
    TermReferenceVo checkReference(Long academicYearId);

}
