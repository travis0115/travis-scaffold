package com.travis.infrastructure.framework.rocketmq.config;

import com.travis.infrastructure.framework.rocketmq.core.util.RocketMQProducerUtil;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * RocketMQ 自动配置类，基于 rocketmq-spring-boot-starter 进行封装
 *
 * <p>功能说明：
 *
 * <ul>
 *   <li>注入 {@link RocketMQProducerUtil}，提供静态方法封装常用发送操作
 * </ul>
 *
 * <p>使用方式：在 pom.xml 中引入本 starter 后，通过 {@code RocketMQProducerUtil.syncSendNormalMessage(...)}
 * 等静态方法发送消息。
 *
 * @author travis
 */
@AutoConfiguration
@ConditionalOnClass(RocketMQClientTemplate.class)
public class TravisRocketMQAutoConfiguration {

    /** 创建 RocketMQProducerUtil Bean，注入 RocketMQClientTemplate */
    @Bean
    @ConditionalOnBean(RocketMQClientTemplate.class)
    public RocketMQProducerUtil rocketMQProducerUtil(RocketMQClientTemplate rocketMQClientTemplate) {
        var util = new RocketMQProducerUtil();
        util.setRocketMQClientTemplate(rocketMQClientTemplate);
        return util;
    }
}
