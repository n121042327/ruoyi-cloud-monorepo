package org.dromara.edu.datascope;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 教育域数据范围 SQL 拦截器装配
 *
 * 覆盖列表 / 详情 / 统计 / 更新 / 删除（`docs/30-architecture/09-permission-architecture.md` 第 4.2 节）。
 * 用 MyBatis-Plus 的 `DataPermissionInterceptor` + `MultiDataPermissionHandler`，
 * 不自行解析 SQL 语法树（同一份 jsqlparser 由框架的租户插件共用）。
 *
 * **默认关闭**：`ruoyi-common-mybatis` 的 `MybatisPlusConfiguration` 在装配插件链时只显式取
 * `TenantLineInnerInterceptor` + 框架自带的 `PlusDataPermissionInterceptor`，
 * 因此本配置类通过 `edu.data-scope.enabled=true` 显式开启，并把教育域拦截器追加到插件链
 * （位置在分页插件之后，阶段 8 联调时按实际执行计划确认是否需要前移）。
 *
 * 关闭状态下行为与之前完全一致，不会影响任何既有查询。
 *
 * @author Codex
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "edu.data-scope", name = "enabled", havingValue = "true")
public class DataScopeInterceptor {

    /**
     * 教育域数据范围拦截器
     *
     * @param handler 范围片段生成器
     * @return 拦截器
     */
    @Bean
    public InnerInterceptor eduDataPermissionInterceptor(EduDataPermissionHandler handler) {
        return new DataPermissionInterceptor(handler);
    }

    /**
     * 把教育域拦截器追加到 MyBatis-Plus 插件链
     *
     * @param mybatisPlusInterceptor     框架已装配的插件链
     * @param eduDataPermissionInterceptor 教育域拦截器
     * @return 装配标记对象
     */
    @Bean
    public EduDataScopeChainRegistrar eduDataScopeChainRegistrar(
        MybatisPlusInterceptor mybatisPlusInterceptor,
        InnerInterceptor eduDataPermissionInterceptor) {
        return new EduDataScopeChainRegistrar(mybatisPlusInterceptor, eduDataPermissionInterceptor);
    }

}
