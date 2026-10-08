package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;

/**
 * 选科相关业务对象（我的选科 / 清单 / 统计 / 未选科 / 教学班生成共用）
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduStreamSelectionBo extends BaseEntity {

    /** 学年学期 */
    private Long termId;

    /** 学校 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 首选科目编码（物理 physics / 历史 history） */
    private String primarySubjectCode;

    /** 再选科目编码集合 */
    private List<String> secondarySubjectCodes;

    /** 变更原因（截止后提交 / 变更时必填） */
    private String reason;

    /** 年级筛选 */
    private Long gradeId;

    /** 班级筛选 */
    private Long classId;

    /** 组合筛选 */
    private String combination;

    /** 状态筛选：已生效 / 待审批 / 未选择 */
    private String status;

    /** 关键字：姓名 / 学号 */
    private String keyword;

    /** 教学班生成方式：combination 按完整组合 / subject 按单学科 */
    private String generateMode;

    /** 教学班命名规则 */
    private String classNameRule;

    /** 生成批次号（幂等键，REQ-STR-057） */
    private String batchNo;

}
