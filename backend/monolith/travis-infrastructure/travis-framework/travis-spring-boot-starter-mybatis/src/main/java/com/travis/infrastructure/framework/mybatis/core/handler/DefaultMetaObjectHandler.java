package com.travis.infrastructure.framework.mybatis.core.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.Setter;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * 默认字段自动填充处理器
 * <p>
 * 在插入和更新时自动填充 createTime、updateTime、createBy、updateBy、deleted 等审计字段。
 * <p>
 * 当前用户ID通过 {@link #currentUserIdSupplier} 获取，默认返回 0L。
 * 业务层可通过 {@link #setCurrentUserIdSupplier(Supplier)} 注入实际的用户获取逻辑。
 *
 * @author travis
 */
public class DefaultMetaObjectHandler implements MetaObjectHandler {

    @Setter
    private static Supplier<Long> currentUserIdSupplier = () -> 0L;

    @Override
    public void insertFill(MetaObject metaObject) {
        var userId =  StpUtil.getLoginIdAsLong();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, Boolean.FALSE);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserIdSupplier.get());
    }
}
