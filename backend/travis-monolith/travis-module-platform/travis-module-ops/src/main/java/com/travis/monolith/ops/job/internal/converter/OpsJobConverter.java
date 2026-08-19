package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** 定时任务及执行日志对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface OpsJobConverter {

    /** 将任务写入参数转换为任务实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isBuiltin", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
    OpsJob toEntity(OpsJobCreateReq req);

    /** 将任务写入参数更新到已有实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isBuiltin", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
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

    /** 将任务实体转换为分页响应。 */
    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "lastExecutionTime", ignore = true)
    @Mapping(target = "lastExecutionStatus", ignore = true)
    @Mapping(target = "createByUsername", ignore = true)
    OpsJobPageResp toPageResp(OpsJob job);

    /** 将任务实体转换为详情响应。 */
    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    OpsJobDetailResp toDetailResp(OpsJob job);

    /** 将调度预览参数转换为临时任务实体。 */
    OpsJob toPreviewEntity(OpsJobPreviewReq req);

    /** 将执行日志实体转换为分页响应。 */
    OpsJobLogPageResp toLogPageResp(OpsJobLog log);

    /** 将执行日志实体转换为详情响应。 */
    OpsJobLogDetailResp toLogDetailResp(OpsJobLog log);

    /** 将用户 ID 列表序列化为逗号分隔字符串。 */
    @Named("serializeIds")
    default String serializeIds(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? null
                : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /** 将逗号分隔的用户 ID 字符串解析为列表。 */
    @Named("parseIds")
    default List<Long> parseIds(String ids) {
        return ids == null || ids.isBlank()
                ? List.of()
                : java.util.Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
    }

    /** 补充默认错过执行策略。 */
    @Named("defaultMisfirePolicy")
    default Integer defaultMisfirePolicy(Integer misfirePolicy) {
        return misfirePolicy == null ? OpsJobMisfirePolicy.SMART.getValue() : misfirePolicy;
    }

    /** 将空任务参数规范化为空 JSON 对象。 */
    @Named("defaultParams")
    default String defaultParams(String params) {
        return params == null || params.isBlank() ? "{}" : params;
    }
}
