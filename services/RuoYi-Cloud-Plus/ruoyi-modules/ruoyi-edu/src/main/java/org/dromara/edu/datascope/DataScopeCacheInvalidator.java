package org.dromara.edu.datascope;

import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

/**
 * 数据范围缓存失效器
 *
 * 业务写操作后删除相关缓存键（`docs/30-architecture/09-permission-architecture.md` 第 4.1 节；
 * 失效链路见 `08-cache-strategy.md`）。影响的写操作包括：角色调整、年级主任任职、
 * 班主任变更、任教关系变更、共享授权新建 / 撤销。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Component
public class DataScopeCacheInvalidator {

    private final DataScopeResolver dataScopeResolver;

    /**
     * 失效当前登录用户的范围缓存
     */
    public void evictCurrentUser() {
        dataScopeResolver.evict(LoginHelper.getTenantId(), LoginHelper.getUserId());
    }

    /**
     * 失效指定用户的范围缓存
     *
     * @param tenantId 租户
     * @param userId   用户
     */
    public void evict(String tenantId, Long userId) {
        dataScopeResolver.evict(tenantId, userId);
    }

    /**
     * 全量失效：角色 / 任职 / 授权的批量变更后用
     */
    public void evictAll() {
        dataScopeResolver.evictAll();
    }

}
