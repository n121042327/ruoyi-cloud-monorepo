# 学生批量导入（学生管理快捷入口）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-STU-IMPORT`（`pages/student-import.html`） |
| 所属模块 | 学生管理（`student`） |
| 页面类型 | wizard（四步向导，独立页） |
| 骨架模板 | `TPL-WIZARD` |
| 所属批次 | 2-4 |
| 上游需求 | `REQ-STU-052`、`REQ-STU-062`；`REQ-IMP-002`、`REQ-IMP-021` ~ `023` |
| 上游规则 | `BR-IMP-001`（两阶段）、`BR-IMP-002`（幂等）、`BR-IMP-007`（模板版本） |
| 权限资源 | `data.import:import`；查看任务 `data.async_task:read` |
| 数据范围 | 教务主任 / 租户管理员 / 超级管理员可发起；任课教师与平台运营进无权限态 |
| 交付证据 | `evidence/stage2-prototype/verify-import-login.html`、`evidence/stage2-prototype/student-import_1440x900.png` |

## 1. 页面目的

学生管理列表工具条「导入」的模块快捷入口：走与学生导入模板一致的 14 列、只做「先校验再执行」的两阶段导入，并在执行后提供学号对照表。

## 2. 页面结构

与 `PAGE-IMP-WIZARD` 同一套四步结构（步骤条 → 模板 → 上传与校验 → 校验结果 → 执行与进度 → sticky 操作条 + 五类状态），
差别只在模板列、去重口径与失败样例。模板版本：`student-v3`。

## 3. 字段清单

14 列固定顺序（`REQ-STU-052`）：姓名* / 性别* / 入学年份* / 学段* / 年级* / 班级 / 全国学籍号 / 证件类型 / 证件号码 / 出生日期 / 监护人姓名 / 与监护人关系 / 监护人电话 / 联系地址。
**不含学号列**（`BR-STU-019`）：学号由系统统一发号；学校自带学号只用于生成对照表（`REQ-IMP-021` ~ `023`）。
`data-field` 取自字段字典：`student_name` / `gender` / `enroll_year` / `stage_code` / `grade_id` / `class_id` / `national_student_no` / `id_card_no` / `guardian_name` / `guardian_phone` / `remark`。

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

`import_batches` 的 `IMP-20260928-0001`（120 行 / 118 可执行 / 2 失败）+ `import_errors`：
行 17 高若曦「证件号已存在（与 2025000009 重复）」；行 42 彭宇轩「年级『2026 级 高一』在该学校不存在」。
导入后默认不给行政班（`REQ-STU-062`），可在第 2 步选统一目标班级。

## 9. 自查

- [x] 骨架属于 `TPL-WIZARD`；每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 动作编号全部在 `import_common` 登记
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [x] 五类状态齐全；样例数据取自 `content-samples.json`
