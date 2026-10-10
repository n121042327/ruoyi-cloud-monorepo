package org.dromara.edu.job;

import lombok.Data;

/**
 * 导入校验上下文。
 *
 * 来自 `edu_import_batch` 与校验入参：批次号 / 学校 / 目标学年学期 / 统一目标班级 / 重复处理策略。
 * 校验在请求线程内同步执行（REQ-IMP-005 / REQ-STU-053），因此这里可以用登录态数据范围；
 * 但实现类仍应优先用上下文里的学校与学期，避免隐式依赖。
 *
 * @author Codex
 */
@Data
public class EduImportContext {

    /** 批次号 */
    private String batchNo;

    /** 学校归属 */
    private Long schoolId;

    /** 模块编码（student / teacher / class_roster） */
    private String moduleCode;

    /** 目标学年学期 */
    private Long termId;

    /** 统一目标班级（填写后忽略文件里的班级列） */
    private Long targetClassId;

    /** 已存在数据的处理策略：skip / overwrite / fail */
    private String strategy;

    /** 源文件引用（edu_file_ref.file_id，即文件服务 ossId） */
    private String fileId;

    /** 源文件名 */
    private String fileName;
}
