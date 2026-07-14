package com.travis.monolith.system.message.internal.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 消息推送 Mapper。 */
@Mapper
public interface SysMessageMapper extends BaseMapperX<SysMessage> {

    /**
     * 分页查询指定用户可见的站内消息。
     *
     * <p>根据接收端、用户角色和部门匹配消息接收范围，并关联用户接收状态排除已删除消息；阅读状态和查询条件均为可选过滤项。
     */
    @Select(
            """
            <script>
            SELECT m.*
            FROM sys_message m
            LEFT JOIN sys_message_receiver r
              ON r.message_id = m.id
             AND r.receiver_type = #{receiverType}
             AND r.receiver_id = #{userId}
             AND r.is_deleted = 0
            WHERE m.is_deleted = 0
              AND m.status = 1
              AND m.receiver_type = #{receiverType}
              AND m.channel = 'IN_APP'
              AND m.publish_time &lt;= NOW()
              AND (r.id IS NULL OR r.read_status != 2)
              <if test="readStatus != null">
                <choose>
                  <when test="readStatus == 0">
                    AND (r.id IS NULL OR r.read_status = 0)
                  </when>
                  <otherwise>
                    AND r.read_status = #{readStatus}
                  </otherwise>
                </choose>
              </if>
              <if test="title != null and title != ''">
                AND m.title LIKE CONCAT('%', #{title}, '%')
              </if>
              <if test="messageType != null">
                AND m.message_type = #{messageType}
              </if>
              <if test="publishStartDate != null">
                AND m.publish_time &gt;= #{publishStartDate}
              </if>
              <if test="publishEndDate != null">
                AND m.publish_time &lt; DATE_ADD(#{publishEndDate}, INTERVAL 1 DAY)
              </if>
              AND (
                m.receiver_scope = 0
                OR (
                  m.receiver_scope = 1
                  AND JSON_CONTAINS(m.receiver_values, CAST(#{userId} AS CHAR))
                )
                <if test="roleIds != null and roleIds.size() > 0">
                  OR (
                    m.receiver_scope = 2
                    AND JSON_OVERLAPS(
                      m.receiver_values,
                      JSON_ARRAY(
                        <foreach collection="roleIds" item="roleId" separator=",">
                          #{roleId}
                        </foreach>
                      )
                    )
                  )
                </if>
                <if test="deptId != null">
                  OR (
                    m.receiver_scope = 3
                    AND JSON_CONTAINS(m.receiver_values, CAST(#{deptId} AS CHAR))
                  )
                </if>
              )
            ORDER BY m.publish_time DESC, m.id DESC
            </script>
            """)
    Page<SysMessage> selectInboxPage(
            Page<SysMessage> page,
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId,
            @Param("title") String title,
            @Param("messageType") Integer messageType,
            @Param("publishStartDate") LocalDate publishStartDate,
            @Param("publishEndDate") LocalDate publishEndDate,
            @Param("readStatus") Integer readStatus);

    /**
     * 统计指定用户的未读站内消息数。
     *
     * <p>没有接收状态记录的消息按未读处理，同时根据用户、角色和部门匹配消息接收范围。
     */
    @Select(
            """
            <script>
            SELECT COUNT(1)
            FROM sys_message m
            LEFT JOIN sys_message_receiver r
              ON r.message_id = m.id
             AND r.receiver_type = #{receiverType}
             AND r.receiver_id = #{userId}
             AND r.is_deleted = 0
            WHERE m.is_deleted = 0
              AND m.status = 1
              AND m.receiver_type = #{receiverType}
              AND m.channel = 'IN_APP'
              AND m.publish_time &lt;= NOW()
              AND (r.id IS NULL OR r.read_status = 0)
              AND (
                m.receiver_scope = 0
                OR (
                  m.receiver_scope = 1
                  AND JSON_CONTAINS(m.receiver_values, CAST(#{userId} AS CHAR))
                )
                <if test="roleIds != null and roleIds.size() > 0">
                  OR (
                    m.receiver_scope = 2
                    AND JSON_OVERLAPS(
                      m.receiver_values,
                      JSON_ARRAY(
                        <foreach collection="roleIds" item="roleId" separator=",">
                          #{roleId}
                        </foreach>
                      )
                    )
                  )
                </if>
                <if test="deptId != null">
                  OR (
                    m.receiver_scope = 3
                    AND JSON_CONTAINS(m.receiver_values, CAST(#{deptId} AS CHAR))
                  )
                </if>
              )
            </script>
            """)
    Long countUnreadInbox(
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId);

    /**
     * 查询指定用户可见的站内消息ID。
     *
     * <p>用于批量标记已读或清空收件箱；{@code readStatus} 为空时查询全部未删除消息，否则按指定阅读状态过滤。
     */
    @Select(
            """
            <script>
            SELECT m.id
            FROM sys_message m
            LEFT JOIN sys_message_receiver r
              ON r.message_id = m.id
             AND r.receiver_type = #{receiverType}
             AND r.receiver_id = #{userId}
             AND r.is_deleted = 0
            WHERE m.is_deleted = 0
              AND m.status = 1
              AND m.receiver_type = #{receiverType}
              AND m.channel = 'IN_APP'
              AND m.publish_time &lt;= NOW()
              AND (r.id IS NULL OR r.read_status != 2)
              <if test="readStatus != null">
                <choose>
                  <when test="readStatus == 0">
                    AND (r.id IS NULL OR r.read_status = 0)
                  </when>
                  <otherwise>
                    AND r.read_status = #{readStatus}
                  </otherwise>
                </choose>
              </if>
              AND (
                m.receiver_scope = 0
                OR (
                  m.receiver_scope = 1
                  AND JSON_CONTAINS(m.receiver_values, CAST(#{userId} AS CHAR))
                )
                <if test="roleIds != null and roleIds.size() > 0">
                  OR (
                    m.receiver_scope = 2
                    AND JSON_OVERLAPS(
                      m.receiver_values,
                      JSON_ARRAY(
                        <foreach collection="roleIds" item="roleId" separator=",">
                          #{roleId}
                        </foreach>
                      )
                    )
                  )
                </if>
                <if test="deptId != null">
                  OR (
                    m.receiver_scope = 3
                    AND JSON_CONTAINS(m.receiver_values, CAST(#{deptId} AS CHAR))
                  )
                </if>
              )
            </script>
            """)
    List<Long> selectInboxMessageIds(
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId,
            @Param("readStatus") Integer readStatus);
}
