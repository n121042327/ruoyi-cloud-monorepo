# 技术栈锁定表

规则：

1. 本表的值有出处。"出处"列指出证据文件或决策编号。
2. **实际代码优先于本表。** 发现冲突时，以 `pom.xml` / `package.json` / 实际运行结果为准，
   并把差异登记到 `decisions.md`，再回写本表。
3. 变更本表任意一行，必须走 `change-control.md` 流程。

## 1. 后端

| 类别 | 选型 | 版本 | 出处 | 可否变更 |
|---|---|---|---|---|
| JDK | OpenJDK | 17 | `services/RuoYi-Cloud-Plus/pom.xml` `<java.version>` | 不可，用户已确认 17 |
| 基线框架 | RuoYi-Cloud-Plus | 2.6.2 | `pom.xml` `<revision>` | 不可，回退该版本是为了租户逻辑 |
| 应用框架 | Spring Boot | 3.5.15 | `pom.xml` `<spring-boot.version>` | 随基线，不单独升 |
| 微服务框架 | Spring Cloud | 2025.0.3 | `pom.xml` `<spring-cloud.version>` | 随基线 |
| RPC | Apache Dubbo | 随基线 BOM | `ruoyi-common-dubbo` | 可，需 ADR |
| 认证鉴权 | Sa-Token | 随基线 BOM | `ruoyi-common-satoken` | 可，需 ADR |
| ORM | MyBatis | 3.5.19 | `pom.xml` `<mybatis.version>` | 不可单独升 |
| ORM 增强 | MyBatis-Plus | 3.5.16 | `pom.xml` `<mybatis-plus.version>` | 不可单独升 |
| 数据库 | MySQL | 8 | 用户确认 + `script/sql/ry-cloud.sql` | 不可 |
| 缓存 | Redis | 随部署 | `ruoyi-common-redis`、`script/docker/redis` | 不可，用途需限定 |
| 消息队列 | RabbitMQ | 随部署 | `script/docker/rabbitmq` | 可，与 RocketMQ 二选一需 ADR |
| 检索 | Elasticsearch | 随部署 | `ruoyi-common-elasticsearch`、`script/docker/elk` | 首轮不启用 |
| 任务调度 | SnailJob | 随基线 | `script/sql/ry-job.sql` | 随基线 |
| 分布式事务 | Seata | 随基线 | `script/sql/ry-seata.sql`、`ruoyi-common-seata` | 首轮不启用 |
| 构建 | Maven | 3.9.9（本机） | `mvn -version` | 不可 |
| 部署 | Docker | 随环境 | `script/docker/docker-compose.yml` | 不可 |

## 2. 前端

| 类别 | 选型 | 版本 | 出处 | 可否变更 |
|---|---|---|---|---|
| 框架 | Vue | 3.5.30 | `apps/plus-ui/package.json` | 不可 |
| 语言 | TypeScript | ~5.9.3 | 同上 | 不可 |
| 构建 | Vite | 7.3.2 | 同上 | 不可 |
| 状态 | Pinia | 3.0.4 | 同上 | 不可 |
| 路由 | Vue Router | 5.0.3 | 同上 | 不可 |
| UI 库 | Element Plus | 2.13.5 | 同上 | 不可 |
| 图标 | `@element-plus/icons-vue` | 2.3.2 | 同上 | 不可 |
| 样式引擎 | UnoCSS | 66.6.6 | 同上 | 不可 |
| 样式预处理 | Sass | 1.98.0 | 同上 | 不可 |
| 请求 | Axios | 1.13.6 | 同上 | 不可 |
| 工具库 | `@vueuse/core` | 14.2.1 | 同上 | 可 |
| 表格 | vxe-table | 4.18.1 | 同上 | 教育模块是否使用需 ADR |
| 测试 | Vitest | 4.0.18 | 同上 | 可 |
| 类型检查 | vue-tsc | ^3.2.5 | 同上 | 不可 |
| Lint | ESLint | 9.39.1 | 同上 | 不可 |
| 格式化 | Prettier | 3.8.1 | 同上 | 不可 |
| 包管理 | pnpm | 12.3.4 | 本机 `pnpm --version`；`apps/plus-ui` 原无锁文件，由本轮 `pnpm install` 生成 `pnpm-lock.yaml` | 不可 |
| 编排 | Turbo | 2.11.5 | 根 `package.json` `devDependencies`；`turbo.json` 定义任务 | 可，需 ADR |
| Node | Node.js | 24.20.0（本机）；`engines` 要求 `>=20.19.0` | 本机 `node --version` | 可 |

## 3. 后期引入（首轮禁止使用）

| 项 | 用途 | 引入阶段 |
|---|---|---|
| Tiptap | 富文本题干录入 | 题库阶段 |
| KaTeX | 数学公式渲染 | 题库阶段 |
| Elasticsearch 检索 | 题库全文检索 | 题库阶段 |

## 4. 命名与写法约定

后端：

- Controller 只做参数校验与编排，业务逻辑在 Service
- 分层：`controller` / `service` / `service.impl` / `mapper` / `domain` / `domain.bo` / `domain.vo`
- 列表接口返回 `TableDataInfo<T>`，形状 `{ code, msg, rows, total }`
- 详情与操作接口返回 `R<T>`，形状 `{ code, msg, data }`
- 新增教育能力全部落在 `ruoyi-modules/ruoyi-edu`，**不新增四个微服务**
- 禁止跨服务直接调用对方 Mapper 或写对方表

前端：

- 单文件组件一律 `<script setup lang="ts">`
- 页面状态用 composable 抽取，不把逻辑堆在 `.vue` 里
- 请求集中在 `src/api/edu/**`，页面不直接 `axios`
- 类型定义与后端 DTO 一一对应，新增 `src/types/edu/**`
- 后端返回的长整型 ID 一律按字符串处理，禁止 `Number()` 转换
