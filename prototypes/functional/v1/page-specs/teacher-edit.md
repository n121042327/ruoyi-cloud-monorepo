# 编辑教师

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-TCH-EDIT` |
| 所属模块 | 教师管理（`teacher`） |
| 页面类型 | drawer（`md` = 640px，右侧滑出，单页表单） |
| 骨架模板 | `TPL-OVERLAY` |
| 所属批次 | 2-2b-1 |
| 上游需求 | `REQ-TCH-021` ~ `REQ-TCH-025`；另引用 `REQ-TCH-022`（工号修改）、`REQ-TCH-023`（所属学校不可改）、`REQ-TCH-059`（离职走独立入口） |
| 上游规则 | `BR-TEACHER-001`、`BR-TEACHER-007`、`BR-AUDIT-001`、`NFR-SEC-05`、`NFR-PERF-03` |
| 权限资源 | `person.teacher` 的 `update`（列表行内"编辑"与详情页"编辑"共用 `ACT-TCH-006`） |
| 数据范围 | 行必须在本人的数据范围内，否则后端按 `DS-DENY-02` 拒绝 |
| 原型文件 | `pages/teacher-list.html` 的 `data-demo-panel="PAGE-TCH-EDIT"` |

## 1. 页面目的

教务主任、租户管理员修改教师主体字段。本页的核心不是"有哪些字段"，而是**同一字段在不同角色下可写还是只读**：
只读字段以灰底 disabled 呈现，不隐藏、不换成纯文本，避免"看起来能改"。
保存成功后回到来源页面并刷新详情与列表（`page-actions.yaml` 的 `state_rules`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 抽屉头 | — | `el-drawer` 的 `#header` | 姓名、工号、在职状态、关闭按钮 |
| 2 | 字段级权限说明 | — | `el-alert.info` | 说明只读字段为灰底 disabled，并列出可写角色 |
| 3 | 校验汇总 | — | `el-alert.danger` | 默认隐藏；校验失败时出现 |
| 4 | 表单 | `form` | `el-form` + `form-grid` | 10 个字段，含所属学校（永久只读）与教育角色多选 |
| 5 | 并发保护 | — | `el-alert.warning` | 说明并发冲突提示文案，并提供"模拟并发冲突"按钮（`ACT-TCH-023`） |
| 6 | 底部操作条 | — | 固定 footer | 取消（打开放弃确认）/ 保存 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `teacher_no` | 工号 | `el-input` | 否 | 仅租户管理员 | 取当前教师 | 长度 2–32 位字母/数字/连字符 | 修改需校级管理员且强制留审计（`REQ-TCH-022`） |
| `teacher_name` | 姓名 | `el-input` | 是 | 教务主任、租户管理员 | 取当前教师 | 必填；长度 2–50 | |
| `gender` | 性别 | `el-select` | 是 | 教务主任、租户管理员 | 取当前教师 | 必填 | |
| `school_id` | 所属学校 | `el-input` | — | 任何人都不可改 | 取当前教师 | — | 永久 disabled；换校走离职 + 新增或跨校任教（`REQ-TCH-023`） |
| `teacher_phone` | 手机号 | `el-input` | 否 | 教务主任、租户管理员 | 取当前教师（掩码） | 非空时为 11 位手机号 | 掩码值不可直接编辑；完整值在详情页查看并留痕 |
| `email` | 邮箱 | `el-input` | 否 | 教务主任、租户管理员 | 取当前教师 | 邮箱格式 | |
| `hire_date` | 入职日期 | `el-date-picker` | 否 | 仅租户管理员 | 取当前教师 | 格式 YYYY-MM-DD | |
| `edu_role` | 教育角色 | 多选（`el-select multiple` 形态） | 否 | 教务主任、租户管理员 | 取当前教师 | 只可选 `edu_role` 枚举值 | 年级主任必须绑定学年学期与年级（`REQ-TCH-027`） |
| `employment_status` | 在职状态 | `el-select` | 否 | 教务主任、租户管理员 | 取当前教师 | 枚举 `employment_status`（在职 / 离职 / 调离） | 本页不承担离职流程，提示改用 `ACT-TCH-009` |
| `remark` | 备注 | `el-input` | 否 | 教务主任、租户管理员 | 取当前教师 | 长度 ≤ 200 | |

> `school_id`、`teacher_phone`、`email`、`hire_date`、`employment_status` 已由 `CR-004` 补登记进 `06-field-dictionary.yaml`（`GAP-033` 已关闭）。
> 可写角色按 `05-permission-matrix.yaml` 判定：对 `person.teacher` 有 `update` 的只有 `academic_director` 与 `tenant_admin`。

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-TCH-006` | 行内 / 详情"编辑" | 点击 | `person.teacher:update` | 打开本抽屉并按字段级权限渲染 | — |
| `ACT-TCH-011` | 查看完整手机号 | 点击掩码旁链接 | `read_contact` | 就地展示并写访问日志 | — |
| `ACT-TCH-021` | 保存 | 点击 | 校验通过且字段在可写集合内 | 保存并刷新详情与列表，写审计（含变更前后值） | `updateTeacher` |
| `ACT-TCH-022` | 取消 / 关闭 | 点击 | 始终可用 | 打开放弃确认弹窗 | — |
| `ACT-TCH-023` | 模拟并发冲突 | 点击 | `person.teacher:update` | 演示"数据已被更新，请刷新后重试"（`REQ-TCH-025`） | — |
| `ACT-TCH-027` / `ACT-TCH-028` | 放弃并关闭 / 继续填写 | 点击 | 放弃确认弹窗已打开 | 关闭两个浮层 / 关回表单 | — |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 加载中 | `loading` | 不适用（数据由列表带入）；生产端取详情时以骨架屏覆盖表单 | — |
| 空数据 | `empty` | 不适用（编辑对象必然存在） | — |
| 查询失败 | `error` | 保存失败按 `feedback_templates` 给原因与请求编号；并发冲突给专门文案 | 重试 |
| 无权限 | `forbidden` | 行不在数据范围内时后端按 `DS-DENY-02` 拒绝；无 update 权限时不渲染入口 | — |
| 提交中 | `submitting` | 保存按钮 loading + 禁用；校验失败时字段级红字 + 顶部汇总 | — |

## 6. 跳转关系

| 触发 | 目标 | 打开方式 | 返回行为 |
|---|---|---|---|
| 列表行内"编辑" | `PAGE-TCH-EDIT` | 抽屉 | 关闭后回到列表，保留筛选与页码 |
| 详情"编辑" | `PAGE-TCH-EDIT` | 抽屉 | 关闭后回到详情 |
| 取消 / 关闭 | `DIALOG-TCH-DISCARD` | 弹窗 | 确认放弃则同时关闭两个浮层 |

## 7. 权限与数据范围

| 角色 | 可见内容 | 可见按钮 | 字段级限制 |
|---|---|---|---|
| 教务主任（`academic_director`） | 全表单 | 保存、取消、模拟并发冲突、查看完整手机号 | 工号与入职日期只读 |
| 租户管理员（`tenant_admin`） | 全表单 | 上列全部 | 无（工号可改，强制审计） |
| 年级主任（`grade_leader`） | 不渲染入口 | — | — |
| 班主任 / 任课教师 | 不渲染入口 | — | — |
| 校领导（`school_leader`） | 不渲染入口 | — | `CR-004` 已把 PRD 4.4 改为校领导只读，与权限矩阵一致（`GAP-032` 已关闭） |
| 平台运营（`platform_ops`） | 不渲染入口（只读角色） | — | — |

## 8. 样例数据

| 节点 | 用途 | 为什么用它 |
|---|---|---|
| `content-samples.json` → `teacher_detail_samples` 的 `teacher_id=1007`（苏睿） | 默认初值 | 跨校任教样本能同时验证"所属学校永久只读"与"工号仅租户管理员可改" |
| `content-samples.json` → `teacher_detail_samples` 的 `teacher_id=1003`（邓丽娟） | 第二种形态 | 无学校级教育角色，验证角色多选在空值下的呈现 |
| `content-samples.json` → `teachers_edge_cases` 的 `teacher_id=1011`（高洪波，离职） | 只读形态参照 | 离职教师不提供编辑入口，用于验证入口条件 |

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-OVERLAY`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-TCH-006`、`011`、`021` ~ `023`、`027`、`028`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记（`CR-004`，`GAP-033` 已关闭）
- [x] 表单字段标 `data-role-editable`，只读字段用灰底 disabled 形态
- [x] 五类状态按本页实际情况给出形态（见第 5 节）
- [x] 1440×900 与 1366×768 下未出现横向滚动条与元素重叠
- [x] 样例数据取自 `content-samples.json`，未出现占位人名
