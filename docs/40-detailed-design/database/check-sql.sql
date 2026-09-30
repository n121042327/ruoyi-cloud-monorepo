-- check-sql.sql —— 结构与数据一致性检查
-- 生成工具 tools/gen_schema_artifacts.py；每条检查在「库为空」时返回 0 行，返回非 0 行表示不一致。
-- 用法：mysql -h<host> -u<user> -p<password> <db> < check-sql.sql

SET NAMES utf8mb4;

-- 1. 表是否齐全
SELECT '缺表' AS check_name, expect.table_name AS detail FROM (
  SELECT 'edu_student' AS table_name UNION ALL SELECT 'edu_student_enrollment' AS table_name UNION ALL SELECT 'edu_guardian' AS table_name UNION ALL SELECT 'edu_student_guardian' AS table_name UNION ALL SELECT 'edu_student_field_change' AS table_name UNION ALL SELECT 'edu_activation_code' AS table_name UNION ALL SELECT 'edu_teacher' AS table_name UNION ALL SELECT 'edu_user_role' AS table_name UNION ALL SELECT 'edu_grade_leader' AS table_name UNION ALL SELECT 'edu_teaching_assignment' AS table_name UNION ALL SELECT 'edu_grade' AS table_name UNION ALL SELECT 'edu_class' AS table_name UNION ALL SELECT 'edu_class_member' AS table_name UNION ALL SELECT 'edu_teaching_class' AS table_name UNION ALL SELECT 'edu_teaching_class_member' AS table_name UNION ALL SELECT 'edu_promotion_task' AS table_name UNION ALL SELECT 'edu_promotion_item' AS table_name UNION ALL SELECT 'edu_enrollment_change' AS table_name UNION ALL SELECT 'edu_transfer_order' AS table_name UNION ALL SELECT 'edu_stream_config' AS table_name UNION ALL SELECT 'edu_student_stream' AS table_name UNION ALL SELECT 'edu_stream_change_request' AS table_name UNION ALL SELECT 'edu_stream_history' AS table_name UNION ALL SELECT 'edu_subject' AS table_name UNION ALL SELECT 'edu_subject_stage' AS table_name UNION ALL SELECT 'edu_school' AS table_name UNION ALL SELECT 'edu_campus' AS table_name UNION ALL SELECT 'edu_school_stage' AS table_name UNION ALL SELECT 'edu_academic_year' AS table_name UNION ALL SELECT 'edu_term' AS table_name UNION ALL SELECT 'edu_import_template' AS table_name UNION ALL SELECT 'edu_import_batch' AS table_name UNION ALL SELECT 'edu_import_error' AS table_name UNION ALL SELECT 'edu_async_task' AS table_name UNION ALL SELECT 'edu_async_task_retry' AS table_name UNION ALL SELECT 'edu_dead_letter_task' AS table_name UNION ALL SELECT 'edu_file_ref' AS table_name UNION ALL SELECT 'edu_audit_log' AS table_name UNION ALL SELECT 'edu_audit_change' AS table_name UNION ALL SELECT 'edu_audit_archive_batch' AS table_name UNION ALL SELECT 'edu_data_grant' AS table_name UNION ALL SELECT 'edu_data_grant_scope' AS table_name
) AS expect
LEFT JOIN information_schema.tables AS actual
  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name
WHERE actual.table_name IS NULL;

-- 2. 唯一键是否齐全
SELECT '缺唯一键' AS check_name, expect.table_name, expect.index_name FROM (
SELECT 'edu_student' AS table_name, 'uk_student_no' AS index_name UNION ALL SELECT 'edu_student' AS table_name, 'uk_national_student_no' AS index_name UNION ALL SELECT 'edu_student' AS table_name, 'uk_id_card_no' AS index_name UNION ALL SELECT 'edu_student_enrollment' AS table_name, 'uk_enrollment_student_school' AS index_name UNION ALL SELECT 'edu_guardian' AS table_name, 'uk_guardian_phone' AS index_name UNION ALL SELECT 'edu_student_guardian' AS table_name, 'uk_student_guardian' AS index_name UNION ALL SELECT 'edu_student_field_change' AS table_name, 'uk_sfc_pending' AS index_name UNION ALL SELECT 'edu_activation_code' AS table_name, 'uk_activation_code' AS index_name UNION ALL SELECT 'edu_activation_code' AS table_name, 'uk_activation_active' AS index_name UNION ALL SELECT 'edu_teacher' AS table_name, 'uk_teacher_no' AS index_name UNION ALL SELECT 'edu_user_role' AS table_name, 'uk_user_role' AS index_name UNION ALL SELECT 'edu_grade_leader' AS table_name, 'uk_grade_leader' AS index_name UNION ALL SELECT 'edu_teaching_assignment' AS table_name, 'uk_assignment' AS index_name UNION ALL SELECT 'edu_grade' AS table_name, 'uk_grade_seq' AS index_name UNION ALL SELECT 'edu_grade' AS table_name, 'uk_grade_name' AS index_name UNION ALL SELECT 'edu_class' AS table_name, 'uk_class_name' AS index_name UNION ALL SELECT 'edu_class' AS table_name, 'uk_class_teaching' AS index_name UNION ALL SELECT 'edu_class_member' AS table_name, 'uk_class_member_admin' AS index_name UNION ALL SELECT 'edu_teaching_class' AS table_name, 'uk_teaching_class' AS index_name UNION ALL SELECT 'edu_teaching_class_member' AS table_name, 'uk_tclass_member' AS index_name UNION ALL SELECT 'edu_promotion_task' AS table_name, 'uk_promotion_task_no' AS index_name UNION ALL SELECT 'edu_promotion_item' AS table_name, 'uk_promotion_item' AS index_name UNION ALL SELECT 'edu_transfer_order' AS table_name, 'uk_transfer_no' AS index_name UNION ALL SELECT 'edu_stream_config' AS table_name, 'uk_stream_config' AS index_name UNION ALL SELECT 'edu_student_stream' AS table_name, 'uk_student_stream' AS index_name UNION ALL SELECT 'edu_stream_change_request' AS table_name, 'uk_stream_request_no' AS index_name UNION ALL SELECT 'edu_subject' AS table_name, 'uk_subject_code' AS index_name UNION ALL SELECT 'edu_subject' AS table_name, 'uk_subject_name' AS index_name UNION ALL SELECT 'edu_subject_stage' AS table_name, 'uk_subject_stage' AS index_name UNION ALL SELECT 'edu_school' AS table_name, 'uk_school_tenant' AS index_name UNION ALL SELECT 'edu_school' AS table_name, 'uk_school_code' AS index_name UNION ALL SELECT 'edu_campus' AS table_name, 'uk_campus_name' AS index_name UNION ALL SELECT 'edu_campus' AS table_name, 'uk_campus_code' AS index_name UNION ALL SELECT 'edu_school_stage' AS table_name, 'uk_school_stage' AS index_name UNION ALL SELECT 'edu_academic_year' AS table_name, 'uk_academic_year_code' AS index_name UNION ALL SELECT 'edu_term' AS table_name, 'uk_term_code' AS index_name UNION ALL SELECT 'edu_import_template' AS table_name, 'uk_import_template' AS index_name UNION ALL SELECT 'edu_import_batch' AS table_name, 'uk_import_batch_no' AS index_name UNION ALL SELECT 'edu_import_error' AS table_name, 'uk_import_error_row' AS index_name UNION ALL SELECT 'edu_async_task' AS table_name, 'uk_async_task_no' AS index_name UNION ALL SELECT 'edu_async_task_retry' AS table_name, 'uk_task_retry' AS index_name UNION ALL SELECT 'edu_dead_letter_task' AS table_name, 'uk_dead_letter_task' AS index_name UNION ALL SELECT 'edu_file_ref' AS table_name, 'uk_file_id' AS index_name UNION ALL SELECT 'edu_audit_log' AS table_name, 'uk_audit_idempotent' AS index_name UNION ALL SELECT 'edu_audit_change' AS table_name, 'uk_audit_change' AS index_name UNION ALL SELECT 'edu_audit_archive_batch' AS table_name, 'uk_archive_no' AS index_name UNION ALL SELECT 'edu_data_grant' AS table_name, 'uk_grant_no' AS index_name UNION ALL SELECT 'edu_data_grant_scope' AS table_name, 'uk_grant_scope' AS index_name
) AS expect
LEFT JOIN information_schema.statistics AS actual
  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name
 AND actual.index_name = expect.index_name
WHERE actual.index_name IS NULL;

-- 3. 外键是否齐全
SELECT '缺外键' AS check_name, expect.table_name, expect.constraint_name FROM (
SELECT 'edu_student_enrollment' AS table_name, 'fk_enrollment_student' AS constraint_name UNION ALL SELECT 'edu_student_guardian' AS table_name, 'fk_sg_student' AS constraint_name UNION ALL SELECT 'edu_student_guardian' AS table_name, 'fk_sg_guardian' AS constraint_name UNION ALL SELECT 'edu_student_field_change' AS table_name, 'fk_sfc_student' AS constraint_name UNION ALL SELECT 'edu_activation_code' AS table_name, 'fk_activation_student' AS constraint_name UNION ALL SELECT 'edu_grade_leader' AS table_name, 'fk_grade_leader_grade' AS constraint_name UNION ALL SELECT 'edu_teaching_assignment' AS table_name, 'fk_assignment_teacher' AS constraint_name UNION ALL SELECT 'edu_teaching_assignment' AS table_name, 'fk_assignment_subject' AS constraint_name UNION ALL SELECT 'edu_class' AS table_name, 'fk_class_grade' AS constraint_name UNION ALL SELECT 'edu_class' AS table_name, 'fk_class_term' AS constraint_name UNION ALL SELECT 'edu_class' AS table_name, 'fk_class_head_teacher' AS constraint_name UNION ALL SELECT 'edu_class_member' AS table_name, 'fk_member_class' AS constraint_name UNION ALL SELECT 'edu_class_member' AS table_name, 'fk_member_student' AS constraint_name UNION ALL SELECT 'edu_teaching_class' AS table_name, 'fk_tclass_term' AS constraint_name UNION ALL SELECT 'edu_teaching_class_member' AS table_name, 'fk_tcm_class' AS constraint_name UNION ALL SELECT 'edu_teaching_class_member' AS table_name, 'fk_tcm_student' AS constraint_name UNION ALL SELECT 'edu_promotion_task' AS table_name, 'fk_promotion_source_term' AS constraint_name UNION ALL SELECT 'edu_promotion_task' AS table_name, 'fk_promotion_target_term' AS constraint_name UNION ALL SELECT 'edu_promotion_item' AS table_name, 'fk_promotion_item_task' AS constraint_name UNION ALL SELECT 'edu_promotion_item' AS table_name, 'fk_promotion_item_student' AS constraint_name UNION ALL SELECT 'edu_enrollment_change' AS table_name, 'fk_change_student' AS constraint_name UNION ALL SELECT 'edu_transfer_order' AS table_name, 'fk_transfer_student' AS constraint_name UNION ALL SELECT 'edu_stream_config' AS table_name, 'fk_stream_config_term' AS constraint_name UNION ALL SELECT 'edu_student_stream' AS table_name, 'fk_student_stream_student' AS constraint_name UNION ALL SELECT 'edu_stream_change_request' AS table_name, 'fk_stream_request_student' AS constraint_name UNION ALL SELECT 'edu_stream_history' AS table_name, 'fk_stream_history_student' AS constraint_name UNION ALL SELECT 'edu_subject_stage' AS table_name, 'fk_subject_stage_subject' AS constraint_name UNION ALL SELECT 'edu_term' AS table_name, 'fk_term_year' AS constraint_name UNION ALL SELECT 'edu_import_batch' AS table_name, 'fk_import_batch_task' AS constraint_name UNION ALL SELECT 'edu_async_task_retry' AS table_name, 'fk_task_retry_task' AS constraint_name UNION ALL SELECT 'edu_audit_change' AS table_name, 'fk_audit_change_log' AS constraint_name UNION ALL SELECT 'edu_data_grant_scope' AS table_name, 'fk_grant_scope_grant' AS constraint_name
) AS expect
LEFT JOIN information_schema.table_constraints AS actual
  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name
 AND actual.constraint_name = expect.constraint_name
WHERE actual.constraint_name IS NULL;

-- 4. 平台级实体不得出现 tenant_id（DS-DENY-09）
SELECT '平台级实体带了 tenant_id' AS check_name, table_name, column_name
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name IN ('edu_student', 'edu_guardian')
  AND column_name = 'tenant_id';

-- 5. 学生必须有一条在校记录（否则学校侧看不到该学生）
SELECT '学生无在校记录' AS check_name, s.student_no, s.student_name
FROM edu_student s
LEFT JOIN edu_student_enrollment e ON e.student_id = s.id AND e.del_flag = '0'
WHERE s.del_flag = '0' AND e.id IS NULL;

-- 6. 一名学生同一学期只能有一个行政班关系（uk 已保证；此处防历史脏数据）
SELECT '行政班关系重复' AS check_name, m.term_id, m.student_id, COUNT(*) AS cnt
FROM edu_class_member m
WHERE m.class_type = 'administrative' AND m.status = '1'
GROUP BY m.term_id, m.student_id HAVING cnt > 1;

-- 7. 班主任必须是本校在职教师
SELECT '班主任不是本校在职教师' AS check_name, c.id, c.class_name, c.head_teacher_id
FROM edu_class c
LEFT JOIN edu_teacher t ON t.id = c.head_teacher_id
WHERE c.head_teacher_id IS NOT NULL AND c.del_flag = '0'
  AND (t.id IS NULL OR t.employment_status <> 'active');

-- 8. 教学班不得设班主任（REQ-CLS-039）
SELECT '教学班设了班主任' AS check_name, id, class_name, head_teacher_id
FROM edu_class WHERE class_type = 'teaching' AND head_teacher_id IS NOT NULL;

-- 9. 选科的学科必须存在且参与 3+1+2
SELECT '选科引用未启用学科' AS check_name, ss.id, ss.primary_subject_code
FROM edu_student_stream ss
LEFT JOIN edu_subject sub ON sub.subject_code = ss.primary_subject_code AND sub.del_flag = '0'
WHERE sub.id IS NULL OR sub.stream_enabled <> '1';

-- 10. 再选科目必须恰好 2 门（BR-STREAM-002）
SELECT '再选科目数量不是 2' AS check_name, id, secondary_subject_codes
FROM edu_student_stream
WHERE (LENGTH(secondary_subject_codes) - LENGTH(REPLACE(secondary_subject_codes, ',', '')) + 1) <> 2;

-- 11. 变更申请通过后必须已生效（不允许出现「已通过但没有选科记录」）
SELECT '审批通过的申请没有生效记录' AS check_name, r.request_no, r.student_id
FROM edu_stream_change_request r
LEFT JOIN edu_student_stream s ON s.student_id = r.student_id AND s.term_id = r.term_id
WHERE r.request_status = 'approved' AND s.id IS NULL;

-- 12. 异步任务状态与时间字段自洽（running 必须有 start_time，终态必须有 finish_time）
SELECT '任务时间字段不自洽' AS check_name, task_no, task_status
FROM edu_async_task
WHERE (task_status = 'running' AND start_time IS NULL)
   OR (task_status IN ('succeeded','partial_failed','failed','cancelled') AND finish_time IS NULL);

-- 13. 死信记录必须来自失败任务（超过重试上限）
SELECT '死信记录与任务状态不一致' AS check_name, d.task_no, t.task_status, d.retry_count
FROM edu_dead_letter_task d JOIN edu_async_task t ON t.task_no = d.task_no
WHERE t.task_status NOT IN ('failed','dead') AND d.replay_status = 'replayable';

-- 14. 共享授权不得包含业务数据资源（BR-DATA-018）
SELECT '共享授权出现业务数据资源' AS check_name, grant_no, resource_types
FROM edu_data_grant
WHERE resource_types REGEXP 'person\.|org\.(class|grade|school)|enrollment\.|stream\.';

-- 15. 共享授权不得开放写权限（BR-DATA-015）
SELECT '共享授权出现写权限' AS check_name, g.grant_no, s.access_level
FROM edu_data_grant g JOIN edu_data_grant_scope s ON s.grant_id = g.id
WHERE s.access_level NOT IN ('read','export');

-- 16. 审计日志不得出现明文证件号（BR-AUDIT-007 / REQ-AUD-010）
SELECT '审计日志疑似明文证件号' AS check_name, c.id, c.log_id, c.field_name
FROM edu_audit_change c
WHERE c.before_value REGEXP '^[0-9]{17}[0-9Xx]$' OR c.after_value REGEXP '^[0-9]{17}[0-9Xx]$'
LIMIT 50;

-- 17. 在职教师工号在租户内唯一（uk 已保证；此处防数据迁移遗留）
SELECT '工号重复' AS check_name, tenant_id, teacher_no, COUNT(*) AS cnt
FROM edu_teacher WHERE del_flag = '0' GROUP BY tenant_id, teacher_no HAVING cnt > 1;

-- 18. 学年日期必须连续不重叠（RV-TERM-08）
SELECT '学年日期重叠' AS check_name, a.academic_year_code, b.academic_year_code
FROM edu_academic_year a JOIN edu_academic_year b
  ON a.school_id = b.school_id AND a.id < b.id
WHERE a.start_date <= b.end_date AND b.start_date <= a.end_date;

-- 19. 同一学校同一学年只能有一个当前学期（BR-TERM-002）
SELECT '当前学期不唯一' AS check_name, t.school_id, t.academic_year_id, COUNT(*) AS cnt
FROM edu_term t WHERE t.is_current = '1'
GROUP BY t.school_id, t.academic_year_id HAVING cnt > 1;

-- 20. 教学班成员必须能对应到有效的教学班与学生在校记录
SELECT '教学班成员无在校记录' AS check_name, m.teaching_class_id, m.student_id
FROM edu_teaching_class_member m
LEFT JOIN edu_student_enrollment e ON e.student_id = m.student_id AND e.school_id = m.school_id
WHERE m.status = '1' AND e.id IS NULL;

-- 21. 视图是否齐全
SELECT '缺视图' AS check_name, expect.view_name FROM (
SELECT 'v_edu_audit_operator_access' AS view_name UNION ALL SELECT 'v_edu_audit_security_event' AS view_name
) AS expect
LEFT JOIN information_schema.views AS actual
  ON actual.table_schema = DATABASE() AND actual.table_name = expect.view_name
WHERE actual.table_name IS NULL;

-- 22. 归档表是否已建
SELECT '缺派生表' AS check_name, expect.table_name FROM (
  SELECT 'edu_audit_log_archive' AS table_name
) AS expect
LEFT JOIN information_schema.tables AS actual
  ON actual.table_schema = DATABASE() AND actual.table_name = expect.table_name
WHERE actual.table_name IS NULL;
