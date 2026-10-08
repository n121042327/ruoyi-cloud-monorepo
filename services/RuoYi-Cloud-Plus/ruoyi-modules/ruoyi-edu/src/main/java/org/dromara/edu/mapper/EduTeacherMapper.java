package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.vo.EduTeacherVo;

/**
 * 教师数据层
 *
 * 列表的教育角色 / 任教学科 / 任课班级数来自 edu_user_role 与 edu_teaching_assignment，
 * 自定义 SQL 见 resources/mapper/edu/EduTeacherMapper.xml（阶段 7 后续批次补齐）。
 *
 * @author Codex
 */
public interface EduTeacherMapper extends BaseMapperPlus<EduTeacher, EduTeacherVo> {

    /**
     * 分页查询教师列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件（含数据范围）
     * @return 教师分页结果
     */
    default Page<EduTeacherVo> selectPageTeacherList(Page<EduTeacher> page, Wrapper<EduTeacher> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
