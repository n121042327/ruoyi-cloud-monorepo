-- V1__edu_student_teacher.sql
-- 学生与教师（含平台级实体：学生主体 / 监护人主体）
-- 共 10 张表；生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml
-- 外键统一在 V5__edu_foreign_keys.sql 里添加，避免脚本顺序耦合

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- edu_student（学生主体） ----------
DROP TABLE IF EXISTS `edu_student`;
CREATE TABLE `edu_student` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_no` varchar(32) NOT NULL COMMENT '学号（平台唯一；入学年份 4 位 + 全局序号 6 位）',
  `national_student_no` varchar(64) NULL COMMENT '全国学籍号（G/L 开头；非空时平台唯一）',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `gender` char(1) NULL COMMENT '性别 1 男 / 2 女 / 0 未知',
  `id_type` varchar(20) NULL COMMENT '证件类型',
  `id_card_no` varchar(64) NULL COMMENT '证件号码（非空时平台唯一；掩码展示）',
  `birth_date` date NULL COMMENT '出生日期',
  `enroll_year` smallint NOT NULL COMMENT '入学年份',
  `graduation_date` date NULL COMMENT '毕业日期',
  `photo_url` varchar(255) NULL COMMENT '学籍照片引用（文件服务）',
  `remark` varchar(500) NULL COMMENT '备注',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  UNIQUE KEY `uk_national_student_no` (`national_student_no`),
  UNIQUE KEY `uk_id_card_no` (`id_card_no`),
  KEY `idx_student_name` (`student_name`),
  KEY `idx_enroll_year` (`enroll_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生主体';

-- ---------- edu_student_enrollment（在校记录） ----------
DROP TABLE IF EXISTS `edu_student_enrollment`;
CREATE TABLE `edu_student_enrollment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `enroll_date` date NOT NULL COMMENT '入校日期',
  `enrollment_status` varchar(20) NOT NULL COMMENT '学籍状态（在读 / 休学 / 转入未报到 /  graduated 等）',
  `status_effective_date` date NULL COMMENT '当前状态生效日期',
  `leave_date` date NULL COMMENT '离校日期（毕业 / 转出 / 开除等终态时写入）',
  `campus_id` bigint unsigned NULL COMMENT '校区（入校时归属，参考信息）',
  `entry_grade_id` bigint unsigned NULL COMMENT '入校年级',
  `remark` varchar(500) NULL COMMENT '备注',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enrollment_student_school` (`student_id`, `school_id`),
  KEY `idx_enrollment_school_status` (`school_id`, `enrollment_status`),
  KEY `idx_enrollment_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='在校记录';

-- ---------- edu_guardian（监护人主体） ----------
DROP TABLE IF EXISTS `edu_guardian`;
CREATE TABLE `edu_guardian` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `guardian_name` varchar(50) NOT NULL COMMENT '监护人姓名',
  `guardian_phone` varchar(20) NOT NULL COMMENT '手机号（平台唯一；家长登录名）',
  `id_card_no` varchar(64) NULL COMMENT '证件号码（可选）',
  `user_id` bigint unsigned NULL COMMENT '关联系统账号（家长小程序端登录后写入）',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态 1 正常 / 0 停用',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guardian_phone` (`guardian_phone`),
  KEY `idx_guardian_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监护人主体';

-- ---------- edu_student_guardian（监护人与学生关联） ----------
DROP TABLE IF EXISTS `edu_student_guardian`;
CREATE TABLE `edu_student_guardian` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `guardian_id` bigint unsigned NOT NULL COMMENT '监护人主体 ID',
  `relation` varchar(20) NOT NULL COMMENT '关系（father / mother / other）',
  `is_primary` char(1) NOT NULL DEFAULT '0' COMMENT '是否主要联系人',
  `bind_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '绑定状态（pending 待审核 / approved 已通过 / rejected 已驳回 / unbinding 待解绑）',
  `source` varchar(20) NULL COMMENT '来源（qrcode 扫码绑定 / teacher 教师录入 / import 导入）',
  `audit_by` bigint unsigned NULL COMMENT '审核人（班主任）',
  `audit_time` datetime NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) NULL COMMENT '审核意见（驳回必填）',
  `bind_time` datetime NULL COMMENT '绑定时间',
  `unbind_time` datetime NULL COMMENT '解绑时间',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_guardian` (`student_id`, `guardian_id`),
  KEY `idx_sg_guardian` (`guardian_id`, `bind_status`),
  KEY `idx_sg_student` (`student_id`, `bind_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监护人与学生关联';

-- ---------- edu_student_field_change（学生资料变更申请） ----------
DROP TABLE IF EXISTS `edu_student_field_change`;
CREATE TABLE `edu_student_field_change` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `field_name` varchar(64) NOT NULL COMMENT '申请修改的字段名',
  `old_value` varchar(500) NULL COMMENT '原值（敏感字段需掩码）',
  `new_value` varchar(500) NULL COMMENT '申请值',
  `apply_by_user_id` bigint unsigned NOT NULL COMMENT '申请人',
  `apply_reason` varchar(500) NULL COMMENT '申请原因',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '待审核 / 已通过 / 已驳回 / 已撤销',
  `audit_by` bigint unsigned NULL COMMENT '审核人（班主任 / 教务主任）',
  `audit_time` datetime NULL COMMENT '审核时间',
  `audit_opinion` varchar(500) NULL COMMENT '审核意见',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  `pending_guard` varchar(96) GENERATED ALWAYS AS (CASE WHEN status = 'pending' AND del_flag = '0' THEN CONCAT(student_id, ':', field_name) ELSE NULL END) STORED NULL COMMENT '待审核唯一键：仅 pending 且未删除时取值，用于保证同一学生同一字段同时只有一条待审核',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sfc_pending` (`pending_guard`),
  KEY `idx_sfc_pending` (`student_id`, `field_name`, `status`),
  KEY `idx_sfc_school` (`school_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生资料变更申请';

-- ---------- edu_activation_code（一次性激活凭据） ----------
DROP TABLE IF EXISTS `edu_activation_code`;
CREATE TABLE `edu_activation_code` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `code` varchar(64) NOT NULL COMMENT '一次性激活码（全局唯一）',
  `status` varchar(20) NOT NULL DEFAULT 'unused' COMMENT '未使用 / 已使用 / 已作废',
  `issue_batch_no` varchar(32) NULL COMMENT '打印批次号（班主任一次打印一个批次）',
  `print_time` datetime NULL COMMENT '打印（查看）时间',
  `used_time` datetime NULL COMMENT '使用时间',
  `used_ip` varchar(64) NULL COMMENT '使用时来源 IP',
  `reset_by` bigint unsigned NULL COMMENT '重置人（班主任）',
  `reset_time` datetime NULL COMMENT '重置时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `active_guard` bigint unsigned GENERATED ALWAYS AS (CASE WHEN status = 'unused' THEN student_id ELSE NULL END) STORED NULL COMMENT '有效激活码唯一键：仅 unused 时取值，保证同一学生同一时刻只有一个未使用激活码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activation_code` (`code`),
  UNIQUE KEY `uk_activation_active` (`active_guard`),
  KEY `idx_activation_student` (`student_id`, `status`),
  KEY `idx_activation_batch` (`issue_batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='一次性激活凭据';

-- ---------- edu_teacher（教师主体） ----------
DROP TABLE IF EXISTS `edu_teacher`;
CREATE TABLE `edu_teacher` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `teacher_no` varchar(32) NOT NULL COMMENT '工号（学校租户内唯一）',
  `teacher_name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` char(1) NULL COMMENT '性别',
  `phone` varchar(20) NULL COMMENT '联系电话',
  `email` varchar(100) NULL COMMENT '邮箱',
  `hire_date` date NULL COMMENT '入职日期',
  `employment_status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '在职 / 离职 / 调离',
  `leave_date` date NULL COMMENT '离职或调离生效日期',
  `user_id` bigint unsigned NULL COMMENT '关联系统账号',
  `remark` varchar(500) NULL COMMENT '备注',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_no` (`tenant_id`, `teacher_no`),
  KEY `idx_teacher_school` (`tenant_id`, `school_id`),
  KEY `idx_teacher_name` (`teacher_name`),
  KEY `idx_teacher_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师主体';

-- ---------- edu_user_role（用户教育角色（学校级）） ----------
DROP TABLE IF EXISTS `edu_user_role`;
CREATE TABLE `edu_user_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `user_id` bigint unsigned NOT NULL COMMENT '系统用户',
  `teacher_id` bigint unsigned NULL COMMENT '教师（如该用户是教师）',
  `edu_role` varchar(30) NOT NULL COMMENT '学校级教育角色',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 启用 / 0 停用',
  `start_date` date NULL COMMENT '任职开始日期',
  `end_date` date NULL COMMENT '任职结束日期',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`tenant_id`, `school_id`, `user_id`, `edu_role`),
  KEY `idx_user_role_user` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户教育角色（学校级）';

-- ---------- edu_grade_leader（年级主任任职） ----------
DROP TABLE IF EXISTS `edu_grade_leader`;
CREATE TABLE `edu_grade_leader` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `grade_id` bigint unsigned NOT NULL COMMENT '年级',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `user_id` bigint unsigned NOT NULL COMMENT '年级主任',
  `teacher_id` bigint unsigned NULL COMMENT '教师',
  `is_primary` char(1) NOT NULL DEFAULT '0' COMMENT '是否主要负责人',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 在职 / 0 离任',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grade_leader` (`term_id`, `grade_id`, `user_id`),
  KEY `idx_grade_leader_user_term` (`user_id`, `term_id`, `status`),
  KEY `idx_grade_leader_grade` (`grade_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='年级主任任职';

-- ---------- edu_teaching_assignment（任教关系） ----------
DROP TABLE IF EXISTS `edu_teaching_assignment`;
CREATE TABLE `edu_teaching_assignment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `teacher_id` bigint unsigned NOT NULL COMMENT '教师',
  `subject_id` bigint unsigned NOT NULL COMMENT '学科',
  `class_type` varchar(20) NOT NULL COMMENT 'administrative 行政班 / teaching 教学班',
  `class_id` bigint unsigned NOT NULL COMMENT '行政班或教学班 ID',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 有效 / 0 失效',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment` (`term_id`, `teacher_id`, `subject_id`, `class_type`, `class_id`),
  KEY `idx_assignment_teacher_term` (`teacher_id`, `term_id`, `status`),
  KEY `idx_assignment_class_term` (`term_id`, `class_type`, `class_id`, `status`),
  KEY `idx_assignment_tenant` (`tenant_id`, `school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任教关系';

SET FOREIGN_KEY_CHECKS = 1;
