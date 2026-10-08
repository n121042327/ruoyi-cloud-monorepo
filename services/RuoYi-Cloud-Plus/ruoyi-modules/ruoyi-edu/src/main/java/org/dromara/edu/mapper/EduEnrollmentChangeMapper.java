package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduEnrollmentChange;
import org.dromara.edu.domain.vo.EduEnrollmentChangeVo;

/**
 * 学籍异动记录数据层
 *
 * 追加式、不更新不删除（BR-PROMO-012）；异动历史页与升班模块共用本表。
 *
 * @author Codex
 */
public interface EduEnrollmentChangeMapper extends BaseMapperPlus<EduEnrollmentChange, EduEnrollmentChangeVo> {

    /**
     * 分页查询学籍异动记录
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 异动记录分页结果
     */
    default Page<EduEnrollmentChangeVo> selectPageChangeList(Page<EduEnrollmentChange> page,
                                                             Wrapper<EduEnrollmentChange> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
