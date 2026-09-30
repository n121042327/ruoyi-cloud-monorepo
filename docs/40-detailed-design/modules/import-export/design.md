# 导入导出与异步任务 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-IMP-*`（见 `docs/10-prd/modules/import-export/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：导入模板（`edu_import_template`）、导入批次与错误行（`edu_import_batch` / `edu_import_error`）、
异步任务与重试（`edu_async_task` / `edu_async_task_retry`）、死信（`edu_dead_letter_task`）、
文件引用（`edu_file_ref`）、通用导出。

不负责：各业务模块的业务校验（由对应模块在导入执行时提供校验器）；文件存储本体（对象存储）。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_async_task` | 异步任务 | `school` | 5-2 | 19 | 1 | 0 |
| `edu_async_task_retry` | 任务重试记录 | `school` | 5-2 | 5 | 1 | 1 |
| `edu_dead_letter_task` | 死信任务 | `school` | 5-2 | 11 | 1 | 0 |
| `edu_file_ref` | 文件引用 | `school` | 5-2 | 11 | 1 | 0 |
| `edu_import_batch` | 导入批次 | `school` | 5-2 | 14 | 1 | 1 |
| `edu_import_error` | 导入行结果（失败与跳过明细） | `school` | 5-2 | 7 | 1 | 0 |
| `edu_import_template` | 导入模板 | `tenant` | 5-2 | 6 | 1 | 0 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listImportTemplate` | GET | `/edu/import/template` | 模板清单与当前版本 | `data.import` | 同步 |
| `downloadImportTemplate` | GET | `/edu/import/template/{module}` | 模板下载 | `data.import` | 同步 |
| `validateImportFile` | POST | `/edu/import/validate` | 上传并同步校验 | `data.import` | 同步 |
| `executeImport` | POST | `/edu/import/execute` | 确认执行（异步） | `data.import` | 异步 |
| `downloadImportFailedRows` | GET | `/edu/import/{batchNo}/failed-rows` | 失败行下载 | `data.import` | 同步 |
| `downloadImportResult` | GET | `/edu/import/{batchNo}/result` | 结果摘要与对照表 | `data.import` | 同步 |
| `exportData` | POST | `/edu/export` | 导出（同步或异步） | `data.export` | 异步 |
| `listAsyncTask` | GET | `/edu/async-task/list` | 任务列表 | `data.async_task` | 同步 |
| `getAsyncTask` | GET | `/edu/async-task/{taskNo}` | 任务详情 | `data.async_task` | 同步 |
| `cancelAsyncTask` | POST | `/edu/async-task/{taskNo}/cancel` | 取消排队中的任务 | `data.async_task` | 同步 |
| `retryAsyncTask` | POST | `/edu/async-task/{taskNo}/retry` | 重试 | `data.async_task` | 同步 |
| `downloadTaskResult` | GET | `/edu/async-task/{taskNo}/file/{fileId}` | 结果文件下载（短时签名） | `data.async_task` | 同步 |
| `listDeadLetterTask` | GET | `/edu/async-task/dead-letter` | 死信任务列表 | `data.async_task` | 同步 |
| `replayDeadLetterTask` | POST | `/edu/async-task/dead-letter/{taskNo}/replay` | 死信重放 | `data.async_task` | 同步 |

共 14 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 导入向导 | `PAGE-IMP-WIZARD` | `/edu/import/wizard` | 2-4 | 12 | 6 | `views/edu/import-export/imp_wizard/index.vue` |
| 异步任务列表 | `PAGE-IMP-TASK-LIST` | `/edu/async-task/list` | 2-9 | 15 | 5 | `views/edu/import-export/imp_task_list/index.vue` |
| 死信任务 | `PAGE-IMP-DEADLETTER` | `/edu/async-task/dead-letter` | 2-9 | 6 | 3 | `views/edu/import-export/imp_deadletter/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 上传 + 结构校验 | `edu_import_batch` + `edu_async_task` | 同事务创建批次与任务；校验结果写批次 |
| 导入执行 | 每 500 行一个事务 | 错误行独立提交，保证部分成功可续跑 |
| 导出 | `edu_async_task` + `edu_file_ref` | 任务完成时写文件引用 |
| 任务重试 | `edu_async_task_retry` + 任务状态 | 同事务记录重试序号 |
| 死信重放 | `edu_dead_letter_task` 状态 + 原任务 | 复用原幂等键，不新建任务 |

## 6. 并发与幂等

- `uk_import_batch_no`、`uk_async_task_no`、`uk_task_retry (task_no, retry_no)`、`uk_dead_letter_task` 保证幂等
- 消费幂等：先按 `task_no` / `batch_no` 查状态，已成功的消息直接 ack；唯一索引兜底
- 并发配额：同一用户 1 个、同一学校 3 个，超出排队（`REQ-IMP-047` / `REQ-IMP-048`）
- 任务状态更新使用条件更新（`WHERE task_status = 'running'`），避免重复消费覆盖结果

## 7. 校验规则

| 规则 | 取值 |
|---|---|
| 同步导入上限 | 5000 行 / 10 MB / 30 秒校验（`IMP-Q-01`） |
| 同步导出上限 | 2000 行，超出转异步 |
| 学生导入模板 | 14 列（已确认） |
| 模板过期 | 过期后仍可下载但强提示（`IMP-Q-05`） |
| 文件有效期 | 导出与结果文件 7 天，任务元数据 90 天（`IMP-Q-02`） |
| 重试 | 最大 3 次，退避 30s / 2m / 8m（`REQ-IMP-039`） |
| 平台运营导出 | 默认不可导出，需逐次授权并留痕（`IMP-Q-04`） |

## 8. 失败恢复与补偿

- 超过重试上限：进 `edu.task.dead`，落 `edu_dead_letter_task`，运维在死信页重放
- 消息丢失：定时巡检把长时间 `queued` / `running` 的任务标记异常并可重试（`NFR-MQ-01`）
- 文件过期：任务元数据保留 90 天，文件失效后提示重新生成
- 导出失败：任务置 `failed`，重试复用 `task_no`

## 9. 权限与数据范围

- 任务列表默认只看本人任务；学校管理员可看本校任务（按 `DS-04`）
- 下载结果文件时重新解析数据范围（`DS-DENY-04`），文件行数可能少于列表
- 平台运营下载需逐次授权，授权记录对租户可见（`AUD-Q-03`）

## 10. 关联图与时序

- 时序图：`diagrams/sequence/import-execute.mmd`、`diagrams/sequence/export-generate.mmd`
- 状态机：`diagrams/state/async-task.mmd`
- 领域模型：`diagrams/class/task-domain.mmd`

## 11. 验收要点

1. 同一 `batch_no` 重复执行不产生重复数据
2. 重试 3 次后进死信，重放复用原幂等键
3. 超出并发配额的任务进入排队而不是失败
4. 导出下载时范围变化，文件行数随之减少并给出说明
5. 过期模板仍可下载但出现强提示

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
