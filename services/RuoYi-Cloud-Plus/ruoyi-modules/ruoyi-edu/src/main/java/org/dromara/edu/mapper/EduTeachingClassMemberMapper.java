package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTeachingClassMember;
import org.dromara.edu.domain.vo.EduTeachingClassMemberVo;

/**
 * 教学班成员数据层
 *
 * 唯一键 uk_tclass_member = (teaching_class_id, student_id)：生成幂等的前提。
 *
 * @author Codex
 */
public interface EduTeachingClassMemberMapper extends BaseMapperPlus<EduTeachingClassMember, EduTeachingClassMemberVo> {

    /**
     * 分页查询教学班成员
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 成员分页结果
     */
    default Page<EduTeachingClassMemberVo> selectPageMemberList(Page<EduTeachingClassMember> page,
                                                               Wrapper<EduTeachingClassMember> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
