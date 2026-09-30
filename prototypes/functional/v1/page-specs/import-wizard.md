# 导入向导（含模板 / 校验 / 执行三个区块）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-IMP-WIZARD`（`pages/import-wizard.html`）+ 页内区块 `PAGE-IMP-TEMPLATE` / `PAGE-IMP-VALIDATE` / `PAGE-IMP-EXECUTE` |
| 所属模块 | 导入导出与异步任务（`import-export`） |
| 页面类型 | wizard（四步独立页；三个区块是同一页内的 `data-page` 段落） |
| 骨架模板 | `TPL-WIZARD` |
| 所属批次 | 2-4 |
| 上游需求 | `REQ-IMP-001` ~ `REQ-IMP-023`、`REQ-IMP-050`、`REQ-IMP-052` |
| 上游规则 | `BR-IMP-001`（两阶段）、`BR-IMP-002`（幂等）、`BR-IMP-003`、`BR-IMP-007`（模板版本）、`BR-IMP-008`、`BR-STU-019`（模板不含学号） |
| 权限资源 | `data.import:import`；查看任务 `data.async_task:read` |
| 数据范围 | 教务主任 / 租户管理员 / 超级管理员可发起导入（`DS-04` / 平台级）；任课教师与平台运营进无权限态（`DS-07` / `DS-01`） |
| 交付证据 | `evidence/stage2-prototype/verify-import-login.html`、`import-wizard_*.png` |
| 已确认口径 | `IMP-Q-01`（5000 行 / 10 MB / 30 秒同步校验）、`IMP-Q-02`（结果与导出文件 7 天、任务元数据 90 天）、`IMP-Q-03`（同一用户 1 个 / 同一学校 3 个，超出排队）、`IMP-Q-04`（平台运营默认不可导出，需逐次授权）、`IMP-Q-05`（模板过期仍可下载但强提示） |

## 1. 页面目的

把「选模板 → 上传并同步校验 → 看校验结果 → 异步执行」四步做成一条固定路径（`BR-IMP-001`），
让教务主任在**执行前**看到可执行行数与失败明细（`REQ-IMP-012`），确认后由异步任务完成写入（`REQ-IMP-015`），
同一批次重复提交幂等（`BR-IMP-002`）。

## 2. 页面结构

| 顺序 | 区块 | `data-page` | `data-block` | 组件 | 说明 |
|---|---|---|---|---|---|
| 1 | 页头 | — | — | 页头 | 标题 + 数据范围 + 步骤号 + 单文件上限 |
| 2 | 步骤条 | — | `steps` | `el-steps` | 四步可点（页内切换）；已完成步骤标绿 |
| 3 | 步骤 1 模板 | `PAGE-IMP-TEMPLATE` | `template` | 表单 + 有序列表 | 模块选择、模板版本与过期强提示、14 / 9 / 4 列清单、下载模板 |
| 4 | 步骤 2 上传 | — | `upload` | 拖拽区 + 表单 | 已选文件与行数、目标学年学期（必填）、统一目标班级（选填）、限额与配额口径 |
| 5 | 步骤 3 校验 | `PAGE-IMP-VALIDATE` | `validate` | 数值卡 + `el-table` | 总行数 / 可执行 / 失败；失败明细（行号·对象·原因）；下载失败明细；失败行处理策略；学号对照表 |
| 6 | 步骤 4 执行 | `PAGE-IMP-EXECUTE` | `execute` | 进度条 + 数值卡 | 任务编号、进度、成功 / 失败、配额、结果文件有效期 |
| 7 | 底部操作条 | — | `footer` | sticky 操作条 | 上一步 / 下一步 / 确认执行（第 3 步）/ 重新导入（第 4 步） |
| 8 | 状态片段 | — | — | 状态块 | 加载中 / 空数据 / 校验失败 / 无权限 / 提交中 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `module_code` | 导入模块 | `el-select` | 是 | 教务主任 / 租户管理员 | student | 四选一 | 决定模板版本与列清单 |
| `template_version` | 模板版本 | 只读值 | — | — | student-v3 | — | 与字段字典一致（`BR-IMP-007`） |
| `file_id` | 上传文件 | 拖拽区 + 选择文件 | 是（进入第 3 步前） | 同上 | 学生导入模板_student-v3_20261001.xlsx | ≤ 5000 行 / 10 MB，扩展名 .xlsx / .xls | 超限给「拆分后重传」提示（`REQ-IMP-010`） |
| `term_id` | 目标学年学期 | `el-select` | 是 | 同上 | 2026-2027 第一学期 | — | 导入数据落该学期 |
| `class_id` | 统一目标班级 | `el-select` | 否 | 同上 | 不指定 | — | 学生导入默认不带行政班（`REQ-STU-062`） |
| `row_no` / `error_msg` | 行号 / 失败原因 | 只读单元格 | — | — | — | — | 失败明细，可下载（`REQ-IMP-012`） |

## 4. 动作清单

| 动作编号 | 元素 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|
| `ACT-IMP-001` | 下载模板 | `data.import:import` | 下载当前版本模板（含说明与示例行），写审计 | `downloadImportTemplate` |
| `ACT-IMP-002` | 下一步 | `data.import:import` | 进入下一步（上传 → 校验 → 确认执行） | — |
| `ACT-IMP-003` | 上一步 | `data.import:import` | 回到上一步，已上传文件与校验结果保留 | — |
| `ACT-IMP-005` | 下载失败明细 | 存在失败行 | 下载失败明细（行号 + 对象 + 原因） | `downloadImportFailedRows` |
| `ACT-IMP-006` | 确认执行（异步） | 校验已完成且存在可执行行 | 异步提交并返回任务编号 | `executeImport` |
| `ACT-IMP-007` | 查看异步任务 | `data.async_task:read` | 去任务中心（批次 2-9） | — |
| `ACT-IMP-008` | 下载结果摘要 / 学号对照表 | `data.import:import` | 下载结果摘要与对照表 | `downloadImportResult` |
| `ACT-IMP-009` | 重新导入 | `data.import:import` | 回到第 1 步 | — |
| `ACT-IMP-010` | 去异步任务中心（无权限态） | `data.async_task:read` | 只读出口 | — |
| `ACT-IO-001` / `ACT-IO-002` | 拖拽上传区 / 选择文件 | `data.import:import` | 接收单文件并回填文件名与行数 | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 正常 | `normal` | 四步向导；第 3 步显示 120 / 118 / 2 与 2 条失败明细 | 下一步 / 确认执行 |
| 加载中 | `loading` | 同步校验骨架行（≤ 30 秒，不后台静默接收） | — |
| 空数据 | `empty` | 「文件里没有数据行」+ 重新下载模板 | 重新下载模板 |
| 校验失败 | `error` | 请求编号 + 错误码 + 文件名校验失败说明 | 下载模板后重试 |
| 无权限 | `forbidden` | 缺 `data.import:import`（任课教师 / 平台运营），不降级为可读 | 去异步任务中心（只读） |
| 提交中 | `submitting` | 按钮 loading 并禁用 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 查看异步任务 | `PAGE-IMP-TASK-LIST` | 跳页 | 该页在批次 2-9 交付，本批给批次提示 |
| 步骤条 | 页内四步 | 页内切换 | 不跳页；已填内容保留 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任 / 租户管理员 | 全部四步 | 下载模板、上一步 / 下一步、下载失败明细、确认执行、下载结果 | 全部可写 |
| 超级管理员 | 全部 | 全部 | 强制留痕（`BR-ORG-014`） |
| 平台运营 | 无（无权限面板） | 去异步任务中心（只读） | `DS-01`，默认不可导出（`IMP-Q-04`） |
| 任课教师 | 无（无权限面板） | 同上 | 矩阵未授予 `data.import` |

## 8. 样例数据

`content-samples.json` 的 `import_batches`（`IMP-20260928-0001`：120 / 118 / 2）、
`import_errors`（行 17 证件号重复、行 42 年级不存在）、`async_tasks`（`TASK-20260928-000312`：running 62%）。
行号与原因与三个模块导入向导的样例一一对应。

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-WIZARD`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 的 `import_common` 登记（`ACT-IMP-001` ~ `010`、`ACT-IO-001` / `002`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`module_code` / `template_version` / `file_id` / `term_id` / `class_id` / `row_no` / `error_msg`）
- [x] 五类状态齐全
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
