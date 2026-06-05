package com.travis.monolith.system.internal.service;

import com.travis.monolith.system.internal.model.req.SysUserLoginReq;
import com.travis.monolith.system.internal.model.resp.SysUserLoginResp;
import com.travis.monolith.system.internal.model.resp.UserInfoResp;
import com.travis.monolith.system.internal.model.resp.VbenMenuResp;

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
     * @return 登录响应（包含访问令牌）
     */
    SysUserLoginResp login(SysUserLoginReq req);

    /**
     * 获取当前登录用户的详细信息，包含角色编码和权限列表
     *
     * @return 用户信息视图
     */
    UserInfoResp getUserInfo();

    /**
     * 获取当前用户的菜单树（用于前端路由渲染）
     *
     * @return Vben Admin 格式的菜单树
     */
    List<VbenMenuResp> getMenuList();

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
