-- V3__edu_grade_class.sql
-- 年级与班级（含教学班与两套成员关系）
-- 共 5 张表；生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml
-- 外键统一在 V5__edu_foreign_keys.sql 里添加，避免脚本顺序耦合

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- edu_grade（年级） ----------
DROP TABLE IF EXISTS `edu_grade`;
CREATE TABLE `edu_grade` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `stage_code` varchar(20) NOT NULL COMMENT '学段',
  `enroll_year` smallint NOT NULL COMMENT '入学年份（如 2026）',
  `grade_level` tinyint NOT NULL COMMENT '学段内序号（小学 1–6、初中 / 高中 1–3）',
  `grade_name` varchar(50) NOT NULL COMMENT '年级名称（如 2026 级 高一）',
  `class_count` int NOT NULL DEFAULT 0 COMMENT '班级数（冗余统计，写入时维护）',
  `student_count` int NOT NULL DEFAULT 0 COMMENT '在读学生数（冗余统计）',
  `grade_status` varchar(20) NOT NULL DEFAULT 'normal' COMMENT '正常 / 已归档',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grade_seq` (`school_id`, `stage_code`, `enroll_year`, `grade_level`),
  UNIQUE KEY `uk_grade_name` (`school_id`, `stage_code`, `grade_name`),
  KEY `idx_grade_school_status` (`tenant_id`, `school_id`, `grade_status`),
  KEY `idx_grade_enroll_year` (`school_id`, `enroll_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='年级';

-- ---------- edu_class（班级（行政班 / 教学班容器）） ----------
DROP TABLE IF EXISTS `edu_class`;
CREATE TABLE `edu_class` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `grade_id` bigint unsigned NULL COMMENT '年级（教学班按组合生成时可空；行政班必填）',
  `stage_code` varchar(20) NOT NULL COMMENT '学段',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `class_type` varchar(20) NOT NULL COMMENT 'administrative / teaching',
  `class_capacity` int NULL COMMENT '容量上限（只提示不拦截）',
  `head_teacher_id` bigint unsigned NULL COMMENT '班主任（行政班唯一在任；教学班为空）',
  `head_teacher_start_date` date NULL COMMENT '班主任任职开始日期',
  `head_teacher_end_date` date NULL COMMENT '班主任任职结束日期（学年切换时保留历史）',
  `campus_id` bigint unsigned NULL COMMENT '校区（参考信息，不参与权限判定）',
  `classroom` varchar(50) NULL COMMENT '教室（自由文本）',
  `subject_combination` varchar(64) NULL COMMENT '教学班的组合 / 单学科标识（教学班使用）',
  `class_status` varchar(16) NOT NULL DEFAULT 'active' COMMENT 'active 在读 / disabled 已停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_name` (`school_id`, `term_id`, `stage_code`, `class_name`),
  UNIQUE KEY `uk_class_teaching` (`school_id`, `term_id`, `subject_combination`, `class_type`),
  KEY `idx_class_school_term_status` (`school_id`, `term_id`, `class_status`),
  KEY `idx_class_head_teacher` (`head_teacher_id`, `class_status`),
  KEY `idx_class_grade` (`school_id`, `grade_id`),
  KEY `idx_class_campus` (`school_id`, `campus_id`, `class_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='班级（行政班 / 教学班容器）';

-- ---------- edu_class_member（班级成员关系（花名册）） ----------
DROP TABLE IF EXISTS `edu_class_member`;
CREATE TABLE `edu_class_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `class_id` bigint unsigned NOT NULL COMMENT '行政班',
  `class_type` varchar(20) NOT NULL DEFAULT 'administrative' COMMENT '恒为 administrative（教学班成员在 edu_teaching_class_member）',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID（平台级，经本表两段式取数）',
  `student_enrollment_id` bigint unsigned NULL COMMENT '在校记录（学校侧身份）',
  `join_date` date NOT NULL COMMENT '加入日期',
  `leave_date` date NULL COMMENT '离开日期',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 在班 / 0 已离开',
  `gender_snapshot` char(1) NULL COMMENT '性别快照（花名册排序与展示）',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_member_admin` (`term_id`, `student_id`),
  KEY `idx_member_class` (`class_id`, `class_type`, `status`),
  KEY `idx_member_student` (`term_id`, `student_id`, `class_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='班级成员关系（花名册）';

-- ---------- edu_teaching_class（教学班） ----------
DROP TABLE IF EXISTS `edu_teaching_class`;
CREATE TABLE `edu_teaching_class` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `grade_id` bigint unsigned NULL COMMENT '年级',
  `class_name` varchar(100) NOT NULL COMMENT '教学班名称（如 高一 · 物化生 A 层）',
  `combination` varchar(64) NOT NULL COMMENT '组合或单学科标识（如 物理+化学+生物 或 单学科：物理）',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数（冗余统计）',
  `teaching_class_status` varchar(16) NOT NULL DEFAULT 'active' COMMENT 'active 正常 / disabled 已停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teaching_class` (`school_id`, `term_id`, `combination`, `class_name`),
  KEY `idx_tclass_school_term` (`school_id`, `term_id`, `teaching_class_status`),
  KEY `idx_tclass_grade` (`grade_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教学班';

-- ---------- edu_teaching_class_member（教学班成员关系） ----------
DROP TABLE IF EXISTS `edu_teaching_class_member`;
CREATE TABLE `edu_teaching_class_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `teaching_class_id` bigint unsigned NOT NULL COMMENT '教学班',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `source` varchar(20) NOT NULL DEFAULT 'generate' COMMENT 'generate 生成 / manual 手工调整',
  `generate_task_no` varchar(32) NULL COMMENT '生成任务的幂等键（REQ-STR-057）',
  `join_date` date NOT NULL COMMENT '加入日期',
  `leave_date` date NULL COMMENT '离开日期',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 在班 / 0 已离开',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tclass_member` (`teaching_class_id`, `student_id`),
  KEY `idx_tcm_student` (`term_id`, `student_id`),
  KEY `idx_tcm_task` (`generate_task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教学班成员关系';

SET FOREIGN_KEY_CHECKS = 1;
