package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduImportError;
import org.dromara.edu.domain.vo.EduImportErrorVo;

/**
 * 导入行结果数据层
 *
 * @author Codex
 */
public interface EduImportErrorMapper extends BaseMapperPlus<EduImportError, EduImportErrorVo> {

    /**
     * 分页查询批次行结果
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 行结果分页数据
     */
    default Page<EduImportErrorVo> selectPageError(Page<EduImportError> page,
                                                  Wrapper<EduImportError> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
