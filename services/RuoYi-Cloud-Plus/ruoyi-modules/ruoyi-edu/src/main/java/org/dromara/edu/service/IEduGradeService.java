package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduGradeBo;
import org.dromara.edu.domain.vo.EduGradeLeaderVo;
import org.dromara.edu.domain.vo.EduGradeVo;

import java.util.List;

/**
 * 年级服务层
 *
 * 覆盖 06-api-catalog.md 的 grade 模块：listGrade / getGrade / addGrade / updateGrade /
 * batchAddGrade / removeGrade / archiveGrade / listGradeLeader / saveGradeLeader /
 * removeGradeLeader / getGradePromotionView。
 *
 * exportGrade 需要导入导出引擎，放到阶段 7 后续批次。
 *
 * @author Codex
 */
public interface IEduGradeService {

    /** 分页查询年级列表 */
    TableDataInfo<EduGradeVo> queryPageList(EduGradeBo grade, PageQuery pageQuery);

    /** 查询年级详情 */
    EduGradeVo queryById(Long gradeId);

    /** 新增年级（学段与学段内序号一经创建不可修改，REQ-GRD-018） */
    Boolean insertByBo(EduGradeBo grade);

    /** 修改年级（不允许跨学段改名，BR-GRADE-006） */
    Boolean updateByBo(EduGradeBo grade);

    /** 批量新增年级 */
    Boolean batchAddGrade(EduGradeBo grade);

    /** 删除年级（有班级或学生关系时不允许删除，BR-GRADE-004） */
    Boolean removeGrade(Long gradeId, String reason);

    /** 归档年级（有班级或学生关系时只允许归档） */
    Boolean archiveGrade(Long gradeId, String reason);

    /** 查询年级主任任职 */
    List<EduGradeLeaderVo> listLeader(Long gradeId, Long termId);

    /** 保存 / 变更年级主任任职（DS-05 的写入入口） */
    Boolean saveLeader(EduGradeBo grade);

    /** 年级主任离任（置 status=0，不物理删除） */
    Boolean removeLeader(Long leaderId);

    /**
     * 年级升班只读视图
     *
     * 升班的唯一执行入口是升班模块，年级管理只提供「学段内序号 +1」的只读视图
     * （REQ-GRD-032 / AGENTS 第 7 节模块边界）。
     */
    List<EduGradeVo> promotionView(Long schoolId, Long termId);

}
