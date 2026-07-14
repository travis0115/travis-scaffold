package com.travis.monolith.system.user.internal.service;

import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.user.api.request.SysUserLoginReq;
import com.travis.monolith.system.user.api.response.SysUserInfoResp;
import com.travis.monolith.system.user.api.response.SysWebSocketTicketResp;
import java.util.List;

/**
 * 后台认证服务接口，提供登录认证、用户信息获取、菜单列表和权限码查询
 *
 * @author travis
 */
public interface SysAuthService {

    /**
     * 管理员登录
     *
     * @param req 登录请求参数（用户名 + 密码）
     */
    void login(SysUserLoginReq req);

    /** 管理员登出 */
    void logout();

    /** 签发 WebSocket 握手 ticket */
    SysWebSocketTicketResp createWebSocketTicket();

    /**
     * 获取当前登录用户的详细信息，包含角色编码和权限列表
     *
     * @return 用户信息视图
     */
    SysUserInfoResp getUserInfoOrThrow();

    /**
     * 获取当前用户的菜单树（用于前端路由渲染）
     *
     * @return Vben Admin 格式的菜单树
     */
    List<VbenMenuResp> listMenus();

    /**
     * 获取当前用户的权限标识列表（用于前端按钮级权限控制）
     *
     * @return 权限标识列表
     */
    List<String> getAccessCodes();

    /**
     * 获取当前用户的权限标识列表（用于前端按钮级权限控制）
     *
     * @return 权限标识列表
     */
    List<String> getAccessCodes(Long userId);
}
