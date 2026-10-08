package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduCampusBo;
import org.dromara.edu.domain.bo.EduSchoolBo;
import org.dromara.edu.domain.bo.EduSchoolStageBo;
import org.dromara.edu.domain.vo.EduCampusVo;
import org.dromara.edu.domain.vo.EduSchoolStageVo;
import org.dromara.edu.domain.vo.EduSchoolVo;
import org.dromara.edu.domain.vo.SchoolSummaryVo;

import java.util.List;

/**
 * 学校与校区服务层
 *
 * 覆盖 school 模块 14 / 16 个 operationId：listSchool / getSchool / getCurrentSchool /
 * addSchool / updateSchool / updateSchoolCode / disableSchool / enableSchool /
 * listCampus / saveCampus / removeCampus / listSchoolStage / saveSchoolStage /
 * getSchoolSummary / initSchoolBaseline。
 *
 * exportSchool 需要导入导出引擎，放到后续批次。
 *
 * @author Codex
 */
public interface IEduSchoolService {

    /** 分页查询学校列表 */
    TableDataInfo<EduSchoolVo> queryPageList(EduSchoolBo school, PageQuery pageQuery);

    /** 查询学校详情 */
    EduSchoolVo queryById(Long schoolId);

    /** 查询当前租户对应的学校（学校用户固定本校） */
    EduSchoolVo getCurrentSchool();

    /** 新增学校（学校与租户一一对应，BR-ORG-002） */
    Boolean insertByBo(EduSchoolBo school);

    /** 修改学校 */
    Boolean updateByBo(EduSchoolBo school);

    /** 修改学校编码（父租户内唯一，BR-ORG-011） */
    Boolean updateSchoolCode(Long schoolId, String schoolCode, String reason);

    /** 停用学校（学校与租户的绑定关系不可解除，只能停用） */
    Boolean disableSchool(Long schoolId, String reason);

    /** 启用学校 */
    Boolean enableSchool(Long schoolId, String reason);

    /** 查询校区列表 */
    TableDataInfo<EduCampusVo> queryCampusPageList(Long schoolId, EduCampusBo campus, PageQuery pageQuery);

    /** 新增 / 编辑校区 */
    Boolean saveCampus(EduCampusBo campus);

    /** 停用校区（写 campusStatus=disabled 并留原因） */
    Boolean removeCampus(Long campusId, String reason);

    /** 查询学校开设学段 */
    List<EduSchoolStageVo> listSchoolStage(Long schoolId);

    /** 保存学校开设学段（批量；未开设的学段在学科与年级配置里置灰，REQ-SUB-026） */
    Boolean saveSchoolStage(Long schoolId, EduSchoolStageBo stage);

    /** 学校数据摘要（详情页与初始化结果摘要） */
    SchoolSummaryVo getSchoolSummary(Long schoolId);

    /**
     * 开通初始化（幂等）
     *
     * 一次性完成：学校开设学段 + 学年与默认学期 + 可选学科模板与基础角色（REQ-SCH-019 / 045）。
     * 重复执行不产生重复数据。
     */
    SchoolSummaryVo initSchoolBaseline(EduSchoolBo school);

}
