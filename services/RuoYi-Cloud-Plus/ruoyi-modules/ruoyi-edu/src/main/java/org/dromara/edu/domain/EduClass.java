package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 班级（行政班 / 教学班容器）edu_class
 *
 * 行政班与教学班共用本表，由 classType 区分：行政班设班主任、参与 DS-06 解析；教学班不设班主任、
 * 不参与 DS-06（BR-CLASS-007 / REQ-CLS-039）。容量只提示不拦截（BR-CLASS-005）；
 * 有在读学生时不允许删除、只允许停用（BR-CLASS-006）。
 *
 * 字段以唯一事实源 docs/40-detailed-design/database/schema.yaml 的 edu_class 为准；
 * 物理主键是 `id`，业务名按接口契约叫 classId。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_class")
public class EduClass extends TenantEntity {

    /** 班级 ID（物理列 `id`；接口契约字段名 classId） */
    @TableId(value = "id")
    private Long classId;

    /** 学校归属（scope: school） */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 年级（教学班按组合生成时可空；行政班必填） */
    private Long gradeId;

    /** 学段 */
    private String stageCode;

    /** 班级名称 */
    private String className;

    /** 班级类型：administrative / teaching */
    private String classType;

    /** 容量上限（只提示不拦截） */
    private Integer classCapacity;

    /** 班主任（行政班唯一在任；教学班为空） */
    private Long headTeacherId;

    /** 班主任任职开始日期 */
    private Date headTeacherStartDate;

    /** 班主任任职结束日期（学年切换时保留历史） */
    private Date headTeacherEndDate;

    /** 校区（参考信息，不参与权限判定） */
    private Long campusId;

    /** 教室（自由文本） */
    private String classroom;

    /** 教学班的组合 / 单学科标识（教学班使用） */
    private String subjectCombination;

    /** 班级状态：active 在读 / disabled 已停用 */
    private String classStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
