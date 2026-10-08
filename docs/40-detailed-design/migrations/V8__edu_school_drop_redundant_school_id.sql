-- V8__edu_school_drop_redundant_school_id.sql
-- 阶段 5 产物新版本（CR-104）：去掉 edu_school 的冗余列 school_id
-- 背景：生成器对「scope: school」的表会自动注入 `tenant_id` + `school_id` 两列。
--       `edu_school` 本身就是学校主体（一所学校对应一个租户，用 tenant_id 唯一），再注入 `school_id`
--       会得到一个 NOT NULL 且没有任何写入方的列：实体按主键约定把 Java 字段 schoolId 映射到 id，
--       MyBatis-Plus 生成的 INSERT 里不含 school_id，于是任何建校请求都以
--       “Field 'school_id' doesn't have a default value” 失败（edu 的学校管理页因此永远没有数据）。
-- 本脚本是**追加式增量**：
--   * V1__edu_student_teacher.sql ~ V7__edu_audit_create_dept.sql 为已冻结版本，本脚本不修改、不重建它们；
--   * 事实源 docs/40-detailed-design/database/schema.yaml 已把 edu_school 的 scope 由 school 改为 tenant，
--     下一次由 tools/gen_schema_artifacts.py 完整重新生成时，V2 的 CREATE TABLE 里不会再出现该列；
--   * 该列没有任何外键、索引与读写方（全仓库 grep 无引用），且执行时 edu_school 为空表，删除无数据风险。
-- 配套代码：EduDataPermissionHandler 的 SCHOOL_TABLES 已移除 edu_school
--          （其数据范围由 tenant_id 保证一校一租户，无需再按 school_id 过滤）。

SET NAMES utf8mb4;

ALTER TABLE `edu_school` DROP COLUMN `school_id`;
