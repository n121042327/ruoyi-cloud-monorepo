package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAuditArchiveBatch;
import org.dromara.edu.domain.vo.EduAuditArchiveBatchVo;

/**
 * 日志归档批次数据层
 *
 * @author Codex
 */
public interface EduAuditArchiveBatchMapper extends BaseMapperPlus<EduAuditArchiveBatch, EduAuditArchiveBatchVo> {

    /**
     * 分页查询归档批次
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 归档批次分页结果
     */
    default Page<EduAuditArchiveBatchVo> selectPageArchiveBatch(Page<EduAuditArchiveBatch> page,
                                                               Wrapper<EduAuditArchiveBatch> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
