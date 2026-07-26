package com.travis.monolith.system.message.internal.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/** 消息接收记录 Mapper。 */
@Mapper
public interface SysMessageReceiverMapper extends BaseMapperX<SysMessageReceiver> {

    /** 分页查询当前用户可见的站内信。 */
    Page<SysUserMessageResp> selectInboxPage(
            Page<SysUserMessageResp> page,
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId,
            @Param("title") String title,
            @Param("messageType") Integer messageType,
            @Param("publishStartDate") LocalDate publishStartDate,
            @Param("publishEndDate") LocalDate publishEndDate,
            @Param("readStatus") Integer readStatus);

    /** 统计当前用户可见的未读站内信数量。 */
    Long countUnreadInbox(
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId);

    /** 查询当前用户可见且符合读取状态的站内信编号。 */
    List<Long> selectInboxMessageIds(
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId,
            @Param("readStatus") Integer readStatus);

    /** 物理删除消息的接收状态，避免保留无主记录。 */
    int deleteByMessageId(@Param("messageId") Long messageId);

    /** 原子新增或更新用户消息状态。 */
    int upsertStates(@Param("states") List<SysMessageReceiver> states);
}
