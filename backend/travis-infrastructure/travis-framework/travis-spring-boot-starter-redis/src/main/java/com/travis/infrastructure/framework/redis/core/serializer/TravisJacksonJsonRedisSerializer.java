package com.travis.infrastructure.framework.redis.core.serializer;

import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonObjectReader;
import tools.jackson.databind.ObjectMapper;

/** Redis JSON 序列化器，保留 root value 的类型信息，避免 Long 等标量值按 Object 读回时丢失类型。 */
public class TravisJacksonJsonRedisSerializer extends GenericJacksonJsonRedisSerializer {

    public TravisJacksonJsonRedisSerializer(ObjectMapper mapper) {
        super(
                mapper,
                JacksonObjectReader.create(),
                (objectMapper, source) ->
                        objectMapper.writerFor(Object.class).writeValueAsBytes(source));
    }
}
