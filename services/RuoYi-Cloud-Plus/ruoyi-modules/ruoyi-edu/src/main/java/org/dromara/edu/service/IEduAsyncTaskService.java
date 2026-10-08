package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduAsyncTaskBo;
import org.dromara.edu.domain.bo.EduDeadLetterTaskBo;
import org.dromara.edu.domain.vo.EduAsyncTaskVo;
import org.dromara.edu.domain.vo.EduDeadLetterTaskVo;
import org.dromara.edu.domain.vo.EduFileRefVo;

/**
 * 异步任务服务层
 *
 * 覆盖 import-export 模块的异步任务中心 7 个 operationId：
 * listAsyncTask / getAsyncTask / cancelAsyncTask / retryAsyncTask / downloadTaskResult /
 * listDeadLetterTask / replayDeadLetterTask。
 *
 * 另外提供两个**非契约**内部方法，给导入导出执行器调用（它们本身不对应端点）：
 * `recordRetryResult` 与 `moveToDeadLetter`。
 *
 * @author Codex
 */
public interface IEduAsyncTaskService {

    /**
     * 分页查询异步任务（默认只查本人发起的任务，REQ-IMP-032）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 任务分页结果
     */
    TableDataInfo<EduAsyncTaskVo> queryPageList(EduAsyncTaskBo query, PageQuery pageQuery);

    /**
     * 按任务编号查询任务详情（结果查询重新解析数据范围，REQ-IMP-036）
     *
     * @param taskNo 任务编号
     * @return 任务详情
     */
    EduAsyncTaskVo queryByTaskNo(String taskNo);

    /**
     * 取消排队中的任务（只有 queued 可取消，REQ-IMP-034）
     *
     * @param taskNo 任务编号
     * @param reason 取消原因（至少 5 个字）
     * @return 是否成功
     */
    Boolean cancelAsyncTask(String taskNo, String reason);

    /**
     * 重试失败 / 部分失败的任务（沿用原幂等键，REQ-IMP-037）
     *
     * @param taskNo 任务编号
     * @param reason 重试原因（至少 5 个字）
     * @return 重试后的任务详情
     */
    EduAsyncTaskVo retryAsyncTask(String taskNo, String reason);

    /**
     * 解析任务结果文件下载（短时签名链接，REQ-IMP-042 / 043）
     *
     * @param taskNo 任务编号
     * @param fileId 文件 ID（字符串形式，避免前端 Number() 精度丢失）
     * @return 文件元信息 + 签名链接描述
     */
    EduFileRefVo resolveDownloadFile(String taskNo, String fileId);

    /**
     * 分页查询死信任务（REQ-IMP-038）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 死信任务分页结果
     */
    TableDataInfo<EduDeadLetterTaskVo> queryDeadLetterPageList(EduDeadLetterTaskBo query, PageQuery pageQuery);

    /**
     * 重放死信任务（重放原因必填，写审计，REQ-IMP-038）
     *
     * @param taskNo 任务编号
     * @param reason 重放原因（至少 5 个字）
     * @return 重放后的死信记录
     */
    EduDeadLetterTaskVo replayDeadLetterTask(String taskNo, String reason);

    /**
     * 【内部方法，无对应端点】记录一次重试结果，由导入导出执行器在本次尝试结束后调用
     *
     * @param taskNo   任务编号
     * @param retryNo  第几次重试
     * @param result   success / failed / timeout
     * @param errorMsg 本次失败原因，成功时可为空
     */
    void recordRetryResult(String taskNo, Integer retryNo, String result, String errorMsg);

    /**
     * 【内部方法，无对应端点】把任务移入死信（超过最大重试次数时调用，REQ-IMP-038）
     *
     * @param taskNo 任务编号
     */
    void moveToDeadLetter(String taskNo);

}
