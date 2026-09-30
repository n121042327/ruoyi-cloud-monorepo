# 唯一键、外键与索引清单（含理由）

> 本文件由 `tools/gen_schema_artifacts.py` 从 `database/schema.yaml` 生成。
> 索引的取舍原则：先满足**数据范围解析**与**唯一性约束**，再满足列表筛选与排序；不做「以防万一」的索引。

## 1. 唯一键（业务不变式的最后一道防线）

| 表 | 唯一键 | 列 | 理由 |
|---|---|---|---|
| `edu_student` | `uk_student_no` | `student_no` | 学号平台唯一，永不回收 |
| `edu_student` | `uk_national_student_no` | `national_student_no` | 非空时唯一（MySQL 唯一索引允许多个 NULL） |
| `edu_student` | `uk_id_card_no` | `id_card_no` | 非空时唯一（GAP-020 平台唯一） |
| `edu_student_enrollment` | `uk_enrollment_student_school` | `student_id`, `school_id` | 同一学生同一学校一条在校记录 |
| `edu_guardian` | `uk_guardian_phone` | `guardian_phone` | 手机号平台唯一（一个家长一个账号） |
| `edu_student_guardian` | `uk_student_guardian` | `student_id`, `guardian_id` | 同一学生同一监护人只有一条关系记录 |
| `edu_student_field_change` | `uk_sfc_pending` | `pending_guard` | 同一学生同一字段同时只允许一条待审核（DB 级强制，GAP-018） |
| `edu_activation_code` | `uk_activation_code` | `code` |  |
| `edu_activation_code` | `uk_activation_active` | `active_guard` | 同一学生同一时刻只允许一个未使用激活码（DB 级强制） |
| `edu_teacher` | `uk_teacher_no` | `tenant_id`, `teacher_no` | 工号学校租户内唯一（BR-TEACHER-007） |
| `edu_user_role` | `uk_user_role` | `tenant_id`, `school_id`, `user_id`, `edu_role` |  |
| `edu_grade_leader` | `uk_grade_leader` | `term_id`, `grade_id`, `user_id` |  |
| `edu_teaching_assignment` | `uk_assignment` | `term_id`, `teacher_id`, `subject_id`, `class_type`, `class_id` |  |
| `edu_grade` | `uk_grade_seq` | `school_id`, `stage_code`, `enroll_year`, `grade_level` | BR-GRADE-002 |
| `edu_grade` | `uk_grade_name` | `school_id`, `stage_code`, `grade_name` | BR-GRADE-003 |
| `edu_class` | `uk_class_name` | `school_id`, `term_id`, `stage_code`, `class_name` | BR-CLASS-003 |
| `edu_class` | `uk_class_teaching` | `school_id`, `term_id`, `subject_combination`, `class_type` | 同一学期同一组合只有一个教学班（幂等生成的前提，REQ-STR-057） |
| `edu_class_member` | `uk_class_member_admin` | `term_id`, `student_id` | 行政班唯一：同一学年学期一名学生只能属于一个行政班（BR-STU-003）；教学班成员另表存放，因此本键可由 MySQL 直接表达，不需要部分唯一索引 |
| `edu_teaching_class` | `uk_teaching_class` | `school_id`, `term_id`, `combination`, `class_name` | 幂等生成：同一学期同一组合同一名称只建一次 |
| `edu_teaching_class_member` | `uk_tclass_member` | `teaching_class_id`, `student_id` | 同一教学班同一学生只允许一条有效关系（幂等） |
| `edu_promotion_task` | `uk_promotion_task_no` | `task_no` |  |
| `edu_promotion_item` | `uk_promotion_item` | `task_id`, `student_id` | 幂等键（REQ-PRM-029） |
| `edu_transfer_order` | `uk_transfer_no` | `transfer_no` |  |
| `edu_stream_config` | `uk_stream_config` | `school_id`, `term_id` | 同一学校同一学期只允许一份生效配置（REQ-STR-004） |
| `edu_student_stream` | `uk_student_stream` | `term_id`, `student_id` | BR-STREAM-007 |
| `edu_stream_change_request` | `uk_stream_request_no` | `request_no` |  |
| `edu_subject` | `uk_subject_code` | `tenant_id`, `school_id`, `subject_code` | BR-SUBJECT-001 |
| `edu_subject` | `uk_subject_name` | `school_id`, `subject_name` |  |
| `edu_subject_stage` | `uk_subject_stage` | `subject_id`, `stage_code` |  |
| `edu_school` | `uk_school_tenant` | `tenant_id` | 一个学校对应一个租户（BR-ORG-002） |
| `edu_school` | `uk_school_code` | `parent_tenant_id`, `school_code` | BR-ORG-011 |
| `edu_campus` | `uk_campus_name` | `school_id`, `campus_name` |  |
| `edu_campus` | `uk_campus_code` | `school_id`, `campus_code` |  |
| `edu_school_stage` | `uk_school_stage` | `school_id`, `stage_code` |  |
| `edu_academic_year` | `uk_academic_year_code` | `tenant_id`, `school_id`, `academic_year_code` | BR-TERM-003 |
| `edu_term` | `uk_term_code` | `academic_year_id`, `term_code` |  |
| `edu_import_template` | `uk_import_template` | `module_code`, `template_version` |  |
| `edu_import_batch` | `uk_import_batch_no` | `batch_no` |  |
| `edu_import_error` | `uk_import_error_row` | `batch_no`, `row_no` |  |
| `edu_async_task` | `uk_async_task_no` | `task_no` |  |
| `edu_async_task_retry` | `uk_task_retry` | `task_no`, `retry_no` |  |
| `edu_dead_letter_task` | `uk_dead_letter_task` | `task_no` |  |
| `edu_file_ref` | `uk_file_id` | `file_id` |  |
| `edu_audit_log` | `uk_audit_idempotent` | `request_id`, `object_id`, `action_type` | 幂等写入（PRD 7.1：request_id + object_id + action_type） |
| `edu_audit_change` | `uk_audit_change` | `log_id`, `field_name` |  |
| `edu_audit_archive_batch` | `uk_archive_no` | `archive_no` |  |
| `edu_data_grant` | `uk_grant_no` | `grant_no` |  |
| `edu_data_grant_scope` | `uk_grant_scope` | `grant_id`, `scope_type`, `scope_id`, `resource_code`, `access_level` |  |

### 无法用唯一索引表达的约束（由业务层保证 + 索引兜底）

| 约束 | 表 | 做法 |
|---|---|---|
| 同一学生同一学期同时只允许一条**待审批**的选科变更 | `edu_stream_change_request` | 业务层在提交前按 `(term_id, student_id, request_status='pending')` 计数；`idx_stream_request_student` 用于这次检查 |
| 同一学生同一字段同时只允许一条**待审核**的资料变更 | `edu_student_field_change` | 业务层按 `(student_id, field_name, status='pending')` 计数；`idx_sfc_pending` 兜底 |
| 同一学生同一时刻只允许一个**未使用**的激活码 | `edu_activation_code` | 业务层按 `(student_id, status='unused')` 计数；重置时先把旧码置为已作废 |
| 同一学校同一学期只允许一个教学班同一组合 | `edu_teaching_class` | `uk_teaching_class` 已覆盖（同一学期同一组合同一名称唯一） |
| 一名班主任同时只负责一个班级的「在任」关系 | `edu_class` | 班主任是 `edu_class.head_teacher_id` 字段，一名教师可同时是多个班的班主任（现实中常见），因此不加唯一键 |

## 2. 外键（同服务内核心关系，禁止级联删除）

| 表 | 外键 | 列 → 目标 | 级联 |
|---|---|---|---|
| `edu_student_enrollment` | `fk_enrollment_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_student_guardian` | `fk_sg_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_student_guardian` | `fk_sg_guardian` | `guardian_id` → `edu_guardian` | RESTRICT / RESTRICT |
| `edu_student_field_change` | `fk_sfc_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_activation_code` | `fk_activation_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_grade_leader` | `fk_grade_leader_grade` | `grade_id` → `edu_grade` | RESTRICT / RESTRICT |
| `edu_teaching_assignment` | `fk_assignment_teacher` | `teacher_id` → `edu_teacher` | RESTRICT / RESTRICT |
| `edu_teaching_assignment` | `fk_assignment_subject` | `subject_id` → `edu_subject` | RESTRICT / RESTRICT |
| `edu_class` | `fk_class_grade` | `grade_id` → `edu_grade` | RESTRICT / RESTRICT |
| `edu_class` | `fk_class_term` | `term_id` → `edu_term` | RESTRICT / RESTRICT |
| `edu_class` | `fk_class_head_teacher` | `head_teacher_id` → `edu_teacher` | RESTRICT / RESTRICT |
| `edu_class_member` | `fk_member_class` | `class_id` → `edu_class` | RESTRICT / RESTRICT |
| `edu_class_member` | `fk_member_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_teaching_class` | `fk_tclass_term` | `term_id` → `edu_term` | RESTRICT / RESTRICT |
| `edu_teaching_class_member` | `fk_tcm_class` | `teaching_class_id` → `edu_teaching_class` | RESTRICT / RESTRICT |
| `edu_teaching_class_member` | `fk_tcm_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_promotion_task` | `fk_promotion_source_term` | `source_term_id` → `edu_term` | RESTRICT / RESTRICT |
| `edu_promotion_task` | `fk_promotion_target_term` | `target_term_id` → `edu_term` | RESTRICT / RESTRICT |
| `edu_promotion_item` | `fk_promotion_item_task` | `task_id` → `edu_promotion_task` | RESTRICT / RESTRICT |
| `edu_promotion_item` | `fk_promotion_item_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_enrollment_change` | `fk_change_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_transfer_order` | `fk_transfer_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_stream_config` | `fk_stream_config_term` | `term_id` → `edu_term` | RESTRICT / RESTRICT |
| `edu_student_stream` | `fk_student_stream_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_stream_change_request` | `fk_stream_request_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_stream_history` | `fk_stream_history_student` | `student_id` → `edu_student` | RESTRICT / RESTRICT |
| `edu_subject_stage` | `fk_subject_stage_subject` | `subject_id` → `edu_subject` | RESTRICT / RESTRICT |
| `edu_term` | `fk_term_year` | `academic_year_id` → `edu_academic_year` | RESTRICT / RESTRICT |
| `edu_import_batch` | `fk_import_batch_task` | `async_task_no` → `edu_async_task` | RESTRICT / RESTRICT |
| `edu_async_task_retry` | `fk_task_retry_task` | `task_no` → `edu_async_task` | RESTRICT / RESTRICT |
| `edu_audit_change` | `fk_audit_change_log` | `log_id` → `edu_audit_log` | RESTRICT / RESTRICT |
| `edu_data_grant_scope` | `fk_grant_scope_grant` | `grant_id` → `edu_data_grant` | RESTRICT / RESTRICT |

> 跨服务关系（例如未来题库引用学科）用逻辑引用 + 一致性检查，不建物理外键。
> 全部外键在 `V5__edu_foreign_keys.sql` 中统一添加：建表脚本按依赖顺序执行，外键最后加可以避免脚本顺序耦合。

## 3. 索引

| 表 | 索引 | 列 | 用途 |
|---|---|---|---|
| `edu_student` | `idx_student_name` | `student_name` |  |
| `edu_student` | `idx_enroll_year` | `enroll_year` |  |
| `edu_student_enrollment` | `idx_enrollment_school_status` | `school_id`, `enrollment_status` |  |
| `edu_student_enrollment` | `idx_enrollment_tenant` | `tenant_id` |  |
| `edu_guardian` | `idx_guardian_user` | `user_id` |  |
| `edu_student_guardian` | `idx_sg_guardian` | `guardian_id`, `bind_status` |  |
| `edu_student_guardian` | `idx_sg_student` | `student_id`, `bind_status` |  |
| `edu_student_field_change` | `idx_sfc_pending` | `student_id`, `field_name`, `status` | 待审核申请列表查询（唯一性由 uk_sfc_pending 强制） |
| `edu_student_field_change` | `idx_sfc_school` | `school_id`, `status` |  |
| `edu_activation_code` | `idx_activation_student` | `student_id`, `status` |  |
| `edu_activation_code` | `idx_activation_batch` | `issue_batch_no` |  |
| `edu_teacher` | `idx_teacher_school` | `tenant_id`, `school_id` |  |
| `edu_teacher` | `idx_teacher_name` | `teacher_name` |  |
| `edu_teacher` | `idx_teacher_user` | `user_id` |  |
| `edu_user_role` | `idx_user_role_user` | `user_id`, `status` | 数据范围解析的入口索引 |
| `edu_grade_leader` | `idx_grade_leader_user_term` | `user_id`, `term_id`, `status` | 数据范围解析入口 |
| `edu_grade_leader` | `idx_grade_leader_grade` | `grade_id`, `term_id` |  |
| `edu_teaching_assignment` | `idx_assignment_teacher_term` | `teacher_id`, `term_id`, `status` | 数据范围解析入口 |
| `edu_teaching_assignment` | `idx_assignment_class_term` | `term_id`, `class_type`, `class_id`, `status` |  |
| `edu_teaching_assignment` | `idx_assignment_tenant` | `tenant_id`, `school_id` |  |
| `edu_grade` | `idx_grade_school_status` | `tenant_id`, `school_id`, `grade_status` |  |
| `edu_grade` | `idx_grade_enroll_year` | `school_id`, `enroll_year` |  |
| `edu_class` | `idx_class_school_term_status` | `school_id`, `term_id`, `class_status` |  |
| `edu_class` | `idx_class_head_teacher` | `head_teacher_id`, `class_status` | 数据范围解析入口（DS-06） |
| `edu_class` | `idx_class_grade` | `school_id`, `grade_id` |  |
| `edu_class` | `idx_class_campus` | `school_id`, `campus_id`, `class_status` |  |
| `edu_class_member` | `idx_member_class` | `class_id`, `class_type`, `status` |  |
| `edu_class_member` | `idx_member_student` | `term_id`, `student_id`, `class_type` |  |
| `edu_teaching_class` | `idx_tclass_school_term` | `school_id`, `term_id`, `teaching_class_status` |  |
| `edu_teaching_class` | `idx_tclass_grade` | `grade_id`, `term_id` |  |
| `edu_teaching_class_member` | `idx_tcm_student` | `term_id`, `student_id` |  |
| `edu_teaching_class_member` | `idx_tcm_task` | `generate_task_no` |  |
| `edu_promotion_task` | `idx_promotion_task_term` | `school_id`, `source_term_id`, `target_term_id`, `task_status` |  |
| `edu_promotion_task` | `idx_promotion_task_tenant` | `tenant_id`, `task_status` |  |
| `edu_promotion_item` | `idx_promotion_item_status` | `task_id`, `item_status` |  |
| `edu_promotion_item` | `idx_promotion_item_student` | `student_id` |  |
| `edu_enrollment_change` | `idx_change_student` | `student_id`, `operate_time` |  |
| `edu_enrollment_change` | `idx_change_school` | `school_id`, `change_type`, `effective_date` |  |
| `edu_enrollment_change` | `idx_change_approval` | `school_id`, `approval_status` |  |
| `edu_transfer_order` | `idx_transfer_student_status` | `student_id`, `transfer_status` |  |
| `edu_transfer_order` | `idx_transfer_to_school` | `to_school_id`, `transfer_status` |  |
| `edu_transfer_order` | `idx_transfer_from_school` | `from_school_id`, `transfer_status` |  |
| `edu_stream_config` | `idx_stream_config_tenant` | `tenant_id`, `term_id` |  |
| `edu_student_stream` | `idx_stream_stat` | `term_id`, `primary_subject_code`, `secondary_subject_codes` | 组合分布统计（REQ-STR-049） |
| `edu_student_stream` | `idx_stream_school` | `school_id`, `term_id` |  |
| `edu_stream_change_request` | `idx_stream_request_pending` | `school_id`, `request_status`, `apply_time` | 审批待办按提交时间升序（REQ-STR-035）；同一学生待审批唯一由业务层 + 本索引兜底 |
| `edu_stream_change_request` | `idx_stream_request_student` | `term_id`, `student_id`, `request_status` |  |
| `edu_stream_history` | `idx_stream_history_student` | `student_id`, `operate_time` |  |
| `edu_stream_history` | `idx_stream_history_term` | `term_id`, `school_id` |  |
| `edu_subject` | `idx_subject_school` | `tenant_id`, `school_id`, `subject_status` |  |
| `edu_subject` | `idx_subject_stream` | `school_id`, `stream_enabled`, `stream_role` |  |
| `edu_subject_stage` | `idx_subject_stage_school` | `school_id`, `stage_code`, `status` |  |
| `edu_school` | `idx_school_name` | `school_name` |  |
| `edu_school` | `idx_school_status` | `school_status` |  |
| `edu_campus` | `idx_campus_school` | `tenant_id`, `school_id`, `campus_status` |  |
| `edu_school_stage` | `idx_school_stage_tenant` | `tenant_id`, `status` |  |
| `edu_academic_year` | `idx_academic_year_range` | `school_id`, `start_date`, `end_date` |  |
| `edu_term` | `idx_term_school_current` | `school_id`, `is_current` | 同一学校同一学年只允许一个当前学期（业务层 + 本索引兜底） |
| `edu_term` | `idx_term_range` | `school_id`, `start_date`, `end_date` |  |
| `edu_import_template` | `idx_import_template_current` | `module_code`, `status` |  |
| `edu_import_batch` | `idx_import_batch_school` | `school_id`, `import_status`, `create_time` |  |
| `edu_import_batch` | `idx_import_batch_operator` | `operator_id`, `create_time` |  |
| `edu_import_error` | `idx_import_error_result` | `batch_no`, `result` |  |
| `edu_async_task` | `idx_async_task_owner` | `owner_id`, `create_time` | 默认只看本人任务（REQ-IMP-032） |
| `edu_async_task` | `idx_async_task_school_status` | `school_id`, `task_status`, `create_time` |  |
| `edu_async_task` | `idx_async_task_type` | `task_type`, `task_status` |  |
| `edu_async_task_retry` | `idx_task_retry_task` | `task_no`, `create_time` |  |
| `edu_dead_letter_task` | `idx_dead_letter_status` | `replay_status`, `dead_time` |  |
| `edu_dead_letter_task` | `idx_dead_letter_type` | `task_type`, `dead_time` |  |
| `edu_file_ref` | `idx_file_biz` | `biz_type`, `biz_id` |  |
| `edu_file_ref` | `idx_file_expire` | `expire_time` |  |
| `edu_audit_log` | `idx_audit_time` | `log_time` |  |
| `edu_audit_log` | `idx_audit_operator` | `operator_id`, `log_time` |  |
| `edu_audit_log` | `idx_audit_object` | `object_type`, `object_id`, `log_time` | 对象变更时间线（REQ-AUD-023） |
| `edu_audit_log` | `idx_audit_school_type` | `school_id`, `action_type`, `log_time` |  |
| `edu_audit_log` | `idx_audit_tenant` | `tenant_id`, `log_time` |  |
| `edu_audit_change` | `idx_audit_change_field` | `field_name` |  |
| `edu_audit_archive_batch` | `idx_archive_range` | `range_start`, `range_end` |  |
| `edu_audit_archive_batch` | `idx_archive_status` | `archive_status` |  |
| `edu_data_grant` | `idx_grant_grantee` | `grantee_type`, `grantee_id`, `grant_status` |  |
| `edu_data_grant` | `idx_grant_effective` | `grant_status`, `effective_end` |  |
| `edu_data_grant_scope` | `idx_grant_scope_resource` | `resource_code`, `access_level` |  |

## 4. 数据范围解析依赖的索引（性能关键路径）

| 范围 | 依赖索引 |
|---|---|
| `DS-04` 本校 | `edu_user_role.idx_user_role_user`、各表的 `(tenant_id, school_id, …)` 前缀 |
| `DS-05` 本年级 | `edu_grade_leader.idx_grade_leader_user_term` |
| `DS-06` 本班 | `edu_class.idx_class_head_teacher` |
| `DS-07` 任教班级 | `edu_teaching_assignment.idx_assignment_teacher_term` |
| `DS-01` / 共享授权 | `edu_data_grant.idx_grant_grantee`、`edu_data_grant_scope.idx_grant_scope_resource` |

## 5. 统计

- 表：42（另有 1 张派生表、2 个派生视图）
- 唯一键：48
- 外键：32
- 索引：82
