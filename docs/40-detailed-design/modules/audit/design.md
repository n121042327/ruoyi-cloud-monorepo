# 审计与操作日志 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-AUD-*`（见 `docs/10-prd/modules/audit/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：操作日志（`edu_audit_log`）、变更明细（`edu_audit_change`）、归档批次
（`edu_audit_archive_batch`）、派生归档表与视图、运营访问记录、安全事件、归档检索。

不负责：业务对象的变更本身；日志的业务语义解释由各模块在写入时提供；
登录认证日志的主体在基线系统，本模块只做教育域扩展。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_audit_archive_batch` | 日志归档批次 | `tenant` | 5-2 | 9 | 1 | 0 |
| `edu_audit_change` | 日志变更明细 | `school` | 5-2 | 4 | 1 | 1 |
| `edu_audit_log` | 操作日志 | `school` | 5-2 | 14 | 1 | 0 |
| `edu_audit_log_archive` |  | `school` | 5-2 | 0 | 0 | 0 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listOperationLog` | GET | `/edu/audit/log/list` | 日志分页查询 | `audit.log` | 同步 |
| `getOperationLog` | GET | `/edu/audit/log/{id}` | 日志详情（含变更明细） | `audit.log` | 同步 |
| `listObjectChangeLog` | GET | `/edu/audit/object/{objectType}/{objectId}/timeline` | 对象变更时间线 | `audit.log` | 同步 |
| `exportOperationLog` | POST | `/edu/audit/log/export` | 日志导出 | `audit.log` | 异步 |
| `listOperatorAccess` | GET | `/edu/audit/operator-access/list` | 运营访问记录（租户侧） | `audit.log` | 同步 |
| `exportOperatorAccess` | POST | `/edu/audit/operator-access/export` | 运营访问记录导出 | `audit.log` | 异步 |
| `listSensitiveAccess` | GET | `/edu/audit/sensitive-access/list` | 敏感数据访问记录 | `audit.log` | 同步 |
| `listSecurityEvent` | GET | `/edu/audit/security-event/list` | 登录与安全事件 | `audit.log` | 同步 |
| `listArchiveBatch` | GET | `/edu/audit/archive/list` | 归档批次列表 | `audit.log` | 同步 |
| `searchArchivedLog` | POST | `/edu/audit/archive/search` | 归档区间检索 | `audit.log` | 异步 |

共 10 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 操作日志 | `PAGE-AUDIT-LOG-LIST` | `/edu/audit/log/list` | 2-8 | 13 | 3 | `views/edu/audit/audit_log_list/index.vue` |
| 运营访问记录 | `PAGE-AUDIT-OPS-ACCESS` | `/edu/audit/operator-access` | 2-8 | 7 | 3 | `views/edu/audit/audit_ops_access/index.vue` |
| 敏感数据访问记录 | `PAGE-AUDIT-SENSITIVE-ACCESS` | `/edu/audit/sensitive-access` | 2-8 | 6 | 3 | `views/edu/audit/audit_sensitive_access/index.vue` |
| 登录与安全事件 | `PAGE-AUDIT-SECURITY-EVENT` | `/edu/audit/security-event` | 2-8 | 4 | 2 | `views/edu/audit/audit_security_event/index.vue` |
| 归档管理 | `PAGE-AUDIT-ARCHIVE` | `/edu/audit/archive` | 2-8 | 5 | 3 | `views/edu/audit/audit_archive/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 业务写 + 日志 | 业务事务内写 `edu_audit_log`（+ `edu_audit_change`） | 与业务同事务，保证「业务成功则日志必在」 |
| 异步场景日志 | 消费者处理成功后写日志 | 通过 `edu.audit.exchange` 补偿 |
| 归档批次创建 | `edu_audit_archive_batch` + 数据搬迁 | 同构归档表，按批次搬迁后记录区间 |
| 归档检索 | 只读 | 命中归档区间时路由到归档表 |

## 6. 并发与幂等

- `uk_audit_idempotent (request_id, object_id, action_type)` 保证重试不产生重复日志
- `uk_audit_change (log_id, field_name)` 保证同一日志同一字段只有一条变更明细
- 归档与在线查询并发：归档只搬迁已关闭批次区间，查询按「在线 + 归档」并集去重
- 不使用 MySQL 原生分区：分区要求所有唯一键含分区列，会破坏幂等语义；
  改用同构归档表 `edu_audit_log_archive` + 归档批次留痕（已在 `schema.yaml` 记录）

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 变更明细 | 只记录发生变化的字段（`AUD-Q-02`） |
| 运营访问 | 对租户全量可见，用途说明必填（`AUD-Q-03`） |
| 保留策略 | ≥ 3 年；在线 12 个月后归档，归档仍可检索（`AUD-Q-04`） |
| 写入降级 | 日志写入持续失败时业务进入只读降级（`AUD-Q-05`） |
| 查询 / 导出上限 | 90 天 / 5 万行（`AUD-Q-06`） |
| 操作日志与审计日志 | 合并为一张表 + 视图区分（已确认 A 方案） |

## 8. 失败恢复与补偿

- 日志写入失败：重试；持续失败进入只读降级并告警，不静默丢日志
- 归档中断：批次保持 `running`，可重试；已搬迁数据不重复搬迁（按区间幂等）
- 归档查询超时：> 30 秒转异步（审计 PRD 第 9 节）
- 幂等冲突：视为重复请求，直接跳过写入

## 9. 权限与数据范围

- 校领导 `DS-04` 看本校日志；年级主任 / 班主任按各自范围看本人相关记录
- 平台运营访问记录对租户全量可见（`AUD-Q-03`）
- 安全事件仅平台运营与租户管理员可见
- 日志只追加，不提供修改与删除接口

## 10. 关联图与时序

- 时序图：`diagrams/sequence/audit-write.mmd`、`diagrams/sequence/audit-archive.mmd`
- 状态机：`diagrams/state/archive-batch.mmd`
- 领域模型：`diagrams/class/audit-domain.mmd`

## 11. 验收要点

1. 同一 `(request_id, object_id, action_type)` 重复写入只产生一条日志
2. 变更明细只包含实际变化的字段
3. 运营访问缺用途说明被拒绝
4. 日志写入持续失败时业务进入只读降级并告警
5. 归档后在线表与归档表检索结果并集完整、无重复

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
