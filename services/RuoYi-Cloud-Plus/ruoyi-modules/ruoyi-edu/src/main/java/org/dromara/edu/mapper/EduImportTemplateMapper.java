package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduImportTemplate;
import org.dromara.edu.domain.vo.EduImportTemplateVo;

/**
 * 导入模板数据层
 *
 * @author Codex
 */
public interface EduImportTemplateMapper extends BaseMapperPlus<EduImportTemplate, EduImportTemplateVo> {

    /**
     * 分页查询导入模板
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 模板分页结果
     */
    default Page<EduImportTemplateVo> selectPageTemplate(Page<EduImportTemplate> page,
                                                        Wrapper<EduImportTemplate> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
