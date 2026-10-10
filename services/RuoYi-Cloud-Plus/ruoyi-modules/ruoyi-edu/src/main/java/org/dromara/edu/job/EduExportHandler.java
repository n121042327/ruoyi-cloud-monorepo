package org.dromara.edu.job;

/**
 * 模块导出器。
 *
 * 导出引擎（文件生成、上传、结果文件登记、进度与审计）由导入导出模块统一负责，
 * 业务模块只声明「本模块默认导出哪些列、怎么取数」（REQ-IMP-002 / GAP-086 裁决 A）。
 * 新增一个可导出模块 = 新增一个本接口的实现，编码用下划线风格。
 *
 * **数据范围**：导出在后台线程执行，没有登录态，MyBatis 的数据权限插件不会生效。
 * 因此实现类必须自己按 {@link EduExportContext#getSchoolId()} 过滤学校级数据，
 * 并在学校上下文缺失时抛错拒绝导出（宁可失败，也不能退回「全平台」）。
 *
 * @author Codex
 */
public interface EduExportHandler {

    /** 模块编码（与 {@code EduExportBo.moduleCode} 一致） */
    String moduleCode();

    /** 生成导出文件 */
    EduExportedFile export(EduExportContext context);
}
