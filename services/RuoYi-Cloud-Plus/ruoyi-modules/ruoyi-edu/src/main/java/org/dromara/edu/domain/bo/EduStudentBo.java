package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduStudent;

import java.util.Date;

/**
 * 学生业务对象 edu_student
 *
 * 查询参数与 PRD「8.1 listStudent 查询参数」逐条对齐（CR-035 / GAP-066）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduStudent.class, reverseConvertGenerate = false)
public class EduStudentBo extends BaseEntity {

    /** 学生 ID */
    private Long studentId;

    /** 学号 */
    private String studentNo;

    /** 姓名 */
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    /** 全国学籍号 */
    private String nationalStudentNo;

    /** 性别 */
    @NotBlank(message = "性别不能为空")
    private String gender;

    /** 入学年份（必填；学号前 4 位与此一致） */
    private Integer enrollYear;

    /** 毕业日期 */
    private Date graduationDate;

    /** 学校上下文（学校用户固定本校，平台运营与超级管理员可切换） */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 年级 */
    private Long gradeId;

    /** 班级 */
    private Long classId;

    /** 学籍状态，多值以逗号分隔 */
    private String enrollmentStatus;

    /** 学段 */
    private String stageCode;

    /** 关键字：学号 / 姓名 / 全国学籍号 */
    private String keyword;

    /** 证件号后四位（需 person.student:read_sensitive） */
    private String idCardSuffix;

    /** 排序字段：studentNo / studentName / enrollYear / className / updateTime */
    private String sortBy;

    /** 排序方向：asc / desc */
    private String sortOrder;

    /** 证件类型 */
    private String idType;

    /** 证件号 */
    private String idCardNo;

    /** 出生日期 */
    private Date birthDate;

    /** 备注 */
    private String remark;

    /** 重置学生账号密码时的新密码（明文，provider 侧加密落库，GAP-091） */
    private String password;

    /** 需要填原因的操作（删除 / 重置等）填写的说明 */
    private String reason;

}
