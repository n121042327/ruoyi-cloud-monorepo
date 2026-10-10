package org.dromara.resource.api;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.List;

/**
 * 文件服务(降级处理)
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteFileServiceMock implements RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @Override
    public RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    @Override
    public String selectUrlByIds(String ossIds) {
        log.warn("服务调用异常 -> 降级处理");
        return StringUtils.EMPTY;
    }

    /**
     * 按 ossId 签发短时预签名下载链接（服务不可用时降级返回空）
     */
    @Override
    public String signedDownloadUrl(String ossId, long ttlSeconds) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    @Override
    public List<RemoteFile> selectByIds(String ossIds) {
        log.warn("服务调用异常 -> 降级处理");
        return List.of();
    }

    /**
     * 按文件地址读取文件字节（降级）
     *
     * @param url 文件地址
     * @return 空数组
     */
    @Override
    public byte[] downloadByUrl(String url) {
        log.warn("服务调用异常 -> 降级处理");
        return new byte[0];
    }

}
