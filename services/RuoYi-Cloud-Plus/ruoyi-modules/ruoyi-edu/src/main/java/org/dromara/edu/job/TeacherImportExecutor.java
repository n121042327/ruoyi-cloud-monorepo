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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 教师导入执行器（模块编码 {@code teacher}）。
 *
 * 一行一个教师：幂等（工号在本校已存在 → skipped）→ 建档（复用 {@link IEduTeacherService#insertByBo}，
 * 与教师页新增同一条路径）→ 教育角色（{@link IEduTeacherService#saveRole}，模板的中文角色码值化）。
 *
 * **账号不在本执行器创建**：导入模板没有「登录名」列，而 `BR-ACCOUNT-001` 规定「登录名由学校统一分配」，
 * 初始密码也没有依据可循 —— 因此不自行编排规则，登记 GAP-116；导入出来的教师需要管理员在教师页
 * 「重置密码 / 开通账号」后再使用（参见 REQ-TCH-018 与 REQ-TCH-045）。
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
        return EduImportRowResult.success();
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
