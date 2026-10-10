package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduClassMember;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 班级成员（花名册）视图对象
 *
 * studentNo / studentName / enrollmentStatus / currentClassName 来自学生主体与在校记录，
 * 由自定义 SQL 连接填充（阶段 7 后续批次补齐 EduClassMemberMapper.xml）；本批先返回关系表字段。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduClassMember.class)
public class EduClassMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private Long termId;

    private Long classId;

    private String className;

    private String classType;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String gender;

    /** 学籍状态（来自在校记录） */
    private String enrollmentStatus;

    /** 当前行政班（两段式取数的第二段） */
    private Long currentClassId;

    private String currentClassName;

    private Date joinDate;

    private Date leaveDate;

    /** 1 在班 / 0 已离开 */
    private String status;

    /** 加入校验结论（PAGE-CLS-ROSTER-ADD 的「加入校验」列） */
    private String joinCheck;

    /** 迁移校验结果（PAGE-CLS-MOVE 的「校验结果」列） */
    private String moveCheck;

    /**
     * 监护人姓名（花名册「监护人」列，原型 PAGE-CLS-DETAIL）。
     *
     * 取值：主监护人优先，没有主监护人时取第一条绑定关系（GAP-104）。
     */
    private String guardianName;

    /** 监护人联系电话（**默认掩码**，保留前 3 后 4；全量查看走学生详情的敏感数据访问日志，REQ-AUD-009） */
    private String guardianPhone;

}
