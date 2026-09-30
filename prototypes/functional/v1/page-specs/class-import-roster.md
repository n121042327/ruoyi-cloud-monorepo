# 编班表导入（按班级列）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-CLS-ROSTER-IMPORT`（`pages/class-import-roster.html`） |
| 所属模块 | 班级管理（`class`） |
| 页面类型 | wizard（四步向导，独立页） |
| 骨架模板 | `TPL-WIZARD` |
| 所属批次 | 2-4 |
| 上游需求 | `REQ-CLS-035` ~ `REQ-CLS-037`；`BR-STU-016` |
| 上游规则 | `BR-IMP-001`（两阶段）、`BR-IMP-002`（幂等）、`BR-IMP-007`（模板版本） |
| 权限资源 | `data.import:import`；查看任务 `data.async_task:read` |
| 数据范围 | 教务主任 / 租户管理员 / 超级管理员可发起；任课教师与平台运营进无权限态 |
| 交付证据 | `evidence/stage2-prototype/verify-import-login.html`、`evidence/stage2-prototype/class-import-roster_1440x900.png` |

## 1. 页面目的

编班表的导入入口：一行一个学生、含目标班级列；两阶段（先校验后执行），冲突行给出「调班 / 移出」的修正提示。

## 2. 页面结构

与 `PAGE-IMP-WIZARD` 同一套四步结构（步骤条 → 模板 → 上传与校验 → 校验结果 → 执行与进度 → sticky 操作条 + 五类状态），
差别只在模板列、去重口径与失败样例。模板版本：`roster-v1`。

## 3. 字段清单

4 列（`REQ-CLS-035` 只要求「一行一学生，含目标班级列」，具体列数由本批取推荐方案，见 `GAP-056`）：
学号* / 姓名（选填，用于核对）/ 目标班级*（按班级列）/ 班级类型（选填，默认行政班）。
`data-field`：`student_no` / `student_name` / `class_id` / `class_type`。

## 4. 动作清单

复用 `import_common`：`ACT-IMP-001`（下载模板）、`ACT-IMP-002` / `003`（下一步 / 上一步）、
`ACT-IMP-005`（下载失败明细）、`ACT-IMP-006`（确认执行）、`ACT-IMP-007`（查看异步任务）、
`ACT-IMP-008`（下载结果摘要）、`ACT-IMP-009`（重新导入）、`ACT-IMP-010`（无权限态出口）、`ACT-IO-002`（选择文件）。

## 5. 状态清单

正常 / 加载中 / 空数据 / 校验失败 / 无权限 / 提交中，与 `PAGE-IMP-WIZARD` 同形态。

## 6. 跳转关系

查看异步任务 → `PAGE-IMP-TASK-LIST`（批次 2-9，本批给批次提示）；四步在页内切换，不跳页。

## 7. 权限与数据范围

教务主任 / 租户管理员 / 超级管理员可发起；平台运营与任课教师进无权限态（`DS-01` / `DS-07`）。

## 8. 样例数据

`import_errors` 形态复用：
行 23「学生在转入未报到状态，不允许编班」（`BR-STU-012`，先报到为在读）；行 88「学生在目标学年学期已有行政班关系」（`REQ-CLS-029`，请先调班或移出）。
导入幂等：同一批次重复提交不产生重复关系（`REQ-CLS-037`）。

## 9. 自查

- [x] 骨架属于 `TPL-WIZARD`；每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 动作编号全部在 `import_common` 登记
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [x] 五类状态齐全；样例数据取自 `content-samples.json`
