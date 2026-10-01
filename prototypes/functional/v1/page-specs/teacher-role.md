# 教育角色分配

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-ROLE` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | dialog（`md` = 600px；含年级主任任职子表单） |
| 骨架模板 | `TPL-OVERLAY` |
| 所属批次 | 2-2b-2 |
| 上游需求 | `REQ-TCH-026` ~ `REQ-TCH-034` |
| 上游规则 | `BR-TEACHER-005`、`BR-DATA-002`、`BR-DATA-009`、`BR-GRADE-004`、`BR-AUDIT-001`、`DS-RULE-01`、`DS-DENY-02` |
| 权限资源 | `person.teacher` 的 `update`（入口与保存都用此权限） |
| 数据范围 | 分配对象与任职范围都必须在操作人的数据范围内（`DS-DENY-02`）：教务主任 `DS-04`、租户管理员 `DS-02` / `DS-10` |
| 原型文件 | `pages/teacher-list.html` 的 `data-demo-panel="PAGE-TCH-ROLE"` |

## 1. 页面目的

教务主任或租户管理员给教师分配**教育角色**：学校级角色（校领导 / 教务主任）与年级主任任职。
教育角色决定**数据范围**，系统角色决定**功能权限**，两者不可混用（`REQ-TCH-034`）——
这正是页面上第一条提示要讲清楚的事。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 弹窗头 | — | `el-dialog` 的 `#header` | 教师姓名、工号、"教育角色 ≠ 系统角色"标签 |
| 2 | 概念提示 | — | `el-alert.info` | 数据范围 vs 功能权限 |
| 3 | 学校级角色 | `form` | `el-table` + `el-checkbox` | 教务主任（已选中）/ 校领导（未选中）：任职范围、任职期间、数据范围 |
| 4 | 年级主任任职 | `form` | `el-table` + 内联 `el-form` | 已任职表（含历史只读行）+ 可展开的新增表单 |
| 5 | 保存前检查 | — | `el-alert.warning` | 唯一性、必填、缓存失效、审计四条 |
| 6 | 底部操作条 | — | `el-dialog` 的 `#footer` | 取消（走放弃确认）/ 新增年级主任任职 / 保存 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `edu_role` | 学校级角色 | `el-checkbox` 组 | 否 | 教务主任、租户管理员 | 教务主任 | 只可选 `edu_role` 枚举中的学校级角色 | 取值来自 `edu_role` 枚举 |
| `term_id` | 学年学期 | `el-select` | 是（年级主任任职） | 教务主任、租户管理员 | 当前学期 | 必填；历史学期只读 | 年级主任必须绑定学年学期 |
| `grade_id` | 年级 | `el-select` | 是（年级主任任职） | 教务主任、租户管理员 | 空 | 必填；只列数据范围内的年级 | 同一年级同一学期只允许一名主管 |
| `is_primary` | 是否主管 | `el-radio` | 否 | 教务主任、租户管理员 | 是 | — | `(term_id, grade_id, user_id)` 唯一 |
| `start_date` | 任职起始 | `el-date-picker` | 否 | 教务主任、租户管理员 | 2026-09-01 | 格式 `YYYY-MM-DD` | 留空表示长期有效（`REQ-TCH-029`） |

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-TCH-007` | 行内 / 详情"角色" | 点击 | `person.teacher:update` | 打开本弹窗 | — |
| `ACT-TCH-030` | 新增年级主任任职 / 收起 | 点击 | `person.teacher:update` | 展开或收起年级主任任职表单 | — |
| `ACT-TCH-031` | 加入待保存 | 点击 | 学年学期与年级已选 | 把该条任职加入待保存列表 | — |
| `ACT-TCH-032` | 保存 | 点击 | `person.teacher:update` | 保存角色与任职，失效数据范围缓存并写审计 | `saveTeacherEduRole` |
| `ACT-TCH-012` | 解除角色 | 点击角色行内"解除" | `person.teacher:update` | 二次确认后解除并刷新 | `removeTeacherEduRole` |
| `ACT-TCH-022` | 取消 / 关闭 | 点击 | 始终可用 | 打开放弃确认弹窗 | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 不适用（数据由列表 / 详情带入）；生产端保存时按钮置 loading | — |
| 空数据 | `empty` | 学校级角色可以有"全部未勾选"形态；年级主任任职无记录时表格给出"暂无任职"说明 | 新增年级主任任职 |
| 查询失败 | `error` | 保存失败按 `feedback_templates` 给原因与请求编号 | 重试 |
| 无权限 | `forbidden` | 无 `person.teacher:update` 时不渲染入口；绕过前端由后端拒绝 | — |
| 提交中 | `submitting` | 保存按钮 loading + 禁用，禁止重复提交 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 列表行内"角色" | `PAGE-TCH-ROLE` | 弹窗 | 关闭后回到列表，保留筛选与页码 |
| 详情"分配角色" | `PAGE-TCH-ROLE` | 弹窗 | 关闭后回到详情 |
| 取消 | `DIALOG-TCH-DISCARD` | 弹窗 | 确认放弃则同时关闭两个浮层 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（`academic_director`） | 全部角色与年级主任任职 | 保存、新增年级主任任职、解除、取消 | 无 |
| 租户管理员（`tenant_admin`） | 全部角色与年级主任任职 | 同上 | 无 |
| 年级主任（`grade_leader`） | 不渲染入口 | — | — |
| 班主任 / 任课教师 | 不渲染入口（`REQ-TCH-033`：班主任不提供本模块写入入口） | — | — |
| 校领导（`school_leader`） | 不渲染入口 | — | 校领导对教师主体只读（`CR-004` / `GAP-032`） |
| 平台运营（`platform_ops`） | 不渲染入口（只读角色） | — | — |

## 8. 样例数据

| 节点 | 用途 | 为什么用它 |
|---|---|---|
| `content-samples.json` → `teacher_role_samples.school_level_roles` | 学校级角色两行（一行已分配、一行未分配） | 未分配行用来验证勾选态渲染，同时对应校领导只读口径 |
| `content-samples.json` → `teacher_role_samples.grade_leader_assignments` | 年级主任任职（当前学期 + 历史学期） | 历史行只读，验证"历史任职记录保留、不可修改" |
| `content-samples.json` → `grades` | 年级下拉的三个选项 | 年级与学段、序号映射来自同一份样例数据 |
| `content-samples.json` → `calendar.terms` | 学年学期下拉 | 年级主任任职必须绑定学年学期 |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-OVERLAY`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`（跳转类带 `data-nav`）
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-007`、`012`、`022`、`030` ~ `032`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`term_id` / `grade_id` / `is_primary` / `start_date` 均已登记；`edu_role` 为枚举）
- [x] 五类状态按本页实际情况给出形态（见第 5 节）
- [x] 1440×900 与 1366×768 下未出现横向滚动条与元素重叠
- [x] 样例数据取自 `content-samples.json`，未出现占位人名

