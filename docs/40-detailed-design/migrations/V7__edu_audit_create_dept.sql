-- V7__edu_audit_create_dept.sql
-- 阶段 5 产物新版本（CR-103）：补齐 RuoYi 平台通用审计列 create_dept
-- 背景：RuoYi 的 BaseEntity 带 @TableField(fill = INSERT) 的 createDept 字段，edu 实体按阶段 7 约定
--       平台级实体继承 BaseEntity、学校/租户级实体继承 TenantEntity，因此 MyBatis-Plus 的通用
--       select / insert / selectVoById 会带上 create_dept 列；而事实上原先的 conventions.audit_columns
--       只登记了 create_by / create_time / update_by / update_time，建表脚本因此没有这一列，
--       表现为 “Unknown column 'create_dept' in 'field list'”，edu 侧通用读写接口全部 500（GAP-098）。
-- 本脚本是**追加式增量**：
--   * V1__edu_student_teacher.sql ~ V6__edu_student_contact.sql 为已冻结版本，本脚本不修改、不重建它们；
--   * 同名列表已同步写入事实源 docs/40-detailed-design/database/schema.yaml 的 conventions.audit_columns，
--     下一次由 tools/gen_schema_artifacts.py 完整重新生成时会把该列并入各表的 CREATE TABLE。
-- 覆盖范围：with_audit 为 true 的 31 张表。
--   with_audit 为 false 的 13 张表（audit_log / promotion_item / activation_code / stream_history 等）
--   既没有 audit_columns，其实体也不继承 BaseEntity，因此不加该列。
-- 注意：MySQL 8 的 ADD COLUMN 不支持 IF NOT EXISTS，本脚本按“执行一次”使用。

SET NAMES utf8mb4;

ALTER TABLE `edu_academic_year`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `academic_year_status`;
ALTER TABLE `edu_async_task`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `error_msg`;
ALTER TABLE `edu_campus`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `campus_status`;
ALTER TABLE `edu_class`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `class_status`;
ALTER TABLE `edu_class_member`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `gender_snapshot`;
ALTER TABLE `edu_data_grant`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `revoke_reason`;
ALTER TABLE `edu_enrollment_change`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `operate_time`;
ALTER TABLE `edu_grade`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `grade_status`;
ALTER TABLE `edu_grade_leader`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_guardian`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_import_batch`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `operator_id`;
ALTER TABLE `edu_import_template`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_promotion_task`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `cancel_reason`;
ALTER TABLE `edu_school`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `school_status`;
ALTER TABLE `edu_school_stage`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_stream_change_request`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `cancel_time`;
ALTER TABLE `edu_stream_config`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `config_status`;
ALTER TABLE `edu_student`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `remark`;
ALTER TABLE `edu_student_enrollment`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `remark`;
ALTER TABLE `edu_student_field_change`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `audit_opinion`;
ALTER TABLE `edu_student_guardian`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `unbind_time`;
ALTER TABLE `edu_student_stream`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `stream_status`;
ALTER TABLE `edu_subject`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `subject_status`;
ALTER TABLE `edu_subject_stage`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_teacher`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `remark`;
ALTER TABLE `edu_teaching_assignment`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_teaching_class`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `teaching_class_status`;
ALTER TABLE `edu_teaching_class_member`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `status`;
ALTER TABLE `edu_term`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `term_status`;
ALTER TABLE `edu_transfer_order`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `remark`;
ALTER TABLE `edu_user_role`
  ADD COLUMN `create_dept` bigint unsigned NULL COMMENT '创建部门' AFTER `end_date`;
