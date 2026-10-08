package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件引用 edu_file_ref
 *
 * 数据库只保存文件引用，不保存二进制（REQ-IMP-041）；下载走短时签名链接并与登录态绑定
 * （REQ-IMP-042）；每次下载写审计（REQ-IMP-043）；引用带租户与学校前缀（REQ-IMP-046）。
 *
 * **命名提示**：物理列里 `id` 是主键、`file_id` 是业务上的文件标识（唯一），
 * 两者语义不同，因此主键字段叫 `refId`、业务标识字段叫 `fileId`。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：只有 `create_time`，
 * 因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_file_ref")
public class EduFileRef {

    /** 引用记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long refId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 文件 ID（业务标识，唯一） */
    private Long fileId;

    /** import_source / export_result / failed_rows / template / photo */
    private String fileKind;

    /** 原始文件名 */
    private String fileName;

    /** 对象存储键（含租户与学校前缀） */
    private String storageKey;

    /** MIME 类型 */
    private String contentType;

    /** 字节数 */
    private Long fileSize;

    /** 业务类型（import / export / task） */
    private String bizType;

    /** 业务标识（批次号 / 任务号） */
    private String bizId;

    /** 有效期（结果文件默认 7 天） */
    private Date expireTime;

    /** 下载次数 */
    private Integer downloadCount;

    /** 创建时间 */
    private Date createTime;

}
