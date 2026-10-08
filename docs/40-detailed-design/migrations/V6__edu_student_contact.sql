-- V6__edu_student_contact.sql
-- 阶段 5 产物新版本（CR-095）：学生联系电话落点
-- 背景：GAP-090 裁决「照片按 DDL 用 photo_url；学生电话属于学校侧联系方式，改放 edu_student_enrollment」。
-- 本脚本是**追加式增量**：
--   * V1__edu_student_teacher.sql ~ V5__edu_foreign_keys.sql 为已冻结版本，本脚本不修改、不重建它们；
--   * 同名的列定义已同步写入事实源 docs/40-detailed-design/database/schema.yaml 的 edu_student_enrollment，
--     下一次由 tools/gen_schema_artifacts.py 完整重新生成时会把该列并入 V1 的 CREATE TABLE。
-- 注意：若在已有 V1 建表结果的库上执行完整重新生成，请先确认本增量是否已被吸收，避免重复加列。

SET NAMES utf8mb4;

ALTER TABLE `edu_student_enrollment`
  ADD COLUMN `student_phone` varchar(20) NULL COMMENT '学生联系电话（敏感，默认掩码展示，查看全量需 read_contact 并写访问日志）' AFTER `entry_grade_id`;
