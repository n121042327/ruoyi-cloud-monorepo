# 跨校转学（转出校 / 转入校两侧向导）
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-STU-CROSS-TRANSFER`（`pages/student-cross-transfer.html`，转出校）与 `PAGE-PRM-TRANSFER`（`pages/promotion-transfer.html`，转入校） |
| 所属模块 | 学生管理（`student`）+ 升班与学籍异动（`promotion`） |
| 页面类型 | wizard × 2（各四步） |
| 所属批次 | 2-5 |
| 上游需求 | `REQ-PRM-050` ~ `REQ-PRM-057` |
| 上游规则 | `BR-PROMO-011`（跨学校租户）、`BR-STU-020`（学号不变）、`BR-STU-023`（转入未报到）、`NFR-SEC-06` |
| 权限资源 | 转出校 `enrollment.transfer` 的 `create` / `cancel`；转入校 `approve` / `check-in` |
| 交付证据 | `verify-student-module.html`（`CT-01` ~ `CT-04`、`PT-01` ~ `PT-04`）、`student-cross-transfer_*.png` / `promotion-transfer_*.png` |

## 1. 页面目的
把跨校转学做成**两侧各自执行**的四步向导：转出校发起（释放行政班并把原在校记录置为已转出）→ 转入校接收（新建「转入未报到」）→ 学生报到（转「在读」）。
转学单只传递必要字段（学号 / 姓名 / 性别 / 原学校 / 原年级），学号与学生主体不迁移。

## 2. 页面结构
两侧都是：页头（含「学号跨校保持不变」）+ 四步步骤条 + 每步一张卡片 + sticky 操作条 + 五类状态片段。

| 侧 | 步骤 |
|---|---|
| 转出校 | 选择学生（在读且无未完成转学单）→ 转入校与目标年级班级 → 确认与提交 → 结果（转学单号 / 状态 / 撤销申请） |
| 转入校 | 待接收转学单 → 核对信息与接收班级 → 接收确认（接收即审批）→ 报到 |

## 3. 字段清单
转学单字段：`student_no`、`student_name`、`gender`、`from_school_id`、`to_school_id`、`grade_id`、`class_id`、`effective_date`、`transfer_no`、`status`（待接收 / 已接收 / 已报到 / 已撤销）。

## 4. 动作清单
转出校：`ACT-STU-040` / `ACT-STU-041`（下一步 / 上一步）、`ACT-STU-042`（提交转学申请，`addTransfer`）、`ACT-STU-043`（撤销申请，`cancelTransfer`）。
转入校：`ACT-PRM-052` / `ACT-PRM-053`（下一步 / 上一步）、`ACT-PRM-050`（接收，`acceptTransfer`，`approve`）、`ACT-PRM-051`（报到，`checkInTransfer`）、`ACT-PRM-054`（撤销接收，`cancelTransfer`）。

## 5. 状态清单
正常 / 加载中 / 空数据（没有可发起或没有待接收）/ 读取失败（不放开任一侧范围）/ 无权限 / 提交中。

## 6. 跳转关系
入口：学生详情抽屉「跨校转学」与异动历史；两侧各自四步页内切换；接收后转入校在「异动历史」可查。

## 7. 权限与数据范围
转出校是本校范围（`DS-04`）；转入校是对方学校范围，两侧分别写审计且可被任一侧租户导出（`REQ-PRM-056`）；平台运营只读。

## 8. 样例数据
转出校 4 行（在读 / 休学 / 转入未报到）；转入校 2 条待接收转学单（顾南嘉 2025010007、温子瑜 2025010008），见 `student_module_notes`。

## 9. 自查
- [x] 两侧动作编号分别在 `student_cross_transfer` / `promotion_transfer` 组登记
- [x] 「接收前不计入在读数」与「学号不变」在页面上可见（`REQ-PRM-053` / `BR-STU-020`）
- [x] 五类状态齐全；向导页用 `wizard.css`，不重复样式
