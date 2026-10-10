package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.EduStudentEnrollment;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduStudentEnrollmentMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 编班表导入校验器（模块编码 {@code class_roster}）。
 *
 * 语义：**把已有学生编入班级**，不新建学生主体（学生建档走学生模块）。
 * 模板 4 列（编班表导入页说明 + `REQ-CLS-035`：一行一个学生、含目标班级列）：
 * 学号（必填）/ 姓名（选填，用于核对）/ 目标班级（必填）/ 班级类型（选填，默认行政班）。
 * 校验：学号必须存在且属于本校（经在校记录两段式）；姓名填了必须与档案一致；
 * 目标班级必须存在于该学年学期；文件内学号不得重复。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class ClassRosterImportHandler implements EduImportHandler {

    public static final String MODULE_CODE = "class_roster";

    private static final List<String> HEADERS = List.of("学号", "姓名", "目标班级", "班级类型");

    private static final String TYPE_ADMINISTRATIVE = "administrative";
    private static final String TYPE_TEACHING = "teaching";

    private final EduStudentMapper studentMapper;
    private final EduStudentEnrollmentMapper enrollmentMapper;
    private final EduClassMapper classMapper;

    @Override
    public String moduleCode() {
        return MODULE_CODE;
    }

    @Override
    public List<String> templateHeaders() {
        return HEADERS;
    }

    @Override
    public void validateRows(EduImportContext context, List<EduImportRow> rows) {
        if (context.getSchoolId() == null) {
            throw new ServiceException("导入缺少学校上下文，拒绝校验（DS-DENY-02）");
        }
        // 学号 → 学生（学生主体是平台级实体，后面再用在校记录确认属于本校）
        Set<String> studentNos = new HashSet<>();
        for (EduImportRow row : rows) {
            String no = row.getCells().get("学号");
            if (StringUtils.isNotBlank(no)) {
                studentNos.add(no.trim());
            }
        }
        Map<String, EduStudent> students = new HashMap<>();
        if (!studentNos.isEmpty()) {
            for (EduStudent student : studentMapper.selectList(new LambdaQueryWrapper<EduStudent>()
                .in(EduStudent::getStudentNo, studentNos))) {
                students.putIfAbsent(student.getStudentNo(), student);
            }
        }
        Set<Long> schoolStudentIds = new HashSet<>();
        if (!students.isEmpty()) {
            for (EduStudentEnrollment enrollment : enrollmentMapper.selectList(
                new LambdaQueryWrapper<EduStudentEnrollment>()
                    .eq(EduStudentEnrollment::getSchoolId, context.getSchoolId())
                    .in(EduStudentEnrollment::getStudentId,
                        students.values().stream().map(EduStudent::getStudentId).filter(Objects::nonNull).toList()))) {
                if (enrollment.getStudentId() != null) {
                    schoolStudentIds.add(enrollment.getStudentId());
                }
            }
        }
        // 目标班级：本校 + 该学年学期（班级类型按行决定，这里按名称+类型建索引）
        Map<String, EduClass> classes = new HashMap<>();
        for (EduClass clazz : classMapper.selectList(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getSchoolId, context.getSchoolId())
            .eq(context.getTermId() != null, EduClass::getTermId, context.getTermId()))) {
            classes.putIfAbsent(clazz.getClassName() + "|" + clazz.getClassType(), clazz);
        }
        Set<String> seenStudentNos = new HashSet<>();
        for (EduImportRow row : rows) {
            List<String> reasons = new ArrayList<>();
            Map<String, String> cells = row.getCells();
            for (String required : List.of("学号", "目标班级")) {
                if (StringUtils.isBlank(cells.get(required))) {
                    reasons.add(required + "不能为空");
                }
            }
            String studentNo = StringUtils.trimToEmpty(cells.get("学号"));
            String classType = normalizeClassType(cells.get("班级类型"), reasons);
            if (StringUtils.isNotBlank(studentNo)) {
                if (!seenStudentNos.add(studentNo)) {
                    reasons.add("文件内学号重复：" + studentNo);
                }
                EduStudent student = students.get(studentNo);
                if (student == null) {
                    reasons.add("学号不存在：" + studentNo);
                } else {
                    if (!schoolStudentIds.contains(student.getStudentId())) {
                        reasons.add("该学生不在本校在校记录中：" + studentNo);
                    }
                    String name = cells.get("姓名");
                    if (StringUtils.isNotBlank(name) && !name.trim().equals(student.getStudentName())) {
                        reasons.add("姓名与档案不一致：档案为「" + student.getStudentName() + "」");
                    }
                }
            }
            String className = StringUtils.trimToEmpty(cells.get("目标班级"));
            if (StringUtils.isNotBlank(className) && classType != null
                && !classes.containsKey(className + "|" + classType)) {
                reasons.add("目标班级不存在或不属于该学年学期：" + className
                    + "（班级类型：" + classNameOf(classType) + "）");
            }
            if (!reasons.isEmpty()) {
                row.setFailReason(String.join("；", reasons));
            }
        }
    }

    /** 班级类型：选填，默认行政班；中文与码值都接受 */
    private String normalizeClassType(String value, List<String> reasons) {
        if (StringUtils.isBlank(value)) {
            return TYPE_ADMINISTRATIVE;
        }
        String type = value.trim();
        if ("行政班".equals(type) || TYPE_ADMINISTRATIVE.equals(type)) {
            return TYPE_ADMINISTRATIVE;
        }
        if ("教学班".equals(type) || TYPE_TEACHING.equals(type)) {
            return TYPE_TEACHING;
        }
        reasons.add("班级类型只能是行政班 / 教学班");
        return null;
    }

    private String classNameOf(String classType) {
        return TYPE_ADMINISTRATIVE.equals(classType) ? "行政班" : "教学班";
    }
}
