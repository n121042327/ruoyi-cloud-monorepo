# 3+1+2 选科与教学班 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-STR-*`（见 `docs/10-prd/modules/stream/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：选科配置（`edu_stream_config`）、学生选科结果（`edu_student_stream`）、选科变更申请
（`edu_stream_change_request`）、变更历史（`edu_stream_history`）、教学班生成触发与统计。

不负责：

- 教学班与成员的物理写入 —— 由班级模块执行（`edu_teaching_class` / `edu_teaching_class_member`），
  本模块只创建生成任务并校验结果（`REQ-STR-056` / `DP-01`）
- 学科主体与选科角色定义（学科与配置）
- 行政班归属（班级管理）；选科组合不等于行政班

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_stream_change_request` | 选科变更申请 | `school` | 5-2 | 16 | 1 | 1 |
| `edu_stream_config` | 选科配置 | `school` | 5-2 | 5 | 1 | 1 |
| `edu_stream_history` | 选科历史 | `school` | 5-2 | 9 | 0 | 1 |
| `edu_student_stream` | 学生选科 | `school` | 5-2 | 6 | 1 | 1 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `getStreamConfig` | GET | `/edu/stream/config` | 查询选科配置 | `stream.*` | 同步 |
| `saveStreamConfig` | POST | `/edu/stream/config` | 保存选科配置 | `stream.*` | 同步 |
| `getStreamOption` | GET | `/edu/stream/option` | 可选科目与规则 | `stream.*` | 同步 |
| `getMyStream` | GET | `/edu/stream/my` | 学生查询本人选科 | `stream.*` | 同步 |
| `submitMyStream` | POST | `/edu/stream/my` | 学生提交选科 | `stream.*` | 同步 |
| `updateMyStream` | PUT | `/edu/stream/my` | 截止前自助修改 | `stream.*` | 同步 |
| `listStreamSelection` | GET | `/edu/stream/selection/list` | 选科清单 | `stream.*` | 同步 |
| `getStreamStat` | GET | `/edu/stream/stat` | 组合分布统计 | `stream.*` | 同步 |
| `listUnselectedStudent` | GET | `/edu/stream/unselected` | 未选科学生清单 | `stream.*` | 同步 |
| `listStreamChangeRequest` | GET | `/edu/stream/change/list` | 变更申请列表 | `stream.*` | 同步 |
| `addStreamChangeRequest` | POST | `/edu/stream/change` | 发起变更申请 | `stream.*` | 同步 |
| `cancelStreamChangeRequest` | POST | `/edu/stream/change/{id}/cancel` | 撤回申请 | `stream.*` | 同步 |
| `approveStreamChangeRequest` | POST | `/edu/stream/change/{id}/approve` | 审批 | `stream.*` | 同步 |
| `listStreamHistory` | GET | `/edu/stream/history` | 选科历史 | `stream.*` | 同步 |
| `exportStreamSelection` | POST | `/edu/stream/export` | 导出选科结果 | `stream.*` | 异步 |
| `previewTeachingClassGenerate` | POST | `/edu/stream/teaching-class/preview` | 教学班生成预览 | `stream.*` | 异步 |
| `executeTeachingClassGenerate` | POST | `/edu/stream/teaching-class/generate` | 触发生成（写入由班级模块执行） | `stream.*` | 异步 |

共 17 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 选科配置 | `PAGE-STR-CONFIG` | `/edu/stream/config` | 2-7 | 4 | 3 | `views/edu/stream/str_config/index.vue` |
| 学生选科 | `PAGE-STR-STUDENT` | `/edu/stream/selection` | 2-7 | 9 | 1 | `views/edu/stream/str_student/index.vue` |
| 选科清单 | `PAGE-STR-LIST` | `/edu/stream/list` | 2-7 | 8 | 4 | `views/edu/stream/str_list/index.vue` |
| 组合分布统计 | `PAGE-STR-STAT` | `/edu/stream/stat` | 2-7 | 7 | 4 | `views/edu/stream/str_stat/index.vue` |
| 变更审批待办 | `PAGE-STR-APPROVE` | `/edu/stream/approve` | 2-7 | 9 | 4 | `views/edu/stream/str_approve/index.vue` |
| 按组合生成教学班 | `PAGE-STR-GEN-CLASS` | `/edu/stream/generate-class` | 2-7 | 11 | 3 | `views/edu/stream/str_gen_class/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 保存选科配置 | `edu_stream_config` 单表 | 保存后立即失效 `stream:config:<term>` 缓存 |
| 学生提交选科 | `edu_student_stream` + `edu_stream_history` | 同事务；`term_id + student_id` 唯一 |
| 变更申请 | `edu_stream_change_request` 单表 | 状态机驱动的审批流 |
| 审批通过 | 申请状态 + `edu_student_stream` + 历史 | 同事务；失效选科统计缓存 |
| 生成教学班 | 预览同步；生成为异步任务 | 任务写入由班级模块执行，本模块校验幂等键与结果 |

## 6. 并发与幂等

- `uk_student_stream (term_id, student_id)` 保证同一学期一名学生只有一份选科结果
- `uk_stream_request_no (request_no)` 保证申请幂等；同一学生同一学期同时只允许一条待审申请
  （由业务层加锁 + 申请状态索引保证，见 `idx_stream_request_student`）
- 教学班生成幂等：`uk_class_teaching` 与 `uk_tclass_member` 在班级模块兜底
- 统计缓存按「范围摘要哈希」分键，范围变化自然落到新键（`C-04`）

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 首选学科 | 固定物理 / 历史（`RV-SUB-03`） |
| 再选学科 | 固定化学 / 生物 / 思想政治 / 地理，不可自由配置（`RV-SUB-03`） |
| 学段 | 仅高中适用；非高中学生不可提交 |
| 截止时间 | 学校级可配置；逾期需校级管理员审批（已确认） |
| 变更次数 | 按配置限制；每次变更写历史 |
| 未选科 | `listUnselectedStudent` 可查应选未选学生；统计与明细口径一致（`REQ-STR-050`） |
| 组合校验 | 首选 1 门 + 再选 2 门，共 3 门；不得重复 |

## 8. 失败恢复与补偿

- 生成教学班失败：异步任务重试，幂等键 `generate_task_no`，不产生半成品
- 审批并发：乐观锁（`update_time` 或状态条件更新）拒绝重复审批
- 缓存失效失败：统计结果带范围摘要，范围变化即落新键
- 学生撤单：仅待审状态可撤，历史保留

## 9. 权限与数据范围

- 学生本人：`getMyStream` / `submitMyStream` / `updateMyStream` 只看自己
- 年级主任 `DS-05`、班主任 `DS-06`、校领导 `DS-04` 按范围看统计与名单
- 统计接口与明细接口使用同一 `DataScopeResolver` 结果（`DS-DENY-08`）

## 10. 关联图与时序

- 时序图：`diagrams/sequence/stream-submit.mmd`、`diagrams/sequence/teaching-class-generate.mmd`
- 状态机：`diagrams/state/stream-change-request.mmd`
- 领域模型：`diagrams/class/stream-domain.mmd`

## 11. 验收要点

1. 首选非物理 / 历史被拒绝；再选超出固定集合被拒绝
2. 同一学生同一学期第二次提交选科被 `uk_student_stream` 拒绝（走变更申请）
3. 逾期提交未走审批时被拒绝，走审批后成功
4. 重复生成教学班不产生重复行
5. 统计数与明细数一致（同一用户同一范围）

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
