package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
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

/** 定时任务及执行日志对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface OpsJobConverter {

    /** 将任务写入参数转换为任务实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "calendarConfig", source = "req", qualifiedByName = "calendarConfig")
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "priority", qualifiedByName = "defaultPriority")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "logRetentionDays", qualifiedByName = "defaultLogRetentionDays")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
    OpsJob toEntity(OpsJobWriteReq req);

    /** 将任务写入参数更新到已有实体。 */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "calendarConfig", source = "req", qualifiedByName = "calendarConfig")
    @Mapping(target = "alertUserIds", qualifiedByName = "serializeIds")
    @Mapping(target = "priority", qualifiedByName = "defaultPriority")
    @Mapping(target = "misfirePolicy", qualifiedByName = "defaultMisfirePolicy")
    @Mapping(target = "logRetentionDays", qualifiedByName = "defaultLogRetentionDays")
    @Mapping(target = "params", qualifiedByName = "defaultParams")
    void update(OpsJobWriteReq req, @MappingTarget OpsJob job);

    /** 复制任务配置，并清除实体审计字段。 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    OpsJob copy(OpsJob source);

    /** 将任务实体转换为分页响应。 */
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

    /** 将任务实体转换为详情响应。 */
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

    /** 将任务实体转换为导出响应。 */
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

    /** 将执行日志实体转换为分页响应。 */
    OpsJobLogPageResp toLogPageResp(OpsJobLog log);

    /** 将执行日志实体转换为详情响应。 */
    OpsJobLogDetailResp toLogDetailResp(OpsJobLog log);

    /** 将执行日志实体转换为导出响应。 */
    OpsJobLogExportResp toLogExportResp(OpsJobLog log);

    /** 将任务日历字段序列化为存储字符串。 */
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

    /** 从日历配置中解析排除日期。 */
    @Named("excludedDates")
    default List<LocalDate> excludedDates(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.excludedDates();
    }

    /** 从日历配置中解析排除星期。 */
    @Named("excludedWeekdays")
    default List<Integer> excludedWeekdays(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.excludedWeekdays();
    }

    /** 从日历配置中解析每日执行时段起点。 */
    @Named("dailyStartTime")
    default LocalTime dailyStartTime(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.dailyStartTime();
    }

    /** 从日历配置中解析每日执行时段终点。 */
    @Named("dailyEndTime")
    default LocalTime dailyEndTime(String calendarConfig) {
        var calendar = parseCalendarConfig(calendarConfig);
        return calendar == null ? null : calendar.dailyEndTime();
    }

    /** 解析序列化的任务日历配置。 */
    default OpsJobCalendarConfig parseCalendarConfig(String calendarConfig) {
        return calendarConfig == null
                ? null
                : JsonUtil.parseObject(calendarConfig, OpsJobCalendarConfig.class);
    }

    /** 补充默认调度优先级。 */
    @Named("defaultPriority")
    default Integer defaultPriority(Integer priority) {
        return priority == null ? 5 : priority;
    }

    /** 补充默认错过执行策略。 */
    @Named("defaultMisfirePolicy")
    default Integer defaultMisfirePolicy(Integer misfirePolicy) {
        return misfirePolicy == null ? OpsJobMisfirePolicy.SMART.getValue() : misfirePolicy;
    }

    /** 补充默认日志保留天数。 */
    @Named("defaultLogRetentionDays")
    default Integer defaultLogRetentionDays(Integer logRetentionDays) {
        return logRetentionDays == null ? 30 : logRetentionDays;
    }

    /** 将空任务参数规范化为空 JSON 对象。 */
    @Named("defaultParams")
    default String defaultParams(String params) {
        return params == null || params.isBlank() ? "{}" : params;
    }
}
