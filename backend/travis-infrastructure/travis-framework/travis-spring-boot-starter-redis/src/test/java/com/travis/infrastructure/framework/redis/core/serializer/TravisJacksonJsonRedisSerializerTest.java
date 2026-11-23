package com.travis.infrastructure.framework.redis.core.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cache.support.NullValue;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

class TravisJacksonJsonRedisSerializerTest {

    private final TravisJacksonJsonRedisSerializer serializer =
            new TravisJacksonJsonRedisSerializer(
                    JsonMapper.builder()
                            .activateDefaultTypingAsProperty(
                                    BasicPolymorphicTypeValidator.builder()
                                            .allowIfBaseType(Object.class)
                                            .allowIfSubType((context, type) -> true)
                                            .build(),
                                    DefaultTyping.NON_FINAL_AND_ENUMS,
                                    "@class")
                            .build());

    @Test
    void shouldRoundTripSpringCacheNullValue() {
        var serialized = serializer.serialize(NullValue.INSTANCE);

        assertThat(serializer.deserialize(serialized)).isSameAs(NullValue.INSTANCE);
    }

    @Test
    void shouldKeepScalarType() {
        var serialized = serializer.serialize(1L);

        assertThat(serializer.deserialize(serialized)).isEqualTo(1L);
    }
}
