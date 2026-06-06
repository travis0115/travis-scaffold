package com.travis.infrastructure.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 默认字段自动填充处理器
 *
 * <p>仅填充纯技术字段：createTime、updateTime、isDeleted、version。 业务审计字段（createBy、updateBy）由业务模块的
 * MetaObjectHandler 负责。
 *
 * <p>可通过注册自定义 {@link MetaObjectHandler} Bean 完全覆盖此默认实现。
 *
 * @author travis
 */
public class DefaultMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
