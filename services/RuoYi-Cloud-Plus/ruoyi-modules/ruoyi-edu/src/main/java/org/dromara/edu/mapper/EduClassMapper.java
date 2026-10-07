package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.vo.EduClassVo;

/**
 * 班级数据层
 *
 * 列表走班级 PRD 6.1 的查询参数；年级 / 校区 / 班主任姓名与在读人数的连接查询
 * 见 resources/mapper/edu/EduClassMapper.xml（阶段 7 后续批次补齐）。
 *
 * @author Codex
 */
public interface EduClassMapper extends BaseMapperPlus<EduClass, EduClassVo> {

    /**
     * 分页查询班级列表（数据范围由 DataScopeResolver 在服务层注入后拼进 wrapper）
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 班级分页结果
     */
    default Page<EduClassVo> selectPageClassList(Page<EduClass> page, Wrapper<EduClass> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
