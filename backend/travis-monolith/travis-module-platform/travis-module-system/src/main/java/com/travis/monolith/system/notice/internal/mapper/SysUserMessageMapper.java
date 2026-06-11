package com.travis.monolith.system.notice.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.notice.internal.entity.SysUserMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMessageMapper extends BaseMapperX<SysUserMessage> {
    @Delete("DELETE FROM sys_user_message WHERE id = #{id} AND user_id = #{userId}")
    int deleteMessage(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM sys_user_message WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM sys_user_message WHERE notice_id = #{noticeId}")
    int deleteByNoticeId(@Param("noticeId") Long noticeId);
}
