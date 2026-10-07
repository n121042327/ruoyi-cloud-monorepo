package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.vo.EduClassMemberVo;

/**
 * 班级成员（花名册）数据层
 *
 * 花名册列表需要连接学生主体与在校记录（两段式取数，DS-DENY-09），
 * 自定义 SQL 见 resources/mapper/edu/EduClassMemberMapper.xml（阶段 7 后续批次补齐）。
 *
 * @author Codex
 */
public interface EduClassMemberMapper extends BaseMapperPlus<EduClassMember, EduClassMemberVo> {

    /**
     * 分页查询花名册
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 花名册分页结果
     */
    default Page<EduClassMemberVo> selectPageRoster(Page<EduClassMember> page, Wrapper<EduClassMember> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
