-- V5__edu_foreign_keys.sql
-- 全部外键约束（同服务内核心关系使用物理外键，禁止级联删除；跨服务关系用逻辑引用）
-- 生成工具 tools/gen_schema_artifacts.py

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `edu_student_enrollment` ADD CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_student_guardian` ADD CONSTRAINT `fk_sg_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_student_guardian` ADD CONSTRAINT `fk_sg_guardian` FOREIGN KEY (`guardian_id`) REFERENCES `edu_guardian` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_student_field_change` ADD CONSTRAINT `fk_sfc_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_activation_code` ADD CONSTRAINT `fk_activation_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_grade_leader` ADD CONSTRAINT `fk_grade_leader_grade` FOREIGN KEY (`grade_id`) REFERENCES `edu_grade` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_teaching_assignment` ADD CONSTRAINT `fk_assignment_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `edu_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_teaching_assignment` ADD CONSTRAINT `fk_assignment_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_class` ADD CONSTRAINT `fk_class_grade` FOREIGN KEY (`grade_id`) REFERENCES `edu_grade` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_class` ADD CONSTRAINT `fk_class_term` FOREIGN KEY (`term_id`) REFERENCES `edu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_class` ADD CONSTRAINT `fk_class_head_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `edu_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_class_member` ADD CONSTRAINT `fk_member_class` FOREIGN KEY (`class_id`) REFERENCES `edu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_class_member` ADD CONSTRAINT `fk_member_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_teaching_class` ADD CONSTRAINT `fk_tclass_term` FOREIGN KEY (`term_id`) REFERENCES `edu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_teaching_class_member` ADD CONSTRAINT `fk_tcm_class` FOREIGN KEY (`teaching_class_id`) REFERENCES `edu_teaching_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_teaching_class_member` ADD CONSTRAINT `fk_tcm_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_promotion_task` ADD CONSTRAINT `fk_promotion_source_term` FOREIGN KEY (`source_term_id`) REFERENCES `edu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_promotion_task` ADD CONSTRAINT `fk_promotion_target_term` FOREIGN KEY (`target_term_id`) REFERENCES `edu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_promotion_item` ADD CONSTRAINT `fk_promotion_item_task` FOREIGN KEY (`task_id`) REFERENCES `edu_promotion_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_promotion_item` ADD CONSTRAINT `fk_promotion_item_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_enrollment_change` ADD CONSTRAINT `fk_change_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_transfer_order` ADD CONSTRAINT `fk_transfer_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_stream_config` ADD CONSTRAINT `fk_stream_config_term` FOREIGN KEY (`term_id`) REFERENCES `edu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_student_stream` ADD CONSTRAINT `fk_student_stream_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_stream_change_request` ADD CONSTRAINT `fk_stream_request_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_stream_history` ADD CONSTRAINT `fk_stream_history_student` FOREIGN KEY (`student_id`) REFERENCES `edu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_subject_stage` ADD CONSTRAINT `fk_subject_stage_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_term` ADD CONSTRAINT `fk_term_year` FOREIGN KEY (`academic_year_id`) REFERENCES `edu_academic_year` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_import_batch` ADD CONSTRAINT `fk_import_batch_task` FOREIGN KEY (`async_task_no`) REFERENCES `edu_async_task` (`task_no`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_async_task_retry` ADD CONSTRAINT `fk_task_retry_task` FOREIGN KEY (`task_no`) REFERENCES `edu_async_task` (`task_no`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_audit_change` ADD CONSTRAINT `fk_audit_change_log` FOREIGN KEY (`log_id`) REFERENCES `edu_audit_log` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `edu_data_grant_scope` ADD CONSTRAINT `fk_grant_scope_grant` FOREIGN KEY (`grant_id`) REFERENCES `edu_data_grant` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
