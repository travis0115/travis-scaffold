package com.travis.monolith.ops.job.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobHandlerResp;
import com.travis.monolith.ops.job.api.response.OpsJobResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.model.OpsJobExecutionConfig;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** 定时任务管理服务。 */
public interface OpsJobService extends IService<OpsJob> {
    /** 分页查询定时任务。 */
    PageResp<OpsJobResp> page(OpsJobPageReq req);

    /** 查询任务详情，任务不存在时抛出业务异常。 */
    OpsJobResp getOrThrow(Long id);

    /** 查询执行观察器所需配置，任务不存在时返回 {@code null}。 */
    OpsJobExecutionConfig findExecutionConfig(Long id);

    /** 创建并注册定时任务。 */
    void create(OpsJobCreateReq req);

    /** 更新任务配置并重新注册调度。 */
    void update(Long id, OpsJobUpdateReq req);

    /** 删除任务及其调度配置。 */
    void delete(Long id);

    /** 修改任务状态并同步暂停或恢复调度。 */
    void changeStatus(Long id, Integer status);

    /** 立即触发一次任务执行。 */
    void runNow(Long id, String params);

    /** 当前配置对应的单次计划执行结束后将任务恢复为停用状态。 */
    void completeOnce(Long id, String configFingerprint);

    /** 复制指定任务。 */
    void copy(Long id);

    /** 预览任务接下来的计划执行时间。 */
    List<LocalDateTime> preview(OpsJobPreviewReq req, Integer count);

    /** 查询当前已注册的任务处理器名称及说明。 */
    List<OpsJobHandlerResp> listHandlers(boolean includeBuiltin);

    /** 查询任务告警接收人的可选用户。 */
    List<SysUserOptionResp> listUserOptions(String keyword, Collection<Long> userIds);
}
