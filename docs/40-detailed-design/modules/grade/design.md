# 年级管理 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-GRD-*`（见 `docs/10-prd/modules/grade/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：年级主体（`edu_grade`）、年级主任任职（`edu_grade_leader`）、升班的只读视图
（`getGradePromotionView`）。

不负责：升班执行（升班模块唯一入口）；班级与学生的实际归属（班级管理）；年级下的班级数量统计
由班级模块提供数据。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_grade` | 年级 | `school` | 5-1 | 7 | 2 | 0 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listGrade` | GET | `/edu/grade/list` | 分页查询 | `org.grade` | 同步 |
| `getGrade` | GET | `/edu/grade/{id}` | 详情 | `org.grade` | 同步 |
| `addGrade` | POST | `/edu/grade` | 新建 | `org.grade` | 同步 |
| `batchAddGrade` | POST | `/edu/grade/batch` | 按学段批量生成 | `org.grade` | 同步 |
| `updateGrade` | PUT | `/edu/grade` | 编辑 | `org.grade` | 同步 |
| `removeGrade` | DELETE | `/edu/grade/{id}` | 逻辑删除 | `org.grade` | 同步 |
| `archiveGrade` | POST | `/edu/grade/{id}/archive` | 归档 | `org.grade` | 同步 |
| `listGradeLeader` | GET | `/edu/grade/{id}/leader` | 年级主任列表 | `org.grade` | 同步 |
| `saveGradeLeader` | POST | `/edu/grade/{id}/leader` | 指定年级主任 | `org.grade` | 同步 |
| `removeGradeLeader` | DELETE | `/edu/grade/{id}/leader/{leaderId}` | 解除任职 | `org.grade` | 同步 |
| `getGradePromotionView` | GET | `/edu/grade/promotion-view` | 学年升级只读视图 | `org.grade` | 同步 |
| `exportGrade` | POST | `/edu/grade/export` | 年级列表导出（`CR-037` 补登记：模块级导出，≤ 2000 行直接下载、超出转异步） | `org.grade` | 异步 |

共 12 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 年级管理列表 | `PAGE-GRD-LIST` | `/edu/grade/list` | 2-2 | 40 | 8 | `views/edu/grade/grd_list/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改年级 | `edu_grade` 单表 | 校验学段序号映射 |
| 批量新增 | 每 100 条一个事务 | 部分失败返回逐行结果 |
| 指定 / 变更年级主任 | `edu_grade_leader` | 变更后失效 `scope:grade-leader:*` |
| 归档 / 删除年级 | `edu_grade`（+ 引用检查） | 有班级或学生时拒绝删除，只允许归档 |

## 6. 并发与幂等

- `uk_grade_seq (school_id, stage_code, enroll_year, grade_level)` 与
  `uk_grade_name (school_id, stage_code, grade_name)` 兜底并发建同名 / 同序号年级
- 删除与新增并发：删除先做引用检查再提交；新增班级时校验年级存在且未归档
- 年级主任变更与升班执行并发：升班读取年级时使用同一 `term_id` 视图，不缓存跨学期结果

## 7. 校验规则

| 规则 | 取值 |
|---|---|
| 学段序号固定映射 | 小学 1–6、初中 1–3、高中 1–3（`RV-GRD-03`），不可自由配置 |
| 年级与学段对应 | 固定，年级不得跨学段（`RV-GRD-03`） |
| 入学年份 | 必填，用于升班与「3+1+2」推算 |
| 年级名称 | 同一学校同一学段内唯一 |
| 年级主任 | 必须是本校在职教师；同一学期同一教师可负责多个年级 |
| 删除权限 | 教务主任与租户管理员可删空年级（已确认） |

## 8. 失败恢复与补偿

- 删除失败：整体回滚，返回引用清单（班级数 / 学生数），前端给出跳转入口
- 归档后误操作：支持解除归档（`archiveGrade` 反向接口），留痕
- 年级主任变更失败：保留原任职，不出现空档

## 9. 权限与数据范围

- 年级主任 `DS-05` 看负责年级；校领导 `DS-04` 看本校全部年级
- 年级列表按 `school_id` + 数据范围过滤；平台运营 `DS-01` 只读并留痕
- 升班只读视图与年级列表使用同一套范围解析，避免「列表看不到、视图能看到」

## 10. 关联图与时序

- 状态机：`diagrams/state/promotion-task.mmd`（年级在升班中的只读角色）
- 领域模型：`diagrams/class/grade-domain.mmd`

## 11. 验收要点

1. 建立「小学 7 年级」被拒绝（学段序号映射固定）
2. 同一学校同一学段重复年级名被唯一键拒绝
3. 有班级的年级执行删除被拒绝，并能看到阻塞它的班级
4. 年级主任在升班只读视图中只能看到自己负责的年级

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
