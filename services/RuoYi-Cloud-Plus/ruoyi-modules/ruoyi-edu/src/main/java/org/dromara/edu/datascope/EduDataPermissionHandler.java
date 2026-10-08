package org.dromara.edu.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 教育域数据范围 SQL 片段生成器
 *
 * 由 `DataPermissionInterceptor` 在查询构造时回调，按表名给出范围条件
 * （`docs/30-architecture/09-permission-architecture.md` 第 4.2 节：用 SQL 拦截器而不是每个 Service 自己拼条件）。
 *
 * 表清单按 `docs/40-detailed-design/database/schema.yaml` 的 `scope` 与显式列推导：
 * - `school` 范围的表都有 `school_id` → 用 `school_id IN (...)`
 * - `edu_school` 本身不在此列：它即学校主体，`tenant_id` 已保证一校一租户（CR-104 去掉冗余 school_id）。
 * - 显式带 `grade_id` 的表（`edu_grade_leader` / `edu_class` / `edu_teaching_class`）→ 用 `school_id IN (...) OR grade_id IN (...)`
 * - 显式带 `class_id` 的表（`edu_teaching_assignment` / `edu_class_member`）→ 用 `school_id IN (...) OR class_id IN (...)`
 *
 * **范围为空集时生成 `1 = 2`**，即返回空结果而不是全量（`DS-DENY-03` / `REQ-AUD-024`）。
 * 条件里不带任何业务参数，全部来自 {@link DataScopeContext}，避免 SQL 注入面。
 *
 * @author Codex
 */
@Slf4j
@Component
public class EduDataPermissionHandler implements MultiDataPermissionHandler {

    /** schema.yaml 中 scope=school 的表（含派生表 edu_audit_log_archive，其 DDL 为 CREATE TABLE ... LIKE edu_audit_log） */
    private static final Set<String> SCHOOL_TABLES = Set.of(
        "edu_student_enrollment", "edu_student_field_change", "edu_activation_code", "edu_teacher",
        "edu_user_role", "edu_grade_leader", "edu_teaching_assignment", "edu_grade", "edu_class",
        "edu_class_member", "edu_teaching_class", "edu_teaching_class_member", "edu_promotion_task",
        "edu_promotion_item", "edu_enrollment_change", "edu_transfer_order", "edu_stream_config",
        "edu_student_stream", "edu_stream_change_request", "edu_stream_history", "edu_subject",
        "edu_subject_stage", "edu_campus", "edu_school_stage", "edu_academic_year",
        "edu_term", "edu_import_batch", "edu_import_error", "edu_async_task", "edu_async_task_retry",
        "edu_dead_letter_task", "edu_file_ref", "edu_audit_log", "edu_audit_change",
        "edu_audit_log_archive");

    /** 显式带 grade_id 的表 */
    private static final Set<String> GRADE_TABLES = Set.of(
        "edu_grade_leader", "edu_class", "edu_teaching_class");

    /** 显式带 class_id 的表 */
    private static final Set<String> CLASS_TABLES = Set.of(
        "edu_teaching_assignment", "edu_class_member");

    /** 空集时使用的恒假条件 */
    private static final String EMPTY_CONDITION = "1 = 2";

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataScopeContext context = DataScopeContext.get();
        if (context == null || context.isUnrestricted() || table == null) {
            return null;
        }
        String tableName = normalize(table.getName());
        if (!SCHOOL_TABLES.contains(tableName)) {
            // 平台级实体（edu_student / edu_guardian / edu_student_guardian）与租户级表不在教育域范围列内
            return null;
        }
        String prefix = "";
        if (table.getAlias() != null && StringUtils.isNotBlank(table.getAlias().getName())) {
            prefix = table.getAlias().getName() + ".";
        }
        String condition = buildCondition(context, tableName, prefix);
        if (condition == null) {
            return null;
        }
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception e) {
            // 解析失败时记录并放行（不生成条件），由阶段 8 的联调用例兜住；绝不生成恒真条件
            log.error("教育域数据范围条件解析失败，已跳过条件注入：table={}, condition={}", tableName, condition, e);
            return null;
        }
    }

    private String buildCondition(DataScopeContext context, String tableName, String prefix) {
        String schoolCondition = inCondition(prefix + "school_id", context.getSchoolIds());
        if (GRADE_TABLES.contains(tableName)) {
            String gradeCondition = inCondition(prefix + "grade_id", context.getGradeIds());
            if (schoolCondition == null && gradeCondition == null) {
                return EMPTY_CONDITION;
            }
            return "(" + or(schoolCondition, gradeCondition) + ")";
        }
        if (CLASS_TABLES.contains(tableName)) {
            String classCondition = inCondition(prefix + "class_id", context.getClassIds());
            if (schoolCondition == null && classCondition == null) {
                return EMPTY_CONDITION;
            }
            return "(" + or(schoolCondition, classCondition) + ")";
        }
        return schoolCondition == null ? EMPTY_CONDITION : schoolCondition;
    }

    private String inCondition(String column, Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        String values = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return column + " IN (" + values + ")";
    }

    private String or(String left, String right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left + " OR " + right;
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.replace("`", "").trim().toLowerCase();
    }

}
