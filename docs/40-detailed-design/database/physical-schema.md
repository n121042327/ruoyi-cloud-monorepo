# 物理表结构说明（逐表）

> 本文件由 `tools/gen_schema_artifacts.py` 从 `database/schema.yaml` 生成，**不要手工编辑**；
> 改表结构先改 `schema.yaml`，再重跑生成器（这样 ER 图、领域对象映射表与建表脚本必然一致）。

## 0. 约定

- 方言：MySQL 8；引擎 InnoDB；字符集 utf8mb4 / utf8mb4_general_ci
- 主键：bigint unsigned NOT NULL AUTO_INCREMENT（平台级与业务表统一）
- 公共列：create_by / create_time / update_by / update_time（表的 with_audit 为 true 时自动追加）
- 逻辑删除：del_flag char(1) DEFAULT '0'（表的 soft_delete 为 true 时自动追加；历史与学籍类表禁止物理删除）
- 外键：同服务内核心关系使用物理外键且禁止级联删除（ON DELETE RESTRICT / UPDATE RESTRICT）
- 平台级实体（scope=platform）不带 `tenant_id`，读取走两段式（`DS-DENY-09`）

## 1. 表总览

| # | 表 | 中文名 | 模块 | 范围 | 批次 | 列数 | 唯一键 | 外键 |
|---|---|---|---|---|---|---|---|---|
| 1 | `edu_student` | 学生主体 | 学生与监护人 | platform | 5-0 | 17 | 3 | 0 |
| 2 | `edu_student_enrollment` | 在校记录 | 学生与监护人 | school | 5-0 | 16 | 1 | 1 |
| 3 | `edu_guardian` | 监护人主体 | 学生与监护人 | platform | 5-0 | 11 | 1 | 0 |
| 4 | `edu_student_guardian` | 监护人与学生关联 | 学生与监护人 | platform | 5-0 | 17 | 1 | 2 |
| 5 | `edu_student_field_change` | 学生资料变更申请 | 学生与监护人 | school | 5-0 | 19 | 1 | 1 |
| 6 | `edu_activation_code` | 一次性激活凭据 | 学生与监护人 | school | 5-0 | 14 | 2 | 1 |
| 7 | `edu_teacher` | 教师主体 | 教师与任教 | school | 5-0 | 18 | 1 | 0 |
| 8 | `edu_user_role` | 用户教育角色（学校级） | 教师与任教 | school | 5-0 | 14 | 1 | 0 |
| 9 | `edu_grade_leader` | 年级主任任职 | 教师与任教 | school | 5-0 | 13 | 1 | 1 |
| 10 | `edu_teaching_assignment` | 任教关系 | 教师与任教 | school | 5-0 | 13 | 1 | 2 |
| 11 | `edu_grade` | 年级 | 年级 | school | 5-1 | 15 | 2 | 0 |
| 12 | `edu_class` | 班级（行政班 / 教学班容器） | 班级与教学班 | school | 5-1 | 21 | 2 | 3 |
| 13 | `edu_class_member` | 班级成员关系（花名册） | 班级与教学班 | school | 5-1 | 16 | 1 | 2 |
| 14 | `edu_teaching_class` | 教学班 | 班级与教学班 | school | 5-1 | 14 | 1 | 1 |
| 15 | `edu_teaching_class_member` | 教学班成员关系 | 班级与教学班 | school | 5-1 | 15 | 1 | 2 |
| 16 | `edu_promotion_task` | 升班任务 | 升班与学籍异动 | school | 5-2 | 22 | 1 | 2 |
| 17 | `edu_promotion_item` | 升班明细 | 升班与学籍异动 | school | 5-2 | 12 | 1 | 2 |
| 18 | `edu_enrollment_change` | 学籍异动记录 | 升班与学籍异动 | school | 5-2 | 20 | 0 | 1 |
| 19 | `edu_transfer_order` | 跨校转学单 | 升班与学籍异动 | school | 5-2 | 25 | 1 | 1 |
| 20 | `edu_stream_config` | 选科配置 | 3+1+2 选科 | school | 5-2 | 12 | 1 | 1 |
| 21 | `edu_student_stream` | 学生选科 | 3+1+2 选科 | school | 5-2 | 13 | 1 | 1 |
| 22 | `edu_stream_change_request` | 选科变更申请 | 3+1+2 选科 | school | 5-2 | 23 | 1 | 1 |
| 23 | `edu_stream_history` | 选科历史 | 3+1+2 选科 | school | 5-2 | 12 | 0 | 1 |
| 24 | `edu_subject` | 学科 | 学科与配置 | school | 5-2 | 14 | 2 | 0 |
| 25 | `edu_subject_stage` | 学科与学段启用 | 学科与配置 | school | 5-2 | 10 | 1 | 1 |
| 26 | `edu_school` | 学校 | 学校与租户 | school | 5-2 | 17 | 2 | 0 |
| 27 | `edu_campus` | 校区 | 学校与租户 | school | 5-2 | 14 | 2 | 0 |
| 28 | `edu_school_stage` | 学校开设学段 | 学校与租户 | school | 5-2 | 9 | 1 | 0 |
| 29 | `edu_academic_year` | 学年 | 学年学期 | school | 5-2 | 11 | 1 | 0 |
| 30 | `edu_term` | 学期 | 学年学期 | school | 5-2 | 14 | 1 | 1 |
| 31 | `edu_import_template` | 导入模板 | 导入导出与异步任务 | tenant | 5-2 | 12 | 1 | 0 |
| 32 | `edu_import_batch` | 导入批次 | 导入导出与异步任务 | school | 5-2 | 21 | 1 | 1 |
| 33 | `edu_import_error` | 导入行结果（失败与跳过明细） | 导入导出与异步任务 | school | 5-2 | 10 | 1 | 0 |
| 34 | `edu_async_task` | 异步任务 | 导入导出与异步任务 | school | 5-2 | 26 | 1 | 0 |
| 35 | `edu_async_task_retry` | 任务重试记录 | 导入导出与异步任务 | school | 5-2 | 8 | 1 | 1 |
| 36 | `edu_dead_letter_task` | 死信任务 | 导入导出与异步任务 | school | 5-2 | 14 | 1 | 0 |
| 37 | `edu_file_ref` | 文件引用 | 导入导出与异步任务 | school | 5-2 | 14 | 1 | 0 |
| 38 | `edu_audit_log` | 操作日志 | 审计与操作日志 | school | 5-2 | 17 | 1 | 0 |
| 39 | `edu_audit_change` | 日志变更明细 | 审计与操作日志 | school | 5-2 | 7 | 1 | 1 |
| 40 | `edu_audit_archive_batch` | 日志归档批次 | 审计与操作日志 | tenant | 5-2 | 11 | 1 | 0 |
| 41 | `edu_data_grant` | 数据共享授权（仅教学资源） | 数据权限（横切） | tenant | 5-2 | 20 | 1 | 0 |
| 42 | `edu_data_grant_scope` | 授权范围明细 | 数据权限（横切） | tenant | 5-2 | 8 | 1 | 1 |

### 派生表（与主表同构）

| 表 | 依据 | 说明 |
|---|---|---|
| `edu_audit_log_archive` | `edu_audit_log` | 归档落点：与在线表同构（含全部索引）。归档 = 按时间范围把行搬进本表并写 edu_audit_archive_batch； |

### 派生视图

| 视图 | 来源 | 过滤条件 | 说明 |
|---|---|---|---|
| `v_edu_audit_operator_access` | `edu_audit_log` | `operator_role = 'platform_ops'` | 审计 PRD 7.1 明确"由操作日志派生视图，不单独建主表"。 |
| `v_edu_audit_security_event` | `edu_audit_log` | `action_type IN ('login','account_locked','activation_view','activation_reset','student_no_change','permission_change')` | 审计 PRD 7.1 明确"由操作日志派生，action_type = login"。 |

## edu_student · 学生主体

- 模块：学生与监护人（`student`）
- 范围：`platform`｜批次：`5-0`
- 说明：平台级实体，**不设 tenant_id**（D-030 / D-037）。学校侧一律经 edu_student_enrollment 或
edu_class_member 两段式取数（DS-DENY-09）。学号系统统一发号、永不回收，导入时忽略源学号（BR-STU-001）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `student_no` | varchar(32) | 否 | — | 学号（平台唯一；入学年份 4 位 + 全局序号 6 位） |
| `national_student_no` | varchar(64) | 是 | — | 全国学籍号（G/L 开头；非空时平台唯一） |
| `student_name` | varchar(50) | 否 | — | 学生姓名 |
| `gender` | char(1) | 是 | — | 性别 1 男 / 2 女 / 0 未知（枚举 `gender`） |
| `id_type` | varchar(20) | 是 | — | 证件类型 |
| `id_card_no` | varchar(64) | 是 | — | 证件号码（非空时平台唯一；掩码展示）（**敏感字段**（掩码 + 访问留痕）） |
| `birth_date` | date | 是 | — | 出生日期（**敏感字段**（掩码 + 访问留痕）） |
| `enroll_year` | smallint | 否 | — | 入学年份 |
| `graduation_date` | date | 是 | — | 毕业日期 |
| `photo_url` | varchar(255) | 是 | — | 学籍照片引用（文件服务） |
| `remark` | varchar(500) | 是 | — | 备注 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_student_no` | `student_no` | 学号平台唯一，永不回收 |
| `uk_national_student_no` | `national_student_no` | 非空时唯一（MySQL 唯一索引允许多个 NULL） |
| `uk_id_card_no` | `id_card_no` | 非空时唯一（GAP-020 平台唯一） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_student_name` | `student_name` |  |
| `idx_enroll_year` | `enroll_year` |  |

## edu_student_enrollment · 在校记录

- 模块：学生与监护人（`student`）
- 范围：`school`｜批次：`5-0`
- 说明：学校侧的学生身份。学籍状态的**唯一流转入口**是升班与学籍异动模块（DP-01）；
本表只承载当前状态，历史变更在 edu_enrollment_change。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `enroll_date` | date | 否 | — | 入校日期 |
| `enrollment_status` | varchar(20) | 否 | — | 学籍状态（在读 / 休学 / 转入未报到 /  graduated 等）（枚举 `enrollment_status`） |
| `status_effective_date` | date | 是 | — | 当前状态生效日期 |
| `leave_date` | date | 是 | — | 离校日期（毕业 / 转出 / 开除等终态时写入） |
| `campus_id` | bigint unsigned | 是 | — | 校区（入校时归属，参考信息） |
| `entry_grade_id` | bigint unsigned | 是 | — | 入校年级 |
| `remark` | varchar(500) | 是 | — | 备注 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_enrollment_student_school` | `student_id`, `school_id` | 同一学生同一学校一条在校记录 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_enrollment_school_status` | `school_id`, `enrollment_status` |  |
| `idx_enrollment_tenant` | `tenant_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_enrollment_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_guardian · 监护人主体

- 模块：学生与监护人（`student`）
- 范围：`platform`｜批次：`5-0`
- 说明：平台级实体，**不设 tenant_id**（D-030 / GAP-015）。手机号是家长登录名候选且平台唯一，
一个家长可对应多个孩子（跨租户、多对多）。账号关联 sys_user。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `guardian_name` | varchar(50) | 否 | — | 监护人姓名 |
| `guardian_phone` | varchar(20) | 否 | — | 手机号（平台唯一；家长登录名）（**敏感字段**（掩码 + 访问留痕）） |
| `id_card_no` | varchar(64) | 是 | — | 证件号码（可选）（**敏感字段**（掩码 + 访问留痕）） |
| `user_id` | bigint unsigned | 是 | — | 关联系统账号（家长小程序端登录后写入） |
| `status` | char(1) | 否 | '1' | 状态 1 正常 / 0 停用 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_guardian_phone` | `guardian_phone` | 手机号平台唯一（一个家长一个账号） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_guardian_user` | `user_id` |  |

## edu_student_guardian · 监护人与学生关联

- 模块：学生与监护人（`student`）
- 范围：`platform`｜批次：`5-0`
- 说明：跨租户多对多关系 + 学校侧审核。绑定上限 3（BR-ACCOUNT-018 同口径）；
解绑需班主任确认（GAP-015）；审核未过可重提，但同一学生同一字段同时只允许一条待审核。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `guardian_id` | bigint unsigned | 否 | — | 监护人主体 ID |
| `relation` | varchar(20) | 否 | — | 关系（father / mother / other） |
| `is_primary` | char(1) | 否 | '0' | 是否主要联系人 |
| `bind_status` | varchar(20) | 否 | 'pending' | 绑定状态（pending 待审核 / approved 已通过 / rejected 已驳回 / unbinding 待解绑） |
| `source` | varchar(20) | 是 | — | 来源（qrcode 扫码绑定 / teacher 教师录入 / import 导入） |
| `audit_by` | bigint unsigned | 是 | — | 审核人（班主任） |
| `audit_time` | datetime | 是 | — | 审核时间 |
| `audit_opinion` | varchar(500) | 是 | — | 审核意见（驳回必填） |
| `bind_time` | datetime | 是 | — | 绑定时间 |
| `unbind_time` | datetime | 是 | — | 解绑时间 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_student_guardian` | `student_id`, `guardian_id` | 同一学生同一监护人只有一条关系记录 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_sg_guardian` | `guardian_id`, `bind_status` |  |
| `idx_sg_student` | `student_id`, `bind_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_sg_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |
| `fk_sg_guardian` | `guardian_id` → `edu_guardian`(`id`) | RESTRICT / RESTRICT |

## edu_student_field_change · 学生资料变更申请

- 模块：学生与监护人（`student`）
- 范围：`school`｜批次：`5-0`
- 说明：字段级可编辑性矩阵的落点：非班主任角色（如家长、学生本人）修改关键字段时走申请。
同一学生同一字段同时只允许一条待审核（GAP-018）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `field_name` | varchar(64) | 否 | — | 申请修改的字段名 |
| `old_value` | varchar(500) | 是 | — | 原值（敏感字段需掩码） |
| `new_value` | varchar(500) | 是 | — | 申请值 |
| `apply_by_user_id` | bigint unsigned | 否 | — | 申请人 |
| `apply_reason` | varchar(500) | 是 | — | 申请原因 |
| `status` | varchar(20) | 否 | 'pending' | 待审核 / 已通过 / 已驳回 / 已撤销 |
| `audit_by` | bigint unsigned | 是 | — | 审核人（班主任 / 教务主任） |
| `audit_time` | datetime | 是 | — | 审核时间 |
| `audit_opinion` | varchar(500) | 是 | — | 审核意见 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |
| `pending_guard` | varchar(96) | 是 | 生成列（CASE WHEN status = 'pending' AND del_flag = '0' THEN CONCAT(student_id, ':', field_name) ELSE NULL END，STORED） | 待审核唯一键：仅 pending 且未删除时取值，用于保证同一学生同一字段同时只有一条待审核（生成列） |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_sfc_pending` | `pending_guard` | 同一学生同一字段同时只允许一条待审核（DB 级强制，GAP-018） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_sfc_pending` | `student_id`, `field_name`, `status` | 待审核申请列表查询（唯一性由 uk_sfc_pending 强制） |
| `idx_sfc_school` | `school_id`, `status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_sfc_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_activation_code · 一次性激活凭据

- 模块：学生与监护人（`student`）
- 范围：`school`｜批次：`5-0`
- 说明：班主任打印密码条分发 → 学生首登即设密码 → 激活码用完即废 → 丢码由班主任重置（D-039 / GAP-021）。
同一学生同一时刻只允许一个未使用的激活码，由生成列 `active_guard` + `uk_activation_active` 在数据库层强制。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `code` | varchar(64) | 否 | — | 一次性激活码（全局唯一） |
| `status` | varchar(20) | 否 | 'unused' | 未使用 / 已使用 / 已作废 |
| `issue_batch_no` | varchar(32) | 是 | — | 打印批次号（班主任一次打印一个批次） |
| `print_time` | datetime | 是 | — | 打印（查看）时间 |
| `used_time` | datetime | 是 | — | 使用时间 |
| `used_ip` | varchar(64) | 是 | — | 使用时来源 IP |
| `reset_by` | bigint unsigned | 是 | — | 重置人（班主任） |
| `reset_time` | datetime | 是 | — | 重置时间 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `active_guard` | bigint unsigned | 是 | 生成列（CASE WHEN status = 'unused' THEN student_id ELSE NULL END，STORED） | 有效激活码唯一键：仅 unused 时取值，保证同一学生同一时刻只有一个未使用激活码（生成列） |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_activation_code` | `code` |  |
| `uk_activation_active` | `active_guard` | 同一学生同一时刻只允许一个未使用激活码（DB 级强制） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_activation_student` | `student_id`, `status` |  |
| `idx_activation_batch` | `issue_batch_no` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_activation_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_teacher · 教师主体

- 模块：教师与任教（`teacher`）
- 范围：`school`｜批次：`5-0`
- 说明：教师主体带所属学校（`school_id`）。跨校任教不复制教师记录：由 edu_teaching_assignment
（带任教学校 `school_id`）与 edu_user_role 表达，避免"同一教师多行"被误认为重复（BR-TEACHER-001）。
账号信息（login_name / last_login_time）在 sys_user，本表不重复存。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `teacher_no` | varchar(32) | 否 | — | 工号（学校租户内唯一） |
| `teacher_name` | varchar(50) | 否 | — | 姓名 |
| `gender` | char(1) | 是 | — | 性别（枚举 `gender`） |
| `phone` | varchar(20) | 是 | — | 联系电话（**敏感字段**（掩码 + 访问留痕）） |
| `email` | varchar(100) | 是 | — | 邮箱 |
| `hire_date` | date | 是 | — | 入职日期 |
| `employment_status` | varchar(20) | 否 | 'active' | 在职 / 离职 / 调离（枚举 `employment_status`） |
| `leave_date` | date | 是 | — | 离职或调离生效日期 |
| `user_id` | bigint unsigned | 是 | — | 关联系统账号 |
| `remark` | varchar(500) | 是 | — | 备注 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_teacher_no` | `tenant_id`, `teacher_no` | 工号学校租户内唯一（BR-TEACHER-007） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_teacher_school` | `tenant_id`, `school_id` |  |
| `idx_teacher_name` | `teacher_name` |  |
| `idx_teacher_user` | `user_id` |  |

## edu_user_role · 用户教育角色（学校级）

- 模块：教师与任教（`teacher`）
- 范围：`school`｜批次：`5-0`
- 说明：只承载与具体班级、年级无关的学校级角色（校领导 / 教务主任）。
年级主任在 edu_grade_leader，班主任在 edu_class.head_teacher_id，都不写本表（DP-01 / DP-02）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `user_id` | bigint unsigned | 否 | — | 系统用户 |
| `teacher_id` | bigint unsigned | 是 | — | 教师（如该用户是教师） |
| `edu_role` | varchar(30) | 否 | — | 学校级教育角色（枚举 `edu_role`） |
| `status` | char(1) | 否 | '1' | 1 启用 / 0 停用 |
| `start_date` | date | 是 | — | 任职开始日期 |
| `end_date` | date | 是 | — | 任职结束日期 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_user_role` | `tenant_id`, `school_id`, `user_id`, `edu_role` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_user_role_user` | `user_id`, `status` | 数据范围解析的入口索引 |

## edu_grade_leader · 年级主任任职

- 模块：教师与任教（`teacher`）
- 范围：`school`｜批次：`5-0`
- 说明：按学年学期生效；一个年级可有多名主任（is_primary 标主要负责人）。数据范围 DS-05 的权威来源。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `grade_id` | bigint unsigned | 否 | — | 年级 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `user_id` | bigint unsigned | 否 | — | 年级主任 |
| `teacher_id` | bigint unsigned | 是 | — | 教师 |
| `is_primary` | char(1) | 否 | '0' | 是否主要负责人 |
| `status` | char(1) | 否 | '1' | 1 在职 / 0 离任 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_grade_leader` | `term_id`, `grade_id`, `user_id` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_grade_leader_user_term` | `user_id`, `term_id`, `status` | 数据范围解析入口 |
| `idx_grade_leader_grade` | `grade_id`, `term_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_grade_leader_grade` | `grade_id` → `edu_grade`(`id`) | RESTRICT / RESTRICT |

## edu_teaching_assignment · 任教关系

- 模块：教师与任教（`teacher`）
- 范围：`school`｜批次：`5-0`
- 说明："某学期某学科某班由某教师授课"的稳定事实（不含上课时间与教室）。
任课教师的数据范围 DS-07 与字段裁剪（本人所授学科）都依据本表（BR-TEACHER-003）。
`school_id` 是**任教学校**，可与教师所属学校不同，用于表达跨校任教。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `teacher_id` | bigint unsigned | 否 | — | 教师 |
| `subject_id` | bigint unsigned | 否 | — | 学科 |
| `class_type` | varchar(20) | 否 | — | administrative 行政班 / teaching 教学班（枚举 `class_type`） |
| `class_id` | bigint unsigned | 否 | — | 行政班或教学班 ID |
| `status` | char(1) | 否 | '1' | 1 有效 / 0 失效 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_assignment` | `term_id`, `teacher_id`, `subject_id`, `class_type`, `class_id` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_assignment_teacher_term` | `teacher_id`, `term_id`, `status` | 数据范围解析入口 |
| `idx_assignment_class_term` | `term_id`, `class_type`, `class_id`, `status` |  |
| `idx_assignment_tenant` | `tenant_id`, `school_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_assignment_teacher` | `teacher_id` → `edu_teacher`(`id`) | RESTRICT / RESTRICT |
| `fk_assignment_subject` | `subject_id` → `edu_subject`(`id`) | RESTRICT / RESTRICT |

## edu_grade · 年级

- 模块：年级（`grade`）
- 范围：`school`｜批次：`5-1`
- 说明：年级与学段固定映射（学段内序号：小学 1–6、初中 / 高中 1–3，RV-GRD-03）；
年级不能跨学段改名（BR-GRADE-006）。有班级或学生关系时不允许删除，只允许归档（BR-GRADE-004）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `stage_code` | varchar(20) | 否 | — | 学段（枚举 `stage_code`） |
| `enroll_year` | smallint | 否 | — | 入学年份（如 2026） |
| `grade_level` | tinyint | 否 | — | 学段内序号（小学 1–6、初中 / 高中 1–3） |
| `grade_name` | varchar(50) | 否 | — | 年级名称（如 2026 级 高一） |
| `class_count` | int | 否 | 0 | 班级数（冗余统计，写入时维护） |
| `student_count` | int | 否 | 0 | 在读学生数（冗余统计） |
| `grade_status` | varchar(20) | 否 | 'normal' | 正常 / 已归档（枚举 `grade_status`） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_grade_seq` | `school_id`, `stage_code`, `enroll_year`, `grade_level` | BR-GRADE-002 |
| `uk_grade_name` | `school_id`, `stage_code`, `grade_name` | BR-GRADE-003 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_grade_school_status` | `tenant_id`, `school_id`, `grade_status` |  |
| `idx_grade_enroll_year` | `school_id`, `enroll_year` |  |

## edu_class · 班级（行政班 / 教学班容器）

- 模块：班级与教学班（`class`）
- 范围：`school`｜批次：`5-1`
- 说明：行政班与教学班共用本表，由 class_type 区分：行政班设班主任（head_teacher_id）、参与 DS-06 解析；
教学班不设班主任、不参与 DS-06（BR-CLASS-007 / REQ-CLS-039）。
容量只提示不拦截（BR-CLASS-005）。有在读学生时不允许删除，只允许停用（BR-CLASS-006）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `grade_id` | bigint unsigned | 是 | — | 年级（教学班按组合生成时可空；行政班必填） |
| `stage_code` | varchar(20) | 否 | — | 学段（枚举 `stage_code`） |
| `class_name` | varchar(100) | 否 | — | 班级名称 |
| `class_type` | varchar(20) | 否 | — | administrative / teaching（枚举 `class_type`） |
| `class_capacity` | int | 是 | — | 容量上限（只提示不拦截） |
| `head_teacher_id` | bigint unsigned | 是 | — | 班主任（行政班唯一在任；教学班为空） |
| `head_teacher_start_date` | date | 是 | — | 班主任任职开始日期 |
| `head_teacher_end_date` | date | 是 | — | 班主任任职结束日期（学年切换时保留历史） |
| `campus_id` | bigint unsigned | 是 | — | 校区（参考信息，不参与权限判定） |
| `classroom` | varchar(50) | 是 | — | 教室（自由文本） |
| `subject_combination` | varchar(64) | 是 | — | 教学班的组合 / 单学科标识（教学班使用） |
| `class_status` | varchar(16) | 否 | 'active' | active 在读 / disabled 已停用（枚举 `class_status`） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_class_name` | `school_id`, `term_id`, `stage_code`, `class_name` | BR-CLASS-003 |
| `uk_class_teaching` | `school_id`, `term_id`, `subject_combination`, `class_type` | 同一学期同一组合只有一个教学班（幂等生成的前提，REQ-STR-057） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_class_school_term_status` | `school_id`, `term_id`, `class_status` |  |
| `idx_class_head_teacher` | `head_teacher_id`, `class_status` | 数据范围解析入口（DS-06） |
| `idx_class_grade` | `school_id`, `grade_id` |  |
| `idx_class_campus` | `school_id`, `campus_id`, `class_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_class_grade` | `grade_id` → `edu_grade`(`id`) | RESTRICT / RESTRICT |
| `fk_class_term` | `term_id` → `edu_term`(`id`) | RESTRICT / RESTRICT |
| `fk_class_head_teacher` | `head_teacher_id` → `edu_teacher`(`id`) | RESTRICT / RESTRICT |

## edu_class_member · 班级成员关系（花名册）

- 模块：班级与教学班（`class`）
- 范围：`school`｜批次：`5-1`
- 说明：本表只承载**行政班**花名册（class_type 恒为 administrative）。
教学班成员在 edu_teaching_class_member —— 两套独立关系分别落表（BR-CLASS-001），
避免"同一名学生在同一学期既写行政班关系又写教学班关系"造成一处事实两处存储（DP-01）。
关系追加式：离开时写 leave_date 并置 status，不物理删除（保留历史，BR-PROMO-012 同口径）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `class_id` | bigint unsigned | 否 | — | 行政班 |
| `class_type` | varchar(20) | 否 | 'administrative' | 恒为 administrative（教学班成员在 edu_teaching_class_member）（枚举 `class_type`） |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID（平台级，经本表两段式取数） |
| `student_enrollment_id` | bigint unsigned | 是 | — | 在校记录（学校侧身份） |
| `join_date` | date | 否 | — | 加入日期 |
| `leave_date` | date | 是 | — | 离开日期 |
| `status` | char(1) | 否 | '1' | 1 在班 / 0 已离开 |
| `gender_snapshot` | char(1) | 是 | — | 性别快照（花名册排序与展示） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_class_member_admin` | `term_id`, `student_id` | 行政班唯一：同一学年学期一名学生只能属于一个行政班（BR-STU-003）；教学班成员另表存放，因此本键可由 MySQL 直接表达，不需要部分唯一索引 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_member_class` | `class_id`, `class_type`, `status` |  |
| `idx_member_student` | `term_id`, `student_id`, `class_type` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_member_class` | `class_id` → `edu_class`(`id`) | RESTRICT / RESTRICT |
| `fk_member_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_teaching_class · 教学班

- 模块：班级与教学班（`class`）
- 范围：`school`｜批次：`5-1`
- 说明：教学班主体，与行政班完全独立（BR-CLASS-001）。不设班主任、不参与 DS-06 解析。
成员由"按组合生成"触发写入（REQ-STR-056），手工增删成员不开放。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `grade_id` | bigint unsigned | 是 | — | 年级 |
| `class_name` | varchar(100) | 否 | — | 教学班名称（如 高一 · 物化生 A 层） |
| `combination` | varchar(64) | 否 | — | 组合或单学科标识（如 物理+化学+生物 或 单学科：物理） |
| `member_count` | int | 否 | 0 | 成员数（冗余统计） |
| `teaching_class_status` | varchar(16) | 否 | 'active' | active 正常 / disabled 已停用（枚举 `class_status`） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_teaching_class` | `school_id`, `term_id`, `combination`, `class_name` | 幂等生成：同一学期同一组合同一名称只建一次 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_tclass_school_term` | `school_id`, `term_id`, `teaching_class_status` |  |
| `idx_tclass_grade` | `grade_id`, `term_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_tclass_term` | `term_id` → `edu_term`(`id`) | RESTRICT / RESTRICT |

## edu_teaching_class_member · 教学班成员关系

- 模块：班级与教学班（`class`）
- 范围：`school`｜批次：`5-1`
- 说明：归班级管理模块写入（唯一写入入口）：由"按组合生成"触发（REQ-STR-056），
手工调整也在班级模块，选科模块只触发与核对。成员来自选科结果，可跨行政班（BR-STU-004）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `teaching_class_id` | bigint unsigned | 否 | — | 教学班 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `source` | varchar(20) | 否 | 'generate' | generate 生成 / manual 手工调整 |
| `generate_task_no` | varchar(32) | 是 | — | 生成任务的幂等键（REQ-STR-057） |
| `join_date` | date | 否 | — | 加入日期 |
| `leave_date` | date | 是 | — | 离开日期 |
| `status` | char(1) | 否 | '1' | 1 在班 / 0 已离开 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_tclass_member` | `teaching_class_id`, `student_id` | 同一教学班同一学生只允许一条有效关系（幂等） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_tcm_student` | `term_id`, `student_id` |  |
| `idx_tcm_task` | `generate_task_no` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_tcm_class` | `teaching_class_id` → `edu_teaching_class`(`id`) | RESTRICT / RESTRICT |
| `fk_tcm_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_promotion_task · 升班任务

- 模块：升班与学籍异动（`promotion`）
- 范围：`school`｜批次：`5-2`
- 说明：升班按学年**追加**下一学年的班级与学生关系，不改写历史（BR-PROMO-001）。
同一源学期与目标学期的未结束任务唯一（REQ-PRM-005）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `task_no` | varchar(32) | 否 | — | 任务编号（对外标识，唯一） |
| `source_term_id` | bigint unsigned | 否 | — | 源学年学期 |
| `target_term_id` | bigint unsigned | 否 | — | 目标学年学期 |
| `scope_note` | varchar(500) | 是 | — | 范围说明 |
| `task_status` | varchar(20) | 否 | 'draft' | 草稿 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 已归档（枚举 `promotion_task_status`） |
| `total_count` | int | 否 | 0 | 涉及学生总数 |
| `success_count` | int | 否 | 0 | 成功数 |
| `failed_count` | int | 否 | 0 | 失败数 |
| `repeat_count` | int | 否 | 0 | 留级数 |
| `graduate_count` | int | 否 | 0 | 毕业数 |
| `async_task_no` | varchar(32) | 是 | — | 关联异步任务号 |
| `start_time` | datetime | 是 | — | 开始执行时间 |
| `finish_time` | datetime | 是 | — | 结束时间 |
| `cancel_reason` | varchar(500) | 是 | — | 取消原因（取消时必填） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_promotion_task_no` | `task_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_promotion_task_term` | `school_id`, `source_term_id`, `target_term_id`, `task_status` |  |
| `idx_promotion_task_tenant` | `tenant_id`, `task_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_promotion_source_term` | `source_term_id` → `edu_term`(`id`) | RESTRICT / RESTRICT |
| `fk_promotion_target_term` | `target_term_id` → `edu_term`(`id`) | RESTRICT / RESTRICT |

## edu_promotion_item · 升班明细

- 模块：升班与学籍异动（`promotion`）
- 范围：`school`｜批次：`5-2`
- 说明：逐学生结果。幂等键 = (task_id, student_id)（REQ-PRM-029）；
重试只处理失败 / 跳过项，不重复写已成功的行（REQ-PRM-032 / 037）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `task_id` | bigint unsigned | 否 | — | 升班任务 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `source_class_id` | bigint unsigned | 是 | — | 源班级 |
| `target_class_id` | bigint unsigned | 是 | — | 目标班级 |
| `result_type` | varchar(20) | 是 | — | 升级 / 留级 / 转班 / 毕业 / 跳过（枚举 `promotion_result_type`） |
| `item_status` | varchar(20) | 否 | 'pending' | 待处理 / 成功 / 失败 / 已跳过（枚举 `promotion_item_status`） |
| `error_msg` | varchar(500) | 是 | — | 失败原因 |
| `adjust_mode` | varchar(20) | 是 | — | 调整方式（手工指定目标班级时写入） |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_promotion_item` | `task_id`, `student_id` | 幂等键（REQ-PRM-029） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_promotion_item_status` | `task_id`, `item_status` |  |
| `idx_promotion_item_student` | `student_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_promotion_item_task` | `task_id` → `edu_promotion_task`(`id`) | RESTRICT / RESTRICT |
| `fk_promotion_item_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_enrollment_change · 学籍异动记录

- 模块：升班与学籍异动（`promotion`）
- 范围：`school`｜批次：`5-2`
- 说明：追加式，不更新不删除（BR-PROMO-012）。学籍状态的**唯一流转入口**（DP-01）：
开除在义务教育阶段不可用且后端拒绝（REQ-PRM-043）；退学 / 开除 / 死亡需校级管理员审批。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `school_record_id` | bigint unsigned | 是 | — | 在校记录 |
| `change_type` | varchar(30) | 否 | — | 异动类型（休学 / 复学 / 转学 / 退学 / 开除 / 出国 / 失踪 / 死亡 / 转入未报到 / 报到 / 升班） |
| `before_status` | varchar(20) | 是 | — | 变更前状态（枚举 `enrollment_status`） |
| `after_status` | varchar(20) | 否 | — | 变更后状态（枚举 `enrollment_status`） |
| `effective_date` | date | 否 | — | 生效日期 |
| `reason` | varchar(500) | 否 | — | 原因（必填） |
| `approval_status` | varchar(20) | 是 | — | 需审批的异动：待审批 / 已通过 / 已驳回 |
| `approve_by` | bigint unsigned | 是 | — | 审批人（校级管理员） |
| `approve_time` | datetime | 是 | — | 审批时间 |
| `approve_opinion` | varchar(500) | 是 | — | 审批意见 |
| `operator` | bigint unsigned | 否 | — | 操作人 |
| `operate_time` | datetime | 否 | CURRENT_TIMESTAMP | 操作时间 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_change_student` | `student_id`, `operate_time` |  |
| `idx_change_school` | `school_id`, `change_type`, `effective_date` |  |
| `idx_change_approval` | `school_id`, `approval_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_change_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_transfer_order · 跨校转学单

- 模块：升班与学籍异动（`promotion`）
- 范围：`school`｜批次：`5-2`
- 说明：接收动作本身即审批（已确认 3）；接收前不计入在读数（REQ-PRM-053）；
未报到前可撤销接收（REQ-PRM-054）。同一学生未完成转学单唯一（REQ-PRM-057）。
转学单只暴露必要字段给转入校（学号 / 姓名 / 性别 / 原学校 / 原年级，REQ-PRM-055）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `transfer_no` | varchar(32) | 否 | — | 转学单号（唯一） |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `from_tenant_id` | varchar(20) | 否 | — | 转出学校租户 |
| `from_school_id` | bigint unsigned | 否 | — | 转出学校 |
| `from_grade_id` | bigint unsigned | 是 | — | 原年级 |
| `to_tenant_id` | varchar(20) | 否 | — | 转入学校租户 |
| `to_school_id` | bigint unsigned | 否 | — | 转入学校 |
| `to_grade_id` | bigint unsigned | 是 | — | 目标年级 |
| `to_class_id` | bigint unsigned | 是 | — | 目标班级 |
| `transfer_status` | varchar(20) | 否 | 'pending' | 待接收 / 已接收 / 已报到 / 已撤销 / 已驳回 |
| `apply_by` | bigint unsigned | 否 | — | 申请人（转出校） |
| `apply_time` | datetime | 否 | CURRENT_TIMESTAMP | 申请时间 |
| `accept_by` | bigint unsigned | 是 | — | 接收人（转入校，接收即审批） |
| `accept_time` | datetime | 是 | — | 接收时间 |
| `check_in_time` | datetime | 是 | — | 报到时间 |
| `cancel_by` | bigint unsigned | 是 | — | 撤销人 |
| `cancel_time` | datetime | 是 | — | 撤销时间 |
| `remark` | varchar(500) | 是 | — | 备注 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_transfer_no` | `transfer_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_transfer_student_status` | `student_id`, `transfer_status` |  |
| `idx_transfer_to_school` | `to_school_id`, `transfer_status` |  |
| `idx_transfer_from_school` | `from_school_id`, `transfer_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_transfer_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_stream_config · 选科配置

- 模块：3+1+2 选科（`stream`）
- 范围：`school`｜批次：`5-2`
- 说明：按学校 + 学年学期唯一（REQ-STR-004）。截止时间按当前时间**实时比较**，不依赖定时任务刷状态（REQ-STR-005）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `stream_open_from` | datetime | 否 | — | 开放期起点 |
| `stream_deadline` | datetime | 否 | — | 截止时间 |
| `overdue_requires_approval` | tinyint(1) | 否 | 1 | 逾期变更是否需校级管理员审批（BR-STREAM-005） |
| `config_status` | varchar(20) | 否 | 'active' | 生效 / 已失效 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_stream_config` | `school_id`, `term_id` | 同一学校同一学期只允许一份生效配置（REQ-STR-004） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_stream_config_tenant` | `tenant_id`, `term_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_stream_config_term` | `term_id` → `edu_term`(`id`) | RESTRICT / RESTRICT |

## edu_student_stream · 学生选科

- 模块：3+1+2 选科（`stream`）
- 范围：`school`｜批次：`5-2`
- 说明：学生当前生效的选科组合。同一学生同一学期唯一（BR-STREAM-007）。
组合不在库里拼字符串：由 primary_subject_code + secondary_subject_codes 派生展示（CR-016）。
截止前自助修改立即生效；截止后只能走变更申请，审批通过前保持原值（BR-STREAM-006）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `primary_subject_code` | varchar(20) | 否 | — | 首选科目（物理 / 历史） |
| `secondary_subject_codes` | varchar(64) | 否 | — | 再选科目（两个编码，逗号分隔，按学科排序号排序） |
| `effective_time` | datetime | 否 | CURRENT_TIMESTAMP | 生效时间 |
| `stream_status` | varchar(20) | 否 | 'effective' | 生效 / 待审批（有待审批变更时前端展示"审批中"，本字段仍为生效） |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_student_stream` | `term_id`, `student_id` | BR-STREAM-007 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_stream_stat` | `term_id`, `primary_subject_code`, `secondary_subject_codes` | 组合分布统计（REQ-STR-049） |
| `idx_stream_school` | `school_id`, `term_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_student_stream_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_stream_change_request · 选科变更申请

- 模块：3+1+2 选科（`stream`）
- 范围：`school`｜批次：`5-2`
- 说明：截止后的变更单，含审批轨迹。同一学生同一学期**同时只允许一条待审批**（REQ-STR-029 / BR-STREAM-007）。
审批人是校级管理员，教务主任不能自审（REQ-STR-034）；通过前保持原组合不变（REQ-STR-032）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `request_no` | varchar(32) | 否 | — | 申请单号（唯一） |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `before_combination` | varchar(64) | 否 | — | 原组合（展示用文本） |
| `after_combination` | varchar(64) | 否 | — | 新组合（展示用文本） |
| `primary_subject_code` | varchar(20) | 否 | — | 新的首选科目 |
| `secondary_subject_codes` | varchar(64) | 否 | — | 新的再选科目 |
| `request_status` | varchar(20) | 否 | 'pending' | 草稿 / 待审批 / 已通过 / 已驳回 / 已撤销（枚举 `stream_request_status`） |
| `reason` | varchar(500) | 否 | — | 申请原因（必填） |
| `apply_by` | bigint unsigned | 否 | — | 发起人（学生本人或班主任代发起） |
| `apply_by_role` | varchar(30) | 是 | — | 发起人角色（student / homeroom） |
| `apply_time` | datetime | 否 | CURRENT_TIMESTAMP | 提交时间 |
| `approve_by` | bigint unsigned | 是 | — | 审批人（校级管理员） |
| `approve_time` | datetime | 是 | — | 审批时间 |
| `approve_opinion` | varchar(500) | 是 | — | 审批意见（驳回必填） |
| `cancel_time` | datetime | 是 | — | 撤回时间 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_stream_request_no` | `request_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_stream_request_pending` | `school_id`, `request_status`, `apply_time` | 审批待办按提交时间升序（REQ-STR-035）；同一学生待审批唯一由业务层 + 本索引兜底 |
| `idx_stream_request_student` | `term_id`, `student_id`, `request_status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_stream_request_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_stream_history · 选科历史

- 模块：3+1+2 选科（`stream`）
- 范围：`school`｜批次：`5-2`
- 说明：追加式，不可删除不可修改（REQ-STR-043）；跨学年学期保留（REQ-STR-044）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `student_id` | bigint unsigned | 否 | — | 学生主体 ID |
| `term_id` | bigint unsigned | 否 | — | 学年学期 |
| `before_combination` | varchar(64) | 是 | — | 原组合 |
| `after_combination` | varchar(64) | 否 | — | 新组合 |
| `change_type` | varchar(20) | 否 | — | 首次提交 / 开放期内自助变更 / 变更申请通过 |
| `request_no` | varchar(32) | 是 | — | 关联变更申请单号 |
| `reason` | varchar(500) | 是 | — | 变更原因 |
| `operator` | bigint unsigned | 否 | — | 操作人 |
| `operate_time` | datetime | 否 | CURRENT_TIMESTAMP | 操作时间 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_stream_history_student` | `student_id`, `operate_time` |  |
| `idx_stream_history_term` | `term_id`, `school_id` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_stream_history_student` | `student_id` → `edu_student`(`id`) | RESTRICT / RESTRICT |

## edu_subject · 学科

- 模块：学科与配置（`subject`）
- 范围：`school`｜批次：`5-2`
- 说明："一条学科主体 + 学段启用表"（RV-SUB-04）：编码校内唯一，同一学科在不同学段启用只加启用记录，
不拆多条主体。删除前检查三类引用（任教关系 / 教学班 / 学生选科），有引用时只允许停用。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `subject_code` | varchar(32) | 否 | — | 学科编码（校内唯一）（枚举 `subject_code`） |
| `subject_name` | varchar(50) | 否 | — | 学科名称 |
| `sort_no` | int | 否 | 0 | 排序号（决定再选科目的展示顺序，REQ-STR-017） |
| `stream_enabled` | char(1) | 否 | '0' | 是否参与 3+1+2 |
| `stream_role` | varchar(20) | 否 | 'none' | primary 首选 / secondary 再选 / none 不参与 |
| `subject_status` | varchar(16) | 否 | 'active' | 正常 / 已停用 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_subject_code` | `tenant_id`, `school_id`, `subject_code` | BR-SUBJECT-001 |
| `uk_subject_name` | `school_id`, `subject_name` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_subject_school` | `tenant_id`, `school_id`, `subject_status` |  |
| `idx_subject_stream` | `school_id`, `stream_enabled`, `stream_role` |  |

## edu_subject_stage · 学科与学段启用

- 模块：学科与配置（`subject`）
- 范围：`school`｜批次：`5-2`
- 说明：同一学科同一学段一条记录；(subject_id, stage_code) 唯一。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `subject_id` | bigint unsigned | 否 | — | 学科 |
| `stage_code` | varchar(20) | 否 | — | 学段（枚举 `stage_code`） |
| `status` | char(1) | 否 | '1' | 1 启用 / 0 停用 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_subject_stage` | `subject_id`, `stage_code` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_subject_stage_school` | `school_id`, `stage_code`, `status` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_subject_stage_subject` | `subject_id` → `edu_subject`(`id`) | RESTRICT / RESTRICT |

## edu_school · 学校

- 模块：学校与租户（`school`）
- 范围：`school`｜批次：`5-2`
- 说明："一个学校对应一个租户"（BR-ORG-002）：本表 `tenant_id` 唯一，且 `school_id = tenant_id` 的映射在接入时写入。
层级不超过三级（运营方 → 集团 → 学校，BR-ORG-003）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `school_code` | varchar(50) | 否 | — | 学校编码（父租户内唯一） |
| `school_name` | varchar(100) | 否 | — | 学校名称 |
| `short_name` | varchar(50) | 是 | — | 简称 |
| `parent_tenant_id` | varchar(20) | 是 | — | 上级租户（集团 / 运营方） |
| `root_tenant_id` | varchar(20) | 是 | — | 根租户（运营方） |
| `school_type` | varchar(20) | 是 | — | 学校类型（公办 / 民办 / 其他） |
| `address` | varchar(255) | 是 | — | 地址 |
| `phone` | varchar(20) | 是 | — | 联系电话 |
| `school_status` | varchar(16) | 否 | 'active' | 正常 / 已停用 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_school_tenant` | `tenant_id` | 一个学校对应一个租户（BR-ORG-002） |
| `uk_school_code` | `parent_tenant_id`, `school_code` | BR-ORG-011 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_school_name` | `school_name` |  |
| `idx_school_status` | `school_status` |  |

## edu_campus · 校区

- 模块：学校与租户（`school`）
- 范围：`school`｜批次：`5-2`
- 说明：校区不参与数据权限判定，只用于组织与统计（BR-ORG-009 / GAP-045）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `campus_code` | varchar(50) | 否 | — | 校区编码 |
| `campus_name` | varchar(100) | 否 | — | 校区名称 |
| `address` | varchar(255) | 是 | — | 地址 |
| `leader_name` | varchar(50) | 是 | — | 负责人 |
| `leader_phone` | varchar(20) | 是 | — | 负责人电话 |
| `campus_status` | varchar(16) | 否 | 'active' | 正常 / 已停用 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `del_flag` | char(1) | 否 | '0' | 逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_campus_name` | `school_id`, `campus_name` |  |
| `uk_campus_code` | `school_id`, `campus_code` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_campus_school` | `tenant_id`, `school_id`, `campus_status` |  |

## edu_school_stage · 学校开设学段

- 模块：学校与租户（`school`）
- 范围：`school`｜批次：`5-2`
- 说明：学校实际开设的学段；未开设的学段在学科与年级配置里置灰（REQ-SUB-026 同口径）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `stage_code` | varchar(20) | 否 | — | 学段（枚举 `stage_code`） |
| `status` | char(1) | 否 | '1' | 1 开设 / 0 停开 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_school_stage` | `school_id`, `stage_code` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_school_stage_tenant` | `tenant_id`, `status` |  |

## edu_academic_year · 学年

- 模块：学年学期（`term`）
- 范围：`school`｜批次：`5-2`
- 说明：学年日期必须连续不重叠：前一年结束日 = 后一年开始日 − 1 天（RV-TERM-08）。
归档后仍可按时间范围检索历史（学年归档见阶段 2 的 `PAGE-TERM-ARCHIVE`）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `academic_year_code` | varchar(20) | 否 | — | 学年编码（如 2026-2027） |
| `start_date` | date | 否 | — | 开始日期 |
| `end_date` | date | 否 | — | 结束日期 |
| `academic_year_status` | varchar(16) | 否 | 'normal' | 未开始 / 进行中 / 已归档 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_academic_year_code` | `tenant_id`, `school_id`, `academic_year_code` | BR-TERM-003 |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_academic_year_range` | `school_id`, `start_date`, `end_date` |  |

## edu_term · 学期

- 模块：学年学期（`term`）
- 范围：`school`｜批次：`5-2`
- 说明：同一学校同一学年只能有一个当前学期（BR-TERM-002）；学年日期连续不重叠（RV-TERM-08）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `academic_year_id` | bigint unsigned | 否 | — | 学年 |
| `term_code` | varchar(20) | 否 | — | 学期编码（如 1 / 2） |
| `term_name` | varchar(50) | 否 | — | 学期名称（第一学期 / 第二学期） |
| `start_date` | date | 否 | — | 开始日期 |
| `end_date` | date | 否 | — | 结束日期 |
| `is_current` | char(1) | 否 | '0' | 是否当前学期 |
| `term_status` | varchar(16) | 否 | 'normal' | 正常 / 已归档 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_term_code` | `academic_year_id`, `term_code` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_term_school_current` | `school_id`, `is_current` | 同一学校同一学年只允许一个当前学期（业务层 + 本索引兜底） |
| `idx_term_range` | `school_id`, `start_date`, `end_date` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_term_year` | `academic_year_id` → `edu_academic_year`(`id`) | RESTRICT / RESTRICT |

## edu_import_template · 导入模板

- 模块：导入导出与异步任务（`importexport`）
- 范围：`tenant`｜批次：`5-2`
- 说明：模板声明与版本号；模板过期后仍可下载但强提示（IMP-Q-05 / BR-IMP-007）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `module_code` | varchar(30) | 否 | — | 模块（student / teacher / class_roster） |
| `template_version` | varchar(20) | 否 | — | 模板版本（如 student-v3） |
| `column_count` | int | 否 | — | 列数 |
| `file_id` | bigint unsigned | 是 | — | 模板文件引用 |
| `expire_time` | datetime | 是 | — | 版本过期时间（过期后强提示） |
| `status` | char(1) | 否 | '1' | 1 当前版本 / 0 历史版本 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_import_template` | `module_code`, `template_version` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_import_template_current` | `module_code`, `status` |  |

## edu_import_batch · 导入批次

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：批次号是幂等键（BR-IMP-014）：同一批次重复执行不产生重复数据（BR-IMP-009）。
结果文件 7 天 / 任务元数据 90 天（IMP-Q-02）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `batch_no` | varchar(32) | 否 | — | 批次号（全局唯一，幂等键） |
| `module_code` | varchar(30) | 否 | — | 模块 |
| `template_version` | varchar(20) | 是 | — | 使用的模板版本 |
| `async_task_no` | varchar(32) | 是 | — | 关联异步任务号 |
| `source_file_id` | bigint unsigned | 是 | — | 上传文件引用 |
| `result_file_id` | bigint unsigned | 是 | — | 结果文件引用（含学号对照表） |
| `failed_file_id` | bigint unsigned | 是 | — | 失败明细文件引用 |
| `row_total` | int | 否 | 0 | 总行数 |
| `valid_count` | int | 否 | 0 | 校验通过行数 |
| `invalid_count` | int | 否 | 0 | 校验失败行数 |
| `success_count` | int | 否 | 0 | 执行成功行数 |
| `skipped_count` | int | 否 | 0 | 跳过行数（幂等跳过） |
| `import_status` | varchar(20) | 否 | 'validated' | 已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 |
| `operator_id` | bigint unsigned | 否 | — | 操作人 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_import_batch_no` | `batch_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_import_batch_school` | `school_id`, `import_status`, `create_time` |  |
| `idx_import_batch_operator` | `operator_id`, `create_time` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_import_batch_task` | `async_task_no` → `edu_async_task`(`task_no`) | RESTRICT / RESTRICT |

## edu_import_error · 导入行结果（失败与跳过明细）

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：逐行结果；`(batch_no, row_no)` 唯一。失败原因必须可直接指导修正（含行号 + 对象 + 原因）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `batch_no` | varchar(32) | 否 | — | 批次号 |
| `row_no` | int | 否 | — | 行号（Excel 行号，含表头偏移） |
| `result` | varchar(20) | 否 | — | invalid 校验失败 / failed 执行失败 / skipped 跳过 |
| `fail_reason` | varchar(500) | 否 | — | 失败或跳过原因 |
| `object_name` | varchar(100) | 是 | — | 对象标识（姓名 / 学号等） |
| `raw_data` | json | 是 | — | 原始行数据（用于对照修正） |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_import_error_row` | `batch_no`, `row_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_import_error_result` | `batch_no`, `result` |  |

## edu_async_task · 异步任务

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：任务中心的主表。状态推进 queued → running → succeeded / partial_failed / failed（REQ-IMP-034）；
只有 queued 可取消；failed / partial_failed 可重试且沿用原幂等键（REQ-IMP-037）；
默认只显示本人发起的任务（REQ-IMP-032）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `task_no` | varchar(32) | 否 | — | 任务编号（全局唯一，幂等键） |
| `task_type` | varchar(30) | 否 | — | import / export / promotion / teaching_class / archive（枚举 `async_task_type`） |
| `task_status` | varchar(20) | 否 | 'queued' | 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 死信（枚举 `async_task_status`） |
| `progress_percent` | tinyint unsigned | 否 | 0 | 进度百分比 |
| `owner_id` | bigint unsigned | 否 | — | 发起人 |
| `owner_role` | varchar(30) | 是 | — | 发起人角色快照 |
| `params_summary` | json | 是 | — | 参数摘要（模块 / 模板 / 筛选条件等） |
| `total_count` | int | 否 | 0 | 总数 |
| `success_count` | int | 否 | 0 | 成功数 |
| `failed_count` | int | 否 | 0 | 失败数 |
| `skipped_count` | int | 否 | 0 | 跳过数 |
| `result_file_id` | bigint unsigned | 是 | — | 结果文件引用 |
| `failed_file_id` | bigint unsigned | 是 | — | 失败明细文件引用 |
| `queue_position` | int | 是 | — | 排队位置（queue 状态时展示，REQ-IMP-049） |
| `retry_count` | tinyint unsigned | 否 | 0 | 已重试次数 |
| `batch_no` | varchar(32) | 是 | — | 关联批次号（导入 / 升班类任务） |
| `start_time` | datetime | 是 | — | 开始执行时间 |
| `finish_time` | datetime | 是 | — | 结束时间 |
| `error_msg` | varchar(500) | 是 | — | 失败原因 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_async_task_no` | `task_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_async_task_owner` | `owner_id`, `create_time` | 默认只看本人任务（REQ-IMP-032） |
| `idx_async_task_school_status` | `school_id`, `task_status`, `create_time` |  |
| `idx_async_task_type` | `task_type`, `task_status` |  |

## edu_async_task_retry · 任务重试记录

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：每次重试一条记录；`(task_no, retry_no)` 唯一。与死信配合判定"是否超过最大重试次数"（REQ-IMP-039）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `task_no` | varchar(32) | 否 | — | 任务编号 |
| `retry_no` | tinyint unsigned | 否 | — | 第几次重试（从 1 开始） |
| `result` | varchar(20) | 否 | — | success / failed / timeout |
| `error_msg` | varchar(500) | 是 | — | 本次失败原因 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 重试时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_task_retry` | `task_no`, `retry_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_task_retry_task` | `task_no`, `create_time` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_task_retry_task` | `task_no` → `edu_async_task`(`task_no`) | RESTRICT / RESTRICT |

## edu_dead_letter_task · 死信任务

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：超过最大重试次数的任务进入死信，运维查看与重放（REQ-IMP-038）；
重放复用原批次号与幂等键，重放原因必填并写审计；记录只追加与重放，不提供删除。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `task_no` | varchar(32) | 否 | — | 任务编号（唯一） |
| `task_type` | varchar(30) | 否 | — | 任务类型（枚举 `async_task_type`） |
| `dead_time` | datetime | 否 | — | 进入死信时间 |
| `retry_count` | tinyint unsigned | 否 | — | 已重试次数 |
| `last_error` | varchar(500) | 否 | — | 最后一次错误 |
| `batch_no` | varchar(32) | 是 | — | 原批次号 |
| `replay_status` | varchar(20) | 否 | 'replayable' | replayable 待重放 / replayed 已重放 |
| `replay_by` | bigint unsigned | 是 | — | 重放人 |
| `replay_time` | datetime | 是 | — | 重放时间 |
| `replay_reason` | varchar(500) | 是 | — | 重放原因（必填，写审计） |
| `replay_task_status` | varchar(20) | 是 | — | 重放后的执行结果，用于追溯 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_dead_letter_task` | `task_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_dead_letter_status` | `replay_status`, `dead_time` |  |
| `idx_dead_letter_type` | `task_type`, `dead_time` |  |

## edu_file_ref · 文件引用

- 模块：导入导出与异步任务（`importexport`）
- 范围：`school`｜批次：`5-2`
- 说明：数据库只保存文件引用，不保存二进制（REQ-IMP-041）。
下载走短时签名、与登录态绑定（REQ-IMP-042），每次下载写审计（REQ-IMP-043）；
引用带租户前缀，缓存键必须含租户（REQ-IMP-046）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `file_id` | bigint unsigned | 否 | — | 文件 ID（唯一） |
| `file_kind` | varchar(30) | 否 | — | import_source / export_result / failed_rows / template / photo（枚举 `file_kind`） |
| `file_name` | varchar(255) | 否 | — | 原始文件名 |
| `storage_key` | varchar(500) | 否 | — | 对象存储键（含租户与学校前缀） |
| `content_type` | varchar(100) | 是 | — | MIME 类型 |
| `file_size` | bigint | 否 | 0 | 字节数 |
| `biz_type` | varchar(30) | 是 | — | 业务类型（import / export / task） |
| `biz_id` | varchar(64) | 是 | — | 业务标识（批次号 / 任务号） |
| `expire_time` | datetime | 是 | — | 有效期（结果文件默认 7 天） |
| `download_count` | int | 否 | 0 | 下载次数 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_file_id` | `file_id` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_file_biz` | `biz_type`, `biz_id` |  |
| `idx_file_expire` | `expire_time` |  |

## edu_audit_log · 操作日志

- 模块：审计与操作日志（`audit`）
- 范围：`school`｜批次：`5-2`
- 说明：只允许追加，任何业务代码不得更新或删除（BR-AUDIT-003 / NFR-AUDIT-05）。
关键写操作与其日志在同一事务内提交（REQ-AUD-035）。
归档方式：**不启用原生分区**（MySQL 分区要求所有唯一键都含分区列，会破坏 `(request_id, object_id, action_type)` 的幂等语义）；
改为"迁移到同构归档表 + 归档批次留痕"：`edu_audit_log_archive` 与在线表同构，
归档 = 按时间范围搬过去并写 `edu_audit_archive_batch`，检索时按范围决定查在线表还是归档表（REQ-AUD-033）。
「运营访问记录」与「登录与安全事件」不单独建主表，由本表派生视图（见 views）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `action_type` | varchar(30) | 否 | — | create / update / delete / import / export / approve / grant / login / status_change（枚举 `audit_action_type`） |
| `module_code` | varchar(30) | 否 | — | 所属模块 |
| `object_type` | varchar(50) | 否 | — | 对象类型（student / class / teacher / stream 等） |
| `object_id` | varchar(64) | 否 | — | 对象标识 |
| `object_name` | varchar(200) | 是 | — | 对象名称（便于阅读，如 高一 (1) 班） |
| `operator_id` | bigint unsigned | 是 | — | 操作人；平台运营访问时为运营账号 |
| `operator_role` | varchar(30) | 是 | — | 操作人角色快照 |
| `client_ip` | varchar(64) | 是 | — | 来源 IP |
| `request_id` | varchar(64) | 是 | — | 请求标识（用于幂等写入与排障） |
| `batch_no` | varchar(32) | 是 | — | 批次号（批量操作共用） |
| `action_result` | varchar(16) | 否 | 'success' | success / failed |
| `source` | varchar(16) | 否 | 'web' | web / api / job / mq |
| `detail` | varchar(500) | 是 | — | 说明（驳回意见、失败原因等） |
| `log_time` | datetime | 否 | CURRENT_TIMESTAMP | 记录时间（分区键） |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_audit_idempotent` | `request_id`, `object_id`, `action_type` | 幂等写入（PRD 7.1：request_id + object_id + action_type） |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_audit_time` | `log_time` |  |
| `idx_audit_operator` | `operator_id`, `log_time` |  |
| `idx_audit_object` | `object_type`, `object_id`, `log_time` | 对象变更时间线（REQ-AUD-023） |
| `idx_audit_school_type` | `school_id`, `action_type`, `log_time` |  |
| `idx_audit_tenant` | `tenant_id`, `log_time` |  |

## edu_audit_change · 日志变更明细

- 模块：审计与操作日志（`audit`）
- 范围：`school`｜批次：`5-2`
- 说明：只记录**发生变化的字段**（REQ-AUD-003）；日志本身不含敏感字段明文，
只记录字段名与掩码后的值（REQ-AUD-010）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `school_id` | bigint unsigned | 否 | — | 学校归属 |
| `log_id` | bigint unsigned | 否 | — | 操作日志 ID |
| `field_name` | varchar(64) | 否 | — | 字段名 |
| `before_value` | varchar(1000) | 是 | — | 变更前值（敏感字段掩码） |
| `after_value` | varchar(1000) | 是 | — | 变更后值（敏感字段掩码） |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_audit_change` | `log_id`, `field_name` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_audit_change_field` | `field_name` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_audit_change_log` | `log_id` → `edu_audit_log`(`id`) | RESTRICT / RESTRICT |

## edu_audit_archive_batch · 日志归档批次

- 模块：审计与操作日志（`audit`）
- 范围：`tenant`｜批次：`5-2`
- 说明：超过在线保留窗口（默认 12 个月）的日志按时间归档，归档后仍可按时间范围检索（REQ-AUD-033）；
保留期内（不少于 3 年）不得清理（REQ-AUD-032）；归档与归档检索动作本身写日志（REQ-AUD-034）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `archive_no` | varchar(32) | 否 | — | 归档批次号（唯一） |
| `range_start` | date | 否 | — | 归档范围开始 |
| `range_end` | date | 否 | — | 归档范围结束 |
| `row_count` | bigint | 否 | 0 | 归档行数 |
| `file_id` | bigint unsigned | 是 | — | 归档文件引用 |
| `archive_status` | varchar(16) | 否 | 'running' | running 进行中 / done 已完成 / failed 失败 |
| `operator_id` | bigint unsigned | 是 | — | 操作人 |
| `archive_time` | datetime | 是 | — | 完成时间 |
| `error_msg` | varchar(500) | 是 | — | 失败原因 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_archive_no` | `archive_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_archive_range` | `range_start`, `range_end` |  |
| `idx_archive_status` | `archive_status` |  |

## edu_data_grant · 数据共享授权（仅教学资源）

- 模块：数据权限（横切）（`datascope`）
- 范围：`tenant`｜批次：`5-2`
- 说明：授权对象**仅限教学资源**（题库习题、试卷等）；学生、班级、年级、教师、成绩、学籍等业务数据一律不跨校共享
（BR-DATA-018）。授权由运营人员直接创建生效，**不设审批**（BR-DATA-017）；可撤销、有有效期（BR-DATA-013）；
授权与每次访问都留审计（BR-DATA-012）；到期或撤销后历史记录保留（BR-DATA-016）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `grant_no` | varchar(32) | 否 | — | 业务编号（幂等键，唯一） |
| `grantor_user_id` | bigint unsigned | 否 | — | 授权人（运营方用户） |
| `grantee_type` | varchar(20) | 否 | — | tenant / user / role |
| `grantee_id` | varchar(64) | 否 | — | 被授权对象 ID |
| `title` | varchar(200) | 否 | — | 事由标题 |
| `reason` | varchar(500) | 否 | — | 共享原因 |
| `resource_types` | varchar(200) | 否 | — | 资源类型集合（逗号分隔，如 question_bank_item |
| `resource_scope` | json | 是 | — | 资源范围细化（哪些题库 / 试卷；为空表示该类型全部） |
| `effective_start` | datetime | 否 | — | 生效时间 |
| `effective_end` | datetime | 是 | — | 失效时间（为空表示长期有效） |
| `grant_status` | varchar(20) | 否 | 'draft' | 草稿 / 生效 / 已撤销 / 已过期 |
| `revoke_by` | bigint unsigned | 是 | — | 撤销人 |
| `revoke_time` | datetime | 是 | — | 撤销时间 |
| `revoke_reason` | varchar(500) | 是 | — | 撤销原因 |
| `create_by` | bigint unsigned | 是 | — | 创建人 |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint unsigned | 是 | — | 更新人 |
| `update_time` | datetime | 是 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_grant_no` | `grant_no` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_grant_grantee` | `grantee_type`, `grantee_id`, `grant_status` |  |
| `idx_grant_effective` | `grant_status`, `effective_end` |  |

## edu_data_grant_scope · 授权范围明细

- 模块：数据权限（横切）（`datascope`）
- 范围：`tenant`｜批次：`5-2`
- 说明：一条授权可覆盖多个学校与多种资源；首轮只允许 read / export，不开放 write（BR-DATA-015）。

| 列 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | bigint unsigned | 否 | — | 主键（自增） |
| `tenant_id` | varchar(20) | 否 | — | 租户隔离键（NFR-SEC-01） |
| `grant_id` | bigint unsigned | 否 | — | 所属授权 |
| `scope_type` | varchar(20) | 否 | — | school_tenant / grade / class |
| `scope_id` | varchar(64) | 否 | — | 范围对象 ID |
| `resource_code` | varchar(64) | 否 | — | 资源编码（取自权限矩阵，如 question_bank_item） |
| `access_level` | varchar(20) | 否 | — | 首轮仅 read / export，不开放 write（BR-DATA-015） |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |

**唯一键**

| 名称 | 列 | 说明 |
|---|---|---|
| `uk_grant_scope` | `grant_id`, `scope_type`, `scope_id`, `resource_code`, `access_level` |  |

**索引**

| 名称 | 列 | 说明 |
|---|---|---|
| `idx_grant_scope_resource` | `resource_code`, `access_level` |  |

**外键**（在 `V5__edu_foreign_keys.sql` 中统一添加）

| 名称 | 列 → 目标 | 级联 |
|---|---|---|
| `fk_grant_scope_grant` | `grant_id` → `edu_data_grant`(`id`) | RESTRICT / RESTRICT |
