package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.vo.EduSubjectVo;

/**
 * 学科数据层
 *
 * @author Codex
 */
public interface EduSubjectMapper extends BaseMapperPlus<EduSubject, EduSubjectVo> {

    /**
     * 分页查询学科列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 学科分页结果
     */
    default Page<EduSubjectVo> selectPageSubjectList(Page<EduSubject> page, Wrapper<EduSubject> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
