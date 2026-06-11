package com.travis.monolith.system.log.versionlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogResp;
import com.travis.monolith.system.log.versionlog.internal.entity.SysVersionLog;
import java.util.List;

/**
 * 系统版本日志服务接口
 *
 * @author travis
 */
public interface SysVersionLogService extends IService<SysVersionLog> {

    /**
     * 分页查询版本日志（管理端）
     *
     * @param version 版本号（模糊匹配，可为空）
     * @param title 标题（模糊匹配，可为空）
     * @param status 状态（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResp<SysVersionLogResp> page(
            String version, String title, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取版本日志详情
     *
     * @param id 日志ID
     * @return 版本日志详情
     */
    SysVersionLogResp getById(Long id);

    /**
     * 新增版本日志
     *
     * @param req 请求参数
     */
    void create(SysVersionLogReq req);

    /**
     * 更新版本日志
     *
     * @param id 日志ID
     * @param req 请求参数
     */
    void update(Long id, SysVersionLogReq req);

    /**
     * 删除版本日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);

    /**
     * 获取已发布的版本日志列表（供前端用户查看）
     *
     * @param limit 返回条数
     * @return 版本日志列表
     */
    List<SysVersionLogResp> listPublished(Integer limit);
}
