package org.dromara.resource.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.List;

/**
 * 文件服务
 *
 * @author Lion Li
 */
public interface RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) throws ServiceException;

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    String selectUrlByIds(String ossIds);

    /**
     * 按文件地址读取文件字节
     *
     * 教育域「查看学生照片原图」需要二进制内容（前端 `getStudentPhoto` 用 `responseType: 'blob'`），
     * 而 `edu_student.photo_url` 只存文件地址，所以由文件服务按地址把字节取回来（CR-095）。
     * 调用方负责权限校验、数据范围判定与敏感数据访问留痕。
     *
     * @param url 文件地址（对象存储的完整 URL）
     * @return 文件字节
     */
    byte[] downloadByUrl(String url) throws ServiceException;

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    List<RemoteFile> selectByIds(String ossIds);
}
