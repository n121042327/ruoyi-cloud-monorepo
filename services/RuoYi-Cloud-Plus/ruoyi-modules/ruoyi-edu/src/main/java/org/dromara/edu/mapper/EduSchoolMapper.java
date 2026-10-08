package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.vo.EduSchoolVo;

/**
 * 学校数据层
 *
 * @author Codex
 */
public interface EduSchoolMapper extends BaseMapperPlus<EduSchool, EduSchoolVo> {

    /**
     * 分页查询学校列表（平台运营看全平台；学校用户只看本校）
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 学校分页结果
     */
    default Page<EduSchoolVo> selectPageSchoolList(Page<EduSchool> page, Wrapper<EduSchool> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
