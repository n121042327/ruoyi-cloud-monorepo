package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduTeachingClassBo;
import org.dromara.edu.domain.vo.EduTeachingClassMemberVo;
import org.dromara.edu.domain.vo.EduTeachingClassVo;

import java.util.List;

/**
 * 教学班服务层
 *
 * 覆盖 teaching-class 的 5 个 operationId：listTeachingClass / addTeachingClass /
 * getTeachingClass / disableTeachingClass / listTeachingClassRoster。
 *
 * 口径：教学班与行政班完全独立（BR-CLASS-001），不设班主任、不参与 DS-06；
 * 手工增删成员不开放，成员只能由「按组合生成」写入（REQ-STR-056）。
 *
 * @author Codex
 */
public interface IEduTeachingClassService {

    /** 分页查询教学班列表 */
    TableDataInfo<EduTeachingClassVo> queryPageList(EduTeachingClassBo teachingClass, PageQuery pageQuery);

    /** 查询教学班详情 */
    EduTeachingClassVo queryById(Long teachingClassId);

    /** 新增 / 幂等生成教学班（同一学期同一组合同一名称只建一次，REQ-STR-057） */
    EduTeachingClassVo addTeachingClass(EduTeachingClassBo teachingClass);

    /** 停用教学班（必填原因；历史成员保留） */
    Boolean disableTeachingClass(Long teachingClassId, String reason);

    /** 查询教学班成员清单 */
    List<EduTeachingClassMemberVo> listRoster(Long teachingClassId);

}
