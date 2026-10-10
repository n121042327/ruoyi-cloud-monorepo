package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.mapper.EduClassMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 把导出任务里的数据范围换算成「班级 ID 列表」（GAP-114）。
 *
 * 导出在后台线程执行，没有登录态，数据权限插件不生效（D-218 / D-224）；范围必须在发起导出时解析并随任务落库。
 * 负责人可能是班维度的（班主任：classIds），也可能是年级维度的（年级主任：gradeIds）——
 * 后者按「该年级下的行政班」展开，导出器统一按班级 ID 过滤即可。
 *
 * 返回空列表表示「本校全量」（与既有口径一致）。
 *
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class EduExportScopeResolver {

    private final EduClassMapper classMapper;

    /** 生效的班级范围：classIds 优先；只有 gradeIds 时展开为该年级下的行政班 */
    public List<Long> effectiveClassIds(EduExportContext context) {
        if (context == null) {
            return List.of();
        }
        if (context.getClassIds() != null && !context.getClassIds().isEmpty()) {
            return context.getClassIds();
        }
        if (context.getGradeIds() == null || context.getGradeIds().isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (EduClass clazz : classMapper.selectList(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getSchoolId, context.getSchoolId())
            .in(EduClass::getGradeId, context.getGradeIds()))) {
            if (clazz.getClassId() != null) {
                result.add(clazz.getClassId());
            }
        }
        return result;
    }
}
