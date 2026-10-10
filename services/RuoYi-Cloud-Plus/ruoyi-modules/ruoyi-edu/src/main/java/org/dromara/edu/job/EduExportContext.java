package org.dromara.edu.job;

import lombok.Data;

import java.util.List;
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

    /**
     * 发起导出时解析出的数据范围（GAP-114）：年级主任看本年级、班主任看本班、任课教师看本人任教班级。
     *
     * 为空表示「本校全量」（与既有口径一致）；非空时导出器必须按它过滤 —— 后台执行没有登录态，
     * 数据权限插件不生效（D-218 / D-224）。
     */
    private List<Long> gradeIds;

    private List<Long> classIds;
}
