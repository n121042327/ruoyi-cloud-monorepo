# 异动历史
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-HISTORY`（`pages/promotion-history.html`） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | page（`TPL-LIST`） |
| 所属批次 | 2-5 |
| 上游需求 | `REQ-PRM-037`、`REQ-PRM-038`、`REQ-PRM-048`、`REQ-PRM-059` |
| 上游规则 | `BR-PROMO-012`（追加式）、`BR-AUDIT-003`（不可删除） |
| 权限资源 | `enrollment.status` 的 `read`；导出 `data.export:export`；登记入口 `enrollment.status:update` |
| 交付证据 | `verify-student-module.html`（`PH-01` ~ `PH-06`）、`promotion-history_*.png` |

## 1. 页面目的
按学生维度查看异动历史（类型 / 生效日期 / 原状态 / 新状态 / 操作人 / 原因），支持筛选与导出；
本页只做查询，**登记入口唯一**（跳学生列表并由 `data-panel-hash` 打开 `PAGE-PRM-CHANGE`）。

## 2. 页面结构
页头（总条数 + 追加式口径）→ 筛选卡片（异动类型 / 学年学期 / 年级 / 生效日期）→ 工具条（异动登记 / 导出 / 隐藏搜索 / 刷新）→ 表格（8 列 + 操作）→ 分页 → 口径说明 → 五类状态片段。

## 3. 字段清单
`student_no`、`student_name`、`change_type`、`effective_date`、`before_status`、`after_status`、`operator`（审计字段）、`reason`。

## 4. 动作清单
`ACT-PRM-046`（异动登记 → `PAGE-STU-LIST` + `#panel=PAGE-PRM-CHANGE`）、`ACT-PRM-047`（行内查看学生 → `PAGE-STU-LIST`）、`ACT-PRM-048`（导出，`exportData`）、`ACT-COM-001` / `002` / `003` / `005` / `006`（搜索 / 重置 / 隐藏搜索 / 刷新 / 分页）。

## 5. 状态清单
正常（4 行样例 / 共 34 条）/ 加载中 / 空数据（筛选无结果，另一档为「还没有任何异动记录」）/ 查询失败（重试不放宽范围）/ 无权限 / 提交中。

## 6. 跳转关系
「异动登记」与行内「查看学生」都跳 `PAGE-STU-LIST`；前者用 `data-panel-hash` 直接打开登记弹窗（跨页片段）。

## 7. 权限与数据范围
教务主任 / 校领导本校（`DS-04`）；年级主任本年级（`DS-05`）；班主任本班（`DS-06`）；平台运营只读并留痕（`DS-01`）。

## 8. 样例数据
4 行（陈思远休学 / 徐昊然出国 / 周子涵报到 / 王梓萱报到入学），总条数 34，见 `student_module_notes`。

## 9. 自查
- [x] 骨架属于 `TPL-LIST`；动作编号在 `promotion_history` 组登记
- [x] 表格 `min-width` 1200 = 列宽之和；每行带状态标签（原状态 / 新状态）
- [x] 五类状态齐全；样例数据取自 `content-samples.json`
