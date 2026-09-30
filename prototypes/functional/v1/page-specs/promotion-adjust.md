# 调整学生去向（升班预览内的弹窗）

| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-ADJUST`（取自 `navigation.yaml`） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | dialog（md，父页面 `PAGE-PRM-PREVIEW` 内的同页片段） |
| 骨架模板 | `TPL-OVERLAY` |
| 所属批次 | 2-3e-s3 |
| 上游需求 | `REQ-PRM-014` ~ `REQ-PRM-018`、`REQ-PRM-020` |
| 上游规则 | `BR-PROMO-004`、`BR-PROMO-005`、`BR-PROMO-006`（留级去向 = 目标学年学期的同学段同名年级）、`BR-CLASS-005`（容量只警告） |
| 权限资源 | `promotion.batch` 的 `update` |
| 数据范围 | 与父页面一致（`DS-04` / `DS-05` / `DS-01`）；只读角色不渲染行内「调整」入口，因此打不开本弹窗 |
| 原型文件 | `pages/promotion-preview.html` 内的 `[data-demo-panel="PAGE-PRM-ADJUST"]` |
| 交付证据 | `evidence/stage2-prototype/verify-promotion-wizard.html`、`promotion-preview_dialog-adjust_1440x900.png` |

## 1. 页面目的

教务主任对单个学生显式指定处理方式：默认升级（同学段序号 +1，`REQ-PRM-014`）、留级（`REQ-PRM-016`）、转班、毕业（`REQ-PRM-015`）、跳过（`REQ-PRM-017`）。
弹窗只写升班明细，不改学生与班级关系（`REQ-PRM-020`）；真正写入发生在第 4 步执行，且只新增下一学年的关系（`REQ-PRM-030`）。

## 2. 页面结构

| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 弹窗头 | — | `el-dialog` 标题区 | 学生姓名 + 源班级标签 + 关闭 |
| 2 | 口径提示 | — | `el-alert` | 「只改任务明细」+ 引用 `REQ-PRM-020` / `REQ-PRM-030` |
| 3 | 表单 | `form` | 两列 `form-grid` | 学号（只读）、姓名（只读）、处理方式（单选）、目标班级（下拉 / 留级只读展示）、调整原因（textarea） |
| 4 | 校验汇总 | — | `el-alert.danger` | 提交校验失败时显示 |
| 5 | 底部操作条 | `footer` | `el-dialog` footer | 取消 + 保存调整 |

## 3. 字段清单

| 字段名 | 中文 | 组件 | 必填 | 可编辑角色 | 初始值 | 校验规则 | 备注 |
|---|---|---|---|---|---|---|---|
| `student_no` | 学号 | 只读输入框 | — | — | 带入 | — | `disabled` + `data-keep-enabled` |
| `student_name` | 姓名 | 只读输入框 | — | — | 带入 | — | 同上 |
| `result_type` | 处理方式 | `el-radio-group` | 是 | 教务主任 / 超级管理员 | 该行当前值 | 五选一 | 决定目标班级与原因的必填性 |
| `target_class_id` | 目标班级 | `el-select` | 升级 / 转班必填 | 同上 | 该行当前目标班级 | 必须属于目标学年学期 | 留级时替换为只读展示「同学段同名年级」 |
| `remark` | 调整原因 | `el-input.textarea` | 留级 / 跳过必填 | 同上 | 空 | ≤ 500 字 | 写审计 |

## 4. 动作清单

| 动作编号 | 元素 | 触发 | 可用条件 | 结果 | 接口 |
|---|---|---|---|---|---|
| `ACT-PRM-027` | 取消 | 点击 / Esc | 始终可用 | 关闭片段，不写入 | — |
| `ACT-PRM-028` | 保存调整 | 点击 | 见字段必填规则 | 只写明细字段（`REQ-PRM-017`） | `updatePromotionItem` |

## 5. 状态清单

| 状态 | `data-state` | 表现 | 主动作 |
|---|---|---|---|
| 正常 | `normal` | 按当前处理方式渲染目标班级与必填项 | 保存调整 |
| 提交中 | `submitting` | 保存按钮 loading 并禁用 | — |
| 校验失败 | — | 汇总条 + 字段级红字，弹窗不关闭 | 修正后重试 |

## 6. 跳转关系

弹窗不跳页；关闭后回到 `PAGE-PRM-PREVIEW`，左栏选中的源班级与右栏过滤保留。

## 7. 权限与数据范围

只有 `promotion.batch:update` 的角色能打开（行内「调整」按钮用 `data-role-visible` 控制）；年级主任 / 校领导 / 平台运营看不到入口。

## 8. 样例数据

`content-samples.json` 的 `promotion_preview.rows`：升级（王梓萱）、转班（赵一诺）、留级（吴雨桐）、毕业（马嘉懿）四种形态都能从行内入口打开。

## 9. 自查

- [x] 页面骨架属于四种模板之一（`TPL-OVERLAY`）
- [x] 每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 每个 `data-action-id` 已在 `page-actions.yaml` 登记（`ACT-PRM-027` / `028`）
- [x] 每个 `data-field` 已在 `06-field-dictionary.yaml` 登记
- [x] 弹窗内不放表格（用 `form-grid` 与 `readonly-value`）
- [x] 样例数据取自 `content-samples.json`
