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
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.infrastructure.framework.satoken.core.LoginSubjectSessionKey;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.api.event.UploaderNameChangedEvent;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.request.*;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.converter.SysUserConverter;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理服务实现，包含密码加密（BCrypt）、角色分配及部门名称关联查询
 *
 * @author travis
 */
@Service
@CacheConfig(cacheNames = "system:user")
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImplX<SysUserMapper, SysUser>
        implements SysUserService {

    private static final String ONLINE_USER_KEY = "system:user:online";

    private static final Map<String, SFunction<SysUser, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("lastOnlineTime", SysUser::getLastOnlineTime),
                    Map.entry("lastOfflineTime", SysUser::getLastOfflineTime),
                    Map.entry("createTime", SysUser::getCreateTime));

    /** 部门 API（用于关联查询部门名称） */
    private final SysDeptApi deptApi;

    /** 角色 API */
    private final SysRoleApi roleApi;

    /** 文件 API */
    private final SysFileApi fileApi;

    private final ApplicationEventPublisher eventPublisher;

    /** 对象转换器 */
    private final SysUserConverter converter;

    /** 分页查询用户列表 */
    @Override
    public PageResp<SysUserResp> page(SysUserPageReq req) {
        var onlineUserIds = getOnlineUserIds();
        if (Boolean.TRUE.equals(req.getOnlineOnly()) && onlineUserIds.isEmpty()) {
            return PageResp.<SysUserResp>builder()
                    .records(List.of())
                    .total(0L)
                    .pageNum((long) req.getPageNum())
                    .pageSize((long) req.getPageSize())
                    .totalPages(0L)
                    .build();
        }
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
        if (Boolean.TRUE.equals(req.getOnlineOnly())) {
            wrapper.in(SysUser::getId, onlineUserIds);
        }
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        var resp = PageConverter.toResp(page.convert(this::toResp));
        fillOnlineStatus(resp.getRecords(), onlineUserIds);
        return resp;
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

    /** 获取所有启用用户列表（不分页） */
    @Override
    @Cacheable(key = "'list:id:all'")
    public List<Long> listUserIds() {
        return list().stream().map(SysUser::getId).toList();
    }

    /** 根据部门ID查询用户ID列表 */
    @Override
    @Cacheable(key = "'list:id:dept:'+#deptId")
    public List<Long> listUserIdsByDeptId(Long deptId) {
        return list(new LambdaQueryWrapperX<SysUser>().eq(SysUser::getDeptId, deptId)).stream()
                .map(SysUser::getId)
                .toList();
    }

    /** 新增用户，密码使用 BCrypt 加密存储 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'list:id:all'"),
                @CacheEvict(key = "'list:id:dept:'+#req.deptId")
            })
    public Long create(SysUserCreateReq req) {
        // 检查用户名唯一性
        var count =
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
    @Caching(evict = {@CacheEvict(key = "'detail:'+#id")})
    public void update(Long id, SysUserUpdateReq req) {
        var user = getByIdOrThrow(id);
        var oldUsername = user.getUsername();
        // 检查用户名唯一性（排除自身）
        var count =
                count(
                        new LambdaQueryWrapperX<SysUser>()
                                .eq(SysUser::getUsername, req.getUsername())
                                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.USER_USERNAME_EXISTS);
        }

        if (req.getDeptId() != null && !req.getDeptId().equals(user.getDeptId())) {
            RedisUtil.deleteCacheKey(
                    "system:user:list:id", "dept:" + user.getDeptId(), "dept:" + req.getDeptId());
        }
        converter.update(req, user);
        updateById(user);
        if (!user.getUsername().equals(oldUsername)) {
            RedisUtil.deleteCacheKey("system:user:username", String.valueOf(user.getId()));
            syncCurrentSessionUsername(id, user.getUsername());
            eventPublisher.publishEvent(
                    new UploaderNameChangedEvent(LoginType.ADMIN, id, user.getUsername()));
        }
    }

    /** 修改用户状态 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'detail:'+#id")})
    public void updateStatus(Long id, Integer status) {
        var user = getByIdOrThrow(id);
        user.setStatus(status);
        updateById(user);
        if (Status.DISABLED.getValue().equals(status)) {
            StpKit.of(LoginType.ADMIN).logout(id);
        }
    }

    /** 删除用户，同时清除用户-角色关联并使其会话失效 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'username:'+#id"),
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'list:id:all'")
            })
    public void deleteById(Long id) {
        var user = getByIdOrThrow(id);
        removeById(id);
        RedisUtil.setRemove(ONLINE_USER_KEY, String.valueOf(id));
        RedisUtil.deleteCacheKey("system:user", "list:id:dept:" + user.getDeptId());
        // 通过角色服务删除用户-角色关联
        roleApi.deleteUserRolesByUserId(id);
        // 使用户会话失效
        StpKit.of(LoginType.ADMIN).logout(id);
    }

    /**
     * 重置用户部门为0
     *
     * @param id 用户ID
     */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void resetDept(Long id) {
        var user = getByIdOrThrow(id);
        var originalDeptId = user.getDeptId();
        user.setDeptId(0L);
        updateById(user);
        RedisUtil.deleteCacheKey(
                "system:user", "list:id:dept:" + originalDeptId, "list:id:dept:" + 0L);
    }

    /** 批量重置指定部门下的用户部门归属 */
    @Override
    @Transactional
    public void resetDeptByDeptIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        lambdaUpdate().in(SysUser::getDeptId, deptIds).set(SysUser::getDeptId, 0L).update();
        RedisUtil.deleteCacheKeyByPattern("system:user", "detail:*");
        var cacheKeys = new ArrayList<String>();
        for (Long deptId : deptIds) {
            cacheKeys.add("list:id:dept:" + deptId);
        }
        cacheKeys.add("list:id:dept:" + 0L);
        RedisUtil.deleteCacheKey("system:user", cacheKeys);
    }

    /** 标记用户上线 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#userId")
    public void markOnline(Long userId, String ip) {
        if (userId == null) {
            return;
        }
        var added = RedisUtil.setAdd(ONLINE_USER_KEY, String.valueOf(userId));
        if (added == null || added == 0) {
            return;
        }
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getLastOnlineTime, LocalDateTime.now())
                .set(SysUser::getLastOnlineIp, ip)
                .update();
    }

    /** 标记用户下线 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#userId")
    public void markOffline(Long userId) {
        if (userId == null) {
            return;
        }
        RedisUtil.setRemove(ONLINE_USER_KEY, String.valueOf(userId));
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getLastOfflineTime, LocalDateTime.now())
                .update();
    }

    /** 获取在线用户数量 */
    @Override
    public Long countOnlineUsers() {
        var count = RedisUtil.setSize(ONLINE_USER_KEY);
        return count == null ? 0L : count;
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
                getOne(
                        new LambdaQueryWrapperX<SysUser>()
                                .select(SysUser::getId, SysUser::getUsername)
                                .eq(SysUser::getId, userId));
        return user == null ? null : user.getUsername();
    }

    /** 当前登录用户修改个人资料 */
    @Override
    @Transactional
    @CacheEvict(
            key =
                    "'detail:' + T(com.travis.infrastructure.framework.satoken.core.StpKit).getLoginIdAsLong(T(com.travis.infrastructure.common.web.constant.LoginType).ADMIN)")
    public void updateProfile(SysUserProfileReq req) {
        var userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        var user = getByIdOrThrow(userId);
        converter.update(req, user);
        updateById(user);
    }

    /** 当前登录用户更新头像 */
    @Override
    @Transactional
    @CacheEvict(
            key =
                    "'detail:' + T(com.travis.infrastructure.framework.satoken.core.StpKit).getLoginIdAsLong(T(com.travis.infrastructure.common.web.constant.LoginType).ADMIN)")
    public void updateAvatar(Long avatarFileId) {
        var userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        var user = getByIdOrThrow(userId);
        user.setAvatarFileId(avatarFileId);
        updateById(user);
    }

    /** 当前登录用户修改密码：校验原密码，加密新密码后更新 */
    @Override
    @Transactional
    public void changePassword(SysUserChangePasswordReq req) {
        var userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        // 显式查询密码字段（SysUser 中 password 标记了 select=false）
        var user =
                lambdaQuery()
                        .eq(SysUser::getId, userId)
                        .select(SysUser::getId, SysUser::getPassword)
                        .one();
        if (user == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
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
        var user = getByIdOrThrow(id);
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
        resp.setAvatar(fileApi.getFileUrlById(user.getAvatarFileId()));
        if (user.getDeptId() != null && user.getDeptId() != 0) {
            var deptName = deptApi.getDeptNameById(user.getDeptId());
            resp.setDeptName(deptName);
        }
        resp.setRoleNames(roleApi.getRoleNamesByUserId(user.getId()));
        if (user.getLastOnlineIp() != null && !user.getLastOnlineIp().isEmpty()) {
            resp.setLastOnlineLocation(Ip2RegionUtil.getRegionByIP(user.getLastOnlineIp()));
        }
        return resp;
    }

    private Set<Long> getOnlineUserIds() {
        var members = RedisUtil.setMembers(ONLINE_USER_KEY);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        var userIds = new HashSet<Long>();
        for (Object member : members) {
            if (member == null) {
                continue;
            }
            try {
                userIds.add(Long.valueOf(member.toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        return userIds;
    }

    private void fillOnlineStatus(List<SysUserResp> records, Set<Long> onlineUserIds) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (SysUserResp record : records) {
            record.setOnline(onlineUserIds.contains(record.getId()));
        }
    }

    private void syncCurrentSessionUsername(Long userId, String username) {
        try {
            var logic = StpKit.of(LoginType.ADMIN);
            if (logic.isLogin() && logic.getLoginIdAsLong() == userId) {
                logic.getSession().set(LoginSubjectSessionKey.USERNAME, username);
            }
        } catch (Exception ignored) {
        }
    }
}
