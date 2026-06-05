package com.travis.infrastructure.framework.satoken.core;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * StpLogic 统一入口，根据 YAML 配置自动创建所有 {@link StpLogic} 实例。
 * <p>
 * 启动时从 {@code travis.web.security.auth-rules} 中读取所有 loginType，
 * 为每个唯一的 loginType 自动创建一个 {@link StpLogicJwtForSimple} 实例，
 * 无需在业务模块手动注册 Bean。
 * <p>
 * 用法：
 * <pre>
 *   StpKit.of(LoginType.ADMIN).login(userId);
 *   StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
 *   StpKit.of("admin").checkLogin();
 *   StpKit.all();
 * </pre>
 * <p>
 * SpEL 表达式：
 * <pre>
 *   T(...StpKit).getLoginIdAsLong(T(...LoginType).ADMIN)
 * </pre>
 * <p>
 * 新增 LoginType 只需：
 * <ol>
 *   <li>在 YAML 的 auth-rules 中添加一条 login-type 配置</li>
 *   <li>在 LoginType 枚举中添加对应枚举值</li>
 * </ol>
 * 无需修改本类，无需手动注册 Bean。
 *
 * @author travis
 */
public class StpKit {

    private static volatile StpKit INSTANCE;

    /** loginType → StpLogic 实例，构造时即确定，不可变 */
    private final Map<String, StpLogic> logicMap;

    /**
     * 根据配置属性创建 StpKit 实例，为每个唯一的 loginType 创建 StpLogic。
     *
     * @param properties Sa-Token 配置属性（从 YAML 绑定）
     */
    public StpKit(SaTokenProperties properties) {
        Map<String, StpLogic> map = new LinkedHashMap<>();
        for (SaTokenProperties.AuthRule rule : properties.getAuthRules()) {
            String loginType = rule.getLoginType();
            if (!map.containsKey(loginType)) {
                map.put(loginType, new StpLogicJwtForSimple(loginType));
            }
        }
        this.logicMap = Collections.unmodifiableMap(map);
        INSTANCE = this;
    }

    // ==================== 核心方法 ====================

    /**
     * 按 LoginType 枚举获取 StpLogic
     *
     * @param loginType 登录类型枚举
     * @return 对应的 StpLogic 实例
     * @throws IllegalStateException 该 loginType 未在 YAML 中配置
     */
    public static StpLogic of(LoginType loginType) {
        return require(loginType.getCode());
    }

    /**
     * 按 loginType 字符串获取 StpLogic
     *
     * @param loginType 登录类型标识
     * @return 对应的 StpLogic 实例
     * @throws IllegalStateException 该 loginType 未在 YAML 中配置
     */
    public static StpLogic of(String loginType) {
        return require(loginType);
    }

    /**
     * 获取所有已创建的 StpLogic 实例
     */
    public static Collection<StpLogic> all() {
        return INSTANCE.logicMap.values();
    }

    // ==================== SpEL 便捷方法 ====================

    /**
     * SpEL 桥接：获取指定 loginType 的当前登录用户 ID
     * <p>
     * 用法：{@code T(...StpKit).getLoginIdAsLong(T(...LoginType).ADMIN)}
     */
    public static long getLoginIdAsLong(LoginType type) {
        return of(type).getLoginIdAsLong();
    }

    /**
     * SpEL 桥接：获取指定 loginType 的当前 Token 值
     */
    public static String getTokenValue(LoginType type) {
        return of(type).getTokenValue();
    }

    // ==================== 内部方法 ====================

    private static StpLogic require(String loginType) {
        StpLogic logic = INSTANCE.logicMap.get(loginType);
        if (logic == null) {
            throw new IllegalStateException(
                    "No StpLogic for loginType: " + loginType + ", available: " + INSTANCE.logicMap.keySet());
        }
        return logic;
    }
}
