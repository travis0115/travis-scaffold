package com.travis.monolith.system.user.internal.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.request.*;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.converter.SysUserConverter;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 用户管理服务实现，包含密码加密（BCrypt）、角色分配及部门名称关联查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:user")
public class SysUserServiceImpl extends ServiceImplX<SysUserMapper, SysUser>
        implements SysUserService {

    private static final Map<String, SFunction<SysUser, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("lastLoginTime", SysUser::getLastLoginTime),
                    Map.entry("createTime", SysUser::getCreateTime));

    /** 部门 API（用于关联查询部门名称） */
    private final SysDeptApi deptApi;

    /** 角色 API */
    private final SysRoleApi roleApi;

    /** 对象转换器 */
    private final SysUserConverter converter;

    /** 分页查询用户列表 */
    @Override
    public PageResp<SysUserResp> page(SysUserPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysUser>()
                        .likeIfPresent(SysUser::getUsername, req.getUsername())
                        .likeIfPresent(SysUser::getNickname, req.getNickname())
                        .likeIfPresent(SysUser::getMobile, req.getMobile())
                        .likeIfPresent(SysUser::getEmail, req.getEmail())
                        .eqIfPresent(SysUser::getStatus, req.getStatus())
                        .eqIfPresent(SysUser::getDeptId, req.getDeptId())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysUser::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    /** 获取用户详情，同时关联查询角色ID和角色名称 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysUserResp getById(Long id) {
        var user = getByIdOrThrow(id);
        var resp = this.toResp(user);
        var roleIds = roleApi.getRoleIdsByUserId(id);
        resp.setRoleIds(roleIds);
        var roleNames = roleApi.getRoleNamesByUserId(id);
        resp.setRoleNames(roleNames);
        return resp;
    }

    /** 新增用户，密码使用 BCrypt 加密存储 */
    @Override
    @Transactional
    public Long create(SysUserCreateReq req) {
        // 检查用户名唯一性
        long count =
                count(
                        new LambdaQueryWrapperX<SysUser>()
                                .eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.USER_USERNAME_EXISTS);
        }
        var user = converter.toEntity(req);
        user.setPassword(BCrypt.hashpw(req.getPassword()));
        save(user);
        return user.getId();
    }

    /** 更新用户信息，密码为空时保持原密码不变 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'username:'+#id"), @CacheEvict(key = "'detail:'+#id")})
    public void update(Long id, SysUserUpdateReq req) {
        var user = getByIdOrThrow(id);
        // 检查用户名唯一性（排除自身）
        long count =
                count(
                        new LambdaQueryWrapperX<SysUser>()
                                .eq(SysUser::getUsername, req.getUsername())
                                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.USER_USERNAME_EXISTS);
        }
        converter.update(req, user);
        updateById(user);
    }

    /** 删除用户，同时清除用户-角色关联并使其会话失效 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'username:'+#id"), @CacheEvict(key = "'detail:'+#id")})
    public void deleteById(Long id) {
        // 通过角色服务删除用户-角色关联
        roleApi.deleteUserRolesByUserId(id);
        removeById(id);
        // 使用户会话失效
        StpKit.of(LoginType.ADMIN).logout(id);
    }

    /** 分配用户角色：委托给角色服务 */
    @Override
    @Transactional
    public void assignRoles(SysUserRoleReq req) {
        roleApi.assignUserRoles(req.getUserId(), req.getRoleIds());
    }
    

    /** 根据用户ID查询用户名 */
    @Override
    @Cacheable(key = "'username:'+#userId")
    public String getUsernameById(Long userId) {
        if (userId == null) {
            return null;
        }
        var user =
                lambdaQuery()
                        .select(SysUser::getId, SysUser::getUsername)
                        .eq(SysUser::getId, userId)
                        .one();
        return user == null ? null : user.getUsername();
    }

    /** 当前登录用户修改个人资料 */
    @Override
    public void updateProfile(SysUserProfileReq req) {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser user = getByIdOrThrow(userId);
        converter.update(req, user);
        updateById(user);
    }

    /** 当前登录用户更新头像 */
    @Override
    public void updateAvatar(SysUserUpdateAvatarReq req) {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser user = getByIdOrThrow(userId);
        converter.update(req, user);
        updateById(user);
    }

    /** 当前登录用户修改密码：校验原密码，加密新密码后更新 */
    @Override
    public void changePassword(SysUserChangePasswordReq req) {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        // 显式查询密码字段（SysUser 中 password 标记了 select=false）
        SysUser user =
                lambdaQuery()
                        .eq(SysUser::getId, userId)
                        .select(SysUser::getId, SysUser::getPassword)
                        .one();
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // BCrypt 校验原密码
        if (!BCrypt.checkpw(req.getOldPassword(), user.getPassword())) {
            throw new BizException(SystemErrorCode.USER_OLD_PASSWORD_ERROR);
        }
        // 加密新密码并更新
        user.setPassword(BCrypt.hashpw(req.getNewPassword()));
        updateById(user);
        // 修改密码后踢出当前登录，需重新登录
        StpKit.of(LoginType.ADMIN).logout(userId);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（可选，为null或空时自动生成随机密码）
     * @return 最终使用的密码（明文，供管理员转达用户）
     */
    @Override
    @Transactional
    public String resetPassword(Long id, String newPassword) {
        SysUser user = getByIdOrThrow(id);
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = RandomUtil.randomString(8);
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        updateById(user);
        // 重置密码后踢出该用户，需重新登录
        StpKit.of(LoginType.ADMIN).logout(id);
        return newPassword;
    }

    private SysUserResp toResp(SysUser user) {
        var resp = converter.toResp(user);
        resp.setAvatar(user.getAvatar());
        if (user.getDeptId() != null) {
            Map<Long, String> deptMap = deptApi.getDeptNameMapByIds(List.of(user.getDeptId()));
            String deptName = deptMap.get(user.getDeptId());
            if (deptName != null) {
                resp.setDeptName(deptName);
            }
        }
        resp.setRoleNames(roleApi.getRoleNamesByUserId(user.getId()));
        if (user.getLastLoginIp() != null && !user.getLastLoginIp().isEmpty()) {
            resp.setLastLoginLocation(Ip2RegionUtil.getRegionByIP(user.getLastLoginIp()));
        }
        return resp;
    }
}
