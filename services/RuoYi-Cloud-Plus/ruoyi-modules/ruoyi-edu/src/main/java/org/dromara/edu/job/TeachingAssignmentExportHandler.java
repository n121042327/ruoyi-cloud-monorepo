package org.dromara.edu.job;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduSubject;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.EduTerm;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduSubjectMapper;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.mapper.EduTermMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任教关系导出器（模块编码 {@code teaching_assignment}）。
 *
 * 默认列：学年学期 / 学科 / 教师 / 工号 / 班级 / 班级类型 / 状态。
 * 取数按任务参数（termId / classId / filters.teacherId）过滤；名称字段按 id 批量回填，避免 N+1。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class TeachingAssignmentExportHandler implements EduExportHandler {

    /** 模块编码：与 GAP-089 / GAP-110 的裁决一致，统一用下划线风格 */
    public static final String MODULE_CODE = "teaching_assignment";

    private static final String TYPE_ADMINISTRATIVE = "administrative";
    private static final String TYPE_TEACHING = "teaching";
    private static final String FLAG_ON = "1";

    private final EduTeachingAssignmentMapper assignmentMapper;
    private final EduSubjectMapper subjectMapper;
    private final EduClassMapper classMapper;
    private final EduTeacherMapper teacherMapper;
    private final EduTermMapper termMapper;

    @Override
    public String moduleCode() {
        return MODULE_CODE;
    }

    @Override
    public EduExportedFile export(EduExportContext context) {
        // 后台执行没有登录态，学校隔离必须在这里显式做（见 EduExportHandler 的范围口径）
        if (context.getSchoolId() == null) {
            throw new ServiceException("导出缺少学校上下文，拒绝导出（DS-DENY-02）");
        }
        Long teacherId = null;
        Map<String, Object> filters = context.getFilters();
        if (filters != null && filters.get("teacherId") != null) {
            teacherId = Convert.toLong(filters.get("teacherId"));
        }
        List<EduTeachingAssignment> list = assignmentMapper.selectList(new LambdaQueryWrapper<EduTeachingAssignment>()
            .eq(EduTeachingAssignment::getSchoolId, context.getSchoolId())
            .eq(context.getTermId() != null, EduTeachingAssignment::getTermId, context.getTermId())
            .eq(context.getClassId() != null, EduTeachingAssignment::getClassId, context.getClassId())
            .eq(teacherId != null, EduTeachingAssignment::getTeacherId, teacherId)
            .orderByAsc(EduTeachingAssignment::getTermId)
            .orderByAsc(EduTeachingAssignment::getSubjectId)
            .orderByAsc(EduTeachingAssignment::getClassId));
        List<TeachingAssignmentExportRow> rows = toRows(list);
        String baseName = "teaching-assignment-" + context.getTaskNo();
        if ("csv".equalsIgnoreCase(StringUtils.trimToEmpty(context.getFormat()))) {
            return new EduExportedFile(toCsv(rows).getBytes(StandardCharsets.UTF_8), baseName + ".csv",
                "text/csv;charset=UTF-8", rows.size());
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelUtil.exportExcel(rows, "任教关系", TeachingAssignmentExportRow.class, out);
            return new EduExportedFile(out.toByteArray(), baseName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", rows.size());
        } catch (Exception e) {
            throw new ServiceException("生成任教关系导出文件失败：" + e.getMessage());
        }
    }

    /** 组装行并批量回填名称 */
    private List<TeachingAssignmentExportRow> toRows(List<EduTeachingAssignment> list) {
        List<TeachingAssignmentExportRow> rows = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return rows;
        }
        Map<Long, String> terms = new HashMap<>();
        for (EduTerm item : termMapper.selectByIds(ids(list, EduTeachingAssignment::getTermId))) {
            terms.put(item.getTermId(), item.getTermName());
        }
        Map<Long, String> subjects = new HashMap<>();
        for (EduSubject item : subjectMapper.selectByIds(ids(list, EduTeachingAssignment::getSubjectId))) {
            subjects.put(item.getSubjectId(), item.getSubjectName());
        }
        Map<Long, String> classes = new HashMap<>();
        for (EduClass item : classMapper.selectByIds(ids(list, EduTeachingAssignment::getClassId))) {
            classes.put(item.getClassId(), item.getClassName());
        }
        Map<Long, EduTeacher> teachers = teacherMapper
            .selectByIds(ids(list, EduTeachingAssignment::getTeacherId)).stream()
            .collect(Collectors.toMap(EduTeacher::getTeacherId, t -> t, (a, b) -> a, HashMap::new));
        for (EduTeachingAssignment item : list) {
            TeachingAssignmentExportRow row = new TeachingAssignmentExportRow();
            row.setTermName(terms.get(item.getTermId()));
            row.setSubjectName(subjects.get(item.getSubjectId()));
            EduTeacher teacher = teachers.get(item.getTeacherId());
            row.setTeacherName(teacher == null ? null : teacher.getTeacherName());
            row.setTeacherNo(teacher == null ? null : teacher.getTeacherNo());
            row.setClassName(classes.get(item.getClassId()));
            row.setClassType(className(item.getClassType()));
            row.setStatus(FLAG_ON.equals(item.getStatus()) ? "有效" : "失效");
            rows.add(row);
        }
        return rows;
    }

    private List<Long> ids(List<EduTeachingAssignment> list, Function<EduTeachingAssignment, Long> getter) {
        return list.stream().map(getter).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private String className(String classType) {
        if (TYPE_ADMINISTRATIVE.equals(classType)) {
            return "行政班";
        }
        return TYPE_TEACHING.equals(classType) ? "教学班" : classType;
    }

    private String toCsv(List<TeachingAssignmentExportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("学年学期,学科,教师,工号,班级,班级类型,状态").append('\n');
        for (TeachingAssignmentExportRow row : rows) {
            sb.append(csv(row.getTermName())).append(',')
                .append(csv(row.getSubjectName())).append(',')
                .append(csv(row.getTeacherName())).append(',')
                .append(csv(row.getTeacherNo())).append(',')
                .append(csv(row.getClassName())).append(',')
                .append(csv(row.getClassType())).append(',')
                .append(csv(row.getStatus())).append('\n');
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
