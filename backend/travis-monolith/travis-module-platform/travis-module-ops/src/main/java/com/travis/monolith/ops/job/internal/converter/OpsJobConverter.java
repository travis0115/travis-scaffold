package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.job.api.request.OpsJobWriteReq;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.model.OpsJobCalendarConfig;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    OpsJob copy(OpsJob source);

    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    @Mapping(target = "excludedDates", source = "calendarConfig", qualifiedByName = "excludedDates")
    @Mapping(
            target = "excludedWeekdays",
            source = "calendarConfig",
            qualifiedByName = "excludedWeekdays")
    @Mapping(
            target = "dailyStartTime",
            source = "calendarConfig",
            qualifiedByName = "dailyStartTime")
    @Mapping(target = "dailyEndTime", source = "calendarConfig", qualifiedByName = "dailyEndTime")
    OpsJobPageResp toPageResp(OpsJob job);

    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    @Mapping(target = "excludedDates", source = "calendarConfig", qualifiedByName = "excludedDates")
    @Mapping(
            target = "excludedWeekdays",
            source = "calendarConfig",
            qualifiedByName = "excludedWeekdays")
    @Mapping(
            target = "dailyStartTime",
            source = "calendarConfig",
            qualifiedByName = "dailyStartTime")
    @Mapping(target = "dailyEndTime", source = "calendarConfig", qualifiedByName = "dailyEndTime")
    OpsJobDetailResp toDetailResp(OpsJob job);

    @Mapping(target = "handlerAvailable", ignore = true)
    @Mapping(target = "ownerUsername", ignore = true)
    @Mapping(target = "nextFireTime", ignore = true)
    @Mapping(target = "alertUserIds", qualifiedByName = "parseIds")
    @Mapping(target = "excludedDates", source = "calendarConfig", qualifiedByName = "excludedDates")
    @Mapping(
            target = "excludedWeekdays",
            source = "calendarConfig",
            qualifiedByName = "excludedWeekdays")
    @Mapping(
            target = "dailyStartTime",
            source = "calendarConfig",
            qualifiedByName = "dailyStartTime")
    @Mapping(target = "dailyEndTime", source = "calendarConfig", qualifiedByName = "dailyEndTime")
    OpsJobExportResp toExportResp(OpsJob job);

    OpsJobLogPageResp toLogPageResp(OpsJobLog log);

    OpsJobLogDetailResp toLogDetailResp(OpsJobLog log);

    OpsJobLogExportResp toLogExportResp(OpsJobLog log);

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

    @Named("parseIds")
    default List<Long> parseIds(String ids) {
        return ids == null || ids.isBlank()
                ? List.of()
                : java.util.Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
    }

    @Named("excludedDates")
    default List<LocalDate> excludedDates(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.excludedDates();
    }

    @Named("excludedWeekdays")
    default List<Integer> excludedWeekdays(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.excludedWeekdays();
    }

    @Named("dailyStartTime")
    default LocalTime dailyStartTime(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.dailyStartTime();
    }

    @Named("dailyEndTime")
    default LocalTime dailyEndTime(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.dailyEndTime();
    }

    default OpsJobCalendarConfig parseCalendarConfig(String calendarConfig) {
        return calendarConfig == null
                ? null
                : JsonUtil.parseObject(calendarConfig, OpsJobCalendarConfig.class);
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
