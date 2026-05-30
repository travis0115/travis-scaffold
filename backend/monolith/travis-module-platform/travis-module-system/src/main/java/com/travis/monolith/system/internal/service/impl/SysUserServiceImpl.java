package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.exception.IErrorCode;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.internal.mapper.SysUserMapper;
import com.travis.monolith.system.internal.mapper.SysUserRoleMapper;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.entity.SysUserRole;
import com.travis.monolith.system.internal.model.req.SysUserReq;
import com.travis.monolith.system.internal.model.req.SysUserRoleReq;
import com.travis.monolith.system.internal.model.resp.SysUserResp;
import com.travis.monolith.system.internal.service.SysRoleService;
import com.travis.monolith.system.internal.service.SysUserService;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现，包含密码加密（BCrypt）、角色分配及部门名称关联查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;
    /** 部门 Mapper（用于关联查询部门名称） */
    private final SysDeptMapper deptMapper;
    /** 角色管理服务 */
    private final SysRoleService roleService;

    /**
     * 分页查询用户列表，支持按用户名、手机号、状态、部门筛选
     */
    @Override
    public PageResult<SysUserResp> getUserPage(String username, String phone, Integer status, Long deptId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(username != null, SysUser::getUsername, username)
                .like(phone != null, SysUser::getPhone, phone)
                .eq(status != null, SysUser::getStatus, status)
                .eq(deptId != null, SysUser::getDeptId, deptId)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<SysUserResp> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), (int) page.getPages());
    }

    /**
     * 获取用户详情，同时关联查询角色ID和角色名称
     */
    @Override
    public SysUserResp getUserDetail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysUserResp vo = toVO(user);
        List<Long> roleIds = roleService.getRoleIdsByUserId(id);
        vo.setRoleIds(roleIds);
        List<String> roleNames = roleService.listByIds(roleIds).stream()
                .map(r -> r.getRoleName()).collect(Collectors.toList());
        vo.setRoleNames(roleNames);
        return vo;
    }

    /**
     * 新增用户，密码使用 BCrypt 加密存储
     */
    @Override
    public void addUser(SysUserReq req) {
        // 检查用户名唯一性
        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException(new IErrorCode() {
                @Override public String getCode() { return CommonErrorCode.BAD_REQUEST.getCode(); }
                @Override public String getMsg() { return "用户名已存在"; }
            }, null);
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(encodePassword(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setAvatar(req.getAvatar());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDeptId(req.getDeptId());
        user.setStatus(req.getStatus());
        save(user);
    }

    /**
     * 更新用户信息，密码为空时保持原密码不变
     */
    @Override
    public void updateUser(Long id, SysUserReq req) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 检查用户名唯一性（排除自身）
        long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername())
                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BizException(new IErrorCode() {
                @Override public String getCode() { return CommonErrorCode.BAD_REQUEST.getCode(); }
                @Override public String getMsg() { return "用户名已存在"; }
            }, null);
        }
        user.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(encodePassword(req.getPassword()));
        }
        user.setNickname(req.getNickname());
        user.setAvatar(req.getAvatar());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDeptId(req.getDeptId());
        user.setStatus(req.getStatus());
        updateById(user);
    }

    /**
     * 删除用户
     */
    @Override
    public void deleteUser(Long id) {
        removeById(id);
    }

    /**
     * 分配用户角色：先删除原有关联，再批量插入新关联
     */
    @Override
    @Transactional
    public void assignRoles(SysUserRoleReq req) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, req.getUserId()));
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            List<SysUserRole> list = req.getRoleIds().stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(req.getUserId());
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());
            list.forEach(userRoleMapper::insert);
        }
    }

    /**
     * 根据用户名查询用户（限制返回1条）
     */
    @Override
    public SysUser getUserByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }

    /**
     * 密码加密，未设置密码时使用默认密码 123456
     *
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = "123456";
        }
        return BCrypt.hashpw(rawPassword);
    }

    /**
     * 实体转视图对象，同时关联查询部门名称
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private SysUserResp toVO(SysUser user) {
        String deptName = null;
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }
        return SysUserResp.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .deptId(user.getDeptId())
                .deptName(deptName)
                .availableBalance(user.getAvailableBalance())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createTime(user.getCreateTime())
                .build();
    }
}
