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

    /** 将创建参数转换为公告实体。 */
    SysNotice toEntity(SysNoticeCreateReq req);

    /** 将更新参数写入已有公告实体。 */
    void update(SysNoticeUpdateReq req, @MappingTarget SysNotice notice);

    /** 将公告实体转换为响应。 */
    SysNoticeResp toResp(SysNotice notice);

    /** 批量将公告实体转换为响应。 */
    List<SysNoticeResp> toRespList(List<SysNotice> notices);
}
