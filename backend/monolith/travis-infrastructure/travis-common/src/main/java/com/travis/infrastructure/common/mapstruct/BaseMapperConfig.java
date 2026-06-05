package com.travis.infrastructure.common.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 全局配置
 * <p>
 * 统一所有 Converter 的 componentModel 和 unmappedTargetPolicy，
 * 避免每个 Converter 重复声明。
 *
 * @author travis
 */
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BaseMapperConfig {
}
