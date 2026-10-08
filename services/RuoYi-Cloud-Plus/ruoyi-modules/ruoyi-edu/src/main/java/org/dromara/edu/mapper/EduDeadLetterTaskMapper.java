package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduDeadLetterTask;
import org.dromara.edu.domain.vo.EduDeadLetterTaskVo;

/**
 * 死信任务数据层
 *
 * @author Codex
 */
public interface EduDeadLetterTaskMapper extends BaseMapperPlus<EduDeadLetterTask, EduDeadLetterTaskVo> {

    /**
     * 分页查询死信任务
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 死信任务分页结果
     */
    default Page<EduDeadLetterTaskVo> selectPageDeadLetterTask(Page<EduDeadLetterTask> page,
                                                              Wrapper<EduDeadLetterTask> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
