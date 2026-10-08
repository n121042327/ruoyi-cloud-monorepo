package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStreamChangeRequest;
import org.dromara.edu.domain.vo.EduStreamChangeRequestVo;

/**
 * 选科变更申请数据层
 *
 * 审批待办按提交时间升序（REQ-STR-035），走 idx_stream_request_pending。
 *
 * @author Codex
 */
public interface EduStreamChangeRequestMapper extends BaseMapperPlus<EduStreamChangeRequest, EduStreamChangeRequestVo> {

    /**
     * 分页查询变更申请
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 变更申请分页结果
     */
    default Page<EduStreamChangeRequestVo> selectPageRequestList(Page<EduStreamChangeRequest> page,
                                                                Wrapper<EduStreamChangeRequest> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
