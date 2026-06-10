package com.travis.monolith.system.log.updatelog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.updatelog.api.request.SysUpdateLogReq;
import com.travis.monolith.system.log.updatelog.api.response.SysUpdateLogResp;
import com.travis.monolith.system.log.updatelog.internal.entity.SysUpdateLog;
import java.util.List;

/**
 * 系统更新日志服务接口
 *
 * @author travis
 */
public interface SysUpdateLogService extends IService<SysUpdateLog> {

    /**
     * 分页查询更新日志（管理端）
     *
     * @param version 版本号（模糊匹配，可为空）
     * @param title 标题（模糊匹配，可为空）
     * @param status 状态（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResp<SysUpdateLogResp> page(
            String version, String title, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取更新日志详情
     *
     * @param id 日志ID
     * @return 更新日志详情
     */
    SysUpdateLogResp getById(Long id);

    /**
     * 新增更新日志
     *
     * @param req 请求参数
     */
    void create(SysUpdateLogReq req);

    /**
     * 更新更新日志
     *
     * @param id 日志ID
     * @param req 请求参数
     */
    void update(Long id, SysUpdateLogReq req);

    /**
     * 删除更新日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);

    /**
     * 获取已发布的更新日志列表（供前端用户查看）
     *
     * @param limit 返回条数
     * @return 更新日志列表
     */
    List<SysUpdateLogResp> listPublished(Integer limit);
}
