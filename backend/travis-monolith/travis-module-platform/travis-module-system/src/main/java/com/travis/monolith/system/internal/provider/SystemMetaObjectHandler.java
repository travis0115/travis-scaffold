package com.travis.monolith.system.internal.provider;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 系统模块字段自动填充处理器
 *
 * <p>覆盖基础设施层的默认实现，在技术字段的基础上额外填充业务审计字段 createBy、updateBy。
 *
 * <p>当前用户ID通过 StpKit 获取，未登录时兜底返回 0L。
 *
 * @author travis
 */
@Component
public class SystemMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        var userId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        var userId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
    }

    private Long getCurrentUserId() {
        try {
            for (var logic : StpKit.all()) {
                if (logic.isLogin()) {
                    return logic.getLoginIdAsLong();
                }
            }
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
