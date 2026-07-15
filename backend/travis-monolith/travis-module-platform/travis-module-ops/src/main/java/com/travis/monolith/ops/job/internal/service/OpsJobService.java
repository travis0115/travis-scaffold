package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobImportReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** 定时任务管理服务。 */
public interface OpsJobService {
    /** 分页查询定时任务。 */
    PageResp<OpsJobPageResp> page(OpsJobPageReq req);

    /** 查询任务详情，任务不存在时抛出业务异常。 */
    OpsJobDetailResp getOrThrow(Long id);

    /** 查询任务详情，任务不存在时返回 {@code null}。 */
    OpsJobDetailResp find(Long id);

    /** 查询全部任务实体，供调度器初始化使用。 */
    List<OpsJob> listAll();

    /** 按状态统计任务数量；状态为空时统计全部任务。 */
    long countJobs(Integer status);

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

    /** 复制指定任务。 */
    void copy(Long id);

    /** 预览任务接下来的计划执行时间。 */
    List<LocalDateTime> preview(OpsJobPreviewReq req, Integer count);

    /** 查询当前已注册的任务处理器名称。 */
    Collection<String> listHandlers();

    /** 查询任务负责人及告警接收人的可选用户。 */
    List<SysUserOptionResp> listUserOptions(String keyword, Collection<Long> userIds);

    /** 查询用于导出的全部任务。 */
    List<OpsJobExportResp> exportJobs();

    /** 批量导入并注册任务。 */
    void importJobs(List<OpsJobImportReq> jobs);
}
