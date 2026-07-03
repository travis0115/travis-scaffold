package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.job.api.request.OpsJobWriteReq;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.model.OpsJobCalendarConfig;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = BaseMapperConfig.class)
public interface OpsJobConverter {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "calendarConfig", source = "req", qualifiedByName = "calendarConfig")
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "priority", qualifiedByName = "defaultPriority")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "logRetentionDays", qualifiedByName = "defaultLogRetentionDays")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
    OpsJob toEntity(OpsJobWriteReq req);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "calendarConfig", source = "req", qualifiedByName = "calendarConfig")
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "priority", qualifiedByName = "defaultPriority")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "logRetentionDays", qualifiedByName = "defaultLogRetentionDays")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
    void update(OpsJobWriteReq req, @MappingTarget OpsJob job);

    @Named("calendarConfig")
    default String calendarConfig(OpsJobWriteReq req) {
        var calendar =
                new OpsJobCalendarConfig(
                        req.getExcludedDates(),
                        req.getExcludedWeekdays(),
                        req.getDailyStartTime(),
                        req.getDailyEndTime());
        return calendar.isEmpty() ? null : JsonUtil.toJsonString(calendar);
    }

    @Named("serializeIds")
    default String serializeIds(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? null
                : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Named("defaultPriority")
    default Integer defaultPriority(Integer priority) {
        return priority == null ? 5 : priority;
    }

    @Named("defaultMisfirePolicy")
    default Integer defaultMisfirePolicy(Integer misfirePolicy) {
        return misfirePolicy == null ? 0 : misfirePolicy;
    }

    @Named("defaultLogRetentionDays")
    default Integer defaultLogRetentionDays(Integer logRetentionDays) {
        return logRetentionDays == null ? 30 : logRetentionDays;
    }

    @Named("defaultParams")
    default String defaultParams(String params) {
        return params == null || params.isBlank() ? "{}" : params;
    }
}
