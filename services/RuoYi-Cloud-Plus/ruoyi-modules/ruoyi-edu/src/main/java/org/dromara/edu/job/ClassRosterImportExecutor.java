package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduClassMember;
import org.dromara.edu.domain.EduStudent;
import org.dromara.edu.domain.bo.EduClassMemberBo;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduClassMemberMapper;
import org.dromara.edu.mapper.EduStudentMapper;
import org.dromara.edu.service.IEduClassService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 编班表导入执行器（模块编码 {@code class_roster}）。
 *
 * 写班级关系的唯一入口是班级服务（`DP-01`）：本执行器只做「行 → EduClassMemberBo」的翻译，
 * 冲突判定与落库复用 {@link IEduClassService#addRoster}，不自己写 SQL。
 * 幂等：同一学生已在本班 → skipped；已在别的行政班 → failed（BR-STU-003）。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class ClassRosterImportExecutor implements EduImportExecutor {

    private static final String MEMBER_IN = "1";
    private static final String TYPE_ADMINISTRATIVE = "administrative";
    private static final String TYPE_TEACHING = "teaching";

    private final EduStudentMapper studentMapper;
    private final EduClassMapper classMapper;
    private final EduClassMemberMapper classMemberMapper;
    private final IEduClassService classService;

    @Override
    public String moduleCode() {
        return ClassRosterImportHandler.MODULE_CODE;
    }

    @Override
    public EduImportRowResult execute(EduImportContext context, EduImportRow row) {
        Map<String, String> cells = row.getCells();
        String studentNo = StringUtils.trimToEmpty(cells.get("学号"));
        String className = StringUtils.trimToEmpty(cells.get("目标班级"));
        String classType = normalizeClassType(cells.get("班级类型"));
        if (StringUtils.isBlank(studentNo) || StringUtils.isBlank(className)) {
            return EduImportRowResult.failed("学号或目标班级为空");
        }
        EduStudent student = studentMapper.selectOne(new LambdaQueryWrapper<EduStudent>()
            .eq(EduStudent::getStudentNo, studentNo).last("limit 1"));
        if (student == null) {
            return EduImportRowResult.failed("学号不存在：" + studentNo);
        }
        EduClass clazz = classMapper.selectOne(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getSchoolId, context.getSchoolId())
            .eq(EduClass::getClassName, className)
            .eq(EduClass::getClassType, classType)
            .eq(context.getTermId() != null, EduClass::getTermId, context.getTermId())
            .last("limit 1"));
        if (clazz == null) {
            return EduImportRowResult.failed("目标班级不存在：" + className);
        }
        // 幂等：已在本班 → 跳过
        Long sameClass = classMemberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getClassId, clazz.getClassId())
            .eq(EduClassMember::getStudentId, student.getStudentId())
            .eq(EduClassMember::getStatus, MEMBER_IN));
        if (sameClass != null && sameClass > 0) {
            return EduImportRowResult.skipped("该学生已在本班，跳过");
        }
        // 已在别的行政班 → 失败（BR-STU-003 同一学年学期只能属于一个行政班）
        Long occupied = classMemberMapper.selectCount(new LambdaQueryWrapper<EduClassMember>()
            .eq(EduClassMember::getStudentId, student.getStudentId())
            .eq(EduClassMember::getStatus, MEMBER_IN));
        if (occupied != null && occupied > 0) {
            return EduImportRowResult.failed("该学生已有在班关系，同一学年学期只能属于一个行政班（BR-STU-003）；"
                + "如需换班请在班级管理里调班");
        }
        EduClassMemberBo bo = new EduClassMemberBo();
        bo.setClassId(clazz.getClassId());
        bo.setStudentIds(List.of(student.getStudentId()));
        try {
            classService.addRoster(bo);
            return EduImportRowResult.success();
        } catch (ServiceException e) {
            return EduImportRowResult.failed(e.getMessage());
        }
    }

    private String normalizeClassType(String value) {
        if (StringUtils.isBlank(value)) {
            return TYPE_ADMINISTRATIVE;
        }
        String type = value.trim();
        if ("行政班".equals(type)) {
            return TYPE_ADMINISTRATIVE;
        }
        if ("教学班".equals(type)) {
            return TYPE_TEACHING;
        }
        return type;
    }
}
