package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduStudentBo;
import org.dromara.edu.domain.vo.EduStudentVo;

/**
 * 学生服务层
 *
 * 对应接口：listStudent / getStudent / addStudent / updateStudent（06-api-catalog.md 的 student 模块）。
 *
 * @author Codex
 */
public interface IEduStudentService {

    /**
     * 分页查询学生列表
     *
     * @param student   查询条件（含数据范围）
     * @param pageQuery 分页参数
     * @return 学生分页列表
     */
    TableDataInfo<EduStudentVo> queryPageList(EduStudentBo student, PageQuery pageQuery);

    /**
     * 查询学生详情
     *
     * @param studentId 学生 ID
     * @return 学生详情
     */
    EduStudentVo queryById(Long studentId);

    /**
     * 新增学生（学号由系统统一发号；同时建立学年学期在校记录）
     *
     * @param student 学生信息
     * @return 是否成功
     */
    Boolean insertByBo(EduStudentBo student);

    /**
     * 修改学生
     *
     * @param student 学生信息
     * @return 是否成功
     */
    Boolean updateByBo(EduStudentBo student);

}
