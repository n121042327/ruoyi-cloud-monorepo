package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.bo.EduTeacherBo;
import org.dromara.edu.domain.bo.EduUserRoleBo;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.service.IEduTeacherService;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 教师导入执行器（模块编码 {@code teacher}）。
 *
 * 一行一个教师：幂等（工号在本校已存在 → skipped）→ 建档（复用 {@link IEduTeacherService#insertByBo}，
 * 与教师页新增同一条路径）→ 教育角色（{@link IEduTeacherService#saveRole}，模板的中文角色码值化）→
 * 账号（模板填了「登录名」才建，见 CR-156 / GAP-116）。
 *
 * **账号口径**：`BR-ACCOUNT-001` 规定「登录名由学校统一分配」，所以登录名由模板提供、本执行器不编排；
 * 初始密码由系统随机生成（12 位、去掉易混字符），随导入结果文件回填给发起人，结果文件 7 天后过期
 * （BR-IMP-013），首次登录后应立即改密（REQ-TCH-045）。登录名为空时只建档案，不建账号。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class TeacherImportExecutor implements EduImportExecutor {

    /** 教育角色中文 → 码值（与 EduTeacherServiceImpl / 前端 TeacherEnum 一致） */
    private static final Map<String, String> ROLE_CODES = Map.of(
        "校领导", "school_leader",
        "教务主任", "academic_director",
        "年级主任", "grade_leader",
        "班主任", "homeroom",
        "任课教师", "subject_teacher"
    );

    private static final String EMPLOYMENT_ACTIVE = "active";

    /** 登录名长度上限：与 sys_user.user_name 的 @Size(max = 30) 基线一致 */
    private static final int LOGIN_NAME_MAX = 30;

    /** 初始密码字符集（去掉易混字符 0/O/1/I，与学生激活码同一口径） */
    private static final char[] PASSWORD_ALPHABET =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".toCharArray();

    /** 初始密码长度 */
    private static final int PASSWORD_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final IEduTeacherService teacherService;
    private final EduTeacherMapper teacherMapper;

    @Override
    public String moduleCode() {
        return TeacherImportHandler.MODULE_CODE;
    }

    @Override
    public EduImportRowResult execute(EduImportContext context, EduImportRow row) {
        Map<String, String> cells = row.getCells();
        String teacherNo = StringUtils.trimToEmpty(cells.get("工号"));
        if (StringUtils.isBlank(teacherNo)) {
            return EduImportRowResult.failed("工号不能为空");
        }
        // 幂等：工号在本校已存在 → 跳过（BR-TEACHER-007）
        EduTeacher existed = teacherMapper.selectOne(new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getSchoolId, context.getSchoolId())
            .eq(EduTeacher::getTeacherNo, teacherNo)
            .last("limit 1"));
        if (existed != null) {
            return EduImportRowResult.skipped("工号已存在，跳过：" + teacherNo);
        }
        EduTeacherBo bo = new EduTeacherBo();
        bo.setSchoolId(context.getSchoolId());
        bo.setTeacherNo(teacherNo);
        bo.setTeacherName(StringUtils.trimToEmpty(cells.get("姓名")));
        bo.setGender(StringUtils.trimToEmpty(cells.get("性别")));
        bo.setPhone(StringUtils.trimToEmpty(cells.get("手机号")));
        bo.setEmail(StringUtils.trimToEmpty(cells.get("邮箱")));
        bo.setEmploymentStatus(EMPLOYMENT_ACTIVE);
        bo.setRemark(StringUtils.trimToEmpty(cells.get("备注")));
        // 选填：填了登录名就建账号，初始密码由系统生成（BR-ACCOUNT-001 要求登录名由学校分配，见 CR-156）
        String loginName = StringUtils.trimToEmpty(cells.get("登录名"));
        String initialPassword = null;
        if (StringUtils.isNotBlank(loginName)) {
            if (loginName.length() > LOGIN_NAME_MAX) {
                return EduImportRowResult.failed("登录名长度不能超过 " + LOGIN_NAME_MAX + " 个字符");
            }
            initialPassword = randomPassword();
            bo.setLoginName(loginName);
            bo.setPassword(initialPassword);
        }
        String hireDate = StringUtils.trimToEmpty(cells.get("入职日期"));
        if (StringUtils.isNotBlank(hireDate)) {
            bo.setHireDate(hireDate);
        }
        try {
            teacherService.insertByBo(bo);
        } catch (Exception e) {
            return EduImportRowResult.failed(e.getMessage());
        }
        EduTeacher created = teacherMapper.selectOne(new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getSchoolId, context.getSchoolId())
            .eq(EduTeacher::getTeacherNo, teacherNo)
            .last("limit 1"));
        if (created == null) {
            return EduImportRowResult.failed("教师建档后未查到记录：" + teacherNo);
        }
        for (String roleCode : roleCodes(cells.get("教育角色"))) {
            EduUserRoleBo role = new EduUserRoleBo();
            role.setSchoolId(context.getSchoolId());
            role.setTeacherId(created.getTeacherId());
            role.setEduRole(roleCode);
            try {
                teacherService.saveRole(created.getTeacherId(), role);
            } catch (Exception e) {
                return EduImportRowResult.failed("教师已建档，但教育角色写入失败：" + e.getMessage());
            }
        }
        Map<String, String> extras = new LinkedHashMap<>();
        if (initialPassword != null) {
            // 结果文件（对账表）回填登录名与初始密码，供发起人分发；文件 7 天后过期（BR-IMP-013）
            extras.put("登录名", loginName);
            extras.put("初始密码", initialPassword);
        }
        return EduImportRowResult.success(extras);
    }

    /** 系统随机生成初始密码（去掉易混字符，避免打印 / 手抄出错） */
    private String randomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_ALPHABET[RANDOM.nextInt(PASSWORD_ALPHABET.length)]);
        }
        return sb.toString();
    }

    /** 模板里的教育角色：中文或码值，多个用逗号 / 顿号分隔 */
    private Set<String> roleCodes(String value) {
        Set<String> codes = new LinkedHashSet<>();
        if (StringUtils.isBlank(value)) {
            return codes;
        }
        for (String item : value.split("[,，、]")) {
            String role = item.trim();
            if (role.isEmpty()) {
                continue;
            }
            codes.add(ROLE_CODES.getOrDefault(role, role));
        }
        return codes;
    }
}
