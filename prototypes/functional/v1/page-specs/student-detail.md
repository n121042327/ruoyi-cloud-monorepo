# 学生详情（抽屉）与变更记录区块
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-STU-DETAIL`（抽屉，`pages/student-list.html` 内片段）+ 区块 `PAGE-STU-HISTORY`（变更记录） |
| 所属模块 | 学生管理（`student`） |
| 页面类型 | detail（`el-drawer` · lg）+ block |
| 所属批次 | 2-5 |
| 上游需求 | 学生 PRD 6.1 的详情页与变更记录；`REQ-STU-0xx`（详情字段）、`REQ-STU-062`（导入学生无行政班） |
| 上游规则 | `DP-01`（每个字段只有一个写入入口）、`BR-STU-001`（学号唯一）、`BR-STU-012`（在读口径）、`BR-AUDIT-003`（记录不可删除） |
| 权限资源 | `person.student:read`；敏感字段 `person.student:read_sensitive` / `person.student_contact:read_contact`；监护人写入 `person.student_guardian:update` |
| 数据范围 | `DS-04` / `DS-05` / `DS-06` / `DS-07` / `DS-01`（任课教师只读本人任教班级成员） |
| 交付证据 | `verify-student-module.html`（`SM-01` ~ `SM-06`、`SM-14`）、`student-detail_*.png` |

## 1. 页面目的
详情是**只读视图**：学籍状态只有「学籍异动」一个写入口、班级归属只有「调班 / 班级管理」一个入口、监护人由班主任在本抽屉内编辑（`DP-01`）。
打开方式：学生列表点数据行（或行内操作）；关闭后回到列表且筛选与页码保留。

## 2. 页面结构
| 顺序 | 区块 | `data-block` | 组件 | 说明 |
|---|---|---|---|---|
| 1 | 抽屉头 | — | 抽屉标题区 | 姓名 + 学籍状态标签 + 学号 |
| 2 | 口径提示 | — | `el-alert` × 2 | 只读边界（`DP-01`）+ 敏感字段掩码与 `read_sensitive` |
| 3 | 基本信息 | `basic` | `form-grid` + 只读值 | 姓名 / 性别 / 学号 / 全国学籍号 / 证件号 / 联系电话 / 入学年份与学段 / 年级班级 / 学籍状态（口径 `counts_as_enrolled`） |
| 4 | 监护人 | `guardian` | 清单行 + 按钮 | 最多 3 人；编辑走 `saveStudentGuardian`；解绑走 `unbindStudentGuardian`（需审核） |
| 5 | 变更记录 | `history`（`PAGE-STU-HISTORY`） | `el-timeline` | 班级变更 / 学籍状态 / 监护人变更三条样例 + 「查看全部」跳审计日志 |
| 6 | 抽屉底部 | — | 操作条 | 编辑 / 学籍异动 / 调班 / 跨校转学 / 异动登记（升班口径）/ 关闭 |

## 3. 字段清单
`student_name`、`gender`、`student_no`、`national_student_no`（掩码）、`id_card_no`（掩码 + `read_sensitive`）、`student_phone`（掩码 + `read_contact`）、`enroll_year`、`stage_code`、`grade_id`、`class_id`、`enrollment_status`（含 `counts_as_enrolled` 口径）、`guardian_name` / `guardian_phone` / `relation`。

## 4. 动作清单
`ACT-STU-023`（查看完整敏感字段，留痕）、`ACT-STU-024`（编辑监护人）、`ACT-STU-034`（解绑监护人）、`ACT-STU-025`（学籍异动）、`ACT-STU-026`（调班）、`ACT-STU-027`（跨校转学）、`ACT-STU-028`（异动登记-升班口径）、`ACT-STU-029`（变更记录查看全部）、`ACT-STU-005`（编辑，复用列表行内动作）。

## 5. 状态清单
正常（抽屉打开，三个分区可见）/ 加载中 / 查询失败（不放开范围）/ 无权限 / 提交中；由学生列表的五类状态片段承载。

## 6. 跳转关系
行点击 → 本抽屉；抽屉内四个动作分别打开 `PAGE-STU-STATUS` / `PAGE-STU-TRANSFER` / `PAGE-PRM-CHANGE`（同页弹窗）与 `PAGE-STU-CROSS-TRANSFER`（跳页）；关闭抽屉回列表并保留筛选。

## 7. 权限与数据范围
教务主任可看全部并可编辑；班主任可编辑监护人、可发起异动；年级主任只读；校领导只读；平台运营只读并留痕；任课教师只读本人任教班级成员（敏感字段全部掩码）。

## 8. 样例数据
`content-samples.json` 的 `students` / `students_edge_cases`（14 行）与 `student_module_notes`；抽屉内联系人、监护人取 `guardians`。

## 9. 自查
- [x] 骨架属于 `TPL-DETAIL`（抽屉）+ `block`；每个可交互元素带 `data-page` / `data-role` / `data-action-id`
- [x] 动作编号已在 `page-actions.yaml` 的 `student_detail` 登记；`data-field` 均在字段字典内
- [x] 敏感字段默认掩码且区分为 `read_sensitive` / `read_contact`
- [x] 抽屉内不放表格（用 `form-grid` 与清单行）
