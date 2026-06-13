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
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OpsJobService {
    PageResp<OpsJobPageResp> page(OpsJobPageReq req);

    OpsJobDetailResp getDetail(Long id);

    void create(OpsJobCreateReq req);

    void update(Long id, OpsJobUpdateReq req);

    void delete(Long id);

    void changeStatus(Long id, Integer status);

    void runNow(Long id, String params);

    void copy(Long id);

    List<LocalDateTime> preview(OpsJobPreviewReq req, Integer count);

    Collection<String> listHandlers();

    List<SysUserOptionResp> listUserOptions(String keyword, Collection<Long> userIds);

    List<OpsJobExportResp> exportJobs();

    void importJobs(List<OpsJobImportReq> jobs);
}
