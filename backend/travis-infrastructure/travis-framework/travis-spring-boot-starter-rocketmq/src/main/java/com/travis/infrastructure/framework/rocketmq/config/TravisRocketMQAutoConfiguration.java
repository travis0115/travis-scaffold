package com.travis.infrastructure.framework.rocketmq.config;

import com.travis.infrastructure.common.event.Event;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.common.event.TopicType;
import com.travis.infrastructure.framework.rocketmq.core.RocketMQInitializer;
import com.travis.infrastructure.framework.rocketmq.core.RocketMQMessagePublisher;
import com.travis.infrastructure.framework.rocketmq.core.RocketMQProducerUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

/**
 * RocketMQ 自动配置类，基于 rocketmq-spring-boot-starter 进行封装
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>注入 {@link RocketMQProducerUtil}，提供底层静态方法封装（内部使用）
 *   <li>注入 {@link MessagePublisher}，基于 {@link Event} 事件枚举提供类型安全的事件发布能力（推荐业务层使用）
 *   <li>注入 {@link RocketMQInitializer}，在 Push Consumer 启动前自动确保 Topic 和消费者分组已存在，并根据 Event 的 {@link
 *       TopicType} 创建对应类型的 Topic
 * </ul>
 *
 * <p>使用方式：在 pom.xml 中引入本 starter 后，注入 {@code MessagePublisher}， 通过 {@code
 * messagePublisher.publish(MyEvent.XXX, payload)} 发布事件。
 *
 * @author travis
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(RocketMQClientTemplate.class)
@AutoConfigureAfter(name = "org.apache.rocketmq.client.autoconfigure.RocketMQAutoConfiguration")
public class TravisRocketMQAutoConfiguration {

    /**
     * 注册 RocketMQ 自动初始化器，在 Push Consumer 启动前通过 Admin API 确保所有 Topic 和消费者分组已存在。 仅当显式开启且 NameServer
     * 地址已配置时才激活。
     *
     * @param applicationContext Spring 应用上下文
     * @param enabled 是否启用自动初始化
     * @param namesrvAddr NameServer 地址
     */
    @Bean
    @ConditionalOnBean(RocketMQClientTemplate.class)
    @ConditionalOnClass(name = "org.apache.rocketmq.tools.admin.DefaultMQAdminExt")
    public RocketMQInitializer rocketMQInitializer(
            ApplicationContext applicationContext,
            @Value("${travis.rocketmq.auto-initialize.enabled:false}") boolean enabled,
            @Value("${travis.rocketmq.auto-initialize.namesrv-addr:}") String namesrvAddr) {
        Map<String, TopicType> topicTypes = scanEventTopicTypes(applicationContext);
        return new RocketMQInitializer(applicationContext, enabled, namesrvAddr, topicTypes);
    }

    /**
     * 扫描 classpath 下所有实现 {@link Event} 接口的枚举类，收集 Topic → TopicType 映射。
     *
     * <p>按 {@code *Event.class} 命名约定扫描，提取每个枚举值的 {@link Event#getTopic()} 和 {@link
     * Event#getTopicType()}。 同一 Topic 的多个 Event 值必须保持 TopicType 一致，否则应用启动失败。
     *
     * @param ctx Spring 应用上下文
     * @return topic → TopicType 映射
     */
    private Map<String, TopicType> scanEventTopicTypes(ApplicationContext ctx) {
        Map<String, TopicType> result = new HashMap<>();
        try {
            var resolver = new PathMatchingResourcePatternResolver(ctx.getClassLoader());
            var readerFactory = new CachingMetadataReaderFactory(ctx.getClassLoader());
            Resource[] resources = resolver.getResources("classpath*:com/travis/**/*Event.class");
            for (Resource resource : resources) {
                try {
                    var metadata = readerFactory.getMetadataReader(resource);
                    String className = metadata.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(className, false, ctx.getClassLoader());
                    if (clazz.isEnum() && Event.class.isAssignableFrom(clazz)) {
                        for (Object constant : clazz.getEnumConstants()) {
                            Event event = (Event) constant;
                            TopicType existingType =
                                    result.putIfAbsent(event.getTopic(), event.getTopicType());
                            if (existingType != null && existingType != event.getTopicType()) {
                                throw new IllegalStateException(
                                        "RocketMQ topic '"
                                                + event.getTopic()
                                                + "' is used with multiple message types: "
                                                + existingType
                                                + " and "
                                                + event.getTopicType());
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    throw e;
                } catch (Throwable ignored) {
                    // 跳过无法加载的类（如第三方依赖中的 Event 类）
                }
            }
            if (!result.isEmpty()) {
                log.debug("[RocketMQ] Discovered topic types from Event enums: {}", result);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.debug("[RocketMQ] Failed to scan Event topic types, defaulting to NORMAL", e);
        }
        return result;
    }

    /** 创建 RocketMQProducerUtil Bean，注入 RocketMQClientTemplate（底层工具，供 MessagePublisher 内部调用） */
    @Bean
    @ConditionalOnBean(RocketMQClientTemplate.class)
    public RocketMQProducerUtil rocketMQProducerUtil(
            RocketMQClientTemplate rocketMQClientTemplate) {
        var util = new RocketMQProducerUtil();
        util.setRocketMQClientTemplate(rocketMQClientTemplate);
        return util;
    }

    /** 创建 MessagePublisher Bean（RocketMQ 实现），提供事件驱动的消息发布接口（推荐业务层使用） */
    @Bean
    @ConditionalOnBean(RocketMQProducerUtil.class)
    @ConditionalOnMissingBean(MessagePublisher.class)
    public MessagePublisher messagePublisher() {
        return new RocketMQMessagePublisher();
    }
}
