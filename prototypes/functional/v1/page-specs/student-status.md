# 学籍异动（学生模块入口）
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-STU-STATUS`（`pages/student-list.html` 内弹窗） |
| 所属模块 | 学生管理（`student`） |
| 页面类型 | dialog（md） |
| 所属批次 | 2-5 |
| 上游需求 | 学生 PRD 学籍异动章节；`REQ-PRM-037` ~ `049`（异动类型与约束由升班模块 PRD 定义） |
| 上游规则 | `BR-PROMO-010`（状态机）、`BR-PROMO-012`（追加式审计）、`BR-STU-012`（在读口径）、`BR-STU-023` ~ `028`、`REQ-PRM-043` / `044`（开除 / 死亡登记） |
| 权限资源 | `enrollment.status` 的 `update` |
| 交付证据 | `verify-student-module.html`（`SM-07`、`SM-09` ~ `SM-11`）、`student-detail_dialog-status_1440x900.png` |

## 1. 页面目的
按当前状态给出可执行的异动（`listEnrollmentStatusOption`），校验流转合法性后提交；开除在义务教育阶段不可用、退学 / 开除 / 死亡需校级管理员审批。

## 2. 页面结构
弹窗头（学生 + 当前状态）→ 状态机口径 `el-alert` → 表单（异动类型 / 生效日期 / 复学后班级 / 原因）→ 审批提示条 → 校验汇总 → footer（取消 / 提交异动）。

## 3. 字段清单
`change_type`（枚举见升班 PRD 4.6 表格）、`effective_date`、`class_id`（复学 / 报到 / 寻回必填）、`reason`（必填，≥5 字）。

## 4. 动作清单
`ACT-STU-030`（提交异动，`changeEnrollmentStatus` + `enrollment.status:update`）、`ACT-STU-031`（取消）。

## 5. 状态清单
正常（按类型给审批提示）/ 校验失败（字段级 + 汇总，弹窗不关）/ 提交中 / 非法流转（直接拒绝并说明原因）。

## 6. 跳转关系
不跳页；关闭后回到学生列表（或详情抽屉），筛选保留。

## 7. 权限与数据范围
教务主任 / 年级主任 / 班主任按 `DS-03` ~ `06` 在自己的范围内提交；终态学生列表行不渲染「异动」入口。

## 8. 样例数据
休学（陈思远）、出国（徐昊然）、转出 / 退学 / 开除（下拉内可用与不可用项）取自 `student_module_notes`。

## 9. 自查
- [x] 动作登记齐全；字段均在字段字典内（`change_type` / `effective_date` / `class_id` / `reason`）
- [x] 「开除」在义务教育阶段不可用（下拉 disabled + 说明 + 后端拒绝口径）
- [x] 弹窗内不放表格；校验失败不关弹窗
