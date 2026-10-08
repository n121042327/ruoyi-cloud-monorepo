package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduCampus;
import org.dromara.edu.domain.vo.EduCampusVo;

/**
 * 校区数据层
 *
 * @author Codex
 */
public interface EduCampusMapper extends BaseMapperPlus<EduCampus, EduCampusVo> {

    /**
     * 分页查询校区列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 校区分页结果
     */
    default Page<EduCampusVo> selectPageCampusList(Page<EduCampus> page, Wrapper<EduCampus> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
