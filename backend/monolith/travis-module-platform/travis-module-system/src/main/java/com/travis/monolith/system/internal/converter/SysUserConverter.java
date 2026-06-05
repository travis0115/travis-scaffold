package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.req.SysUserReq;
import com.travis.monolith.system.internal.model.resp.SysUserResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 用户对象转换器
 * 处理 SysUser ↔ SysUserReq/SysUserResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysUserConverter {

    /**
     * SysUser → SysUserResp（基础字段映射）
     * deptName、roleIds、roleNames、lastLoginLocation 需在Service层手动设置
     */
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    @Mapping(target = "roleNames", ignore = true)
    @Mapping(target = "lastLoginLocation", ignore = true)
    SysUserResp toResp(SysUser user);

    List<SysUserResp> toRespList(List<SysUser> users);

    /**
     * SysUserReq → SysUser（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "availableBalance", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "version", ignore = true)
    SysUser toEntity(SysUserReq req);

    /**
     * SysUserReq → 更新已有的SysUser
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "availableBalance", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(SysUserReq req, @MappingTarget SysUser user);
}
