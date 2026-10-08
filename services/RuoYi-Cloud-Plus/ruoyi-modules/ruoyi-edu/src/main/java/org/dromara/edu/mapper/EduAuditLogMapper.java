package org.dromara.edu.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.edu.domain.EduAuditLog;
import org.dromara.edu.domain.vo.EduAuditLogVo;

/**
 * 操作日志数据层
 *
 * **刻意不提供任何更新 / 删除方法**：日志表只允许追加，数据库层同样不授予更新与删除权限
 * （`BR-AUDIT-003` / `NFR-AUDIT-05` / `REQ-AUD-025` / `REQ-AUD-030` / `REQ-AUD-031`）。
 *
 * @author Codex
 */
public interface EduAuditLogMapper extends BaseMapperPlus<EduAuditLog, EduAuditLogVo> {

    /**
     * 分页查询操作日志
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 操作日志分页结果
     */
    default Page<EduAuditLogVo> selectPageLog(Page<EduAuditLog> page, Wrapper<EduAuditLog> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

}
