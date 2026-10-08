package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * 学生主体 edu_student
 *
 * 学生主体是平台级实体，不参与学校租户隔离（D-037）——因此继承 BaseEntity 而非 TenantEntity；
 * 学校侧读学生一律经在校记录（edu_student_enrollment）与班级关系（edu_class_member）两段式取数。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student")
public class EduStudent extends BaseEntity {

    /** 学生主体 ID */
    @TableId(value = "student_id")
    private Long studentId;

    /** 学号：平台唯一、系统统一发号、永不回收（BR-STU-001） */
    private String studentNo;

    /** 全国学籍号（可空；以 G / L 开头） */
    private String nationalStudentNo;

    /** 姓名 */
    private String studentName;

    /** 性别 */
    private String gender;

    /** 证件类型 */
    private String idType;

    /** 证件号（加密存储；平台唯一，非空时唯一，GAP-020） */
    private String idCardNo;

    /** 出生日期 */
    private Date birthDate;

    /** 联系电话（敏感字段，默认掩码展示） */
    private String studentPhone;

    /** 联系地址 */
    private String address;

    /** 学生照片文件引用（统一文件服务，GAP-027） */
    private Long photoFileId;

    /** 备注 */
    private String remark;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
