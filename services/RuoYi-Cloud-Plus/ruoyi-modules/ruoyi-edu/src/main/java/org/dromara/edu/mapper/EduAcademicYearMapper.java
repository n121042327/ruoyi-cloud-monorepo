package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAcademicYear;
import org.dromara.edu.domain.vo.EduAcademicYearVo;

/**
 * 学年数据层
 *
 * @author Codex
 */
public interface EduAcademicYearMapper extends BaseMapperPlus<EduAcademicYear, EduAcademicYearVo> {

    /**
     * 分页查询学年列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 学年分页结果
     */
    default Page<EduAcademicYearVo> selectPageYearList(Page<EduAcademicYear> page, Wrapper<EduAcademicYear> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
