package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAsyncTask;
import org.dromara.edu.domain.vo.EduAsyncTaskVo;

/**
 * 异步任务数据层
 *
 * @author Codex
 */
public interface EduAsyncTaskMapper extends BaseMapperPlus<EduAsyncTask, EduAsyncTaskVo> {

    /**
     * 分页查询异步任务
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 异步任务分页结果
     */
    default Page<EduAsyncTaskVo> selectPageAsyncTask(Page<EduAsyncTask> page,
                                                    Wrapper<EduAsyncTask> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
