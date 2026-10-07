package org.dromara.edu;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 教育业务模块
 *
 * 首轮教育域全部接口都在该服务内，避免早期拆服务带来分布式事务成本
 * （见 docs/30-architecture/06-api-catalog.md 第 1 节的服务归属）。
 *
 * @author Codex
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiEduApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiEduApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  教育业务模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
