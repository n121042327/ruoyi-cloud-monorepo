# 阶段 5 · 详细设计与建表（索引）

## 1. 交付物清单

| 产物 | 路径 | 说明 |
|---|---|---|
| 表结构事实源 | `docs/40-detailed-design/database/schema.yaml` | 42 张主表 + 1 张派生表 + 2 个视图 |
| 逐表说明 | `docs/40-detailed-design/database/physical-schema.md` | 字段、类型、可空、默认值、注释、唯一键、索引、外键 |
| ER 图 | `docs/40-detailed-design/database/er-diagram.mmd` | 仅物理外键（32 条） |
| 领域对象映射 | `docs/40-detailed-design/database/domain-table-map.csv` | 领域对象 → 表 |
| 键与索引 | `docs/40-detailed-design/database/keys-and-indexes.md` | 唯一键 / 外键 / 索引 + 理由 |
| 结构检查 SQL | `docs/40-detailed-design/database/check-sql.sql` | 22 条结构与不变式检查 |
| 迁移脚本 | `docs/40-detailed-design/migrations/V1` ~ `V5` | 可重复执行的建表与外键脚本 |
| 迁移计划 | `docs/40-detailed-design/database/migration-plan.md` | 顺序、依赖、回滚、存量升级 |
| 接口契约 | `docs/40-detailed-design/api/openapi.yaml` | OpenAPI 3.0.3，173 个 operationId |
| 错误码 | `docs/40-detailed-design/api/error-codes.yaml` | 72 个错误码 |
| 页面动作映射 | `docs/40-detailed-design/page-action-api-map.yaml` | 页面动作 → 权限 → operationId → 组件 |
| 前端页面树 | `docs/40-detailed-design/frontend-page-tree.yaml` | 路由 / 组件归属 / 批次 / 文件 |
| 模块详细设计 | `docs/40-detailed-design/modules/<module>/design.md` | 11 个模块（事务、并发、校验、失败恢复） |
| 运行时设计 | `docs/40-detailed-design/runtime-design.md` | 异步任务、消息、缓存、幂等、降级 |
| 时序图 | `docs/40-detailed-design/diagrams/sequence/*.mmd` | 14 张 |
| 状态机 | `docs/40-detailed-design/diagrams/state/*.mmd` | 9 张 |
| 领域模型图 | `docs/40-detailed-design/diagrams/class/*.mmd` | 11 张 |

## 2. 门禁对照（`docs/00-governance/stage-inputs.yaml` 阶段 5）

| 门禁 | 状态 | 证据 |
|---|---|---|
| `physical-schema.md` 逐表覆盖字段、主键、唯一键、外键、索引 | 通过 | `database/physical-schema.md`；主表 42 张、派生表 1 张、视图 2 个、业务列 373 个 |
| ER 图与领域对象映射与建表脚本三者一致 | 通过 | 三者同源：全部由 `tools/gen_schema_artifacts.py` 从 `database/schema.yaml` 生成，不存在手工维护的第二份结构 |
| 迁移脚本在 MySQL 8 空库安装通过，存量升级路径验证通过 | 通过 | `evidence/stage5-detailed-design/2026-10-01_mysql8-empty-install.log`、`evidence/stage5-detailed-design/2026-10-01_mysql8-upgrade-path.log` |
| OpenAPI 通过校验工具检查 | 通过 | `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log`（`openapi-spec-validator`） |

## 3. 文档生成方式（可复现）

```bash
python tools/extract_api_catalog.py      # PRD 第 8 节 → 30-architecture/06-api-catalog.md
python tools/gen_schema_artifacts.py     # schema.yaml → 物理表 / ER / 键 / 检查 SQL / 迁移脚本
python tools/gen_api_and_map.py          # 接口目录 + 原型 → OpenAPI / 动作映射 / 页面树
python tools/gen_stage5_docs.py          # 本目录的模块详细设计 / 图 / 运行时设计 / 错误码
```

生成器只读取已冻结的上游产物；发现上游缺项时登记 `docs/00-governance/gap-register.yaml`，
不在生成器里补造业务规则。

## 4. 与阶段 4 的对应关系

| 阶段 4 产物 | 阶段 5 落点 |
|---|---|
| `05-data-ownership.md`（33 张表的归属） | `schema.yaml` 的 `module` 字段 + `domain-table-map.csv` |
| `06-api-catalog.md`（173 个 operationId） | `api/openapi.yaml` + `modules/<m>/design.md` 第 3 节 |
| `07-sync-async-boundary.md` | `runtime-design.md` 第 1、2 节 |
| `08-cache-strategy.md` | `runtime-design.md` 第 4 节 |
| `09-permission-architecture.md` | 各模块 design.md 第 9 节 |
| `03-module-division.md` | `modules/<m>/design.md` 的模块边界 |

## 5. 阶段 5 的架构级修订（实证发现）

| 编号 | 发现 | 处理 |
|---|---|---|
| 修订 1 | `edu_audit_log` 原计划用 MySQL 原生分区，但分区要求所有唯一键包含分区列，会破坏 `(request_id, object_id, action_type)` 的幂等语义 | 改为同构归档表 `edu_audit_log_archive` + 归档批次留痕 |
| 修订 2 | `edu_class_member` 原设计用 `(term_id, student_id, class_type)` 兜底行政班唯一性，但 MySQL 唯一索引无法表达「仅 administrative 生效」 | 拆表：本表只存行政班（`class_type` 恒 `administrative`），教学班成员在 `edu_teaching_class_member` |
| 修订 3 | `edu_student_field_change` 的「同一学生同一字段只允许一条待审核」原本只有普通索引，并发下可被绕过 | 增加生成列 `pending_guard` + 唯一键 `uk_sfc_pending` |
| 修订 4 | `edu_activation_code` 的「同一学生同时只有一个未使用激活码」原本只靠业务层 | 增加生成列 `active_guard` + 唯一键 `uk_activation_active` |

## 6. 后续阶段入口

- 阶段 6 生产前端：输入 `modules/*/design.md` + `page-action-api-map.yaml` + `frontend-page-tree.yaml` + `prototypes/high-fidelity/v1/**`
- 阶段 7 生产后端：输入 `modules/*/design.md` + `api/openapi.yaml` + `database/physical-schema.md` + `migrations/*.sql`
