package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduStudentStream;
import org.dromara.edu.domain.vo.EduStreamSelectionVo;

/**
 * 学生选科数据层
 *
 * 组合分布统计走 idx_stream_stat = (term_id, primary_subject_code, secondary_subject_codes)（REQ-STR-049）。
 *
 * @author Codex
 */
public interface EduStudentStreamMapper extends BaseMapperPlus<EduStudentStream, EduStreamSelectionVo> {

    /**
     * 分页查询选科清单
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 选科清单分页结果
     */
    default Page<EduStreamSelectionVo> selectPageSelectionList(Page<EduStudentStream> page,
                                                              Wrapper<EduStudentStream> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
