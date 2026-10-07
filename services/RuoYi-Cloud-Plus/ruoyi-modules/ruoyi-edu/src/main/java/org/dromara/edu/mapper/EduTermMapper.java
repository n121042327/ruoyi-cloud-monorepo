package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTerm;
import org.dromara.edu.domain.vo.EduTermVo;

/**
 * 学期数据层
 *
 * 学期是全局上下文：班级 / 任教关系 / 花名册 / 选科 / 升班都以 term_id 归属，
 * 因此本表的「当前学期」标记（is_current）是各模块默认筛选的来源（BR-TERM-002）。
 *
 * @author Codex
 */
public interface EduTermMapper extends BaseMapperPlus<EduTerm, EduTermVo> {

    /**
     * 分页查询学期列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 学期分页结果
     */
    default Page<EduTermVo> selectPageTermList(Page<EduTerm> page, Wrapper<EduTerm> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
