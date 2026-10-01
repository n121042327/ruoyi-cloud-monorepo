# 教师批量导入（教师管理快捷入口）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-IMPORT`（`pages/teacher-import.html`） |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | wizard（四步向导，独立页） |
| 骨架模板 | `TPL-WIZARD` |
| 所属批次 | 2-4 |
| 上游需求 | `REQ-TCH-050`；`BR-TEACHER-002` |
| 上游规则 | `BR-IMP-001`（两阶段）、`BR-IMP-002`（幂等）、`BR-IMP-007`（模板版本） |
| 权限资源 | `data.import:import`；查看任务 `data.async_task:read` |
| 数据范围 | 教务主任 / 租户管理员 / 超级管理员可发起；任课教师与平台运营进无权限态 |
| 交付证据 | `evidence/stage2-prototype/verify-import-login.html`、`evidence/stage2-prototype/teacher-import_1440x900.png` |

## 1. 页面目的

教师管理列表工具条「导入」的模块快捷入口：批量建教师账号与工号，工号租户内唯一；教育角色列可多值，缺角色时导入后在角色弹窗补。

## 2. 页面结构

与 `PAGE-IMP-WIZARD` 同一套四步结构（步骤条 → 模板 → 上传与校验 → 校验结果 → 执行与进度 → sticky 操作条 + 五类状态），
差别只在模板列、去重口径与失败样例。模板版本：`teacher-v2`。

## 3. 字段清单

9 列固定顺序（`REQ-TCH-050`）：工号* / 姓名* / 性别* / 所属学校* / 手机号 / 邮箱 / 入职日期 / 教育角色（多个用逗号分隔）/ 备注。
`data-field`：`teacher_no` / `teacher_name` / `gender` / `school_id` / `teacher_phone` / `email` / `hire_date` / `edu_role` / `remark`。

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

`import_errors` 形态复用：行 9 沈亦寒「工号已在租户内存在（YX2026012）」——按 `BR-TEACHER-002` 工号租户内唯一。
导入的教师默认不带教育角色，需在教师管理里分配（或由模板的「教育角色」列给出）。

## 9. 自查

- [x] 骨架属于 `TPL-WIZARD`；每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 动作编号全部在 `import_common` 登记
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [x] 五类状态齐全；样例数据取自 `content-samples.json`
