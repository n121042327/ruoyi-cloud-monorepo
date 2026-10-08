package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduPromotionTask;
import org.dromara.edu.domain.vo.EduPromotionTaskVo;

/**
 * 升班任务数据层
 *
 * @author Codex
 */
public interface EduPromotionTaskMapper extends BaseMapperPlus<EduPromotionTask, EduPromotionTaskVo> {

    /**
     * 分页查询升班任务
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 升班任务分页结果
     */
    default Page<EduPromotionTaskVo> selectPagePromotionTask(Page<EduPromotionTask> page,
                                                            Wrapper<EduPromotionTask> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
