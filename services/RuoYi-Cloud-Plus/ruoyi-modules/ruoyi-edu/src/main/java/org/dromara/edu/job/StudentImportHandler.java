package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 学生导入校验器（模块编码 {@code student}）。
 *
 * 模板列来自学生 PRD 第 4.8 节「导入模板列」（14 列，顺序固定，REQ-STU-052）；校验维度来自
 * `REQ-STU-054`：必填缺失、格式错误、年级或班级不存在、证件号重复、全国学籍号重复、文件内自身重复。
 * 模板**不含学号列**（BR-STU-019），学号由系统发号。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class StudentImportHandler implements EduImportHandler {

    public static final String MODULE_CODE = "student";

    /** 表头顺序与学生 PRD 4.8 节一致 */
    private static final List<String> HEADERS = List.of(
        "姓名", "性别", "入学年份", "学段", "年级", "班级", "全国学籍号", "证件类型", "证件号码",
        "出生日期", "监护人姓名", "与监护人关系", "监护人电话", "联系地址"
    );

    private static final Set<String> GENDERS = Set.of("男", "女");

    private static final Set<String> STAGE_NAMES = Set.of("小学", "初中", "高中");

    private static final Pattern YEAR = Pattern.compile("^\\d{4}$");

    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");

    private static final String TYPE_ADMINISTRATIVE = "administrative";

    private final EduGradeMapper gradeMapper;
    private final EduClassMapper classMapper;
    private final EduStudentMapper studentMapper;

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
        Map<String, EduGrade> grades = new HashMap<>();
        for (EduGrade grade : gradeMapper.selectList(new LambdaQueryWrapper<EduGrade>()
            .eq(EduGrade::getSchoolId, context.getSchoolId()))) {
            grades.putIfAbsent(grade.getGradeName(), grade);
        }
        Map<String, EduClass> classes = new HashMap<>();
        List<EduClass> classList = classMapper.selectList(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getSchoolId, context.getSchoolId())
            .eq(EduClass::getClassType, TYPE_ADMINISTRATIVE)
            .eq(context.getTermId() != null, EduClass::getTermId, context.getTermId()));
        for (EduClass clazz : classList) {
            classes.putIfAbsent(clazz.getClassName(), clazz);
        }
        Set<String> existingIdCards = existingValues(rows, "证件号码", true);
        Set<String> existingNationalNos = existingValues(rows, "全国学籍号", false);
        Set<String> seenIdCards = new HashSet<>();
        Set<String> seenNationalNos = new HashSet<>();
        for (EduImportRow row : rows) {
            List<String> reasons = new ArrayList<>();
            Map<String, String> cells = row.getCells();
            // 必填（PRD 4.8 第 1—5 列）
            for (String required : List.of("姓名", "性别", "入学年份", "学段", "年级")) {
                if (StringUtils.isBlank(cells.get(required))) {
                    reasons.add(required + "不能为空");
                }
            }
            String gender = cells.get("性别");
            if (StringUtils.isNotBlank(gender) && !GENDERS.contains(gender)) {
                reasons.add("性别只能是男 / 女");
            }
            String year = cells.get("入学年份");
            if (StringUtils.isNotBlank(year) && !YEAR.matcher(year).matches()) {
                reasons.add("入学年份必须是 4 位年份");
            }
            String stage = cells.get("学段");
            if (StringUtils.isNotBlank(stage) && !STAGE_NAMES.contains(stage)) {
                reasons.add("学段只能是小学 / 初中 / 高中");
            }
            String gradeName = cells.get("年级");
            if (StringUtils.isNotBlank(gradeName) && !grades.containsKey(gradeName)) {
                reasons.add("年级不存在：" + gradeName);
            }
            // 班级：填写了统一目标班级时忽略文件里的班级列（EduImportValidateBo.targetClassId）
            String className = cells.get("班级");
            if (context.getTargetClassId() == null && StringUtils.isNotBlank(className)
                && !classes.containsKey(className)) {
                reasons.add("班级不存在或不属于该学年学期：" + className);
            }
            String birthDate = cells.get("出生日期");
            if (StringUtils.isNotBlank(birthDate) && DateUtils.parseDate(birthDate) == null) {
                reasons.add("出生日期格式应为 YYYY-MM-DD");
            }
            String nationalNo = cells.get("全国学籍号");
            if (StringUtils.isNotBlank(nationalNo)) {
                if (!nationalNo.startsWith("G") && !nationalNo.startsWith("L")) {
                    reasons.add("全国学籍号应以 G 或 L 开头");
                }
                if (!seenNationalNos.add(nationalNo)) {
                    reasons.add("文件内全国学籍号重复：" + nationalNo);
                }
                if (existingNationalNos.contains(nationalNo)) {
                    reasons.add("全国学籍号已存在：" + nationalNo);
                }
            }
            String idCardNo = cells.get("证件号码");
            if (StringUtils.isNotBlank(idCardNo)) {
                if (!seenIdCards.add(idCardNo)) {
                    reasons.add("文件内证件号码重复：" + idCardNo);
                }
                if (existingIdCards.contains(idCardNo)) {
                    reasons.add("证件号码已存在：" + idCardNo);
                }
            }
            String phone = cells.get("监护人电话");
            if (StringUtils.isNotBlank(phone) && !PHONE.matcher(phone).matches()) {
                reasons.add("监护人电话应为 11 位手机号");
            }
            if (!reasons.isEmpty()) {
                row.setFailReason(String.join("；", reasons));
            }
        }
    }

    /** 取文件中出现过的证件号 / 全国学籍号，与库内已有值比对 */
    private Set<String> existingValues(List<EduImportRow> rows, String column, boolean idCard) {
        List<String> values = rows.stream()
            .map(row -> row.getCells().get(column))
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
        Set<String> result = new HashSet<>();
        if (values.isEmpty()) {
            return result;
        }
        LambdaQueryWrapper<EduStudent> wrapper = new LambdaQueryWrapper<>();
        if (idCard) {
            wrapper.in(EduStudent::getIdCardNo, values);
        } else {
            wrapper.in(EduStudent::getNationalStudentNo, values);
        }
        for (EduStudent student : studentMapper.selectList(wrapper)) {
            String value = idCard ? student.getIdCardNo() : student.getNationalStudentNo();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }
}
