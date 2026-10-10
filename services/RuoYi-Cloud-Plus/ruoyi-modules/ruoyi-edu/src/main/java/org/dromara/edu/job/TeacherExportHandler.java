package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.EduUserRole;
import org.dromara.edu.mapper.EduSchoolMapper;
import org.dromara.edu.mapper.EduSubjectMapper;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.mapper.EduUserRoleMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 教师导出器（模块编码 {@code teacher}）。
 *
 * 默认列：工号 / 姓名 / 性别 / 所属学校 / 教育角色 / 任教学科 / 任课班级数 / 在职状态 / 联系电话（掩码）。
 * 教师主体带所属学校，按 {@link EduExportContext#getSchoolId()} 做学校隔离（后台导出没有登录态，见 D-218）；
 * 教育角色来自 `edu_user_role`、任教学科与班级数来自 `edu_teaching_assignment`，都按教师 ID 批量取，避免 N+1。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class TeacherExportHandler implements EduExportHandler {

    /** 模块编码：与 openapi / 前端 exportData 的 moduleCode 一致 */
    public static final String MODULE_CODE = "teacher";

    private static final String FLAG_ON = "1";

    /** 教育角色码值 → 中文（与前端 EDU_ROLE_OPTIONS 一致） */
    private static final Map<String, String> ROLE_NAMES = Map.of(
        "school_leader", "校领导",
        "academic_director", "教务主任",
        "grade_leader", "年级主任",
        "homeroom", "班主任",
        "subject_teacher", "任课教师"
    );

    /** 在职状态码值 → 中文（与 enums/edu/TeacherEnum.ts 一致） */
    private static final Map<String, String> EMPLOYMENT_NAMES = Map.of(
        "active", "在职",
        "resigned", "离职",
        "transferred_out", "调离"
    );

    private static final String[] CSV_HEADERS = {"工号", "姓名", "性别", "所属学校", "教育角色",
        "任教学科", "任课班级数", "在职状态", "联系电话"};

    private final EduTeacherMapper teacherMapper;
    private final EduSchoolMapper schoolMapper;
    private final EduUserRoleMapper userRoleMapper;
    private final EduTeachingAssignmentMapper assignmentMapper;
    private final EduSubjectMapper subjectMapper;
    private final EduExportScopeResolver exportScopeResolver;

    @Override
    public String moduleCode() {
        return MODULE_CODE;
    }

    @Override
    public EduExportedFile export(EduExportContext context) {
        if (context.getSchoolId() == null) {
            throw new ServiceException("导出缺少学校上下文，拒绝导出（DS-DENY-02）");
        }
        LambdaQueryWrapper<EduTeacher> wrapper = new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getSchoolId, context.getSchoolId());
        if (StringUtils.isNotBlank(context.getKeyword())) {
            String key = context.getKeyword().trim();
            wrapper.and(w -> w.like(EduTeacher::getTeacherNo, key)
                .or().like(EduTeacher::getTeacherName, key)
                .or().like(EduTeacher::getPhone, key));
        }
        if (context.getFilters() != null && context.getFilters().get("employmentStatus") != null) {
            wrapper.eq(EduTeacher::getEmploymentStatus, String.valueOf(context.getFilters().get("employmentStatus")));
        }
        // 任务范围（GAP-114）：班主任 / 任课教师 → 本人（任教）班级里的教师；为空 = 本校全量（D-231）。
        // 教师的范围要经「任教关系」落到教师 ID，与班级维度的两张表不是一套写法。
        List<Long> scopeClassIds = exportScopeResolver.effectiveClassIds(context);
        List<EduTeacher> teachers;
        if (scopeClassIds.isEmpty()) {
            teachers = teacherMapper.selectList(wrapper);
        } else {
            List<Long> scopeTeacherIds = teachersInClasses(context.getSchoolId(), scopeClassIds);
            if (scopeTeacherIds.isEmpty()) {
                // 范围内没有教师：给空表，不得退回本校全量
                teachers = List.of();
            } else {
                wrapper.in(EduTeacher::getTeacherId, scopeTeacherIds);
                teachers = teacherMapper.selectList(wrapper);
            }
        }
        teachers = teachers.stream()
            .sorted(Comparator.comparing(EduTeacher::getTeacherNo, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
        List<TeacherExportRow> rows = toRows(context.getSchoolId(), teachers);
        String baseName = "teacher-" + context.getTaskNo();
        if ("csv".equalsIgnoreCase(StringUtils.trimToEmpty(context.getFormat()))) {
            return new EduExportedFile(toCsv(rows).getBytes(StandardCharsets.UTF_8), baseName + ".csv",
                "text/csv;charset=UTF-8", rows.size());
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelUtil.exportExcel(rows, "教师", TeacherExportRow.class, out);
            return new EduExportedFile(out.toByteArray(), baseName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", rows.size());
        } catch (Exception e) {
            throw new ServiceException("生成教师导出文件失败：" + e.getMessage());
        }
    }

    /** 在给定班级列表里有有效任教关系（status = '1'）的教师 ID */
    private List<Long> teachersInClasses(Long schoolId, List<Long> classIds) {
        return assignmentMapper.selectList(new LambdaQueryWrapper<EduTeachingAssignment>()
                .eq(EduTeachingAssignment::getSchoolId, schoolId)
                .in(EduTeachingAssignment::getClassId, classIds)
                .eq(EduTeachingAssignment::getStatus, FLAG_ON))
            .stream()
            .map(EduTeachingAssignment::getTeacherId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    private List<TeacherExportRow> toRows(Long schoolId, List<EduTeacher> teachers) {
        List<TeacherExportRow> rows = new ArrayList<>();
        if (teachers.isEmpty()) {
            return rows;
        }
        EduSchool school = schoolMapper.selectById(schoolId);
        String schoolName = school == null ? null : school.getSchoolName();
        List<Long> teacherIds = teachers.stream().map(EduTeacher::getTeacherId)
            .filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, Set<String>> roleMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            for (EduUserRole role : userRoleMapper.selectList(new LambdaQueryWrapper<EduUserRole>()
                .in(EduUserRole::getTeacherId, teacherIds)
                .eq(EduUserRole::getStatus, FLAG_ON))) {
                roleMap.computeIfAbsent(role.getTeacherId(), k -> new LinkedHashSet<>())
                    .add(ROLE_NAMES.getOrDefault(role.getEduRole(), role.getEduRole()));
            }
        }
        Map<Long, List<EduTeachingAssignment>> assignments = new HashMap<>();
        Map<Long, String> subjectNames = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            List<EduTeachingAssignment> list = assignmentMapper.selectList(
                new LambdaQueryWrapper<EduTeachingAssignment>()
                    .in(EduTeachingAssignment::getTeacherId, teacherIds)
                    .eq(EduTeachingAssignment::getStatus, FLAG_ON));
            List<Long> subjectIds = list.stream().map(EduTeachingAssignment::getSubjectId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (!subjectIds.isEmpty()) {
                for (EduSubject subject : subjectMapper.selectByIds(subjectIds)) {
                    subjectNames.put(subject.getSubjectId(), subject.getSubjectName());
                }
            }
            for (EduTeachingAssignment item : list) {
                assignments.computeIfAbsent(item.getTeacherId(), k -> new ArrayList<>()).add(item);
            }
        }
        for (EduTeacher teacher : teachers) {
            TeacherExportRow row = new TeacherExportRow();
            row.setTeacherNo(teacher.getTeacherNo());
            row.setTeacherName(teacher.getTeacherName());
            row.setGender(teacher.getGender());
            row.setSchoolName(schoolName);
            row.setEduRoles(join(roleMap.get(teacher.getTeacherId())));
            List<EduTeachingAssignment> own = assignments.getOrDefault(teacher.getTeacherId(), List.of());
            row.setSubjectNames(own.stream()
                .map(item -> subjectNames.get(item.getSubjectId()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("、")));
            row.setTeachingClassCount((int) own.stream().map(EduTeachingAssignment::getClassId)
                .filter(Objects::nonNull).distinct().count());
            row.setEmploymentStatusName(EMPLOYMENT_NAMES.getOrDefault(
                teacher.getEmploymentStatus(), teacher.getEmploymentStatus()));
            // 敏感字段默认掩码（BR-TEACHER-005 / REQ-IMP-028）
            row.setPhoneMasked(maskPhone(teacher.getPhone()));
            rows.add(row);
        }
        return rows;
    }

    private String join(Set<String> values) {
        return values == null || values.isEmpty() ? null : String.join("、", values);
    }

    /** 手机号掩码：保留前 3 后 4（与列表页展示口径一致） */
    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String toCsv(List<TeacherExportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", CSV_HEADERS)).append('\n');
        for (TeacherExportRow row : rows) {
            sb.append(csv(row.getTeacherNo())).append(',')
                .append(csv(row.getTeacherName())).append(',')
                .append(csv(row.getGender())).append(',')
                .append(csv(row.getSchoolName())).append(',')
                .append(csv(row.getEduRoles())).append(',')
                .append(csv(row.getSubjectNames())).append(',')
                .append(row.getTeachingClassCount() == null ? "" : row.getTeachingClassCount()).append(',')
                .append(csv(row.getEmploymentStatusName())).append(',')
                .append(csv(row.getPhoneMasked())).append('\n');
        }
        return sb.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") || value.contains("\"")
            ? "\"" + value.replace("\"", "\"\"") + "\""
            : value;
    }
}
