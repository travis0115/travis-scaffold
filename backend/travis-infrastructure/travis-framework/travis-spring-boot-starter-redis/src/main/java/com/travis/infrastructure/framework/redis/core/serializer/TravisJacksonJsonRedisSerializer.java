package com.travis.infrastructure.framework.redis.core.serializer;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonObjectReader;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Redis JSON 序列化器，保留 root value 的类型信息，避免 Long 等标量值按 Object 读回时丢失类型。 */
public class TravisJacksonJsonRedisSerializer implements RedisSerializer<Object> {

    private final GenericJacksonJsonRedisSerializer delegate;

    public TravisJacksonJsonRedisSerializer(ObjectMapper mapper) {
        if (!(mapper instanceof JsonMapper jsonMapper)) {
            throw new IllegalArgumentException("ObjectMapper must be a JsonMapper");
        }
        this.delegate =
                GenericJacksonJsonRedisSerializer.builder(jsonMapper::rebuild)
                        .enableSpringCacheNullValueSupport()
                        .reader(JacksonObjectReader.create())
                        .writer(
                                (objectMapper, source) ->
                                        objectMapper
                                                .writerFor(Object.class)
                                                .writeValueAsBytes(source))
                        .build();
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        return delegate.serialize(value);
    }

    @Override
    public Object deserialize(byte[] source) throws SerializationException {
        var value = delegate.deserialize(source);
        return value instanceof NullValue ? NullValue.INSTANCE : value;
    }
}
