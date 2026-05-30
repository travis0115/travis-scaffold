package com.travis.infrastructure.framework.jackson.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.travis.infrastructure.framework.jackson.core.util.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson自动配置类
 * @author travis
 */
@AutoConfiguration(after = JacksonProperties.class)
@EnableConfigurationProperties({JacksonProperties.class})
@Import(JsonUtils.class)
public class TravisJacksonAutoConfiguration {

    /**
     * 创建自定义Java8时间序列化解析模块
     * 该模块用于处理LocalDateTime类型的JSON序列化和反序列化，支持通过全局配置的date-format进行格式化
     */
    @Bean
    public JacksonModule javaTimeModule(JacksonProperties jacksonProperties) {
        var javaTimeModule = new SimpleModule();
        if (CharSequenceUtil.isNotBlank(jacksonProperties.getDateFormat())) {
            javaTimeModule.addSerializer(LocalDateTime.class,
                            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(jacksonProperties.getDateFormat())))
                    .addDeserializer(LocalDateTime.class,
                            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(jacksonProperties.getDateFormat())));
        }
        return javaTimeModule;
    }


}
