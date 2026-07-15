package com.travis.monolith.system.user.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserProfileReq;
import com.travis.monolith.system.user.api.request.SysUserUpdateReq;
import com.travis.monolith.system.user.api.response.SysUserInfoResp;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 用户对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysUserConverter {

    /** SysUser → SysUserResp（基础字段映射） deptName、roleIds、roleNames 需在Service层手动设置 */
    SysUserInfoResp toInfoResp(SysUserResp userResp);

    /** 将用户实体转换为响应。 */
    SysUserResp toResp(SysUser user);

    /** 批量将用户实体转换为响应。 */
    List<SysUserResp> toRespList(List<SysUser> users);

    /** 将创建参数转换为用户实体。 */
    @Mapping(target = "deptId", defaultValue = "0L")
    SysUser toEntity(SysUserCreateReq req);

    /** 将更新参数写入已有用户实体。 */
    @Mapping(target = "deptId", defaultValue = "0L")
    void update(SysUserUpdateReq req, @MappingTarget SysUser user);

    /** 将个人资料参数写入已有用户实体。 */
    void update(SysUserProfileReq req, @MappingTarget SysUser user);
}
