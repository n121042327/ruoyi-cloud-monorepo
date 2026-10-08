package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduSubject;

import java.util.List;

/**
 * 学科业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduSubject.class, reverseConvertGenerate = false)
public class EduSubjectBo extends BaseEntity {

    /** 学科 ID */
    private Long subjectId;

    /** 学校 */
    private Long schoolId;

    /** 学科编码（校内唯一） */
    @NotBlank(message = "学科编码不能为空")
    private String subjectCode;

    /** 学科名称 */
    @NotBlank(message = "学科名称不能为空")
    private String subjectName;

    /** 排序号 */
    private Integer sortNo;

    /** 是否参与 3+1+2：1 / 0 */
    private String streamEnabled;

    /** 选科角色：primary / secondary / none */
    private String streamRole;

    /** 学科状态 */
    private String subjectStatus;

    /** 关键字：学科名称 / 编码 */
    private String keyword;

    /** 学段筛选（列表查询；来自 edu_subject_stage） */
    private String stageCode;

    /** 选科角色筛选 */
    private String filterStreamRole;

    /** 状态筛选 */
    private String filterStatus;

    /** 启用的学段集合（saveSubjectStage / 新增时一并写入） */
    private List<String> stageCodes;

    /** 停用 / 删除原因（至少 5 个字） */
    private String reason;

    /** 批量初始化：目标学段集合（batchInitSubject） */
    private List<String> initStageCodes;

}
