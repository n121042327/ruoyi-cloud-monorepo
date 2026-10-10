package org.dromara.edu.job;

import lombok.Data;

import java.util.Map;

/**
 * 导出执行上下文。
 *
 * 由 {@code edu_async_task.params_summary} 解析而来：参数在发起导出时就随任务落库
 * （REQ-IMP-025：导出前重新解析数据范围，不复用列表页判定）。
 *
 * @author Codex
 */
@Data
public class EduExportContext {

    /** 任务号 */
    private String taskNo;

    /** 学校归属（导出审计与文件引用都用它） */
    private Long schoolId;

    /** 模块编码（下划线风格，如 teaching_assignment） */
    private String moduleCode;

    /** 导出格式：xlsx / csv */
    private String format;

    private Long termId;

    private Long gradeId;

    private Long classId;

    private Long studentId;

    private String keyword;

    /** 附加筛选条件（如 teaching_assignment 的 teacherId） */
    private Map<String, Object> filters;
}
