package com.travis.infrastructure.framework.redis.config;

import com.travis.infrastructure.framework.jackson.config.TravisJacksonAutoConfiguration;
import com.travis.infrastructure.framework.jackson.core.validator.LaissezFaireSubTypeValidator;
import com.travis.infrastructure.framework.redis.core.util.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;


/**
 * Redis 配置类
 *
 * @author travis
 */
@AutoConfiguration(after = TravisJacksonAutoConfiguration.class)
public class TravisRedisAutoConfiguration {

    private final ObjectMapper objectMapper;

    public TravisRedisAutoConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //创建 RedisTemplate 对象
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);

        var stringRedisSerializer = RedisSerializer.string();

        var jsonMapper = objectMapper.rebuild()
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)    //成功解析值后不验证是否存在额外内容(缓存内容可信)
                //不可全局设置，会导致Spring Boot Actuator等Object/JsonMapper污染
                //添加默认类型到属性中，LaissezFaireSubTypeValidator 是全局放行的校验器，相当于关闭了校验，仅在序列化对象可信时使用，否则请使用白名单校验器
                // BasicPolymorphicTypeValidator
                .activateDefaultTypingAsProperty(new LaissezFaireSubTypeValidator(),
                        DefaultTyping.NON_FINAL_AND_ENUMS, "@class")
                .build();
        var jsonRedisSerializer = new GenericJacksonJsonRedisSerializer(jsonMapper);


        //使用 String 序列化 KEY
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        //使用 JSON 序列化 VALUE
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建 RedisUtils Bean，注入redisTemplate
     *
     */
    @Bean
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate) {
        var utils = new RedisUtils();
        utils.setRedisTemplate(redisTemplate);
        return utils;
    }

}
