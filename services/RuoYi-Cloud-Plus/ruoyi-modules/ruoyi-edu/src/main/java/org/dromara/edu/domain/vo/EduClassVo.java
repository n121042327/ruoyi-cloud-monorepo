package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduClass;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 班级视图对象
 *
 * 主键一律序列化为字符串（见 06-field-dictionary.yaml 的 id 说明）。
 * gradeName / campusName / headTeacherName / enrolledCount 为展示字段，由带连接的
 * 自定义 SQL 或统计查询填充（阶段 7 后续批次补齐 EduClassMapper.xml）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduClass.class)
public class EduClassVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long classId;

    private Long schoolId;

    private String schoolName;

    private Long campusId;

    private String campusName;

    private Long termId;

    private String termName;

    private Long gradeId;

    private String gradeName;

    private String stageCode;

    private String className;

    private String classType;

    private Integer classCapacity;

    private Long headTeacherId;

    private String headTeacherName;

    private Date headTeacherStartDate;

    private Date headTeacherEndDate;

    private String classroom;

    private String subjectCombination;

    /** active 在读 / disabled 已停用 */
    private String classStatus;

    /** 在读人数（只提示不拦截，BR-CLASS-005） */
    private Integer enrolledCount;

    /** 容量（与 classCapacity 同值，前端列表按 capacity 展示） */
    private Integer capacity;

    /**
     * 在读人数：高保真原型 `class-list.html`「在读」列的 `data-field="student_count"`。
     *
     * 与 enrolledCount 同值（同一统计口径的两个消费名），由 `EduClassServiceImpl.fillAggregates`
     * 用 `edu_class_member.status = '1'` 实时统计（阶段 8 验收缺陷 GAP-118）。
     */
    private Integer studentCount;

    private String remark;

    private Date updateTime;

}
