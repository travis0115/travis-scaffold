package com.travis.monolith.system.internal.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.internal.converter.SysUserConverter;
import com.travis.monolith.system.internal.exception.SystemErrorCode;
import com.travis.monolith.system.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.internal.mapper.SysUserMapper;
import com.travis.monolith.system.internal.mapper.SysUserRoleMapper;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.entity.SysUserRole;
import com.travis.monolith.system.internal.model.request.user.*;
import com.travis.monolith.system.internal.model.response.user.SysUserResp;
import com.travis.monolith.system.internal.service.SysFileService;
import com.travis.monolith.system.internal.service.SysRoleService;
import com.travis.monolith.system.internal.service.SysUserService;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理服务实现，包含密码加密（BCrypt）、角色分配及部门名称关联查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;

    /** 部门 Mapper（用于关联查询部门名称） */
    private final SysDeptMapper deptMapper;

    /** 角色管理服务 */
    private final SysRoleService roleService;

    /** 文件服务 */
    private final SysFileService fileService;

    /** 对象转换器 */
    private final SysUserConverter converter;

    /** 分页查询用户列表，支持按用户名、手机号、状态、部门筛选 */
    @Override
    public PageResult<SysUserResp> getUserPage(
            String username,
            String mobile,
            Integer status,
            Long deptId,
            Integer pageNum,
            Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper =
                new LambdaQueryWrapper<SysUser>()
                        .like(username != null, SysUser::getUsername, username)
                        .like(mobile != null, SysUser::getMobile, mobile)
                        .eq(status != null, SysUser::getStatus, status)
                        .eq(deptId != null, SysUser::getDeptId, deptId)
                        .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<SysUserResp> voList = toVOList(page.getRecords());
        return new PageResult<>(
                voList,
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                (int) page.getPages());
    }

    /** 获取用户详情，同时关联查询角色ID和角色名称 */
    @Override
    public SysUserResp getUserDetail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysUserResp vo = toVO(user);
        List<Long> roleIds = roleService.getRoleIdsByUserId(id);
        vo.setRoleIds(roleIds);
        List<String> roleNames =
                roleIds.isEmpty()
                        ? List.of()
                        : roleService.listByIds(roleIds).stream()
                                .map(SysRole::getRoleName)
                                .collect(Collectors.toList());
        vo.setRoleNames(roleNames);
        return vo;
    }

    /** 新增用户，密码使用 BCrypt 加密存储 */
    @Override
    @Transactional
    public Long addUser(SysUserReq req) {
        // 检查用户名唯一性
        long count =
                count(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_USER_USERNAME_EXISTS);
        }
        SysUser user = converter.toEntity(req);
        user.setPassword(encodePassword(req.getPassword()));
        save(user);
        return user.getId();
    }

    /** 更新用户信息，密码为空时保持原密码不变 */
    @Override
    @Transactional
    public void updateUser(Long id, SysUserReq req) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 检查用户名唯一性（排除自身）
        long count =
                count(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUsername, req.getUsername())
                                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_USER_USERNAME_EXISTS);
        }
        converter.update(req, user);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(encodePassword(req.getPassword()));
        }
        updateById(user);
    }

    /** 删除用户，同时清除用户-角色关联并使其会话失效 */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 删除用户-角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        removeById(id);
        // 使用户会话失效
        StpKit.of(LoginType.ADMIN).logout(id);
    }

    /** 分配用户角色：先删除原有关联，再批量插入新关联，清除菜单缓存 */
    @Override
    @Transactional
    @CacheEvict(value = "menus:vben", allEntries = true)
    public void assignRoles(SysUserRoleReq req) {
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, req.getUserId()));
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            List<SysUserRole> list =
                    req.getRoleIds().stream()
                            .map(
                                    roleId -> {
                                        SysUserRole ur = new SysUserRole();
                                        ur.setUserId(req.getUserId());
                                        ur.setRoleId(roleId);
                                        return ur;
                                    })
                            .collect(Collectors.toList());
            list.forEach(userRoleMapper::insert);
        }
    }

    /** 根据用户名查询用户（限制返回1条） */
    @Override
    public SysUser getUserByUsername(String username) {
        return getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .last("LIMIT 1"));
    }

    /** 当前登录用户修改个人资料 */
    @Override
    public void updateProfile(UserProfileReq req) {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setMobile(req.getMobile());
        updateById(user);
    }

    /** 当前登录用户更新头像 */
    @Override
    public void updateAvatar(UpdateAvatarReq req) {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        user.setAvatar(req.getAvatar());
        updateById(user);
    }

    /** 当前登录用户修改密码：校验旧密码，加密新密码后更新 */
    @Override
    public void changePassword(ChangePasswordReq req) {
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
        // BCrypt 校验旧密码
        if (!BCrypt.checkpw(req.getOldPassword(), user.getPassword())) {
            throw new BizException(SystemErrorCode.SYSTEM_USER_OLD_PASSWORD_ERROR);
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
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = generateRandomPassword();
        }
        user.setPassword(encodePassword(newPassword));
        updateById(user);
        // 重置密码后踢出该用户，需重新登录
        StpKit.of(LoginType.ADMIN).logout(id);
        return newPassword;
    }

    /** 生成随机密码（8位，包含大小写字母和数字），使用密码学安全的随机数生成器 */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 密码加密
     *
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateRandomPassword();
        }
        return BCrypt.hashpw(rawPassword);
    }

    /**
     * 实体转视图对象（单条查询使用）
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private SysUserResp toVO(SysUser user) {
        SysUserResp resp = converter.toResp(user);
        resp.setAvatar(fileService.getFileUrl(user.getAvatar()));
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                resp.setDeptName(dept.getDeptName());
            }
        }
        resp.setRoleNames(roleService.getRoleNamesByUserId(user.getId()));
        if (user.getLastLoginIp() != null && !user.getLastLoginIp().isEmpty()) {
            resp.setLastLoginLocation(Ip2RegionUtil.getRegionByIP(user.getLastLoginIp()));
        }
        return resp;
    }

    /**
     * 批量实体转视图对象，优化 N+1 查询：一次性加载所有部门和角色数据
     *
     * @param users 用户实体列表
     * @return 用户视图对象列表
     */
    private List<SysUserResp> toVOList(List<SysUser> users) {
        if (users.isEmpty()) {
            return List.of();
        }

        // 批量查询所有关联的部门
        Set<Long> deptIds =
                users.stream()
                        .map(SysUser::getDeptId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
        Map<Long, String> deptNameMap =
                deptIds.isEmpty()
                        ? Map.of()
                        : deptMapper.selectBatchIds(deptIds).stream()
                                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));

        // 批量查询所有关联的角色名称（按用户分组）
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        Map<Long, List<String>> userRoleNamesMap = batchGetRoleNamesByUserIds(userIds);

        return users.stream()
                .map(
                        user -> {
                            SysUserResp resp = converter.toResp(user);
                            // 头像路径拼接完整URL
                            resp.setAvatar(fileService.getFileUrl(user.getAvatar()));
                            // 设置部门名称
                            if (user.getDeptId() != null) {
                                resp.setDeptName(deptNameMap.get(user.getDeptId()));
                            }
                            // 设置角色名称
                            resp.setRoleNames(
                                    userRoleNamesMap.getOrDefault(user.getId(), List.of()));
                            // 解析最后登录IP到地理位置
                            if (user.getLastLoginIp() != null && !user.getLastLoginIp().isEmpty()) {
                                resp.setLastLoginLocation(
                                        Ip2RegionUtil.getRegionByIP(user.getLastLoginIp()));
                            }
                            return resp;
                        })
                .collect(Collectors.toList());
    }

    /**
     * 批量查询多个用户的角色名称映射
     *
     * @param userIds 用户ID列表
     * @return userId -> roleNameList 映射
     */
    private Map<Long, List<String>> batchGetRoleNamesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        // 批量查询用户-角色关联
        List<SysUserRole> userRoles =
                userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Map.of();
        }
        // 收集所有角色ID，批量查询角色
        Set<Long> roleIds =
                userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleNameMap =
                roleService.listByIds(roleIds).stream()
                        .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
        // 按 userId 分组
        return userRoles.stream()
                .collect(
                        Collectors.groupingBy(
                                SysUserRole::getUserId,
                                Collectors.mapping(
                                        ur -> roleNameMap.getOrDefault(ur.getRoleId(), "未知角色"),
                                        Collectors.toList())));
    }
}
