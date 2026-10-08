package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.bo.EduImportErrorBo;
import org.dromara.edu.domain.bo.EduImportExecuteBo;
import org.dromara.edu.domain.bo.EduImportTemplateBo;
import org.dromara.edu.domain.bo.EduImportValidateBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.domain.vo.EduImportErrorVo;
import org.dromara.edu.domain.vo.EduImportExecuteResultVo;
import org.dromara.edu.domain.vo.EduImportTemplateVo;
import org.dromara.edu.domain.vo.EduImportValidateResultVo;

/**
 * 导入导出引擎服务层
 *
 * 覆盖 import-export 模块的引擎 7 个 operationId：listImportTemplate / downloadImportTemplate /
 * validateImportFile / executeImport / downloadImportFailedRows / downloadImportResult / exportData。
 *
 * 各业务模块的导出端点（exportStudent / exportTeacher / exportClass 等）与班级花名册导入
 * （importRosterValidate / importRosterExecute）都落到本服务，避免每个模块各写一套导入导出。
 *
 * @author Codex
 */
public interface IEduImportExportService {

    /**
     * 分页查询导入模板清单与当前版本
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 模板分页结果
     */
    TableDataInfo<EduImportTemplateVo> queryTemplatePageList(EduImportTemplateBo query, PageQuery pageQuery);

    /**
     * 模板下载（旧版本仍可下载，过期给强提示，REQ-IMP-003）
     *
     * @param moduleCode 模块（路径参数 {module}）
     * @param version    模板版本，留空取当前版本
     * @return 模板文件引用 + 短时签名链接描述
     */
    EduFileRefVo resolveTemplateDownload(String moduleCode, String version);

    /**
     * 上传并同步校验（校验阶段不写业务数据，REQ-IMP-014）
     *
     * @param bo 校验入参
     * @return 校验结果（含批次号、三类计数、失败分组与指引）
     */
    EduImportValidateResultVo validateImportFile(EduImportValidateBo bo);

    /**
     * 确认执行（异步），按批次号幂等（REQ-IMP-015 / 017）
     *
     * @param bo 执行入参
     * @return 执行结果（批次号 + 任务编号）
     */
    EduImportExecuteResultVo executeImport(EduImportExecuteBo bo);

    /**
     * 下载批次文件：失败行 CSV 或结果摘要与学号对照表（REQ-IMP-008 / 020 / 023）
     *
     * @param batchNo 批次号
     * @param kind    failed_rows 失败行 / result 结果摘要与对照表
     * @return 文件引用 + 短时签名链接描述
     */
    EduFileRefVo resolveBatchFile(String batchNo, String kind);

    /**
     * 分页查询批次行结果（校验结果分页展示，REQ-IMP-006）
     *
     * @param query     查询条件（必须含 batchNo）
     * @param pageQuery 分页参数
     * @return 行结果分页数据
     */
    TableDataInfo<EduImportErrorVo> queryBatchRowPageList(EduImportErrorBo query, PageQuery pageQuery);

    /**
     * 导出（同步或异步，REQ-IMP-024 ~ 031）
     *
     * @param bo 导出入参
     * @return 导出结果（异步任务编号，或同步文件）
     */
    EduExportResultVo exportData(EduExportBo bo);

}
