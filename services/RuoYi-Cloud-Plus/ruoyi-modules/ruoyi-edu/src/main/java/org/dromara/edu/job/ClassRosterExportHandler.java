package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduGuardian;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.domain.EduStudentGuardian;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduGuardianMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentGuardianMapper;
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
 * 编班表（班级花名册）导出器（模块编码 {@code class_roster}）。
 *
 * 传了 `classId` 就导该班；没传就导本校全部在班关系（编班表导出的两种用法，见 class 模块 PRD）。
 * 学校隔离按 `edu_class_member.school_id = 任务学校`（后台导出没有登录态，见 D-218）。
 * 监护人取主监护人优先，联系电话默认掩码（REQ-AUD-009）。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class ClassRosterExportHandler implements EduExportHandler {

    /** 模块编码：与 openapi / 前端 exportClassRoster 的 moduleCode 一致 */
    public static final String MODULE_CODE = "class_roster";

    private static final String MEMBER_IN = "1";

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

    private static final String[] CSV_HEADERS = {"班级", "学号", "姓名", "性别", "学籍状态",
        "加入日期", "监护人", "联系电话"};

    private final EduClassMemberMapper classMemberMapper;
    private final EduClassMapper classMapper;
    private final EduStudentMapper studentMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final EduStudentGuardianMapper studentGuardianMapper;
    private final EduGuardianMapper guardianMapper;
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
        // 任务范围（GAP-114）：班主任 → 本班；年级主任 → 本年级下的行政班；为空 = 本校全量
        List<Long> scopeClassIds = exportScopeResolver.effectiveClassIds(context);
        List<EduClassMember> members = classMemberMapper.selectList(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getSchoolId, context.getSchoolId())
            .eq(EduClassMember::getClassType, "administrative")
            .eq(EduClassMember::getStatus, MEMBER_IN)
            .eq(context.getClassId() != null, EduClassMember::getClassId, context.getClassId())
            .in(!scopeClassIds.isEmpty(), EduClassMember::getClassId, scopeClassIds)
            .orderByAsc(EduClassMember::getClassId)
            .orderByAsc(EduClassMember::getStudentId));
        List<ClassRosterExportRow> rows = toRows(members, context.getKeyword(), context.getTermId());
        String baseName = "class-roster-" + context.getTaskNo();
        if ("csv".equalsIgnoreCase(StringUtils.trimToEmpty(context.getFormat()))) {
            return new EduExportedFile(toCsv(rows).getBytes(StandardCharsets.UTF_8), baseName + ".csv",
                "text/csv;charset=UTF-8", rows.size());
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelUtil.exportExcel(rows, "编班表", ClassRosterExportRow.class, out);
            return new EduExportedFile(out.toByteArray(), baseName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", rows.size());
        } catch (Exception e) {
            throw new ServiceException("生成编班表导出文件失败：" + e.getMessage());
        }
    }

    private List<ClassRosterExportRow> toRows(List<EduClassMember> members, String keyword, Long termId) {
        List<ClassRosterExportRow> rows = new ArrayList<>();
        if (members.isEmpty()) {
            return rows;
        }
        List<Long> studentIds = members.stream().map(EduClassMember::getStudentId)
            .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, EduStudent> students = studentIds.isEmpty() ? Map.of()
            : studentMapper.selectByIds(studentIds).stream()
                .collect(Collectors.toMap(EduStudent::getStudentId, s -> s, (a, b) -> a, HashMap::new));
        Map<Long, EduStudentEnrollment> enrollments = new HashMap<>();
        if (!studentIds.isEmpty()) {
            for (EduStudentEnrollment item : enrollmentMapper.selectList(
                new LambdaQueryWrapper<EduStudentEnrollment>()
                    .in(EduStudentEnrollment::getStudentId, studentIds))) {
                enrollments.putIfAbsent(item.getStudentId(), item);
            }
        }
        Map<Long, EduClass> classes = loadClasses(members.stream().map(EduClassMember::getClassId)
            .filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        Map<Long, EduGuardian> guardians = loadGuardians(studentIds);
        for (EduClassMember member : members) {
            EduStudent student = students.get(member.getStudentId());
            if (student == null) {
                continue;
            }
            if (StringUtils.isNotBlank(keyword)) {
                String key = keyword.trim();
                boolean hit = (student.getStudentNo() != null && student.getStudentNo().contains(key))
                    || (student.getStudentName() != null && student.getStudentName().contains(key));
                if (!hit) {
                    continue;
                }
            }
            ClassRosterExportRow row = new ClassRosterExportRow();
            EduClass clazz = classes.get(member.getClassId());
            row.setClassName(clazz == null ? null : clazz.getClassName());
            row.setStudentNo(student.getStudentNo());
            row.setStudentName(student.getStudentName());
            row.setGender(student.getGender());
            EduStudentEnrollment enrollment = enrollments.get(member.getStudentId());
            row.setEnrollmentStatusName(enrollment == null ? null
                : STATUS_NAMES.getOrDefault(enrollment.getEnrollmentStatus(), enrollment.getEnrollmentStatus()));
            row.setJoinDate(member.getJoinDate() == null ? null
                : cn.hutool.core.date.DateUtil.formatDate(member.getJoinDate()));
            EduGuardian guardian = guardians.get(member.getStudentId());
            row.setGuardianName(guardian == null ? null : guardian.getGuardianName());
            row.setGuardianPhone(guardian == null ? null : maskPhone(guardian.getGuardianPhone()));
            rows.add(row);
        }
        return rows;
    }

    private Map<Long, EduClass> loadClasses(List<Long> classIds) {
        Map<Long, EduClass> result = new HashMap<>();
        if (classIds.isEmpty()) {
            return result;
        }
        for (EduClass item : classMapper.selectByIds(classIds)) {
            result.put(item.getClassId(), item);
        }
        return result;
    }

    /** 主监护人优先，没有主监护人时取第一条绑定关系 */
    private Map<Long, EduGuardian> loadGuardians(List<Long> studentIds) {
        Map<Long, EduGuardian> result = new HashMap<>();
        if (studentIds.isEmpty()) {
            return result;
        }
        List<EduStudentGuardian> relations = studentGuardianMapper.selectList(
            new LambdaQueryWrapper<EduStudentGuardian>().in(EduStudentGuardian::getStudentId, studentIds));
        Map<Long, Long> chosen = new HashMap<>();
        for (EduStudentGuardian relation : relations) {
            if (relation.getStudentId() == null || relation.getGuardianId() == null) {
                continue;
            }
            if (MEMBER_IN.equals(relation.getIsPrimary())) {
                chosen.put(relation.getStudentId(), relation.getGuardianId());
            } else {
                chosen.putIfAbsent(relation.getStudentId(), relation.getGuardianId());
            }
        }
        if (chosen.isEmpty()) {
            return result;
        }
        Map<Long, EduGuardian> byId = guardianMapper
            .selectByIds(chosen.values().stream().distinct().collect(Collectors.toList())).stream()
            .collect(Collectors.toMap(EduGuardian::getGuardianId, g -> g, (a, b) -> a, HashMap::new));
        for (Map.Entry<Long, Long> entry : chosen.entrySet()) {
            EduGuardian guardian = byId.get(entry.getValue());
            if (guardian != null) {
                result.put(entry.getKey(), guardian);
            }
        }
        return result;
    }

    /** 手机号掩码：保留前 3 后 4（与花名册列表口径一致，REQ-AUD-009） */
    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String toCsv(List<ClassRosterExportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", CSV_HEADERS)).append('\n');
        for (ClassRosterExportRow row : rows) {
            sb.append(csv(row.getClassName())).append(',')
                .append(csv(row.getStudentNo())).append(',')
                .append(csv(row.getStudentName())).append(',')
                .append(csv(row.getGender())).append(',')
                .append(csv(row.getEnrollmentStatusName())).append(',')
                .append(csv(row.getJoinDate())).append(',')
                .append(csv(row.getGuardianName())).append(',')
                .append(csv(row.getGuardianPhone())).append('\n');
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
