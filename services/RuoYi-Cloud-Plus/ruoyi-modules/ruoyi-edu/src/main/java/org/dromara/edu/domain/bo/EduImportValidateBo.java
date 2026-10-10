package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 导入文件校验入参（validateImportFile）
 *
 * `导入向导固定四步：下载模板 → 上传文件 → 校验结果 → 确认执行`（REQ-IMP-001 / BR-IMP-001）；
 * 本入参对应第 2、3 步：文件已先上传到统一文件服务，这里只传 `file_id` 引用
 * （数据库只保存文件引用，不保存二进制，REQ-IMP-041 / NFR-DATA-03）。
 *
 * 字段名以 `docs/40-detailed-design/database/schema.yaml` 的列名为准（`file_id` /
 * `template_version` / `module_code`）；`targetTermId` / `targetClassId` / `strategy`
 * 是向导里的作业参数，落在 `edu_async_task.params_summary` 与执行器入参，不新增列。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduImportValidateBo extends BaseEntity {

    /** 模块（student / teacher / class_roster） */
    private String moduleCode;

    /** 模板版本，留空表示当前版本 */
    private String templateVersion;

    /** 上传文件引用（字符串形式，避免前端 Number() 精度丢失） */
    private String fileId;

    /** 上传文件名（用于结果摘要展示，可为空） */
    private String fileName;

    /** 目标学年学期（引用校验用，必须已存在，不自动创建　REQ-IMP-010） */
    private Long termId;

    /** 统一目标班级（填写后忽略文件里的目标班级列） */
    private Long targetClassId;

    /** 已存在数据的处理策略：skip 跳过 / overwrite 覆盖 / fail 记失败 */
    private String strategy;

    /**
     * 目标学校（GAP-115）：学校租户只有一所学校，留空即取本校；
     * 集团 / 运营方账号可管理多所学校，必须显式指定，且必须是本人数据范围内的学校 ——
     * 不允许「取第一个」，也不允许「留空由数据库默认」。
     */
    private Long schoolId;

}
