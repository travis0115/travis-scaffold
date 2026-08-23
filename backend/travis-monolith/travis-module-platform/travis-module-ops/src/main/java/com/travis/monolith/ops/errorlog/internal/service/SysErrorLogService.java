package com.travis.monolith.ops.errorlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogHandleReq;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;

/** 系统异常日志查询服务。 */
public interface SysErrorLogService extends IService<SysErrorLog> {
    /** 分页查询系统异常日志。 */
    PageResp<SysErrorLogResp> page(SysErrorLogPageReq req);

    /** 记录并聚合同一次异常。 */
    void record(SysErrorLog errorLog, SysErrorLogOccurrence occurrence);

    /** 查询错误日志详情。 */
    SysErrorLogResp getDetailOrThrow(Long id);

    /** 处理错误日志。 */
    void handle(Long id, SysErrorLogHandleReq req);

    /** 批量处理全部待处理错误日志。 */
    int handleAllPending(SysErrorLogHandleReq req);

    /** 删除错误日志及其发生明细。 */
    void delete(Long id);
}
