# 学科与配置 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-SUB-*`（见 `docs/10-prd/modules/subject/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：学科主体（`edu_subject`）、学段启用（`edu_subject_stage`）、选科角色配置（`stream_enabled` /
`stream_role`）、批量初始化与引用检查、启用 / 停用。

不负责：选科结果（选科模块）、任教关系中的学科校验只读取本模块数据、课程表与排课（首轮不做）。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_subject` | 学科 | `school` | 5-2 | 6 | 2 | 0 |
| `edu_subject_stage` | 学科与学段启用 | `school` | 5-2 | 3 | 1 | 1 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listSubject` | GET | `/edu/subject/list` | 学科列表 | `org.subject` | 同步 |
| `getSubject` | GET | `/edu/subject/{id}` | 详情 | `org.subject` | 同步 |
| `addSubject` | POST | `/edu/subject` | 新建 | `org.subject` | 同步 |
| `updateSubject` | PUT | `/edu/subject` | 编辑 | `org.subject` | 同步 |
| `batchInitSubject` | POST | `/edu/subject/batch-init` | 按学段批量初始化 | `org.subject` | 同步 |
| `saveSubjectStreamRole` | POST | `/edu/subject/{id}/stream-role` | 配置选科角色 | `org.subject` | 同步 |
| `saveSubjectStage` | POST | `/edu/subject/{id}/stage` | 配置学段启用 | `org.subject` | 同步 |
| `disableSubject` | POST | `/edu/subject/{id}/disable` | 停用 | `org.subject` | 同步 |
| `enableSubject` | POST | `/edu/subject/{id}/enable` | 启用 | `org.subject` | 同步 |
| `removeSubject` | DELETE | `/edu/subject/{id}` | 逻辑删除（校验引用） | `org.subject` | 同步 |
| `checkSubjectReference` | GET | `/edu/subject/{id}/reference` | 引用检查 | `org.subject` | 同步 |
| `listSubjectOption` | GET | `/edu/subject/option` | 供各模块使用的下拉清单（按学段过滤） | `org.subject` | 同步 |

共 12 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 学科列表 | `PAGE-SUB-LIST` | `/edu/subject/list` | 2-6 | 18 | 7 | `views/edu/subject/sub_list/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改学科 | `edu_subject` 单表 | 编码与名称双唯一 |
| 批量初始化 | 每 50 条一个事务 | 幂等：已存在的编码跳过并计入结果 |
| 保存学段启用 | `edu_subject_stage` 删除 + 插入 | 同事务；变更后失效 `subject:roles` 缓存 |
| 保存选科角色 | `edu_subject` 单表 | 变更后失效 `subject:roles` |
| 停用 | `edu_subject` 状态 + 引用检查 | 被任教关系或选科结果引用时拒绝 |

## 6. 并发与幂等

- `uk_subject_code (tenant_id, school_id, subject_code)` 与 `uk_subject_name (school_id, subject_name)` 兜底并发重复
- `uk_subject_stage (subject_id, stage_code)` 保证学段启用幂等
- 学科采用「一条主体 + 学段启用表」结构（`RV-SUB-04`）；不得按学段拆成多条主体记录，
  否则与「编码租户内唯一」冲突

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 编码 | 租户内唯一，创建后不可修改 |
| 名称 | 同一学校内唯一 |
| 学段启用 | 至少启用一个学段；停用前检查是否仍有班级 / 选科在用 |
| 选科角色 | 仅高中学科可配置 `stream_role`；首选集合与再选集合固定（`RV-SUB-03`） |
| 停用 | 被引用时拒绝，返回引用清单（任教关系数 / 选科结果数） |

## 8. 失败恢复与补偿

- 批量初始化部分失败：返回逐行结果，已成功记录保留，重跑幂等
- 缓存失效失败：`subject:roles` 版本号兜底
- 停用被拒：返回阻塞引用，前端提供跳转

## 9. 权限与数据范围

- 全校可见作为字典（用于筛选与录入）；配置类操作仅教务主任 / 租户管理员
- 集团不默认读取下属学校学科配置

## 10. 关联图与时序

- 领域模型：`diagrams/class/subject-domain.mmd`

## 11. 验收要点

1. 同一租户重复学科编码被拒绝
2. 按学段拆多条主体记录被结构约束阻止（编码唯一）
3. 被任教关系引用的学科停用被拒绝
4. 修改选科角色后 `subject:roles` 缓存立即失效

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
