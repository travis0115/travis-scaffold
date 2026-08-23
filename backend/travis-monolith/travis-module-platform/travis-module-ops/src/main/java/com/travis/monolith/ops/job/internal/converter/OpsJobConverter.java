package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.model.OpsJobExecutionConfig;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** 定时任务对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface OpsJobConverter {

    /** 将任务写入参数转换为任务实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isBuiltin", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    OpsJob toEntity(OpsJobCreateReq req);

    /** 将任务写入参数更新到已有实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isBuiltin", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    void update(OpsJobUpdateReq req, @MappingTarget OpsJob job);

    /** 复制任务配置，并清除实体审计字段。 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isBuiltin", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    OpsJob copy(OpsJob source);

    /** 将任务实体转换为响应。 */
    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "lastExecutionTime", ignore = true)
    @Mapping(target = "lastExecutionStatus", ignore = true)
    @Mapping(target = "createByUsername", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    OpsJobResp toResp(OpsJob job);

    /** 将调度预览参数转换为临时任务实体。 */
    OpsJob toEntity(OpsJobPreviewReq req);

    /** 将任务实体转换为执行观察器所需配置。 */
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    OpsJobExecutionConfig toExecutionConfig(OpsJob job);

    /** 将用户 ID 列表序列化为逗号分隔字符串。 */
    @Named("serializeIds")
    default String serializeIds(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? null
                : ids.stream().distinct().map(String::valueOf).collect(Collectors.joining(","));
    }

    /** 将逗号分隔的用户 ID 字符串解析为列表。 */
    @Named("parseIds")
    default List<Long> parseIds(String ids) {
        return ids == null || ids.isBlank()
                ? List.of()
                : java.util.Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
    }
}
