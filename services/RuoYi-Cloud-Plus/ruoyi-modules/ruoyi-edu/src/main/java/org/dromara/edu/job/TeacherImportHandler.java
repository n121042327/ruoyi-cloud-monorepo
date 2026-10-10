package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduSchool;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.mapper.EduSchoolMapper;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 教师导入校验器（模块编码 {@code teacher}）。
 *
 * 模板列来自教师 PRD 第 4.8 节「导入模板列」（10 列，顺序固定；「登录名」见 CR-156 / GAP-116）；
 * 校验维度：必填、性别枚举、所属学校必须已存在、工号在同一学校内唯一（BR-TEACHER-007）、
 * 手机号 / 入职日期 / 邮箱格式、教育角色取值（多个用逗号分隔）、登录名格式与文件内唯一。
 *
 * 「登录名」是**选填**列：填了就按它建登录账号（执行器负责生成初始密码），不填则只建档案，
 * 与 `BR-ACCOUNT-001`「登录名由学校统一分配」一致 —— 导入不替学校编排登录名。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class TeacherImportHandler implements EduImportHandler {

    public static final String MODULE_CODE = "teacher";

    private static final List<String> HEADERS = List.of(
        "工号", "姓名", "性别", "所属学校", "手机号", "邮箱", "入职日期", "教育角色", "登录名", "备注"
    );

    /** 登录名长度上限：与 sys_user.user_name 的 @Size(max = 30) 基线一致，不另立规则 */
    private static final int LOGIN_NAME_MAX = 30;

    private static final Set<String> GENDERS = Set.of("男", "女");

    /** 教育角色：码值与中文都接受（原型与页面用中文，接口用下划线码值） */
    private static final Set<String> ROLE_VALUES = Set.of(
        "校领导", "教务主任", "年级主任", "班主任", "任课教师",
        "school_leader", "academic_director", "grade_leader", "homeroom", "subject_teacher"
    );

    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EduSchoolMapper schoolMapper;
    private final EduTeacherMapper teacherMapper;

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
        // 所属学校必须已存在；学校租户账号只能导入本校教师
        Map<String, Long> schoolIds = new HashMap<>();
        for (EduSchool school : schoolMapper.selectList(new LambdaQueryWrapper<EduSchool>())) {
            schoolIds.putIfAbsent(school.getSchoolName(), school.getSchoolId());
        }
        // 工号在同一学校内唯一：本批只校验「本校已有工号」与「文件内重复」
        Set<String> existingTeacherNos = new HashSet<>();
        for (EduTeacher teacher : teacherMapper.selectList(new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getSchoolId, context.getSchoolId()))) {
            if (teacher.getTeacherNo() != null) {
                existingTeacherNos.add(teacher.getTeacherNo());
            }
        }
        Set<String> seenTeacherNos = new HashSet<>();
        Set<String> seenLoginNames = new HashSet<>();
        for (EduImportRow row : rows) {
            List<String> reasons = new ArrayList<>();
            Map<String, String> cells = row.getCells();
            for (String required : List.of("工号", "姓名", "性别", "所属学校")) {
                if (StringUtils.isBlank(cells.get(required))) {
                    reasons.add(required + "不能为空");
                }
            }
            String teacherNo = cells.get("工号");
            if (StringUtils.isNotBlank(teacherNo)) {
                if (!seenTeacherNos.add(teacherNo)) {
                    reasons.add("文件内工号重复：" + teacherNo);
                }
                if (existingTeacherNos.contains(teacherNo)) {
                    reasons.add("工号在本校已存在：" + teacherNo);
                }
            }
            String gender = cells.get("性别");
            if (StringUtils.isNotBlank(gender) && !GENDERS.contains(gender)) {
                reasons.add("性别只能是男 / 女");
            }
            String schoolName = cells.get("所属学校");
            if (StringUtils.isNotBlank(schoolName)) {
                Long schoolId = schoolIds.get(schoolName);
                if (schoolId == null) {
                    reasons.add("所属学校不存在：" + schoolName);
                } else if (!schoolId.equals(context.getSchoolId())) {
                    reasons.add("只能导入本校教师；跨校教师请在目标学校账号下导入（BR-TEACHER-001）");
                }
            }
            String phone = cells.get("手机号");
            if (StringUtils.isNotBlank(phone) && !PHONE.matcher(phone).matches()) {
                reasons.add("手机号应为 11 位");
            }
            String email = cells.get("邮箱");
            if (StringUtils.isNotBlank(email) && !EMAIL.matcher(email).matches()) {
                reasons.add("邮箱格式不正确");
            }
            String hireDate = cells.get("入职日期");
            if (StringUtils.isNotBlank(hireDate) && DateUtils.parseDate(hireDate) == null) {
                reasons.add("入职日期格式应为 YYYY-MM-DD");
            }
            String loginName = StringUtils.trimToEmpty(cells.get("登录名"));
            if (StringUtils.isNotBlank(loginName)) {
                if (loginName.length() > LOGIN_NAME_MAX) {
                    reasons.add("登录名长度不能超过 " + LOGIN_NAME_MAX + " 个字符");
                }
                if (loginName.chars().anyMatch(Character::isWhitespace)) {
                    reasons.add("登录名不能包含空格");
                }
                if (!seenLoginNames.add(loginName)) {
                    reasons.add("文件内登录名重复：" + loginName);
                }
            }
            String roles = cells.get("教育角色");
            if (StringUtils.isNotBlank(roles)) {
                for (String role : roles.split("[,，、]")) {
                    String value = role.trim();
                    if (!value.isEmpty() && !ROLE_VALUES.contains(value)) {
                        reasons.add("教育角色取值不正确：" + value);
                    }
                }
            }
            if (!reasons.isEmpty()) {
                row.setFailReason(String.join("；", reasons));
            }
        }
    }
}
