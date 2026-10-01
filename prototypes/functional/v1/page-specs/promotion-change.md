# 学籍异动登记（升班与学籍异动模块口径）
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-PRM-CHANGE`（`pages/student-list.html` 内弹窗） |
| 所属模块 | 升班与学籍异动（`promotion`） |
| 页面类型 | dialog（md） |
| 所属批次 | 2-5 |
| 上游需求 | `REQ-PRM-037` ~ `049` |
| 上游规则 | `BR-PROMO-008` ~ `012`、`BR-STU-023` ~ `028` |
| 权限资源 | `enrollment.status` 的 `update` |
| 交付证据 | `verify-student-module.html`（`SM-12` / `SM-13`）、`student-detail_dialog-promotion-change_1440x900.png` |

## 1. 页面目的
提供升班与学籍异动模块口径的异动登记：**与 `PAGE-STU-STATUS` 同字段集、同接口**（`changeEnrollmentStatus`），
额外强制阶段限制（义务教育不得开除）与审批要求（退学 / 开除 / 死亡需校级管理员审批），
避免同一字段出现两套写入规则（`DP-01`）。

## 2. 页面结构
弹窗头（学生 + 升班口径标签）→ 「同一写入口」说明 → 表单（异动类型 / 生效日期 / 原因）→ 校验汇总 → footer（取消 / 提交登记）。

## 3. 字段清单
`change_type`、`effective_date`、`reason`（必填），与 `PAGE-STU-STATUS` 相同。

## 4. 动作清单
`ACT-PRM-044`（提交登记，`changeEnrollmentStatus`）、`ACT-PRM-045`（取消）。

## 5. 状态清单
正常 / 校验失败 / 提交中 / 需审批（提示条写明审批通过前不生效）。

## 6. 跳转关系
入口：学生详情抽屉底部「异动登记（升班口径）」；异动历史页的「异动登记」也指向它（跨页 `data-panel-hash`）。

## 7. 权限与数据范围
教务主任 / 年级主任 / 班主任按各自范围提交；退学与开除类异动需校级管理员审批。

## 8. 样例数据
同 `student-status.md`。

## 9. 自查
- [x] 与 `PAGE-STU-STATUS` 同接口、同字段（字段只有一个写入口）
- [x] 弹窗内不放表格；动作编号在 `promotion_change` 组登记
