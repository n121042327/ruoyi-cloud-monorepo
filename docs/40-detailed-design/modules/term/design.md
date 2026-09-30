# 学年学期 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-TERM-*`（见 `docs/10-prd/modules/term/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：学年（`edu_academic_year`）、学期（`edu_term`）、当前学期设置、学年归档与解除归档、引用检查。

不负责：节假日与作息（首轮不做）、课程表（首轮不做）、升班（引用学期但不执行）。

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_academic_year` | 学年 | `school` | 5-2 | 4 | 1 | 0 |
| `edu_term` | 学期 | `school` | 5-2 | 7 | 1 | 1 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listAcademicYear` | GET | `/edu/term/year/list` | 学年列表 | `org.term` | 同步 |
| `getAcademicYear` | GET | `/edu/term/year/{id}` | 学年详情 | `org.term` | 同步 |
| `addAcademicYear` | POST | `/edu/term/year` | 新建学年 | `org.term` | 同步 |
| `updateAcademicYear` | PUT | `/edu/term/year` | 编辑学年 | `org.term` | 同步 |
| `listTerm` | GET | `/edu/term/list` | 学期列表 | `org.term` | 同步 |
| `saveTerm` | POST | `/edu/term` | 新增 / 编辑学期 | `org.term` | 同步 |
| `removeTerm` | DELETE | `/edu/term/{id}` | 删除学期（校验引用） | `org.term` | 同步 |
| `getCurrentTerm` | GET | `/edu/term/current` | 当前学年学期 | `org.term` | 同步 |
| `setCurrentTerm` | POST | `/edu/term/{id}/set-current` | 设为当前 | `org.term` | 同步 |
| `checkTermReference` | GET | `/edu/term/{id}/reference` | 引用检查 | `org.term` | 同步 |
| `archiveAcademicYear` | POST | `/edu/term/year/{id}/archive` | 归档学年 | `org.term` | 同步 |
| `revokeArchiveAcademicYear` | POST | `/edu/term/year/{id}/archive/revoke` | 撤销归档 | `org.term` | 同步 |

共 12 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 学年学期列表 | `PAGE-TERM-LIST` | `/edu/term/list` | 2-6 | 11 | 5 | `views/edu/term/term_list/index.vue` |
| 学期管理 | `PAGE-TERM-TERMS` | `/edu/term/terms` | 2-6 | 9 | 4 | `views/edu/term/term_terms/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增 / 修改学年 | `edu_academic_year` 单表 | 校验日期连续性 |
| 新增 / 修改学期 | `edu_term` 单表 | 校验属于同一学年且日期不重叠 |
| 设为当前学期 | `edu_term` 批量更新 | 同一事务内先清空再设置，保证唯一 |
| 归档学年 | `edu_academic_year` 状态 | 归档前引用检查 |
| 解除归档 | 同上 | 留痕 |

## 6. 并发与幂等

- `uk_academic_year_code (tenant_id, school_id, academic_year_code)`、`uk_term_code (academic_year_id, term_code)` 兜底重复
- 「当前学期唯一」：同一学校同一时刻只有一条 `is_current=1`，由事务 + 应用层唯一性检查保证；
  `idx_term_school_current (school_id, is_current)` 提供查询支持
- 并发「设为当前」：对学校维度加锁（或对 `edu_term` 做条件更新），后者覆盖前者而不是并存

## 7. 校验规则

| 规则 | 说明 |
|---|---|
| 学年日期连续 | 必须连续不重叠：前一年结束日 = 后一年开始日 − 1 天（`RV-TERM-08`） |
| 学期归属 | 学期必须属于同一学年，且日期落在学年范围内 |
| 当前学期 | 同一学校同一时刻只有一条 |
| 归档 | 有未结束的升班任务或未归档班级时，需提示影响范围（不强制阻断，记录确认） |
| 引用检查 | `checkTermReference` 返回班级 / 学生 / 任务数量 |

## 8. 失败恢复与补偿

- 归档失败：回滚，保持未归档
- 解除归档：恢复可编辑，留痕
- 「设为当前」失败：原当前学期不变

## 9. 权限与数据范围

- 全校可见；配置操作仅租户管理员 / 教务主任
- 当前学期缓存键 `edu:<tenant>:term:current`，设为当前后立即删除

## 10. 关联图与时序

- 状态机：`diagrams/state/term-archive.mmd`
- 领域模型：`diagrams/class/term-domain.mmd`

## 11. 验收要点

1. 学年日期出现重叠或断档被拒绝
2. 并发设置当前学期后只保留一条 `is_current=1`
3. 已归档学年不能新增学期
4. 解除归档后恢复可编辑并留痕

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
