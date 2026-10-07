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
 * 字段与列名以唯一事实源 docs/40-detailed-design/database/schema.yaml 的 edu_student 为准：
 * 物理主键是 `id`（业务名按接口契约叫 studentId），表里没有 student_phone / address / photo_file_id，
 * 只有 photo_url；GAP-090 记录了字段字典与 schema.yaml 的这处冲突。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student")
public class EduStudent extends BaseEntity {

    /** 学生主体 ID（物理列 `id`；接口契约字段名 studentId） */
    @TableId(value = "id")
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

    /** 入学年份（学号前 4 位与此一致） */
    private Integer enrollYear;

    /** 毕业日期 */
    private Date graduationDate;

    /** 学籍照片引用（文件服务地址，GAP-027） */
    private String photoUrl;

    /** 备注 */
    private String remark;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
