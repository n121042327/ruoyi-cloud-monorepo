# AGENTS.md — 后端（JDK 17 + Spring Boot 3 + RuoYi-Cloud-Plus）

本文件用于 `RuoYi-Cloud-Plus/`（迁移后为 `services/RuoYi-Cloud-Plus/`）。
根 `AGENTS.md` 的第 3、4、8 节不可放宽。

## 1. 基线

| 项 | 值 |
|---|---|
| 基线版本 | RuoYi-Cloud-Plus 2.6.2 |
| JDK | 17 |
| Spring Boot | 3.5.15 |
| Spring Cloud | 2025.0.3 |
| MyBatis | 3.5.19 |
| MyBatis-Plus | 3.5.16 |
| 数据库 | MySQL 8 |

## 2. 模块约定

新增教育能力全部落在 `ruoyi-modules/ruoyi-edu`，**不新增四个微服务**。

```
ruoyi-edu/
  src/main/java/org/dromara/edu/
    controller/       REST 接口，只做参数校验与编排
    service/          业务接口
    service/impl/     业务实现
    mapper/           MyBatis-Plus Mapper
    domain/           实体（对应数据库表）
    domain/bo/        请求对象
    domain/vo/        响应对象
    enums/            枚举
    datascope/        教育数据权限扩展
    convert/          对象转换（若项目已有 MapStruct 用法则沿用）
  src/main/resources/mapper/edu/   复杂 SQL 的 XML
  src/test/java/org/dromara/edu/   测试
```

禁止：

- 跨服务直接注入对方的 Mapper，或直接写对方负责的表
- 在 Controller 里写业务逻辑
- 在实体类里写业务规则
- 用 `@Async` 替代消息队列处理需要可靠投递的任务

## 3. 接口约定

- 列表接口返回 `TableDataInfo<T>`，形状 `{ code, msg, rows, total }`
- 详情与操作接口返回 `R<T>`，形状 `{ code, msg, data }`
- 路径前缀统一 `/edu/<module>`，例如 `/edu/student/page`
- 每个接口必须能对应到 `docs/40-detailed-design/api/openapi.yaml` 中的 operationId
- 入参用 BO 并加校验注解；出参用 VO，不直接返回实体

## 4. 数据与权限（本项目最重要的一条）

教育数据权限有四层，必须在**同一个拦截机制**里表达：

| 角色 | 范围 |
|---|---|
| 校领导 | 本校全部教育数据 |
| 年级主任 | 所负责年级 |
| 班主任 | 所负责班级 |
| 任课教师 | 任教班级的必要基本资料 + 本人所授学科数据 |
| 集团用户 | 只限集团自有数据，不自动获得学校教学数据 |
| 平台运营 | 全平台可见，但查询 / 修改 / 导出分别授权、分别审计 |

强制要求：

1. 缺少租户、学校或执行人上下文时**拒绝执行**，不要退化为全量查询
2. 列表、详情、批量、导出、文件访问、缓存、异步任务共用同一套数据范围判定
3. 不得依赖现有租户管理员的放行逻辑；教育权限必须独立可测
4. 每个查询方法都要有对应的越权测试用例（跨租户 / 跨校 / 跨年级 / 跨班 / ID 猜测）

## 5. 数据库

- 同服务内核心关系使用物理外键，**禁止级联删除**
- 跨服务关系使用逻辑引用 + 一致性检查 SQL
- 唯一键必须考虑租户、学校、学年、历史有效期与逻辑删除策略
- 索引由实际查询驱动，先有查询场景再建索引
- **迁移脚本是数据库结构的权威来源**
- 已执行过的迁移脚本禁止修改，只能新增
- 迁移脚本必须在真实 MySQL 8 上验证空库安装与存量升级

## 6. 事务、并发与幂等

- 事务边界放在 Service 层方法上，不要跨 Dubbo 调用开事务
- 批量与异步任务必须幂等：使用业务唯一键或幂等表，不能只靠"前端不会重复点"
- 升班、选科变更、导入等长时间操作走异步任务，返回任务 ID，前端轮询或订阅结果
- 并发冲突要有明确策略：乐观锁版本号、唯一索引冲突处理或行锁，三者择一并在设计文档中写明

## 7. 缓存与消息

- Redis 只用于热点读缓存与分布式锁，键必须带租户前缀
- 缓存必须设计失效策略，不能只写不删
- RabbitMQ 用于削峰与异步：必须有重试、死信队列与消费幂等
- 消息 payload 必须有业务幂等键

## 8. 测试（必须显式启用）

根 `pom.xml` 默认 `<skipTests>true</skipTests>`，`mvn package` 通过**不能**作为测试通过的证据。

```bash
# 单模块测试，必须显式关闭 skipTests
mvn -q -DskipTests=false -pl ruoyi-modules/ruoyi-edu -am test
```

交接时必须报告：执行了多少用例、通过多少、失败多少。

必须覆盖的测试类别：

1. 业务规则单元测试
2. 数据权限越权测试
3. 幂等与并发测试
4. 迁移脚本在 MySQL 8 上的安装测试

## 9. 禁止事项

- 不擅自升级基线版本或 Spring Boot / Spring Cloud 版本
- 不引入第二套 ORM 或第二套 JSON 框架
- 不在日志中打印身份证号、手机号等个人信息
- 不使用 `SELECT *` 拼接动态 SQL
- 不为了通过编译而吞掉异常
