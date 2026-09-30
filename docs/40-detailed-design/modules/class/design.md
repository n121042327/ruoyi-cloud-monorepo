# 班级管理 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-CLS-*`（见 `docs/10-prd/modules/class/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：行政班与教学班容器（`edu_class`）、行政班成员（`edu_class_member`）、教学班主体
（`edu_teaching_class`）与成员（`edu_teaching_class_member`）、班主任指派、编班 / 调班 / 移出、
班级合并与停用、编班表导入。

不负责：

- 学生主体与在校记录（学生管理）
- 升班执行（升班模块）；本模块承接升班结果写入的班级关系
- 选科决策（选科模块）；教学班生成由选科模块**触发**，成员写入由本模块执行（`DP-01`）

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_class` | 班级（行政班 / 教学班容器） | `school` | 5-1 | 13 | 2 | 3 |
| `edu_class_member` | 班级成员关系（花名册） | `school` | 5-1 | 9 | 1 | 2 |
| `edu_teaching_class` | 教学班 | `school` | 5-1 | 6 | 1 | 1 |
| `edu_teaching_class_member` | 教学班成员关系 | `school` | 5-1 | 8 | 1 | 2 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listClass` | GET | `/edu/class/list` | 分页查询 | `org.class` | 同步 |
| `getClass` | GET | `/edu/class/{id}` | 详情 | `org.class` | 同步 |
| `addClass` | POST | `/edu/class` | 新建 | `org.class` | 同步 |
| `batchAddClass` | POST | `/edu/class/batch` | 批量生成 | `org.class` | 异步 |
| `updateClass` | PUT | `/edu/class` | 编辑 | `org.class` | 同步 |
| `removeClass` | DELETE | `/edu/class/{id}` | 逻辑删除 | `org.class` | 同步 |
| `disableClass` | POST | `/edu/class/{id}/disable` | 停用 | `org.class` | 同步 |
| `mergeClass` | POST | `/edu/class/merge` | 合并 | `org.class` | 同步 |
| `listClassRoster` | GET | `/edu/class/{id}/roster` | 花名册 | `org.class` | 同步 |
| `addClassRoster` | POST | `/edu/class/{id}/roster` | 添加学生 | `org.class` | 同步 |
| `removeClassRoster` | DELETE | `/edu/class/{id}/roster/{studentId}` | 移出学生 | `org.class` | 同步 |
| `transferClass` | POST | `/edu/class/roster/transfer` | 调班 | `org.class` | 同步 |
| `assignHeadTeacher` | POST | `/edu/class/{id}/head-teacher` | 指定班主任 | `org.class` | 同步 |
| `listClassTeachingAssignment` | GET | `/edu/class/{id}/teaching-assignment` | 任课教师（只读） | `org.class` | 同步 |
| `importRosterValidate` | POST | `/edu/class/roster/import/validate` | 编班校验 | `org.class` | 同步 |
| `importRosterExecute` | POST | `/edu/class/roster/import/execute` | 编班执行（异步） | `org.class` | 异步 |
| `exportClassRoster` | POST | `/edu/class/{id}/roster/export` | 花名册导出 | `org.class` | 异步 |
| `listTeachingClass` | GET | `/edu/teaching-class/list` | 教学班列表 | `org.teaching_class` | 同步 |
| `addTeachingClass` | POST | `/edu/teaching-class` | 新建教学班 | `org.teaching_class` | 同步 |
| `getTeachingClass` | GET | `/edu/teaching-class/{id}` | 教学班详情（`CR-017` 补登记：详情抽屉的数据来源） | `org.teaching_class` | 同步 |
| `disableTeachingClass` | POST | `/edu/teaching-class/{id}/disable` | 停用教学班（`CR-017` 补登记：原因必填、写审计、历史成员保留） | `org.teaching_class` | 同步 |
| `listTeachingClassRoster` | GET | `/edu/teaching-class/{id}/roster` | 教学班成员清单（`CR-017` 补登记：只读，成员写入仍由生成流程触发） | `org.teaching_class` | 同步 |

共 22 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 班级管理列表 | `PAGE-CLS-LIST` | `/edu/class/list` | 2-3 | 30 | 9 | `views/edu/class/cls_list/index.vue` |
| 添加学生 | `PAGE-CLS-ROSTER-ADD` | `/edu/class/roster/add` | 2-3 | 24 | 2 | `views/edu/class/cls_roster_add/index.vue` |
| 批量迁学生 | `PAGE-CLS-MOVE` | `/edu/class/move-students` | 2-3 | 15 | 3 | `views/edu/class/cls_move/index.vue` |
| 编班表导入向导 | `PAGE-CLS-ROSTER-IMPORT` | `/edu/class/import-roster` | 2-4 | 12 | 6 | `views/edu/class/cls_roster_import/index.vue` |
| 教学班管理 | `PAGE-CLS-TEACHING` | `/edu/class/teaching` | 2-7 | 11 | 3 | `views/edu/class/cls_teaching/index.vue` |
| 班级合并 | `PAGE-CLS-MERGE` | `/edu/class/merge` | deferred | 0 | 0 | `views/edu/class/cls_merge/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改班级 | `edu_class` 单表 | 校验班主任与班级类型组合 |
| 编班 / 调班 | `edu_class_member` 单表（调班为一次 UPDATE） | `term_id + student_id` 唯一，保证一个学生一个行政班 |
| 批量编班 | 每 200 条一个事务 | 部分失败返回逐行结果 |
| 合并班级 | 源班成员移入目标班 + 源班停用 | 同一事务；目标班容量只提示不拦截 |
| 指定 / 变更班主任 | `edu_class.head_teacher_id` | 变更后失效 `scope:class-head:*` 与 `scope:user:*` |
| 教学班成员写入 | `edu_teaching_class_member` 批量插入 | 由选科模块触发的异步任务调用，幂等键为 `generate_task_no` |

## 6. 并发与幂等

- `uk_class_member_admin (term_id, student_id)` 是「一个学生一个行政班」的数据库级保证，
  并发调班时后提交者被拒绝，不会出现双班归属
- `uk_class_name (school_id, term_id, stage_code, class_name)` 与
  `uk_class_teaching (school_id, term_id, subject_combination, class_type)` 保证教学班幂等生成
- `uk_tclass_member (teaching_class_id, student_id)` 保证教学班成员幂等
- 班主任变更与权限缓存失效在同一事务提交后触发，失败由范围版本号兜底（`C-04`）
- 容量只提示不拦截（`BR-CLASS-005`），因此不存在「容量锁」

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 班级名称 | 同一学校同一学期同一学段内唯一（`BR-CLASS-003`） |
| 学生归属 | 同一学年学期一名学生只能属于一个行政班（`BR-STU-003`） |
| 班主任 | 必须是本校在职教师；同一行政班同一学期只有一个在任班主任；教学班不设班主任 |
| 班主任权限入口 | 唯一写入入口是本模块（`DP-01`）；教师管理只读 |
| 教学班 | 不参与 `DS-06` 解析（`BR-CLASS-007` / `REQ-CLS-039`） |
| 删除 | 有在读学生时不允许删除，只允许停用（`BR-CLASS-006`） |
| 学生班级归属 | 唯一写入入口是本模块（编班 / 调班 / 移出），学生管理只读 |

## 8. 失败恢复与补偿

- 批量编班部分失败：已提交批次保留，失败行可下载后重提；批次号幂等
- 调班失败：事务回滚，学生留在原班
- 教学班生成失败：异步任务重试（复用 `generate_task_no`），不产生半成品教学班
- 合并班级中途失败：整体回滚，源班成员与状态不变

## 9. 权限与数据范围

- 班主任 `DS-06` 看本班；年级主任 `DS-05` 看本年级全部班级；校领导 `DS-04` 看本校
- 任课教师 `DS-08` 只看本人任教班级的必要资料，且**不能导出名单**
- 教学班不参与班级维度的数据范围继承，成员可见性由行政班与任教关系共同决定

## 10. 关联图与时序

- 时序图：`diagrams/sequence/class-roster-import.mmd`、`diagrams/sequence/class-transfer.mmd`
- 状态机：`diagrams/state/class-status.mmd`
- 领域模型：`diagrams/class/class-domain.mmd`

## 11. 验收要点

1. 同一学生同一学期第二次编入行政班被 `uk_class_member_admin` 拒绝
2. 同一学期同一组合重复生成教学班不产生重复行
3. 教学班不触发 `DS-06`：班主任身份不影响教学班可见性
4. 有在读学生的班级执行删除被拒绝，停用成功
5. 变更班主任后原班主任立即失去该班数据范围（缓存失效验证）

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
