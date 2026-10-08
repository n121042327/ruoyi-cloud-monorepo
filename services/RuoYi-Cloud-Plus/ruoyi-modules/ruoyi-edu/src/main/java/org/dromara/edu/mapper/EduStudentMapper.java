package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.vo.EduStudentVo;

/**
 * 学生数据层
 *
 * 列表走 listStudent 查询参数（PRD 8.1），自定义 SQL 见 resources/mapper/edu/EduStudentMapper.xml。
 *
 * @author Codex
 */
public interface EduStudentMapper extends BaseMapperPlus<EduStudent, EduStudentVo> {

    /**
     * 分页查询学生列表（数据范围由 DataScopeResolver 在服务层注入后拼进 wrapper）
     *
     * @param page          分页对象
     * @param queryWrapper  查询条件
     * @return 学生分页结果
     */
    default Page<EduStudentVo> selectPageStudentList(Page<EduStudent> page, Wrapper<EduStudent> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
