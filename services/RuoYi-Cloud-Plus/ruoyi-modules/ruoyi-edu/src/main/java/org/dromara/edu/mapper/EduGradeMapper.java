package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.vo.EduGradeVo;

/**
 * 年级数据层
 *
 * @author Codex
 */
public interface EduGradeMapper extends BaseMapperPlus<EduGrade, EduGradeVo> {

    /**
     * 分页查询年级列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件（含数据范围）
     * @return 年级分页结果
     */
    default Page<EduGradeVo> selectPageGradeList(Page<EduGrade> page, Wrapper<EduGrade> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
