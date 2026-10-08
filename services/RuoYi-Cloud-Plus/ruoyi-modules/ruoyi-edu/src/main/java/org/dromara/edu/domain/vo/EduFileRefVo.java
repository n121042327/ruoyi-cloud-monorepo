package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduFileRef;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件引用视图对象
 *
 * 下载接口返回的落点：文件元信息 + 短时签名链接描述（REQ-IMP-042）。
 * 签名链接由统一文件服务签发；本模块只负责范围校验、有效期判断与下载计数 / 审计留痕。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduFileRef.class)
public class EduFileRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 引用记录 ID */
    private Long refId;

    /** 文件 ID（业务标识） */
    private Long fileId;

    private String fileKind;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private String bizType;

    private String bizId;

    /** 有效期（过期后下载一律拒绝，REQ-IMP-040） */
    private Date expireTime;

    private Integer downloadCount;

    /** 短时签名下载地址（由统一文件服务签发） */
    private String signedUrl;

    /** 签名链接失效时间 */
    private Date signedUrlExpireTime;

    /**
     * 是否已过期。
     * 结果文件过期后下载一律拒绝（`BR-IMP-013`）；**模板**过期后仍可下载，
     * 由本字段与 `hint` 让页面给出强提示（`REQ-IMP-003`）。
     */
    private Boolean expired;

    /** 下载提示文案（模板版本过期时的强提示等） */
    private String hint;

}
