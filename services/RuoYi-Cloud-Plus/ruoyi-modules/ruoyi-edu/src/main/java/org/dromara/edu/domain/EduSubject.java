package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 学科 edu_subject
 *
 * 「一条学科主体 + 学段启用表」（RV-SUB-04）：编码校内唯一，同一学科在不同学段启用只加启用记录，
 * 不拆多条主体。删除前检查三类引用（任教关系 / 教学班 / 学生选科），有引用时只允许停用。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_subject")
public class EduSubject extends TenantEntity {

    /** 学科 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long subjectId;

    /** 学校归属 */
    private Long schoolId;

    /** 学科编码（校内唯一，BR-SUBJECT-001） */
    private String subjectCode;

    /** 学科名称 */
    private String subjectName;

    /** 排序号（决定再选科目的展示顺序，REQ-STR-017） */
    private Integer sortNo;

    /** 是否参与 3+1+2：1 参与 / 0 不参与 */
    private String streamEnabled;

    /** 选科角色：primary 首选 / secondary 再选 / none 不参与 */
    private String streamRole;

    /** 学科状态：active 正常 / disabled 已停用 */
    private String subjectStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
