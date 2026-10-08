package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;

/**
 * 导出入参（exportData 与各模块导出端点共用）
 *
 * 口径：导出按**当前筛选条件与当前用户数据范围**生成文件（REQ-IMP-024），
 * **导出前重新解析数据范围**，不复用列表页的判定结果（REQ-IMP-025 / DS-DENY-04）；
 * 格式支持 xlsx 与 csv，默认 xlsx（REQ-IMP-026）；导出列可选（REQ-IMP-027）；
 * 敏感字段默认掩码，明文导出需 `read_sensitive` 且逐次写审计（REQ-IMP-028 / BR-IMP-012）；
 * 导出行数 ≤ 2000 同步下载，超过转异步任务（REQ-IMP-029 / NFR-PERF-05）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduExportBo extends BaseEntity {

    /** 导出模块（student / teacher / class / class_roster / grade / school / promotion / stream / activation_code） */
    private String moduleCode;

    /** 导出格式：xlsx（默认）/ csv */
    private String format;

    /** 导出列；留空表示与列表页默认列一致（REQ-IMP-027） */
    private List<String> columns;

    /** 是否导出敏感字段明文（需 read_sensitive，REQ-IMP-028） */
    private Boolean plainText;

    /** 关键字筛选（透传给模块导出器） */
    private String keyword;

    /** 学年学期筛选 */
    private Long termId;

    /** 年级筛选 */
    private Long gradeId;

    /** 班级筛选 */
    private Long classId;

    /** 学生主体筛选（导出学生相关清单时使用） */
    private Long studentId;

    /** 其他筛选条件的 JSON 文本，由模块导出器解释 */
    private String filters;

}
