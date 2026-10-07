package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAcademicYear;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学年视图对象
 *
 * termCount 与 currentTermName 为展示字段：前者是该学年下的学期数，后者是当前学期名称，
 * 由自定义 SQL 或服务层组装填充（EduAcademicYearMapper.xml 在后续批次补齐）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAcademicYear.class)
public class EduAcademicYearVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long academicYearId;

    private Long schoolId;

    private String schoolName;

    private String academicYearCode;

    private Date startDate;

    private Date endDate;

    /** 学期数 */
    private Integer termCount;

    /** 当前学期名称（同一学校唯一） */
    private String currentTermName;

    /** 当前学期 ID（前端「设为当前」用） */
    private Long currentTermId;

    /** normal / 进行中 / archived */
    private String academicYearStatus;

    private Date updateTime;

}
