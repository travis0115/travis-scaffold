package com.travis.monolith.system.version.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionPageReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.entity.SysVersion;

/**
 * 系统版本日志服务接口
 *
 * @author travis
 */
public interface SysVersionService extends IService<SysVersion> {

    /**
     * 分页查询版本日志（管理端）
     *
     * @return 分页结果
     */
    PageResp<SysVersionResp> page(SysVersionPageReq req);

    /**
     * 获取版本日志详情
     *
     * @param id 日志ID
     * @return 版本日志详情
     */
    SysVersionResp getById(Long id);

    /**
     * 新增版本日志
     *
     * @param req 请求参数
     */
    void create(SysVersionCreateReq req);

    /**
     * 更新版本日志
     *
     * @param id 日志ID
     * @param req 请求参数
     */
    void update(Long id, SysVersionUpdateReq req);

    /** 修改版本日志状态 */
    void updateStatus(Long id, Integer status);

    /** 分页查询已发布版本日志 */
    PageResp<SysVersionResp> pagePublished(PageRequest req);

    /**
     * 删除版本日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);
}
