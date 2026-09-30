# 运行时设计：异步任务、消息、缓存与幂等

> 上游依据：`docs/30-architecture/07-sync-async-boundary.md`（同步 / 异步边界与消息）、
> `docs/30-architecture/08-cache-strategy.md`（缓存策略）、`docs/10-prd/07-non-functional-requirements.md`。
> 本文件不复述上游结论，只补齐详细设计阶段需要定下来的实现约束。

## 1. 判断一条操作走同步还是异步

| 判定 | 同步 | 异步 |
|---|---|---|
| 用户必须立刻看到结果 | 是 | 否 |
| 数据量 | ≤ 2000 行导出 / ≤ 5000 行导入校验 | > 2000 行导出 / 执行写库 |
| 需要跨请求重试 | 否 | 是 |

阈值不在本文件重新发明，统一引用 `07-sync-async-boundary.md` 第 1 节。

## 2. 任务与消息

### 2.1 队列

| 交换机 | 队列 | 用途 | 消费者 |
|---|---|---|---|
| `edu.task.exchange` | `edu.task.import` | 导入执行 | 导入消费者 |
| `edu.task.exchange` | `edu.task.export` | 导出生成 | 导出消费者 |
| `edu.task.exchange` | `edu.task.promotion` | 升班执行 | 升班消费者 |
| `edu.task.exchange` | `edu.task.teaching-class` | 教学班生成 | 教学班消费者 |
| `edu.task.exchange` | `edu.task.archive` | 日志归档 | 归档消费者 |
| `edu.task.dlx` | `edu.task.dead` | 死信 | 死信巡视任务 |
| `edu.audit.exchange` | `edu.audit.compensate` | 审计补偿 | 审计消费者 |

路由键：`edu.<type>.<action>`，与 `07-sync-async-boundary.md` 第 3.1 节一致。

### 2.2 统一消息信封

| 字段 | 必填 | 说明 |
|---|---|---|
| `task_no` / `batch_no` | 是 | 幂等键，与 `edu_async_task.task_no` 一致 |
| `tenant_id` / `school_id` | 是 | 消费时恢复上下文；**消费端必须重新解析范围，不得直接信任** |
| `module` / `action` | 是 | 动作标识 |
| `payload` | 是 | 业务参数（文件引用、筛选条件、目标学期等） |
| `operator_id` | 是 | 发起人，用于「本人任务」过滤与审计 |
| `retry_count` | 是 | 当前重试次数 |
| `trace_id` | 是 | 贯穿日志、审计与错误响应 |

### 2.3 重试与死信

| 项 | 取值 | 依据 |
|---|---|---|
| 最大重试 | 3 | `REQ-IMP-039` |
| 退避 | 30s / 2m / 8m | 同上 |
| 幂等 | 以 `task_no` / `batch_no` 为唯一键先查后写；唯一索引兜底 | `REQ-IMP-037` / `NFR-MQ-02` |
| 超限 | 进 `edu.task.dead`，落 `edu_dead_letter_task` | `REQ-IMP-038` |
| 消费者并发 | 导入默认 3 校并发、每校串行 | `REQ-IMP-048` |
| 消息丢失兜底 | 定时巡检把长时间 `queued` / `running` 标记异常 | `NFR-MQ-01` |
| 重放 | 复用原幂等键，不新建任务；重放写审计 | 同上 |

## 3. 幂等键清单

| 幂等键 | 所在表 | 保护的操作 |
|---|---|---|
| `task_no` | `edu_async_task` | 所有异步任务 |
| `batch_no` | `edu_import_batch` | 导入批次 |
| `transfer_no` | `edu_transfer_order` | 转学单 |
| `request_no` | `edu_stream_change_request` | 选科变更申请 |
| `grant_no` | `edu_data_grant` | 数据共享授权 |
| `archive_no` | `edu_audit_archive_batch` | 归档批次 |
| `(task_no, retry_no)` | `edu_async_task_retry` | 重试记录 |
| `(request_id, object_id, action_type)` | `edu_audit_log` | 审计日志写入 |

## 4. 缓存

### 4.1 键与 TTL

| 域 | 键 | TTL | 失效时机 |
|---|---|---|---|
| 数据范围 | `edu:<tenant>:scope:user:<user_id>` | 10 分钟 | 角色 / 任职 / 任教 / 授权变更 |
| 班主任解析 | `edu:<tenant>:scope:class-head:<user_id>` | 10 分钟 | `edu_class.head_teacher_id` 变更 |
| 年级主任解析 | `edu:<tenant>:scope:grade-leader:<user_id>:<term_id>` | 10 分钟 | `edu_grade_leader` 变更 |
| 任教关系 | `edu:<tenant>:scope:teaching:<user_id>:<term_id>` | 10 分钟 | `edu_teaching_assignment` 变更 |
| 共享授权 | `edu:<tenant>:grant:<school_tenant_id>:<resource_code>` | 5 分钟 | 授权创建 / 撤销 / 到期 |
| 当前学年学期 | `edu:<tenant>:term:current` | 30 分钟 | 「设为当前」后立即删除 |
| 学科选科角色 | `edu:<tenant>:subject:roles` | 30 分钟 | 学科角色配置变更 |
| 选科配置 | `edu:<tenant>:stream:config:<term_id>` | 10 分钟 | `saveStreamConfig` 后删除 |
| 选科统计 | `edu:<tenant>:stream:stat:<term_id>:<range_hash>` | 5 分钟 | 提交 / 审批通过后删除 |
| 字典 | `edu:<tenant>:dict:<domain>:<school_id>` | 30 分钟 | 对应实体增删改 |

### 4.2 三条实现约束

1. **只缓存范围片段，不缓存「允许 / 拒绝」结论。** 范围变化时旧键自然不再命中（`C-04`）
2. **键必须带租户前缀**，禁止跨租户复用（`C-02`）
3. **首轮不引入本地缓存**（`C-05`）：多实例下本地缓存会造成「改了一个实例、另一个还放行」

### 4.3 失效失败的处理

删除缓存失败不回滚业务事务，也不阻塞响应；解析结果带范围版本号，版本变化即视为失效。
这样把「正确性」建立在版本号上，而不是建立在删除成功上。

## 5. 并发与锁

| 场景 | 手段 |
|---|---|
| 同一学生并发编班 / 调班 | 数据库唯一键 `uk_class_member_admin` + 行锁 |
| 同一学校并发升班 | 任务表行锁 + 状态检查（同源 / 目标学期唯一 running） |
| 同一学生并发提交选科 | `uk_student_stream` |
| 审批并发 | 条件更新（`WHERE status='pending'`），影响行数为 0 即已被处理 |
| 任务重复消费 | `task_no` 条件更新 + 唯一键 |
| 缓存击穿 | 单飞（single-flight）到数据库；不做「空值缓存」以外的特殊处理 |

## 6. 降级

| 故障 | 降级行为 | 用户可见表现 |
|---|---|---|
| 审计日志写入持续失败 | 业务进入只读降级（`AUD-Q-05`） | 顶部横幅提示「当前只读」，写操作禁用 |
| RabbitMQ 不可用 | 任务保持 `queued`，不丢任务 | 任务列表显示「排队中」，恢复后继续 |
| Redis 不可用 | 直接走数据库解析范围（牺牲性能换正确性） | 列表变慢，功能可用 |
| 导出 / 归档超时 | 转异步 | 页面上出现「已转异步」提示与任务入口 |

## 7. 可观测性

- 每个请求带 `request_id`，错误响应必须回传（错误码表约定）
- 每个异步任务带 `task_no`，可在任务中心按编号检索
- 审计日志记录 `trace_id`，用于把「页面操作 → 接口 → 消息 → 消费者 → 数据变更」串起来
- 教育域新增指标：任务积压数、死信数、日志写入失败率、范围解析缓存命中率

## 8. 与表结构的对应

运行时用到的表共 42 张，全部在 `database/schema.yaml` 有定义；
其中 `edu_async_task` / `edu_async_task_retry` / `edu_dead_letter_task` / `edu_file_ref` 属导入导出模块，
`edu_audit_log` / `edu_audit_change` / `edu_audit_archive_batch` 属审计模块。
