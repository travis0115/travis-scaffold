package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 消息推送 Mapper。 */
@Mapper
public interface SysMessageMapper extends BaseMapperX<SysMessage> {

    /** 物理删除业务来源消息，允许同一来源后续重新发布。 */
    int deletePhysicallyById(@Param("id") Long id);

    /** 按预期状态原子占用消息发送权。 */
    int claimForPublish(
            @Param("id") Long id,
            @Param("expectedStatus") Integer expectedStatus,
            @Param("pushType") Integer pushType,
            @Param("sentStatus") Integer sentStatus,
            @Param("publishTime") LocalDateTime publishTime);
}
