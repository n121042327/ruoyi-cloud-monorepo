package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 选科清单视图对象（与前端 StreamSelectionVO 对齐）
 *
 * @author Codex
 */
@Data
public class EduStreamSelectionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long gradeId;

    private String gradeName;

    private Long classId;

    private String className;

    private String primarySubjectCode;

    private String primarySubjectName;

    private List<String> secondarySubjectCodes;

    private List<String> secondarySubjectNames;

    /** 组合展示名 */
    private String combination;

    /** 已生效 / 待审批 / 未选择 */
    private String status;

}
