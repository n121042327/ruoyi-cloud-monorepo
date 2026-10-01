# 技术选型

## 1. 选型规则

1. **实际代码优先**：`services/RuoYi-Cloud-Plus/pom.xml` 与 `apps/plus-ui/package.json` 是权威值；
   本文件与 `stack-lock.md` 冲突时以实际文件为准，并把差异登记到 `docs/00-governance/decisions.md`。
2. **不引入不必要的新依赖**：首轮教育域不新增基础设施组件（不引 Seata、不启用 Elasticsearch）。
3. **每一项都有理由**：只写"业界主流""大家都用"不算理由，必须说明它解决本项目的哪个具体问题。

## 2. 后端

| 类别 | 选型 / 版本 | 解决的具体问题 | 出处 |
|---|---|---|---|
| 语言 / 运行时 | Java 17 | 基线要求；`record` / `sealed` 等语法让 BO / VO 更简洁 | `pom.xml` `<java.version>` |
| 基线框架 | RuoYi-Cloud-Plus 2.6.2 | 提供租户、权限、菜单、字典、文件、任务等平台能力，避免重造 | `pom.xml` `<revision>` |
| 应用框架 | Spring Boot 3.5.15 | 随基线 | `pom.xml` |
| 微服务框架 | Spring Cloud 2025.0.3 | 随基线；首轮只用到网关路由与配置 | `pom.xml` |
| RPC | Apache Dubbo（随基线） | 首轮**不新增跨服务调用**；保留能力用于后续题库 / 学情拆分 | `ruoyi-common-dubbo` |
| 认证鉴权 | Sa-Token（随基线） | 与基线的登录、令牌、权限注解一致；教育域复用同一套 | `ruoyi-common-satoken` |
| ORM | MyBatis 3.5.19 + MyBatis-Plus 3.5.16 | Mapper 与分页；教育域的复杂列表（花名册、选科统计）用 XML 写 SQL 更可控 | `pom.xml` |
| 数据库 | MySQL 8 | 事务、外键、分区（审计大表按时间分区）都能满足；私有化部署常见 | `script/sql/ry-cloud.sql` |
| 缓存 | Redis | 数据权限解析结果缓存 + 热点读 + 令牌；**不存权威数据** | `ruoyi-common-redis` |
| 消息 | RabbitMQ | 异步任务与削峰；死信队列天然对应 `REQ-IMP-038` 的死信查看与重放 | `script/docker/rabbitmq` |
| 任务调度 | SnailJob（随基线） | 归档、过期授权、定时一致性检查 | `script/sql/ry-job.sql` |
| 文件 | `ruoyi-common-oss` | 导入文件与结果文件的统一引用与短时签名 | `ruoyi-common-oss` |
| 构建 | Maven 3.9.9 | 随仓库；`mvn -DskipTests=false` 显式启用测试作为证据 | `mvn -version` |
| 部署 | Docker | 私有化交付的统一形态 | `script/docker/docker-compose.yml` |

### 2.1 首轮刻意不引入

| 组件 | 不引入的理由 | 何时再评估 |
|---|---|---|
| Seata | 首轮单服务（`02` 的 A-01），没有跨服务事务；引入只会增加运维面 | 拆服务且出现跨服务写时 |
| Elasticsearch | 题库检索未开工；审计归档检索用 MySQL 分区 + 索引即可满足 30 秒目标 | 题库模块开工时 |
| 分库分表中间件 | 目标规模（65 校 / 10 万学生 / 200 并发）单库 + 分区足够 | 单表量级到亿级且分区不够时 |

## 3. 前端

| 类别 | 选型 / 版本 | 解决的具体问题 | 出处 |
|---|---|---|---|
| 框架 | Vue 3.5.30 | 与 `apps/plus-ui` 现有代码一致，可直接复用布局与权限指令 | `apps/plus-ui/package.json` |
| 语言 | TypeScript 5.9 | 教育域字段多、枚举多，类型能在编译期挡住大部分字段拼写错误 | 同上 |
| 构建 | Vite 7 | 随基线 | 同上 |
| 状态 | Pinia 3 | 教育域跨页状态（当前学年学期、学校切换、列配置）集中管理 | 同上 |
| 路由 | Vue Router 5 | 随基线 | 同上 |
| UI | Element Plus 2.13 | 阶段 3 的 `component-mapping.yaml` 就是按它建立的映射 | 同上 |
| 样式 | UnoCSS + Sass | 随基线；阶段 3 的 token 落到 UnoCSS 与 Element Plus 变量 | 同上 |
| 请求 | Axios 1.13 | 统一拦截：令牌、错误码、数据范围提示 | 同上 |
| 表格 | vxe-table 4.18（**教育模块是否使用待 ADR**） | 大列数表格（学生 12 列、审计 9 列）的虚拟滚动；但会带来与 Element Plus 表格两套 API | `stack-lock.md` 已标"教育模块是否使用需 ADR" |
| 测试 | Vitest 4 | 阶段 6 的组件与 composable 测试 | 同上 |
| 编排 | pnpm 12 + Turbo 2.11 | monorepo 的 `apps/*` 与后续 `apps/pad-*` 统一编排 | 根 `package.json` |

### 3.1 vxe-table 的裁决（首轮取推荐方案）

| 项 | 内容 |
|---|---|
| 这是什么 | 基线里带了 `vxe-table`，但项目的表格规范是按 Element Plus 的 `el-table` 建立的（阶段 2 / 3 的列宽口径、`markup-contract` 映射表都按 `el-table` 写） |
| 不选会怎样 | 若教育模块混用两套表格，列宽口径、空态、行内动作的实现会分叉，阶段 8 的验收要维护两套标准 |
| 可选项 | A 首轮统一用 `el-table`，`vxe-table` 留给后续大表格（> 5000 行的检索结果）；B 教育模块全部用 `vxe-table` |
| 推荐 | **A**：首轮列表最大 20 行 / 页，`el-table` 足够；统一一套表格能让"组件映射表"与"列宽 = min-width"的口径不失效 |
| 落点 | 本决策记入 `decisions.md` 的 `D-086`；若后续题目检索需要虚拟滚动，再走 ADR 单独评估 |

## 4. 版本冲突的处理

| 检查方式 | 频率 | 处理 |
|---|---|---|
| `mvn -version` / `java -version` 与 `pom.xml` 的 `<java.version>` 比对 | 阶段 7 开工前 | 不一致时以 `pom.xml` 为准并回写 `stack-lock.md` |
| `pnpm --version` / `node --version` 与 `engines` 比对 | 阶段 6 开工前 | 同上 |
| 教育模块新增依赖 | 每次新增 | 必须在 `stack-lock.md` 登记"可否变更"列，并在 `decisions.md` 记理由 |

## 5. 结论

- 技术栈与 `stack-lock.md` 一致，且每一项都对应本项目的具体问题（不是"业界主流"式选型）。
- 首轮不引入 Seata / Elasticsearch / 分库分表，避免为不存在的需求付复杂度。
- 唯一需要裁决的是 `vxe-table` 是否用于教育模块，已按推荐方案（首轮统一 `el-table`）落定并登记 `D-086`。
