package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduPromotionItem;
import org.dromara.edu.domain.vo.EduPromotionItemVo;

/**
 * 升班明细数据层
 *
 * 幂等键 (task_id, student_id)（REQ-PRM-029）：重试时按该键命中已有行，只更新状态与目标班级，
 * 不重复插入。
 *
 * @author Codex
 */
public interface EduPromotionItemMapper extends BaseMapperPlus<EduPromotionItem, EduPromotionItemVo> {

    /**
     * 分页查询升班明细
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 明细分页结果
     */
    default Page<EduPromotionItemVo> selectPageItemList(Page<EduPromotionItem> page,
                                                       Wrapper<EduPromotionItem> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
