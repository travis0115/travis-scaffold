package com.travis.infrastructure.framework.jackson.core.validator;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * 全局放行的校验器
 * 仅在缓存来源可信时使用，否则请自定义校验器或使用默认的{@link PolymorphicTypeValidator}
 *
 * @author travis
 */
public class LaissezFaireSubTypeValidator extends PolymorphicTypeValidator.Base {

    @Override
    public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType, String subClassName) {
        return Validity.ALLOWED;
    }

    @Override
    public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType) {
        return Validity.ALLOWED;
    }
}