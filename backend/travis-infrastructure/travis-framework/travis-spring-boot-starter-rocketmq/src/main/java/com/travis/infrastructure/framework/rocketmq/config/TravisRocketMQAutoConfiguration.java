package com.travis.infrastructure.framework.rocketmq.config;

import com.travis.infrastructure.common.event.Event;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.framework.rocketmq.core.RocketMQMessagePublisher;
import com.travis.infrastructure.framework.rocketmq.core.RocketMQProducerUtil;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * RocketMQ 自动配置类，基于 rocketmq-spring-boot-starter 进行封装
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>注入 {@link RocketMQProducerUtil}，提供底层静态方法封装（内部使用）
 *   <li>注入 {@link MessagePublisher}，基于 {@link Event} 事件枚举提供类型安全的事件发布能力（推荐业务层使用）
 * </ul>
 *
 * <p>使用方式：在 pom.xml 中引入本 starter 后，注入 {@code MessagePublisher}， 通过 {@code
 * messagePublisher.publish(MyEvent.XXX, payload)} 发布事件。
 *
 * @author travis
 */
@AutoConfiguration
@ConditionalOnClass(RocketMQClientTemplate.class)
@AutoConfigureAfter(name = "org.apache.rocketmq.client.autoconfigure.RocketMQAutoConfiguration")
public class TravisRocketMQAutoConfiguration {

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
