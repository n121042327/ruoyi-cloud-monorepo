-- V2__edu_org_config.sql
-- 组织与配置（学校 / 校区 / 学段 / 学年学期 / 学科）：班级与选科都要引用，必须在它们之前建
-- 共 7 张表；生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml
-- 外键统一在 V5__edu_foreign_keys.sql 里添加，避免脚本顺序耦合

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- edu_subject（学科） ----------
DROP TABLE IF EXISTS `edu_subject`;
CREATE TABLE `edu_subject` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `subject_code` varchar(32) NOT NULL COMMENT '学科编码（校内唯一）',
  `subject_name` varchar(50) NOT NULL COMMENT '学科名称',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号（决定再选科目的展示顺序，REQ-STR-017）',
  `stream_enabled` char(1) NOT NULL DEFAULT '0' COMMENT '是否参与 3+1+2',
  `stream_role` varchar(20) NOT NULL DEFAULT 'none' COMMENT 'primary 首选 / secondary 再选 / none 不参与',
  `subject_status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '正常 / 已停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_code` (`tenant_id`, `school_id`, `subject_code`),
  UNIQUE KEY `uk_subject_name` (`school_id`, `subject_name`),
  KEY `idx_subject_school` (`tenant_id`, `school_id`, `subject_status`),
  KEY `idx_subject_stream` (`school_id`, `stream_enabled`, `stream_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学科';

-- ---------- edu_subject_stage（学科与学段启用） ----------
DROP TABLE IF EXISTS `edu_subject_stage`;
CREATE TABLE `edu_subject_stage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `subject_id` bigint unsigned NOT NULL COMMENT '学科',
  `stage_code` varchar(20) NOT NULL COMMENT '学段',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 启用 / 0 停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_stage` (`subject_id`, `stage_code`),
  KEY `idx_subject_stage_school` (`school_id`, `stage_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学科与学段启用';

-- ---------- edu_school（学校） ----------
DROP TABLE IF EXISTS `edu_school`;
CREATE TABLE `edu_school` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `school_code` varchar(50) NOT NULL COMMENT '学校编码（父租户内唯一）',
  `school_name` varchar(100) NOT NULL COMMENT '学校名称',
  `short_name` varchar(50) NULL COMMENT '简称',
  `parent_tenant_id` varchar(20) NULL COMMENT '上级租户（集团 / 运营方）',
  `root_tenant_id` varchar(20) NULL COMMENT '根租户（运营方）',
  `school_type` varchar(20) NULL COMMENT '学校类型（公办 / 民办 / 其他）',
  `address` varchar(255) NULL COMMENT '地址',
  `phone` varchar(20) NULL COMMENT '联系电话',
  `school_status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '正常 / 已停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_tenant` (`tenant_id`),
  UNIQUE KEY `uk_school_code` (`parent_tenant_id`, `school_code`),
  KEY `idx_school_name` (`school_name`),
  KEY `idx_school_status` (`school_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学校';

-- ---------- edu_campus（校区） ----------
DROP TABLE IF EXISTS `edu_campus`;
CREATE TABLE `edu_campus` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `campus_code` varchar(50) NOT NULL COMMENT '校区编码',
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `address` varchar(255) NULL COMMENT '地址',
  `leader_name` varchar(50) NULL COMMENT '负责人',
  `leader_phone` varchar(20) NULL COMMENT '负责人电话',
  `campus_status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '正常 / 已停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_campus_name` (`school_id`, `campus_name`),
  UNIQUE KEY `uk_campus_code` (`school_id`, `campus_code`),
  KEY `idx_campus_school` (`tenant_id`, `school_id`, `campus_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校区';

-- ---------- edu_school_stage（学校开设学段） ----------
DROP TABLE IF EXISTS `edu_school_stage`;
CREATE TABLE `edu_school_stage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `stage_code` varchar(20) NOT NULL COMMENT '学段',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 开设 / 0 停开',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_stage` (`school_id`, `stage_code`),
  KEY `idx_school_stage_tenant` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学校开设学段';

-- ---------- edu_academic_year（学年） ----------
DROP TABLE IF EXISTS `edu_academic_year`;
CREATE TABLE `edu_academic_year` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `academic_year_code` varchar(20) NOT NULL COMMENT '学年编码（如 2026-2027）',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `academic_year_status` varchar(16) NOT NULL DEFAULT 'normal' COMMENT '未开始 / 进行中 / 已归档',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_academic_year_code` (`tenant_id`, `school_id`, `academic_year_code`),
  KEY `idx_academic_year_range` (`school_id`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学年';

-- ---------- edu_term（学期） ----------
DROP TABLE IF EXISTS `edu_term`;
CREATE TABLE `edu_term` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `academic_year_id` bigint unsigned NOT NULL COMMENT '学年',
  `term_code` varchar(20) NOT NULL COMMENT '学期编码（如 1 / 2）',
  `term_name` varchar(50) NOT NULL COMMENT '学期名称（第一学期 / 第二学期）',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `is_current` char(1) NOT NULL DEFAULT '0' COMMENT '是否当前学期',
  `term_status` varchar(16) NOT NULL DEFAULT 'normal' COMMENT '正常 / 已归档',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term_code` (`academic_year_id`, `term_code`),
  KEY `idx_term_school_current` (`school_id`, `is_current`),
  KEY `idx_term_range` (`school_id`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学期';

SET FOREIGN_KEY_CHECKS = 1;
