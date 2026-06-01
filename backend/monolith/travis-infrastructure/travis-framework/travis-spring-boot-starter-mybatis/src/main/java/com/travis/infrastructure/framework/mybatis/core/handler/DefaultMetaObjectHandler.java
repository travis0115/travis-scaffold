package com.travis.infrastructure.framework.mybatis.core.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 默认字段自动填充处理器
 * <p>
 * 在插入和更新时自动填充 createTime、updateTime、createBy、updateBy、deleted 等审计字段。
 * <p>
 * 当前用户ID通过 Sa-Token 的 {@link StpUtil#getLoginIdAsLong()} 获取。
 * 在未登录场景（如定时任务、系统初始化）下兜底返回 0L。
 *
 * @author travis
 */
public class DefaultMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        var userId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, Boolean.FALSE);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        var userId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
    }

    /**
     * 获取当前登录用户ID
     * <p>
     * 通过 Sa-Token 获取，未登录时兜底返回 0L
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return 0L;
        }
    }
}
