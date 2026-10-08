package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduSchool;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学校视图对象
 *
 * campusCount / classCount / studentCount / teacherCount 为展示统计，由 getSchoolSummary
 * 或自定义 SQL 填充。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduSchool.class)
public class EduSchoolVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long schoolId;

    private String tenantId;

    private String schoolCode;

    private String schoolName;

    private String shortName;

    private String parentTenantId;

    private String rootTenantId;

    private String schoolType;

    private String address;

    private String phone;

    /** active 正常 / disabled 已停用 */
    private String schoolStatus;

    /** 校区数 */
    private Integer campusCount;

    /** 班级数 */
    private Integer classCount;

    /** 在读学生数 */
    private Integer studentCount;

    /** 教师数 */
    private Integer teacherCount;

    /** 是否当前租户的学校（getCurrentSchool / 列表标记本校） */
    private Boolean current;

    private Date updateTime;

}
