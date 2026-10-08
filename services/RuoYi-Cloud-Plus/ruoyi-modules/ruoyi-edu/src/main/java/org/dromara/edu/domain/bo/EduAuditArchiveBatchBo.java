package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduAuditArchiveBatch;

/**
 * 日志归档批次业务对象
 *
 * 覆盖 `listArchiveBatch`（归档批次列表）与 `searchArchivedLog`（归档区间检索）的入参：
 * 归档后仍可按时间范围检索（`REQ-AUD-033`），**归档检索重新解析数据范围**（`DS-DENY-04`）。
 *
 * 时间用字符串接收，服务层用 `DateUtils.parseDate` 解析。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduAuditArchiveBatch.class, reverseConvertGenerate = false)
public class EduAuditArchiveBatchBo extends BaseEntity {

    /** 归档批次 ID */
    private Long archiveId;

    /** 归档批次号 */
    private String archiveNo;

    /** running 进行中 / done 已完成 / failed 失败 */
    private String archiveStatus;

    /** 归档范围开始（含） */
    @AutoMapping(target = "rangeStart", ignore = true)
    private String rangeStart;

    /** 归档范围结束（含） */
    @AutoMapping(target = "rangeEnd", ignore = true)
    private String rangeEnd;

    /** 归档检索：操作类型（searchArchivedLog） */
    private String actionType;

    /** 归档检索：对象类型 */
    private String objectType;

    /** 归档检索：对象标识 */
    private String objectId;

    /** 归档检索：操作人 */
    private Long operatorId;

    /** 关键字：对象标识 / 对象名称模糊匹配 */
    private String keyword;

}
