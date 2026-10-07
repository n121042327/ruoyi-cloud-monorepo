package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTerm;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学期视图对象
 *
 * classCount / studentCount 为统计展示字段（学期管理页展示「班级数 / 在读学生」），
 * 由统计查询填充，阶段 7 后续批次补齐。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTerm.class)
public class EduTermVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long termId;

    private Long schoolId;

    private String schoolName;

    private Long academicYearId;

    private String academicYearName;

    private String termCode;

    private String termName;

    private Date startDate;

    private Date endDate;

    /** 是否当前学期：1 / 0 */
    private String isCurrent;

    /** 是否当前学期（布尔视图，前端按 current 判断） */
    private Boolean current;

    /** normal / archived */
    private String termStatus;

    /** 班级数（统计展示） */
    private Integer classCount;

    /** 在读学生数（统计展示） */
    private Integer studentCount;

    private Date updateTime;

}
