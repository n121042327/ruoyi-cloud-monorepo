package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未选科学生（催办清单，与前端 UnselectedStudentVO 对齐）
 *
 * @author Codex
 */
@Data
public class EduUnselectedStudentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String gradeName;

    private String className;

}
