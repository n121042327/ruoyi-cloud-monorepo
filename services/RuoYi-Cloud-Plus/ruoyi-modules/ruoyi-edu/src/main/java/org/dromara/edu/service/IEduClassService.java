package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduClassBo;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.domain.vo.EduClassMemberVo;
import org.dromara.edu.domain.vo.EduClassVo;

import java.util.List;

/**
 * 班级服务层
 *
 * 覆盖 06-api-catalog.md 的 class 模块：listClass / getClass / addClass / updateClass /
 * batchAddClass / disableClass / mergeClass / assignHeadTeacher / listClassRoster /
 * addClassRoster / removeClassRoster / transferClass。
 *
 * 导入导出（importRosterValidate / importRosterExecute / exportClass / exportClassRoster）
 * 与教学班任教关系（listClassTeachingAssignment）在阶段 7 后续批次接入导入导出引擎后补齐。
 *
 * @author Codex
 */
public interface IEduClassService {

    /** 分页查询班级列表 */
    TableDataInfo<EduClassVo> queryPageList(EduClassBo clazz, PageQuery pageQuery);

    /** 查询班级详情 */
    EduClassVo queryById(Long classId);

    /** 新增班级（行政班必填年级；教学班创建入口唯一在生成向导，CR-017） */
    Boolean insertByBo(EduClassBo clazz);

    /** 修改班级（学年学期与年级一经创建不可修改，REQ-CLS-022） */
    Boolean updateByBo(EduClassBo clazz);

    /** 批量新增班级 */
    Boolean batchAddClass(EduClassBo clazz);

    /** 停用班级（有在读学生不允许删除、只允许停用，BR-CLASS-006） */
    Boolean disableClass(Long classId, String reason);

    /** 班级合并（源班级并入目标班级，源班级置停用） */
    Boolean mergeClass(EduClassBo clazz);

    /** 指定 / 变更班主任（唯一写入入口在班级管理，DP-01） */
    Boolean assignHeadTeacher(EduClassBo clazz);

    /** 查询花名册 */
    TableDataInfo<EduClassMemberVo> queryRoster(Long classId, EduClassMemberBo member, PageQuery pageQuery);

    /** 添加学生到行政班（学生班级归属的唯一写入入口，DP-01） */
    Boolean addRoster(EduClassMemberBo member);

    /** 移出学生（追加式结束关系，不物理删除） */
    Boolean removeRoster(Long classId, Long studentId, String reason);

    /** 调班 / 批量迁学生（单条 = 调班，多条 = 批量迁移，D-067） */
    Boolean transferClass(EduClassMemberBo member);

    /** 学生批量加入时逐条校验（供 addRoster / transferClass 复用） */
    List<String> validateJoin(Long classId, List<Long> studentIds);

    /**
     * 班级任教关系清单（只读视图）
     *
     * 任教关系的唯一写入入口在教师模块，班级侧只提供只读展示与跳转
     * （AGENTS 第 7 节的模块边界；`BR-TEACHER-003`）。
     *
     * @param classId 班级 ID（行政班或教学班）
     * @return 任教关系清单
     */
    List<org.dromara.edu.domain.vo.EduTeachingAssignmentVo> listClassTeachingAssignment(Long classId);

}
