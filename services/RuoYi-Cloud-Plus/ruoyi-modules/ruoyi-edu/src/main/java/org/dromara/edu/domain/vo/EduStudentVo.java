package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStudent;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学生视图对象 edu_student
 *
 * 主键一律序列化为字符串（见 docs/10-prd/06-field-dictionary.yaml 的 id 说明）。
 * 证件号与联系电话默认掩码，明文经 viewStudentIdCard / viewStudentPhone 单独获取并写审计。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStudent.class)
public class EduStudentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentNo;

    private String nationalStudentNo;

    private String studentName;

    private String gender;

    /** 入学年份 */
    private Integer enrollYear;

    /** 毕业日期 */
    private Date graduationDate;

    private String stageCode;

    private Long gradeId;

    private String gradeName;

    private Long classId;

    private String className;

    private String enrollmentStatus;

    /** 证件号（掩码） */
    private String idCardNo;

    private String idType;

    private Date birthDate;

    /** 学籍照片引用（文件服务地址） */
    private String photoUrl;

    private Date updateTime;

    private String remark;

}
