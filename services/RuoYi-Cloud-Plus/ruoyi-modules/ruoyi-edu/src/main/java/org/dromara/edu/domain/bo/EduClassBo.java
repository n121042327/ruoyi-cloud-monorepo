package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduClass;

import java.util.List;

/**
 * 班级业务对象
 *
 * 查询参数与 PRD 6.1 的班级列表查询区逐条对齐；写入字段与 schema.yaml 的 edu_class 列一一对应。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduClass.class, reverseConvertGenerate = false)
public class EduClassBo extends BaseEntity {

    /** 班级 ID */
    private Long classId;

    /** 学校 */
    private Long schoolId;

    /** 校区 */
    private Long campusId;

    /** 学年学期 */
    private Long termId;

    /** 年级 */
    private Long gradeId;

    /** 学段 */
    private String stageCode;

    /** 班级名称 */
    @NotBlank(message = "班级名称不能为空")
    private String className;

    /** 班级类型：administrative / teaching */
    @NotBlank(message = "班级类型不能为空")
    private String classType;

    /** 容量上限（只提示不拦截） */
    private Integer classCapacity;

    /** 班主任 */
    private Long headTeacherId;

    /** 教室 */
    private String classroom;

    /** 教学班的组合 / 单学科标识 */
    private String subjectCombination;

    /** 班级状态：active / disabled */
    private String classStatus;

    /** 备注（非表字段：班级列表查询与新建弹窗的说明文本，落库前并入 classroom/remark 语义时再定） */
    private String remark;

    /** 关键字：班级名称 / 班主任姓名 / 教室 */
    private String keyword;

    /** 停用原因（disableClass 必填） */
    private String reason;

    /** 合并：源班级 ID 列表（mergeClass） */
    private List<Long> sourceClassIds;

    /** 合并 / 批量操作的目标班级 */
    private Long targetClassId;

    /** 生效日期（调班 / 迁学生 / 合并共用 effective_date） */
    private String effectiveDate;

    /** 批量新增的班级行（batchAddClass） */
    private List<EduClassBo> classList;

    /** 班主任任职开始日期（assignHeadTeacher 时在服务层校验必填） */
    private String headTeacherStartDate;

}
