package org.dromara.edu.job;

/**
 * 模块导入执行器。
 *
 * 校验在请求线程同步完成（{@link EduImportHandler}），执行阶段由异步任务驱动：
 * 引擎负责取数、计数、写 `edu_import_error` 与批次 / 任务状态，实现类只负责「把一行写进业务数据」。
 * 实现必须是幂等的：同一行重复执行要么成功要么返回 skipped，不能产生重复数据（BR-IMP-002）。
 *
 * @author Codex
 */
public interface EduImportExecutor {

    /** 模块编码（与 EduImportHandler 一致） */
    String moduleCode();

    /** 执行一行（该行已通过校验） */
    EduImportRowResult execute(EduImportContext context, EduImportRow row);
}
