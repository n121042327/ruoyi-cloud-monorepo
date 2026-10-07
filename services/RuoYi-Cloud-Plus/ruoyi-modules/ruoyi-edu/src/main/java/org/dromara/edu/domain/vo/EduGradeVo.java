package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduGrade;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 年级视图对象
 *
 * 主键序列化为字符串；schoolName 为展示字段，由自定义 SQL 或前端字典填充。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduGrade.class)
public class EduGradeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long gradeId;

    private Long schoolId;

    private String schoolName;

    private String stageCode;

    private Integer enrollYear;

    private Integer gradeLevel;

    private String gradeName;

    private Integer classCount;

    private Integer studentCount;

    /** normal 正常 / archived 已归档 */
    private String gradeStatus;

    private Date updateTime;

}
