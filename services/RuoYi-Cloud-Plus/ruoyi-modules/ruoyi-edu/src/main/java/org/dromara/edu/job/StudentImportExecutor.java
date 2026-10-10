package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduGrade;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.bo.EduGuardianBo;
import org.dromara.edu.domain.bo.EduStudentBo;
import org.dromara.edu.mapper.EduGradeMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduStudentProfileService;
import org.dromara.edu.service.IEduStudentService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 学生导入执行器（模块编码 {@code student}）。
 *
 * 一行一个学生：幂等去重（证件号 / 全国学籍号已存在 → skipped）→ 建档 + 发号 + 在校记录
 * （复用 {@link IEduStudentService#insertWithEnrollment}，不在执行器里写 SQL）→ 监护人
 * （{@link IEduStudentProfileService#saveGuardian}，按姓名 + 电话在库里去重）。
 *
 * 行政班不在本执行器写：导入产生的学生默认不带行政班（REQ-STU-062），编班走班级模块（DP-01）。
 * 「统一目标班级」由用户在确认页选择时，编班动作同样交给班级服务（后续批次接入）。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class StudentImportExecutor implements EduImportExecutor {

    private final IEduStudentService studentService;
    private final IEduStudentProfileService profileService;
    private final EduStudentMapper studentMapper;
    private final EduGradeMapper gradeMapper;

    @Override
    public String moduleCode() {
        return StudentImportHandler.MODULE_CODE;
    }

    @Override
    public EduImportRowResult execute(EduImportContext context, EduImportRow row) {
        Map<String, String> cells = row.getCells();
        String idCardNo = StringUtils.trimToEmpty(cells.get("证件号码"));
        String nationalNo = StringUtils.trimToEmpty(cells.get("全国学籍号"));
        // 幂等：平台唯一（id_card_no / national_student_no 非空唯一，BR-STU-008 / 022）
        if (StringUtils.isNotBlank(idCardNo) && exists("idCardNo", idCardNo)) {
            return EduImportRowResult.skipped("证件号已存在，跳过：" + idCardNo);
        }
        if (StringUtils.isNotBlank(nationalNo) && exists("nationalStudentNo", nationalNo)) {
            return EduImportRowResult.skipped("全国学籍号已存在，跳过：" + nationalNo);
        }
        EduStudentBo bo = new EduStudentBo();
        bo.setSchoolId(context.getSchoolId());
        bo.setTermId(context.getTermId());
        bo.setStudentName(StringUtils.trimToEmpty(cells.get("姓名")));
        bo.setGender(StringUtils.trimToEmpty(cells.get("性别")));
        bo.setIdType(StringUtils.trimToEmpty(cells.get("证件类型")));
        bo.setIdCardNo(idCardNo);
        bo.setNationalStudentNo(nationalNo);
        bo.setRemark(StringUtils.trimToEmpty(cells.get("备注")));
        String year = StringUtils.trimToEmpty(cells.get("入学年份"));
        if (StringUtils.isNotBlank(year)) {
            bo.setEnrollYear(Integer.valueOf(year));
        }
        String birthDate = StringUtils.trimToEmpty(cells.get("出生日期"));
        if (StringUtils.isNotBlank(birthDate)) {
            bo.setBirthDate(org.dromara.common.core.utils.DateUtils.parseDate(birthDate));
        }
        // 年级：模板里是名称，转成 entry_grade_id 记入在校记录
        bo.setGradeId(gradeId(context.getSchoolId(), StringUtils.trimToEmpty(cells.get("年级"))));
        Long studentId;
        try {
            studentId = studentService.insertWithEnrollment(bo);
        } catch (Exception e) {
            return EduImportRowResult.failed(e.getMessage());
        }
        if (studentId == null) {
            return EduImportRowResult.failed("学生建档失败");
        }
        // 监护人：模板给了姓名才写；电话可为空
        String guardianName = StringUtils.trimToEmpty(cells.get("监护人姓名"));
        if (StringUtils.isNotBlank(guardianName)) {
            EduGuardianBo guardian = new EduGuardianBo();
            guardian.setStudentId(studentId);
            guardian.setGuardianName(guardianName);
            guardian.setGuardianPhone(StringUtils.trimToEmpty(cells.get("监护人电话")));
            guardian.setRelation(StringUtils.trimToEmpty(cells.get("与监护人关系")));
            guardian.setRemark(StringUtils.trimToEmpty(cells.get("联系地址")));
            guardian.setIsPrimary(Boolean.TRUE);
            guardian.setSource("import");
            try {
                profileService.saveGuardian(studentId, guardian);
            } catch (Exception e) {
                // 学生已建档，监护人失败只记失败原因，不回滚学生（导入结果里能看见是哪一行）
                return EduImportRowResult.failed("学生已建档，但监护人写入失败：" + e.getMessage());
            }
        }
        return EduImportRowResult.success(java.util.Map.of("学号", studentService.queryById(studentId).getStudentNo()));
    }

    private boolean exists(String field, String value) {
        LambdaQueryWrapper<EduStudent> wrapper = new LambdaQueryWrapper<>();
        if ("idCardNo".equals(field)) {
            wrapper.eq(EduStudent::getIdCardNo, value);
        } else {
            wrapper.eq(EduStudent::getNationalStudentNo, value);
        }
        Long count = studentMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private Long gradeId(Long schoolId, String gradeName) {
        if (StringUtils.isBlank(gradeName)) {
            return null;
        }
        Map<String, Long> grades = new HashMap<>();
        List<EduGrade> list = gradeMapper.selectList(new LambdaQueryWrapper<EduGrade>()
            .eq(EduGrade::getSchoolId, schoolId));
        for (EduGrade grade : list) {
            if (grade.getGradeName() != null) {
                grades.putIfAbsent(grade.getGradeName(), grade.getGradeId());
            }
        }
        return grades.get(gradeName);
    }
}
