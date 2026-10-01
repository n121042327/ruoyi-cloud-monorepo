# 学生管理 · 详细设计

> 本文件由 `tools/gen_stage5_docs.py` 组装：第 2、3 节的表清单与接口清单从
> `docs/40-detailed-design/database/schema.yaml`、`docs/30-architecture/06-api-catalog.md`、
> `docs/40-detailed-design/page-action-api-map.yaml` 提取；设计叙述为人工编写。
> 需求编号前缀：`REQ-STU-*`（见 `docs/10-prd/modules/student/PRD.md`）。
> 改表结构先改 `schema.yaml` 并重跑 `tools/gen_schema_artifacts.py`，不要手改第 2 节。

## 1. 模块边界

负责：学生主体（`edu_student`，**平台级实体，不带 `tenant_id`**）、在校记录（`edu_student_enrollment`）、
监护人主体（`edu_guardian`，平台级）与监护人关系（`edu_student_guardian`）、学生资料变更申请
（`edu_student_field_change`）、一次性激活凭据（`edu_activation_code`）、学籍状态变更、
学生导入导出。

不负责：

- 班级归属写入 —— 唯一写入入口是班级管理（`edu_class_member`），本模块只读展示（`DP-01`）
- 升班执行 —— 唯一执行入口是升班模块；本模块只提供学生维度的历史查询
- 选科结果写入 —— 属选科模块（`edu_student_stream`）
- 家长登录账号本身 —— 属认证与账号域；本模块只维护监护人主体与绑定关系

## 2. 数据归属

| 表 | 中文名 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|
| `edu_activation_code` | 一次性激活凭据 | `school` | 5-0 | 10 | 2 | 1 |
| `edu_guardian` | 监护人主体 | `platform` | 5-0 | 5 | 1 | 0 |
| `edu_student` | 学生主体 | `platform` | 5-0 | 11 | 3 | 0 |
| `edu_student_enrollment` | 在校记录 | `school` | 5-0 | 8 | 1 | 1 |
| `edu_student_field_change` | 学生资料变更申请 | `school` | 5-0 | 10 | 1 | 1 |
| `edu_student_guardian` | 监护人与学生关联 | `platform` | 5-0 | 11 | 1 | 2 |

字段级说明见 `docs/40-detailed-design/database/physical-schema.md`。

## 3. 接口清单（operationId）

| operationId | 方法 | 路径 | 说明 | 权限资源 | 同步/异步 |
|---|---|---|---|---|---|
| `listStudent` | GET | `/edu/student/list` | 分页查询 | `person.student` | 同步 |
| `getStudent` | GET | `/edu/student/{id}` | 详情 | `person.student` | 同步 |
| `addStudent` | POST | `/edu/student` | 新增 | `person.student` | 同步 |
| `updateStudent` | PUT | `/edu/student` | 编辑 | `person.student` | 同步 |
| `updateStudentNo` | PUT | `/edu/student/{id}/student-no` | 修改学号（高级操作） | `person.student` | 同步 |
| `removeStudent` | DELETE | `/edu/student/{id}` | 逻辑删除 | `person.student` | 同步 |
| `listStudentChangeLog` | GET | `/edu/student/{id}/change-log` | 变更记录 | `person.student` | 同步 |
| `listEnrollmentStatusOption` | GET | `/edu/student/{id}/status-options` | 当前状态可执行的异动 | `person.student` | 同步 |
| `changeEnrollmentStatus` | POST | `/edu/student/{id}/enrollment-change` | 学籍异动 | `person.student` | 同步 |
| `transferStudentClass` | POST | `/edu/student/{id}/class-transfer` | 调班 | `person.student` | 同步 |
| `crossSchoolTransfer` | POST | `/edu/student/cross-school-transfer` | 跨校转学 | `person.student` | 同步 |
| `importStudentValidate` | POST | `/edu/student/import/validate` | 导入校验 | `person.student` | 同步 |
| `importStudentExecute` | POST | `/edu/student/import/execute` | 导入执行（异步） | `person.student` | 异步 |
| `downloadStudentImportTemplate` | GET | `/edu/student/import/template` | 模板下载 | `person.student` | 同步 |
| `exportStudent` | POST | `/edu/student/export` | 导出 | `person.student` | 异步 |
| `resetStudentPassword` | POST | `/edu/student/{id}/reset-password` | 重置密码 | `person.student` | 同步 |
| `listStudentGuardian` | GET | `/edu/student/{id}/guardian` | 监护人列表 | `person.student` | 同步 |
| `saveStudentGuardian` | POST | `/edu/student/{id}/guardian` | 新增 / 修改监护人 | `person.student` | 同步 |
| `unbindStudentGuardian` | POST | `/edu/student/{id}/guardian/{guardianId}/unbind` | 解绑（需审核） | `person.student` | 同步 |

共 19 个接口。请求 / 响应契约见 `docs/40-detailed-design/api/openapi.yaml`，错误码见 `docs/40-detailed-design/api/error-codes.yaml`。

## 4. 页面与动作落点

| 页面 | 页面编号 | 路由 | 批次 | 动作数 | 调接口动作数 | 组件文件 |
|---|---|---|---|---|---|---|
| 学生管理列表 | `PAGE-STU-LIST` | `/edu/student/list` | 2-1 | 49 | 16 | `views/edu/student/stu_list/index.vue` |
| 跨校转学 | `PAGE-STU-CROSS-TRANSFER` | `/edu/student/cross-transfer` | 2-5 | 4 | 2 | `views/edu/student/stu_cross_transfer/index.vue` |
| 批量导入向导 | `PAGE-STU-IMPORT` | `/edu/student/import` | 2-4 | 12 | 6 | `views/edu/student/stu_import/index.vue` |

完整映射（含权限码与目标组件库组件）见 `docs/40-detailed-design/page-action-api-map.yaml`。

## 5. 事务边界

| 操作 | 事务范围 | 说明 |
|---|---|---|
| 新增学生 | `edu_student` + `edu_student_enrollment` +（可选）`edu_student_guardian` | 一个事务提交；任一失败整体回滚，不留下孤儿主体 |
| 修改学生 | `edu_student`（+ 触发 `edu_student_field_change` 时） | 敏感字段改动走申请，不在本事务内直接改 |
| 学籍状态变更 | `edu_enrollment_change` + `edu_student_enrollment.enrollment_status` | 同事务；跨校转学另起转学单事务（见升班模块） |
| 监护人绑定 / 解绑审核 | `edu_student_guardian` 单表 | 审核通过后写 `bind_status`，同时写审计 |
| 激活码签发 / 重置 | `edu_activation_code` 单表 | 签发走批量插入，重置走状态更新 |
| 导入执行 | 每 500 行一个事务 | 批次状态与错误行独立提交，保证「部分成功可续跑」 |

禁止跨模块写表：本模块不得写 `edu_class_member`、`edu_student_stream`、`edu_promotion_item`。

## 6. 并发与幂等

- 学号：`uk_student_no` 兜底，平台唯一且永不回收；并发插入冲突转 `EDU-STU-4001`
- 证件号：`uk_id_card_no` 兜底（非空时唯一，`GAP-020`）
- 激活码：使用采取条件更新 `UPDATE ... SET status='used' WHERE code=? AND status='unused'`，
  影响行数为 0 即判定已被使用；`uk_activation_active` 保证同一学生同时只有一个未使用激活码
- 资料变更申请：`uk_sfc_pending`（生成列 `pending_guard`）保证同一学生同一字段同时只有一条待审核
- 导入：同一用户同时 1 个任务、同一学校 3 个任务，超出进入排队（`REQ-IMP-047` / `REQ-IMP-048`）
- 导出：以任务创建时刻的范围快照执行；下载时重新解析范围（`DS-DENY-04`）

## 7. 校验规则

| 字段 / 规则 | 校验 | 依据 |
|---|---|---|
| 学号 | 平台唯一、必填、不可修改后复用 | `GAP-020` |
| 证件号 | 可空；填写时 18 位格式校验 + 平台唯一 | `GAP-020` |
| 登录名 | `s` + 学号，平台唯一 | 已确认口径 |
| 姓名 / 性别 / 出生日期 | 必填，出生日期不得晚于今天 | PRD 第 7 节 |
| 入学年份 / 学段 / 年级 | 必须与所在班级一致 | `BR-STU-*` |
| 监护人手机号 | 平台唯一（一个家长一个账号），绑定上限 3 | `GAP-015` |
| 解绑 | 需班主任确认；驳回可重提，同字段同时只允许一条待审 | `GAP-018` |
| 字段级可编辑性 | 班主任可改监护人信息；年级主任只读；任课教师只读 | 已确认的字段级矩阵 |
| 任课教师导出名单 | **禁止**（导出接口不授予任课教师） | 已确认口径 |

## 8. 失败恢复与补偿

- 单条写失败：事务整体回滚，返回错误码，无副作用
- 导入失败：错误行落 `edu_import_error`，可下载失败行后重提；批次复用 `batch_no`
- 任务重试耗尽：进 `edu_dead_letter_task`，运维重放复用原幂等键，重放写审计
- 激活码丢失：班主任重置，旧码置为作废，新码签发；全过程留痕
- 消息消费重复：以 `batch_no` / `task_no` 先查后写，唯一索引兜底

## 9. 权限与数据范围

- 学生主体与监护人主体是平台级实体：**不带 `tenant_id`**，学校侧读取走两段式
  （先按 `edu_student_enrollment.school_id` 过滤，再取 `edu_student`），见 `DS-DENY-09`
- 校领导看本校全部（`DS-04`）；年级主任看负责年级（`DS-05`）；班主任看本班（`DS-06`）；
  任课教师看本人任教班级的必要资料（`DS-08`）
- 缺少租户 / 学校上下文一律拒绝（`DS-DENY-01` / `DS-DENY-02`）；范围为空返回空列表而非全量（`DS-DENY-03`）
- 按 ID 查详情先注入范围条件，越权 ID 表现为「查不到」（`DS-DENY-07`）

## 10. 关联图与时序

- 时序图：`diagrams/sequence/student-enroll.mmd`、`diagrams/sequence/student-import.mmd`
- 状态机：`diagrams/state/student-enrollment-status.mmd`、`diagrams/state/guardian-bind.mmd`
- 领域模型：`diagrams/class/student-domain.mmd`

## 11. 验收要点

1. 同一学号 / 同一证件号第二次写入被数据库唯一键拒绝，接口返回对应错误码
2. 同一学生同一字段提交两条待审，第二条被 `uk_sfc_pending` 拒绝
3. 同一学生签发第二个未使用激活码被 `uk_activation_active` 拒绝
4. 监护人绑定第 4 条被拒绝；解绑后可由班主任重新确认
5. 平台级实体在缺少学校上下文时读取返回 403，而不是全量
6. 任课教师调用导出接口返回 403

## 12. 状态口径

| 口径 | 当前值 | 说明 |
|---|---|---|
| 功能状态 | 已设计 | 表结构、接口契约、时序与校验规则已产出，待阶段 6 / 7 实现 |
| 迁移脚本 | 已验证 | 在 MySQL 8.4.11 空库安装与重放通过，证据见 `evidence/stage5-detailed-design/` |
| 接口契约 | 已设计 | OpenAPI 3.0.3，校验证据见 `evidence/stage5-detailed-design/2026-10-01_openapi-validate.log` |
