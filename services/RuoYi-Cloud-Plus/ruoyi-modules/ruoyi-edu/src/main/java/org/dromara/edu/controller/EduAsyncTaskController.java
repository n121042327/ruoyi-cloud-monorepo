package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduAsyncTaskBo;
import org.dromara.edu.domain.bo.EduDeadLetterTaskBo;
import org.dromara.edu.domain.vo.EduAsyncTaskVo;
import org.dromara.edu.domain.vo.EduDeadLetterTaskVo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.service.IEduAsyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异步任务中心
 *
 * 覆盖 import-export 模块异步任务中心全部 7 个 operationId：listAsyncTask / getAsyncTask /
 * cancelAsyncTask / retryAsyncTask / downloadTaskResult / listDeadLetterTask / replayDeadLetterTask。
 *
 * 口径：默认只展示本人发起的任务（REQ-IMP-032）；只有 queued 可取消（REQ-IMP-034）；
 * 失败 / 部分失败可重试且沿用原幂等键（REQ-IMP-037）；超过最大重试次数转死信，
 * 重放原因必填并写审计（REQ-IMP-038）；任务结果查询与文件下载都重新解析数据范围（REQ-IMP-036）。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/async-task")
public class EduAsyncTaskController extends BaseController {

    private final IEduAsyncTaskService asyncTaskService;

    /** 分页查询异步任务（可按任务类型、状态、时间范围筛选） */
    @SaCheckPermission("data.async_task:read")
    @GetMapping("/list")
    public TableDataInfo<EduAsyncTaskVo> list(EduAsyncTaskBo asyncTask, PageQuery pageQuery) {
        return asyncTaskService.queryPageList(asyncTask, pageQuery);
    }

    /** 分页查询死信任务列表 */
    @SaCheckPermission("data.async_task:read")
    @GetMapping("/dead-letter")
    public TableDataInfo<EduDeadLetterTaskVo> deadLetterList(EduDeadLetterTaskBo deadLetter, PageQuery pageQuery) {
        return asyncTaskService.queryDeadLetterPageList(deadLetter, pageQuery);
    }

    /** 任务详情（含重试记录） */
    @SaCheckPermission("data.async_task:read")
    @GetMapping("/{taskNo}")
    public R<EduAsyncTaskVo> getInfo(@PathVariable String taskNo) {
        return R.ok(asyncTaskService.queryByTaskNo(taskNo));
    }

    /** 取消排队中的任务（只有 queued 可取消，原因必填） */
    @SaCheckPermission("data.async_task:update")
    @Log(title = "异步任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskNo}/cancel")
    public R<Void> cancel(@PathVariable String taskNo,
                          @Validated @RequestBody(required = false) EduAsyncTaskBo body) {
        String reason = body == null ? null : body.getReason();
        return toAjax(asyncTaskService.cancelAsyncTask(taskNo, reason));
    }

    /** 重试失败 / 部分失败的任务（沿用原幂等键，原因必填） */
    @SaCheckPermission("data.async_task:update")
    @Log(title = "异步任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{taskNo}/retry")
    public R<EduAsyncTaskVo> retry(@PathVariable String taskNo,
                                   @Validated @RequestBody(required = false) EduAsyncTaskBo body) {
        String reason = body == null ? null : body.getReason();
        return R.ok(asyncTaskService.retryAsyncTask(taskNo, reason));
    }

    /** 任务结果文件下载（短时签名链接，短时且与登录态绑定） */
    @SaCheckPermission("data.async_task:read")
    @Log(title = "异步任务", businessType = BusinessType.EXPORT)
    @GetMapping("/{taskNo}/file/{fileId}")
    public R<EduFileRefVo> download(@PathVariable String taskNo, @PathVariable String fileId) {
        return R.ok(asyncTaskService.resolveDownloadFile(taskNo, fileId));
    }

    /** 死信任务重放（重放原因必填，写审计） */
    @SaCheckPermission("data.async_task:update")
    @Log(title = "异步任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/dead-letter/{taskNo}/replay")
    public R<EduDeadLetterTaskVo> replay(@PathVariable String taskNo,
                                         @Validated @RequestBody(required = false) EduDeadLetterTaskBo body) {
        String reason = body == null ? null : body.getReason();
        return R.ok(asyncTaskService.replayDeadLetterTask(taskNo, reason));
    }

}
