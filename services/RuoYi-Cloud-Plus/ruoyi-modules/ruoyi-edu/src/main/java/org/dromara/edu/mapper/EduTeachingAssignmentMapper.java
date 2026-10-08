package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.vo.EduTeachingAssignmentVo;

/**
 * 任教关系数据层
 *
 * 任课教师的数据范围 DS-07 与字段裁剪（本人所授学科）依据本表的 teacher_id + term_id + status。
 *
 * @author Codex
 */
public interface EduTeachingAssignmentMapper extends BaseMapperPlus<EduTeachingAssignment, EduTeachingAssignmentVo> {

    /**
     * 分页查询任教关系（按班级或按教师视角）
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 任教关系分页结果
     */
    default Page<EduTeachingAssignmentVo> selectPageAssignmentList(Page<EduTeachingAssignment> page,
                                                                   Wrapper<EduTeachingAssignment> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
