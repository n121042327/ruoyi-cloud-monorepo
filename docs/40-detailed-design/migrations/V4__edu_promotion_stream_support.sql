-- V4__edu_promotion_stream_support.sql
-- 升班 / 选科 / 导入导出与异步任务 / 审计 / 数据权限 + 归档表 + 两个派生视图
-- 共 20 张表；生成工具 tools/gen_schema_artifacts.py；事实源 database/schema.yaml
-- 外键统一在 V5__edu_foreign_keys.sql 里添加，避免脚本顺序耦合

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- edu_promotion_task（升班任务） ----------
DROP TABLE IF EXISTS `edu_promotion_task`;
CREATE TABLE `edu_promotion_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号（对外标识，唯一）',
  `source_term_id` bigint unsigned NOT NULL COMMENT '源学年学期',
  `target_term_id` bigint unsigned NOT NULL COMMENT '目标学年学期',
  `scope_note` varchar(500) NULL COMMENT '范围说明',
  `task_status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '草稿 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 已归档',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '涉及学生总数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `repeat_count` int NOT NULL DEFAULT 0 COMMENT '留级数',
  `graduate_count` int NOT NULL DEFAULT 0 COMMENT '毕业数',
  `async_task_no` varchar(32) NULL COMMENT '关联异步任务号',
  `start_time` datetime NULL COMMENT '开始执行时间',
  `finish_time` datetime NULL COMMENT '结束时间',
  `cancel_reason` varchar(500) NULL COMMENT '取消原因（取消时必填）',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0 存在 1 删除；历史与学籍类表禁止物理删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_promotion_task_no` (`task_no`),
  KEY `idx_promotion_task_term` (`school_id`, `source_term_id`, `target_term_id`, `task_status`),
  KEY `idx_promotion_task_tenant` (`tenant_id`, `task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='升班任务';

-- ---------- edu_promotion_item（升班明细） ----------
DROP TABLE IF EXISTS `edu_promotion_item`;
CREATE TABLE `edu_promotion_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `task_id` bigint unsigned NOT NULL COMMENT '升班任务',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `source_class_id` bigint unsigned NULL COMMENT '源班级',
  `target_class_id` bigint unsigned NULL COMMENT '目标班级',
  `result_type` varchar(20) NULL COMMENT '升级 / 留级 / 转班 / 毕业 / 跳过',
  `item_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '待处理 / 成功 / 失败 / 已跳过',
  `error_msg` varchar(500) NULL COMMENT '失败原因',
  `adjust_mode` varchar(20) NULL COMMENT '调整方式（手工指定目标班级时写入）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_promotion_item` (`task_id`, `student_id`),
  KEY `idx_promotion_item_status` (`task_id`, `item_status`),
  KEY `idx_promotion_item_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='升班明细';

-- ---------- edu_enrollment_change（学籍异动记录） ----------
DROP TABLE IF EXISTS `edu_enrollment_change`;
CREATE TABLE `edu_enrollment_change` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `school_record_id` bigint unsigned NULL COMMENT '在校记录',
  `change_type` varchar(30) NOT NULL COMMENT '异动类型（休学 / 复学 / 转学 / 退学 / 开除 / 出国 / 失踪 / 死亡 / 转入未报到 / 报到 / 升班）',
  `before_status` varchar(20) NULL COMMENT '变更前状态',
  `after_status` varchar(20) NOT NULL COMMENT '变更后状态',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `reason` varchar(500) NOT NULL COMMENT '原因（必填）',
  `approval_status` varchar(20) NULL COMMENT '需审批的异动：待审批 / 已通过 / 已驳回',
  `approve_by` bigint unsigned NULL COMMENT '审批人（校级管理员）',
  `approve_time` datetime NULL COMMENT '审批时间',
  `approve_opinion` varchar(500) NULL COMMENT '审批意见',
  `operator` bigint unsigned NOT NULL COMMENT '操作人',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_change_student` (`student_id`, `operate_time`),
  KEY `idx_change_school` (`school_id`, `change_type`, `effective_date`),
  KEY `idx_change_approval` (`school_id`, `approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学籍异动记录';

-- ---------- edu_transfer_order（跨校转学单） ----------
DROP TABLE IF EXISTS `edu_transfer_order`;
CREATE TABLE `edu_transfer_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `transfer_no` varchar(32) NOT NULL COMMENT '转学单号（唯一）',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `from_tenant_id` varchar(20) NOT NULL COMMENT '转出学校租户',
  `from_school_id` bigint unsigned NOT NULL COMMENT '转出学校',
  `from_grade_id` bigint unsigned NULL COMMENT '原年级',
  `to_tenant_id` varchar(20) NOT NULL COMMENT '转入学校租户',
  `to_school_id` bigint unsigned NOT NULL COMMENT '转入学校',
  `to_grade_id` bigint unsigned NULL COMMENT '目标年级',
  `to_class_id` bigint unsigned NULL COMMENT '目标班级',
  `transfer_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '待接收 / 已接收 / 已报到 / 已撤销 / 已驳回',
  `apply_by` bigint unsigned NOT NULL COMMENT '申请人（转出校）',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `accept_by` bigint unsigned NULL COMMENT '接收人（转入校，接收即审批）',
  `accept_time` datetime NULL COMMENT '接收时间',
  `check_in_time` datetime NULL COMMENT '报到时间',
  `cancel_by` bigint unsigned NULL COMMENT '撤销人',
  `cancel_time` datetime NULL COMMENT '撤销时间',
  `remark` varchar(500) NULL COMMENT '备注',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_transfer_student_status` (`student_id`, `transfer_status`),
  KEY `idx_transfer_to_school` (`to_school_id`, `transfer_status`),
  KEY `idx_transfer_from_school` (`from_school_id`, `transfer_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='跨校转学单';

-- ---------- edu_stream_config（选科配置） ----------
DROP TABLE IF EXISTS `edu_stream_config`;
CREATE TABLE `edu_stream_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `stream_open_from` datetime NOT NULL COMMENT '开放期起点',
  `stream_deadline` datetime NOT NULL COMMENT '截止时间',
  `overdue_requires_approval` tinyint(1) NOT NULL DEFAULT 1 COMMENT '逾期变更是否需校级管理员审批（BR-STREAM-005）',
  `config_status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '生效 / 已失效',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stream_config` (`school_id`, `term_id`),
  KEY `idx_stream_config_tenant` (`tenant_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='选科配置';

-- ---------- edu_student_stream（学生选科） ----------
DROP TABLE IF EXISTS `edu_student_stream`;
CREATE TABLE `edu_student_stream` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `primary_subject_code` varchar(20) NOT NULL COMMENT '首选科目（物理 / 历史）',
  `secondary_subject_codes` varchar(64) NOT NULL COMMENT '再选科目（两个编码，逗号分隔，按学科排序号排序）',
  `effective_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `stream_status` varchar(20) NOT NULL DEFAULT 'effective' COMMENT '生效 / 待审批（有待审批变更时前端展示"审批中"，本字段仍为生效）',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_stream` (`term_id`, `student_id`),
  KEY `idx_stream_stat` (`term_id`, `primary_subject_code`, `secondary_subject_codes`),
  KEY `idx_stream_school` (`school_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生选科';

-- ---------- edu_stream_change_request（选科变更申请） ----------
DROP TABLE IF EXISTS `edu_stream_change_request`;
CREATE TABLE `edu_stream_change_request` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `request_no` varchar(32) NOT NULL COMMENT '申请单号（唯一）',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `before_combination` varchar(64) NOT NULL COMMENT '原组合（展示用文本）',
  `after_combination` varchar(64) NOT NULL COMMENT '新组合（展示用文本）',
  `primary_subject_code` varchar(20) NOT NULL COMMENT '新的首选科目',
  `secondary_subject_codes` varchar(64) NOT NULL COMMENT '新的再选科目',
  `request_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '草稿 / 待审批 / 已通过 / 已驳回 / 已撤销',
  `reason` varchar(500) NOT NULL COMMENT '申请原因（必填）',
  `apply_by` bigint unsigned NOT NULL COMMENT '发起人（学生本人或班主任代发起）',
  `apply_by_role` varchar(30) NULL COMMENT '发起人角色（student / homeroom）',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `approve_by` bigint unsigned NULL COMMENT '审批人（校级管理员）',
  `approve_time` datetime NULL COMMENT '审批时间',
  `approve_opinion` varchar(500) NULL COMMENT '审批意见（驳回必填）',
  `cancel_time` datetime NULL COMMENT '撤回时间',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stream_request_no` (`request_no`),
  KEY `idx_stream_request_pending` (`school_id`, `request_status`, `apply_time`),
  KEY `idx_stream_request_student` (`term_id`, `student_id`, `request_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='选科变更申请';

-- ---------- edu_stream_history（选科历史） ----------
DROP TABLE IF EXISTS `edu_stream_history`;
CREATE TABLE `edu_stream_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `student_id` bigint unsigned NOT NULL COMMENT '学生主体 ID',
  `term_id` bigint unsigned NOT NULL COMMENT '学年学期',
  `before_combination` varchar(64) NULL COMMENT '原组合',
  `after_combination` varchar(64) NOT NULL COMMENT '新组合',
  `change_type` varchar(20) NOT NULL COMMENT '首次提交 / 开放期内自助变更 / 变更申请通过',
  `request_no` varchar(32) NULL COMMENT '关联变更申请单号',
  `reason` varchar(500) NULL COMMENT '变更原因',
  `operator` bigint unsigned NOT NULL COMMENT '操作人',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_stream_history_student` (`student_id`, `operate_time`),
  KEY `idx_stream_history_term` (`term_id`, `school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='选科历史';

-- ---------- edu_import_template（导入模板） ----------
DROP TABLE IF EXISTS `edu_import_template`;
CREATE TABLE `edu_import_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `module_code` varchar(30) NOT NULL COMMENT '模块（student / teacher / class_roster）',
  `template_version` varchar(20) NOT NULL COMMENT '模板版本（如 student-v3）',
  `column_count` int NOT NULL COMMENT '列数',
  `file_id` bigint unsigned NULL COMMENT '模板文件引用',
  `expire_time` datetime NULL COMMENT '版本过期时间（过期后强提示）',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '1 当前版本 / 0 历史版本',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_import_template` (`module_code`, `template_version`),
  KEY `idx_import_template_current` (`module_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入模板';

-- ---------- edu_import_batch（导入批次） ----------
DROP TABLE IF EXISTS `edu_import_batch`;
CREATE TABLE `edu_import_batch` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号（全局唯一，幂等键）',
  `module_code` varchar(30) NOT NULL COMMENT '模块',
  `template_version` varchar(20) NULL COMMENT '使用的模板版本',
  `async_task_no` varchar(32) NULL COMMENT '关联异步任务号',
  `source_file_id` bigint unsigned NULL COMMENT '上传文件引用',
  `result_file_id` bigint unsigned NULL COMMENT '结果文件引用（含学号对照表）',
  `failed_file_id` bigint unsigned NULL COMMENT '失败明细文件引用',
  `row_total` int NOT NULL DEFAULT 0 COMMENT '总行数',
  `valid_count` int NOT NULL DEFAULT 0 COMMENT '校验通过行数',
  `invalid_count` int NOT NULL DEFAULT 0 COMMENT '校验失败行数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '执行成功行数',
  `skipped_count` int NOT NULL DEFAULT 0 COMMENT '跳过行数（幂等跳过）',
  `import_status` varchar(20) NOT NULL DEFAULT 'validated' COMMENT '已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消',
  `operator_id` bigint unsigned NOT NULL COMMENT '操作人',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_import_batch_no` (`batch_no`),
  KEY `idx_import_batch_school` (`school_id`, `import_status`, `create_time`),
  KEY `idx_import_batch_operator` (`operator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入批次';

-- ---------- edu_import_error（导入行结果（失败与跳过明细）） ----------
DROP TABLE IF EXISTS `edu_import_error`;
CREATE TABLE `edu_import_error` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `row_no` int NOT NULL COMMENT '行号（Excel 行号，含表头偏移）',
  `result` varchar(20) NOT NULL COMMENT 'invalid 校验失败 / failed 执行失败 / skipped 跳过',
  `fail_reason` varchar(500) NOT NULL COMMENT '失败或跳过原因',
  `object_name` varchar(100) NULL COMMENT '对象标识（姓名 / 学号等）',
  `raw_data` json NULL COMMENT '原始行数据（用于对照修正）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_import_error_row` (`batch_no`, `row_no`),
  KEY `idx_import_error_result` (`batch_no`, `result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导入行结果（失败与跳过明细）';

-- ---------- edu_async_task（异步任务） ----------
DROP TABLE IF EXISTS `edu_async_task`;
CREATE TABLE `edu_async_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号（全局唯一，幂等键）',
  `task_type` varchar(30) NOT NULL COMMENT 'import / export / promotion / teaching_class / archive',
  `task_status` varchar(20) NOT NULL DEFAULT 'queued' COMMENT '排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 死信',
  `progress_percent` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '进度百分比',
  `owner_id` bigint unsigned NOT NULL COMMENT '发起人',
  `owner_role` varchar(30) NULL COMMENT '发起人角色快照',
  `params_summary` json NULL COMMENT '参数摘要（模块 / 模板 / 筛选条件等）',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `skipped_count` int NOT NULL DEFAULT 0 COMMENT '跳过数',
  `result_file_id` bigint unsigned NULL COMMENT '结果文件引用',
  `failed_file_id` bigint unsigned NULL COMMENT '失败明细文件引用',
  `queue_position` int NULL COMMENT '排队位置（queue 状态时展示，REQ-IMP-049）',
  `retry_count` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `batch_no` varchar(32) NULL COMMENT '关联批次号（导入 / 升班类任务）',
  `start_time` datetime NULL COMMENT '开始执行时间',
  `finish_time` datetime NULL COMMENT '结束时间',
  `error_msg` varchar(500) NULL COMMENT '失败原因',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_async_task_no` (`task_no`),
  KEY `idx_async_task_owner` (`owner_id`, `create_time`),
  KEY `idx_async_task_school_status` (`school_id`, `task_status`, `create_time`),
  KEY `idx_async_task_type` (`task_type`, `task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='异步任务';

-- ---------- edu_async_task_retry（任务重试记录） ----------
DROP TABLE IF EXISTS `edu_async_task_retry`;
CREATE TABLE `edu_async_task_retry` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `retry_no` tinyint unsigned NOT NULL COMMENT '第几次重试（从 1 开始）',
  `result` varchar(20) NOT NULL COMMENT 'success / failed / timeout',
  `error_msg` varchar(500) NULL COMMENT '本次失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '重试时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_retry` (`task_no`, `retry_no`),
  KEY `idx_task_retry_task` (`task_no`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务重试记录';

-- ---------- edu_dead_letter_task（死信任务） ----------
DROP TABLE IF EXISTS `edu_dead_letter_task`;
CREATE TABLE `edu_dead_letter_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号（唯一）',
  `task_type` varchar(30) NOT NULL COMMENT '任务类型',
  `dead_time` datetime NOT NULL COMMENT '进入死信时间',
  `retry_count` tinyint unsigned NOT NULL COMMENT '已重试次数',
  `last_error` varchar(500) NOT NULL COMMENT '最后一次错误',
  `batch_no` varchar(32) NULL COMMENT '原批次号',
  `replay_status` varchar(20) NOT NULL DEFAULT 'replayable' COMMENT 'replayable 待重放 / replayed 已重放',
  `replay_by` bigint unsigned NULL COMMENT '重放人',
  `replay_time` datetime NULL COMMENT '重放时间',
  `replay_reason` varchar(500) NULL COMMENT '重放原因（必填，写审计）',
  `replay_task_status` varchar(20) NULL COMMENT '重放后的执行结果，用于追溯',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dead_letter_task` (`task_no`),
  KEY `idx_dead_letter_status` (`replay_status`, `dead_time`),
  KEY `idx_dead_letter_type` (`task_type`, `dead_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='死信任务';

-- ---------- edu_file_ref（文件引用） ----------
DROP TABLE IF EXISTS `edu_file_ref`;
CREATE TABLE `edu_file_ref` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `file_id` bigint unsigned NOT NULL COMMENT '文件 ID（唯一）',
  `file_kind` varchar(30) NOT NULL COMMENT 'import_source / export_result / failed_rows / template / photo',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `storage_key` varchar(500) NOT NULL COMMENT '对象存储键（含租户与学校前缀）',
  `content_type` varchar(100) NULL COMMENT 'MIME 类型',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节数',
  `biz_type` varchar(30) NULL COMMENT '业务类型（import / export / task）',
  `biz_id` varchar(64) NULL COMMENT '业务标识（批次号 / 任务号）',
  `expire_time` datetime NULL COMMENT '有效期（结果文件默认 7 天）',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_file_biz` (`biz_type`, `biz_id`),
  KEY `idx_file_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件引用';

-- ---------- edu_audit_log（操作日志） ----------
DROP TABLE IF EXISTS `edu_audit_log`;
CREATE TABLE `edu_audit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `action_type` varchar(30) NOT NULL COMMENT 'create / update / delete / import / export / approve / grant / login / status_change',
  `module_code` varchar(30) NOT NULL COMMENT '所属模块',
  `object_type` varchar(50) NOT NULL COMMENT '对象类型（student / class / teacher / stream 等）',
  `object_id` varchar(64) NOT NULL COMMENT '对象标识',
  `object_name` varchar(200) NULL COMMENT '对象名称（便于阅读，如 高一 (1) 班）',
  `operator_id` bigint unsigned NULL COMMENT '操作人；平台运营访问时为运营账号',
  `operator_role` varchar(30) NULL COMMENT '操作人角色快照',
  `client_ip` varchar(64) NULL COMMENT '来源 IP',
  `request_id` varchar(64) NULL COMMENT '请求标识（用于幂等写入与排障）',
  `batch_no` varchar(32) NULL COMMENT '批次号（批量操作共用）',
  `action_result` varchar(16) NOT NULL DEFAULT 'success' COMMENT 'success / failed',
  `source` varchar(16) NOT NULL DEFAULT 'web' COMMENT 'web / api / job / mq',
  `detail` varchar(500) NULL COMMENT '说明（驳回意见、失败原因等）',
  `log_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间（分区键）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_audit_idempotent` (`request_id`, `object_id`, `action_type`),
  KEY `idx_audit_time` (`log_time`),
  KEY `idx_audit_operator` (`operator_id`, `log_time`),
  KEY `idx_audit_object` (`object_type`, `object_id`, `log_time`),
  KEY `idx_audit_school_type` (`school_id`, `action_type`, `log_time`),
  KEY `idx_audit_tenant` (`tenant_id`, `log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志';

-- ---------- edu_audit_change（日志变更明细） ----------
DROP TABLE IF EXISTS `edu_audit_change`;
CREATE TABLE `edu_audit_change` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `school_id` bigint unsigned NOT NULL COMMENT '学校归属',
  `log_id` bigint unsigned NOT NULL COMMENT '操作日志 ID',
  `field_name` varchar(64) NOT NULL COMMENT '字段名',
  `before_value` varchar(1000) NULL COMMENT '变更前值（敏感字段掩码）',
  `after_value` varchar(1000) NULL COMMENT '变更后值（敏感字段掩码）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_audit_change` (`log_id`, `field_name`),
  KEY `idx_audit_change_field` (`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日志变更明细';

-- ---------- edu_audit_archive_batch（日志归档批次） ----------
DROP TABLE IF EXISTS `edu_audit_archive_batch`;
CREATE TABLE `edu_audit_archive_batch` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `archive_no` varchar(32) NOT NULL COMMENT '归档批次号（唯一）',
  `range_start` date NOT NULL COMMENT '归档范围开始',
  `range_end` date NOT NULL COMMENT '归档范围结束',
  `row_count` bigint NOT NULL DEFAULT 0 COMMENT '归档行数',
  `file_id` bigint unsigned NULL COMMENT '归档文件引用',
  `archive_status` varchar(16) NOT NULL DEFAULT 'running' COMMENT 'running 进行中 / done 已完成 / failed 失败',
  `operator_id` bigint unsigned NULL COMMENT '操作人',
  `archive_time` datetime NULL COMMENT '完成时间',
  `error_msg` varchar(500) NULL COMMENT '失败原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_no` (`archive_no`),
  KEY `idx_archive_range` (`range_start`, `range_end`),
  KEY `idx_archive_status` (`archive_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日志归档批次';

-- ---------- edu_data_grant（数据共享授权（仅教学资源）） ----------
DROP TABLE IF EXISTS `edu_data_grant`;
CREATE TABLE `edu_data_grant` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `grant_no` varchar(32) NOT NULL COMMENT '业务编号（幂等键，唯一）',
  `grantor_user_id` bigint unsigned NOT NULL COMMENT '授权人（运营方用户）',
  `grantee_type` varchar(20) NOT NULL COMMENT 'tenant / user / role',
  `grantee_id` varchar(64) NOT NULL COMMENT '被授权对象 ID',
  `title` varchar(200) NOT NULL COMMENT '事由标题',
  `reason` varchar(500) NOT NULL COMMENT '共享原因',
  `resource_types` varchar(200) NOT NULL COMMENT '资源类型集合（逗号分隔，如 question_bank_item',
  `resource_scope` json NULL COMMENT '资源范围细化（哪些题库 / 试卷；为空表示该类型全部）',
  `effective_start` datetime NOT NULL COMMENT '生效时间',
  `effective_end` datetime NULL COMMENT '失效时间（为空表示长期有效）',
  `grant_status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '草稿 / 生效 / 已撤销 / 已过期',
  `revoke_by` bigint unsigned NULL COMMENT '撤销人',
  `revoke_time` datetime NULL COMMENT '撤销时间',
  `revoke_reason` varchar(500) NULL COMMENT '撤销原因',
  `create_by` bigint unsigned NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grant_no` (`grant_no`),
  KEY `idx_grant_grantee` (`grantee_type`, `grantee_id`, `grant_status`),
  KEY `idx_grant_effective` (`grant_status`, `effective_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据共享授权（仅教学资源）';

-- ---------- edu_data_grant_scope（授权范围明细） ----------
DROP TABLE IF EXISTS `edu_data_grant_scope`;
CREATE TABLE `edu_data_grant_scope` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` varchar(20) NOT NULL COMMENT '租户隔离键（NFR-SEC-01）',
  `grant_id` bigint unsigned NOT NULL COMMENT '所属授权',
  `scope_type` varchar(20) NOT NULL COMMENT 'school_tenant / grade / class',
  `scope_id` varchar(64) NOT NULL COMMENT '范围对象 ID',
  `resource_code` varchar(64) NOT NULL COMMENT '资源编码（取自权限矩阵，如 question_bank_item）',
  `access_level` varchar(20) NOT NULL COMMENT '首轮仅 read / export，不开放 write（BR-DATA-015）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grant_scope` (`grant_id`, `scope_type`, `scope_id`, `resource_code`, `access_level`),
  KEY `idx_grant_scope_resource` (`resource_code`, `access_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='授权范围明细';

-- ---------- edu_audit_log_archive（归档落点：与在线表同构（含全部索引）。归档 = 按时间范围把行搬进本表并写 ed） ----------
DROP TABLE IF EXISTS `edu_audit_log_archive`;
CREATE TABLE edu_audit_log_archive LIKE edu_audit_log;

-- ---------- v_edu_audit_operator_access（运营访问记录（派生视图）） ----------
DROP VIEW IF EXISTS `v_edu_audit_operator_access`;
CREATE VIEW `v_edu_audit_operator_access` AS SELECT `id`, `log_time`, `operator_id`, `object_type`, `object_id`, `action_type`, detail AS purpose, `client_ip`, `tenant_id`, `school_id` FROM `edu_audit_log` WHERE operator_role = 'platform_ops';

-- ---------- v_edu_audit_security_event（登录与安全事件（派生视图）） ----------
DROP VIEW IF EXISTS `v_edu_audit_security_event`;
CREATE VIEW `v_edu_audit_security_event` AS SELECT `id`, `log_time`, `action_type`, `operator_id`, `operator_role`, `object_id`, `action_result`, `client_ip`, `detail` FROM `edu_audit_log` WHERE action_type IN ('login','account_locked','activation_view','activation_reset','student_no_change','permission_change');

SET FOREIGN_KEY_CHECKS = 1;
