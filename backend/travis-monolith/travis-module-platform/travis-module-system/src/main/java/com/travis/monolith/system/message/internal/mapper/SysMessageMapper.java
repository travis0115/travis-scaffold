package com.travis.monolith.system.message.internal.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysMessageMapper extends BaseMapperX<SysMessage> {

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
              AND m.status = 2
              AND m.receiver_type = #{receiverType}
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
            ORDER BY COALESCE(m.publish_time, m.create_time) DESC, m.id DESC
            </script>
            """)
    Page<SysMessage> selectInboxPage(
            Page<SysMessage> page,
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId,
            @Param("title") String title,
            @Param("readStatus") Integer readStatus);

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
              AND m.status = 2
              AND m.receiver_type = #{receiverType}
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
    long countUnreadInbox(
            @Param("userId") Long userId,
            @Param("receiverType") String receiverType,
            @Param("roleIds") List<Long> roleIds,
            @Param("deptId") Long deptId);

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
              AND m.status = 2
              AND m.receiver_type = #{receiverType}
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
