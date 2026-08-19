package com.travis.monolith.ops.errorlog.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysErrorLogMapper extends BaseMapperX<SysErrorLog> {
    /** 按异常指纹原子新增或更新聚合记录。 */
    int upsertAggregate(SysErrorLog errorLog);

    /** 查询指定异常指纹对应的聚合记录 ID。 */
    Long selectIdByFingerprint(@Param("fingerprint") String fingerprint);

    /** 仅当错误日志仍为待处理状态时更新处理结果。 */
    int handleIfPending(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("handledBy") Long handledBy,
            @Param("handledTime") LocalDateTime handledTime,
            @Param("handleRemark") String handleRemark);

    /** 批量更新全部待处理错误日志。 */
    int handleAllPending(
            @Param("status") Integer status,
            @Param("handledBy") Long handledBy,
            @Param("handledTime") LocalDateTime handledTime,
            @Param("handleRemark") String handleRemark);
}
