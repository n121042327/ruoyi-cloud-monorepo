package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学生导出器（模块编码 {@code student}）。
 *
 * 学生主体是平台级实体，学校侧一律经「在校记录」两段式取数（AGENTS 第 7 节）：
 * 先按 `edu_student_enrollment.school_id = 任务所属学校` 拿到本校学生，再按班级关系补年级 / 班级。
 * 后台执行没有登录态，因此**数据范围以任务落库时的学校上下文为准**；缺学校上下文直接拒绝导出，
 * 不退回「全平台」。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class StudentExportHandler implements EduExportHandler {

    /** 模块编码：与 openapi / 前端 exportData 的 moduleCode 一致 */
    public static final String MODULE_CODE = "student";

    private static final String MEMBER_IN = "1";

    private static final Map<String, String> STAGE_NAMES = Map.of(
        "primary", "小学",
        "junior", "初中",
        "senior", "高中"
    );

    private static final Map<String, String> STATUS_NAMES = Map.ofEntries(
        Map.entry("pending_enroll", "转入未报到"),
        Map.entry("enrolled", "在读"),
        Map.entry("suspended", "休学"),
        Map.entry("studying_abroad", "出国"),
        Map.entry("missing", "失踪"),
        Map.entry("transferred_out", "已转出"),
        Map.entry("graduated", "毕业"),
        Map.entry("course_completed", "结业"),
        Map.entry("course_incomplete", "肄业"),
        Map.entry("expelled", "开除"),
        Map.entry("withdrawn", "退学"),
        Map.entry("deceased", "死亡")
    );

    /** 学生列表页默认列（不含敏感列） */
    private static final String[] CSV_HEADERS = {"学号", "姓名", "性别", "入学年份", "学段", "年级", "班级", "学籍状态"};

    private final EduStudentMapper studentMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final EduClassMemberMapper classMemberMapper;
    private final EduClassMapper classMapper;
    private final EduGradeMapper gradeMapper;

    @Override
    public String moduleCode() {
        return MODULE_CODE;
    }

    @Override
    public EduExportedFile export(EduExportContext context) {
        if (context.getSchoolId() == null) {
            throw new ServiceException("导出缺少学校上下文，拒绝导出（DS-DENY-02）");
        }
        List<EduStudentEnrollment> enrollments = enrollmentMapper.selectList(
            new LambdaQueryWrapper<EduStudentEnrollment>()
                .eq(EduStudentEnrollment::getSchoolId, context.getSchoolId()));
        Map<Long, EduStudentEnrollment> enrollmentByStudent = new HashMap<>();
        for (EduStudentEnrollment item : enrollments) {
            if (item.getStudentId() != null) {
                enrollmentByStudent.putIfAbsent(item.getStudentId(), item);
            }
        }
        List<Long> studentIds = new ArrayList<>(enrollmentByStudent.keySet());
        List<StudentExportRow> rows = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            List<EduStudent> students = studentMapper.selectByIds(studentIds);
            students = students.stream()
                .filter(item -> matchKeyword(item, context.getKeyword()))
                .sorted(Comparator.comparing(EduStudent::getStudentNo, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
            Map<Long, Long> classByStudent = currentClass(studentIds);
            Map<Long, EduClass> classes = loadClasses(classByStudent.values());
            Map<Long, EduGrade> grades = loadGrades(
                classes.values().stream().map(EduClass::getGradeId).collect(Collectors.toList()));
            for (EduStudent student : students) {
                StudentExportRow row = new StudentExportRow();
                row.setStudentNo(student.getStudentNo());
                row.setStudentName(student.getStudentName());
                row.setGender(student.getGender());
                row.setEnrollYear(student.getEnrollYear());
                EduClass clazz = classes.get(classByStudent.get(student.getStudentId()));
                EduGrade grade = clazz == null ? null : grades.get(clazz.getGradeId());
                row.setStageName(grade == null ? null : STAGE_NAMES.get(grade.getStageCode()));
                row.setGradeName(grade == null ? null : grade.getGradeName());
                row.setClassName(clazz == null ? null : clazz.getClassName());
                EduStudentEnrollment enrollment = enrollmentByStudent.get(student.getStudentId());
                row.setEnrollmentStatusName(enrollment == null ? null
                    : STATUS_NAMES.getOrDefault(enrollment.getEnrollmentStatus(), enrollment.getEnrollmentStatus()));
                rows.add(row);
            }
        }
        String baseName = "student-" + context.getTaskNo();
        if ("csv".equalsIgnoreCase(StringUtils.trimToEmpty(context.getFormat()))) {
            return new EduExportedFile(toCsv(rows).getBytes(StandardCharsets.UTF_8), baseName + ".csv",
                "text/csv;charset=UTF-8", rows.size());
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelUtil.exportExcel(rows, "学生", StudentExportRow.class, out);
            return new EduExportedFile(out.toByteArray(), baseName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", rows.size());
        } catch (Exception e) {
            throw new ServiceException("生成学生导出文件失败：" + e.getMessage());
        }
    }

    private boolean matchKeyword(EduStudent student, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        String key = keyword.trim();
        return (student.getStudentNo() != null && student.getStudentNo().contains(key))
            || (student.getStudentName() != null && student.getStudentName().contains(key));
    }

    /** 学生在班（status = '1'）时所在班级 */
    private Map<Long, Long> currentClass(List<Long> studentIds) {
        Map<Long, Long> result = new HashMap<>();
        List<EduClassMember> members = classMemberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
            .in(EduClassMember::getStudentId, studentIds)
            .eq(EduClassMember::getStatus, MEMBER_IN));
        for (EduClassMember member : members) {
            if (member.getStudentId() != null && member.getClassId() != null) {
                result.putIfAbsent(member.getStudentId(), member.getClassId());
            }
        }
        return result;
    }

    private Map<Long, EduClass> loadClasses(java.util.Collection<Long> classIds) {
        List<Long> distinct = classIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, EduClass> result = new HashMap<>();
        if (distinct.isEmpty()) {
            return result;
        }
        for (EduClass item : classMapper.selectByIds(distinct)) {
            result.put(item.getClassId(), item);
        }
        return result;
    }

    private Map<Long, EduGrade> loadGrades(List<Long> gradeIds) {
        List<Long> distinct = gradeIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, EduGrade> result = new HashMap<>();
        if (distinct.isEmpty()) {
            return result;
        }
        for (EduGrade item : gradeMapper.selectByIds(distinct)) {
            result.put(item.getGradeId(), item);
        }
        return result;
    }

    private String toCsv(List<StudentExportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", CSV_HEADERS)).append('\n');
        for (StudentExportRow row : rows) {
            sb.append(csv(row.getStudentNo())).append(',')
                .append(csv(row.getStudentName())).append(',')
                .append(csv(row.getGender())).append(',')
                .append(row.getEnrollYear() == null ? "" : row.getEnrollYear()).append(',')
                .append(csv(row.getStageName())).append(',')
                .append(csv(row.getGradeName())).append(',')
                .append(csv(row.getClassName())).append(',')
                .append(csv(row.getEnrollmentStatusName())).append('\n');
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
