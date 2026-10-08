package org.dromara.edu.datascope;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 把教育域数据范围拦截器注册进 MyBatis-Plus 插件链
 *
 * 单独成类是为了让「追加插件」这个副作用只发生一次，并且能在日志里留下明确痕迹
 * （便于阶段 8 排查「为什么多了个 where 条件」）。
 *
 * @author Codex
 */
@Slf4j
public class EduDataScopeChainRegistrar {

    /**
     * 构造时把教育域拦截器追加到插件链
     *
     * @param mybatisPlusInterceptor      框架插件链
     * @param eduDataPermissionInterceptor 教育域拦截器
     */
    public EduDataScopeChainRegistrar(MybatisPlusInterceptor mybatisPlusInterceptor,
                                     InnerInterceptor eduDataPermissionInterceptor) {
        mybatisPlusInterceptor.addInnerInterceptor(eduDataPermissionInterceptor);
        log.warn("edu.data-scope.enabled=true：教育域数据范围拦截器已追加到 MyBatis-Plus 插件链，"
            + "请按 schema.yaml 的表清单在阶段 8 逐条验证范围列映射后再用于生产");
    }

}
