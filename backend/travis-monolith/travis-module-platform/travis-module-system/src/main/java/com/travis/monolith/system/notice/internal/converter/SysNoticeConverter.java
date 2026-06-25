package com.travis.monolith.system.notice.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 系统公告对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysNoticeConverter {

    SysNotice toEntity(SysNoticeCreateReq req);

    void update(SysNoticeUpdateReq req, @MappingTarget SysNotice notice);

    SysNoticeResp toResp(SysNotice notice);

    List<SysNoticeResp> toRespList(List<SysNotice> notices);
}
