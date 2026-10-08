package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTeachingClass;
import org.dromara.edu.domain.vo.EduTeachingClassVo;

/**
 * 教学班数据层
 *
 * @author Codex
 */
public interface EduTeachingClassMapper extends BaseMapperPlus<EduTeachingClass, EduTeachingClassVo> {

    /**
     * 分页查询教学班
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 教学班分页结果
     */
    default Page<EduTeachingClassVo> selectPageTeachingClass(Page<EduTeachingClass> page,
                                                            Wrapper<EduTeachingClass> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
