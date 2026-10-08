package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduTransferOrder;
import org.dromara.edu.domain.vo.EduTransferOrderVo;

/**
 * 跨校转学单数据层
 *
 * 转入校待接收清单按 (to_school_id, transfer_status) 走 idx_transfer_to_school；
 * 转出校清单按 (from_school_id, transfer_status) 走 idx_transfer_from_school。
 *
 * @author Codex
 */
public interface EduTransferOrderMapper extends BaseMapperPlus<EduTransferOrder, EduTransferOrderVo> {

    /**
     * 分页查询转学单
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 转学单分页结果
     */
    default Page<EduTransferOrderVo> selectPageTransferList(Page<EduTransferOrder> page,
                                                           Wrapper<EduTransferOrder> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
