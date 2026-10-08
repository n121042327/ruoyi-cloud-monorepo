package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 教学班成员关系 edu_teaching_class_member
 *
 * 归班级管理模块写入（唯一写入入口）：由「按组合生成」触发（REQ-STR-056），手工调整也在班级模块，
 * 选科模块只触发与核对。成员来自选科结果，可跨行政班（BR-STU-004）。
 * 本表没有 del_flag：离开写 leave_date 并置 status='0'。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_teaching_class_member")
public class EduTeachingClassMember extends TenantEntity {

    /** 成员关系 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long memberId;

    /** 学校归属 */
    private Long schoolId;

    /** 教学班 */
    private Long teachingClassId;

    /** 学年学期 */
    private Long termId;

    /** 学生主体 ID */
    private Long studentId;

    /** generate 生成 / manual 手工调整 */
    private String source;

    /** 生成任务的幂等键（REQ-STR-057） */
    private String generateTaskNo;

    /** 加入日期 */
    private Date joinDate;

    /** 离开日期 */
    private Date leaveDate;

    /** 1 在班 / 0 已离开 */
    private String status;

}
