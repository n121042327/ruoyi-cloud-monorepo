# 调班（学生维度）
| 项 | 值 |
|---|---|
| 页面编号 | `PAGE-STU-TRANSFER`（`pages/student-list.html` 内弹窗） |
| 所属模块 | 学生管理（`student`） |
| 页面类型 | dialog（sm） |
| 所属批次 | 2-5 |
| 上游规则 | `BR-STU-016`（同一学年学期一条行政班关系）、`BR-CLASS-005`（容量只提示） |
| 权限资源 | `org.class` 的 `update` |
| 交付证据 | `verify-student-module.html`（`SM-07` / `SM-08`）、`student-detail_dialog-transfer_1440x900.png` |

## 1. 页面目的
把学生从当前行政班调到目标班级：只改班级关系，学号与学籍状态不变。

## 2. 页面结构
弹窗头（学生 + 当前班级）→ 口径 `el-alert` → 表单（目标班级 / 原因）→ 校验汇总 → footer（取消 / 确认调班）。

## 3. 字段清单
`class_id`（必填；已停用班级不可选）、`reason`（选填）。

## 4. 动作清单
`ACT-STU-032`（确认调班，`transferStudentClass` + `org.class:update`）、`ACT-STU-033`（取消）。

## 5. 状态清单
正常 / 校验失败（未选目标班级）/ 提交中。

## 6. 跳转关系
不跳页；班级批量迁学生走班级模块的独立页（`PAGE-CLS-MOVE`）。

## 7. 权限与数据范围
教务主任 / 年级主任（本年级）；班主任只读（矩阵未授予 `org.class:update`）。

## 8. 样例数据
目标班级下拉取 `classes` 的在读与停用样本（含 `20260104` 已停用）。

## 9. 自查
- [x] 动作登记齐全；`class_id` 在字段字典内；弹窗内不放表格
- [x] 已停用班级在下拉内 `disabled`
