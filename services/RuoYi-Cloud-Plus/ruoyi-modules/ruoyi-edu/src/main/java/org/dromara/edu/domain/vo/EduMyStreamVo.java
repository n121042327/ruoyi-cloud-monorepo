package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMapping;
import io.github.linpeilie.annotations.ReverseAutoMapping;
import lombok.Data;
import org.dromara.edu.domain.EduStudentStream;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 我的选科视图对象（与前端 MyStreamVO 对齐）
 *
 * 组合展示名由 primarySubjectCode + secondarySubjectCodes 派生（CR-016），库里不拼字符串。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStudentStream.class)
public class EduMyStreamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long streamId;

    private Long termId;

    private String termName;

    private String primarySubjectCode;

    private String primarySubjectName;

    /**
     * 再选科目编码列表。
     * 实体侧是逗号分隔的 {@code String}，展示侧拆成 {@code List<String>}；
     * 两个方向都忽略该字段，避免 MapStruct Plus 生成 String ↔ List 的错误映射，
     * 实际转换在 EduStreamServiceImpl 中手工完成。
     */
    @AutoMapping(target = "secondarySubjectCodes", ignore = true)
    @ReverseAutoMapping(target = "secondarySubjectCodes", ignore = true)
    private List<String> secondarySubjectCodes;

    private List<String> secondarySubjectNames;

    /** 组合展示名，如「物理 + 化学 + 生物」 */
    private String combination;

    /** 已生效 / 待审批 / 未选择 */
    private String status;

    private Date effectiveDate;

    /** 教学班归属（展示用；行政班不变） */
    private List<String> teachingClassNames;

}
