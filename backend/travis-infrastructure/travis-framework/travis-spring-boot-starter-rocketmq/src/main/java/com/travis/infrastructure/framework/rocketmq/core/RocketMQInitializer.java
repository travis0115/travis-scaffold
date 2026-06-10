package com.travis.infrastructure.framework.rocketmq.core;

import com.travis.infrastructure.common.event.TopicType;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RocketMQ 自动初始化器，在 Push Consumer 启动之前确保所有需要的 Topic 和消费者分组已存在。
 *
 * <p>工作原理：
 *
 * <ol>
 *   <li>实现 {@link SmartLifecycle}，phase 设为 {@code Integer.MIN_VALUE}，确保在 Push Consumer（phase=0）之前启动
 *   <li>扫描所有标注 {@link RocketMQMessageListener} 的 Bean，收集去重后的 Topic 列表和消费者分组列表
 *   <li>结合 {@link com.travis.infrastructure.common.event.Event} 枚举确定 Topic 类型
 *   <li>通过 {@link DefaultMQAdminExt} 连接 NameServer，在所有 Master Broker 上创建对应类型的 Topic 和消费者分组
 * </ol>
 *
 * <p>配置项：
 *
 * <ul>
 *   <li>{@code travis.rocketmq.auto-initialize.enabled} — 是否启用，默认 {@code false}
 *   <li>{@code travis.rocketmq.auto-initialize.namesrv-addr} — NameServer 地址，必填
 * </ul>
 *
 * @author travis
 */
@Slf4j
public class RocketMQInitializer implements SmartLifecycle {

    private static final int DEFAULT_READ_QUEUE_NUM = 8;
    private static final int DEFAULT_WRITE_QUEUE_NUM = 8;

    private final ApplicationContext applicationContext;
    private final boolean enabled;
    private final String namesrvAddr;
    private final Map<String, TopicType> topicTypes;
    private volatile boolean running = false;

    public RocketMQInitializer(
            ApplicationContext applicationContext,
            boolean enabled,
            String namesrvAddr,
            Map<String, TopicType> topicTypes) {
        this.applicationContext = applicationContext;
        this.enabled = enabled;
        this.namesrvAddr = namesrvAddr;
        this.topicTypes = topicTypes != null ? topicTypes : Map.of();
    }

    @Override
    public void start() {
        if (!enabled) {
            log.debug("[RocketMQ] Auto-initialize is disabled, skipping");
            running = true;
            return;
        }

        Map<String, Object> listeners =
                applicationContext.getBeansWithAnnotation(RocketMQMessageListener.class);

        Map<String, TopicType> topicTypeMap = discoverTopics(listeners);
        Set<String> consumerGroups = discoverConsumerGroups(listeners);

        if (topicTypeMap.isEmpty() && consumerGroups.isEmpty()) {
            log.debug("[RocketMQ] No topics or consumer groups to initialize");
            running = true;
            return;
        }

        log.info(
                "[RocketMQ] Initializing via NameServer({}): {} topic(s), {} consumer group(s)",
                namesrvAddr,
                topicTypeMap.size(),
                consumerGroups.size());

        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(namesrvAddr);
        admin.setInstanceName("MQInitializer-" + System.currentTimeMillis());

        try {
            admin.start();

            // 初始化 Topic
            for (var entry : topicTypeMap.entrySet()) {
                String topic = entry.getKey();
                TopicMessageType targetType = toTopicMessageType(entry.getValue());
                if (!topicExists(admin, topic)) {
                    createTopic(admin, topic, targetType);
                } else if (!isTopicTypeMatched(admin, topic, targetType)) {
                    updateTopic(admin, topic, targetType);
                }
            }

            // 初始化消费者分组
            for (String group : consumerGroups) {
                if (!consumerGroupExists(admin, group)) {
                    createConsumerGroup(admin, group);
                }
            }
        } catch (Exception e) {
            log.warn(
                    "[RocketMQ] Failed to connect to NameServer '{}': {}",
                    namesrvAddr,
                    e.getMessage());
        } finally {
            admin.shutdown();
        }

        running = true;
    }

    // ======================== Topic 相关 ========================

    /** 检查 Topic 是否存在 */
    private boolean topicExists(DefaultMQAdminExt admin, String topic) {
        try {
            admin.examineTopicStats(topic);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 检查已有 Topic 的类型是否与目标类型一致 */
    private boolean isTopicTypeMatched(
            DefaultMQAdminExt admin, String topic, TopicMessageType targetType) {
        try {
            var clusterInfo = admin.examineBrokerClusterInfo();
            var brokerAddrTable = clusterInfo.getBrokerAddrTable();

            String firstBrokerAddr = null;
            for (var entry : brokerAddrTable.entrySet()) {
                String brokerAddr = entry.getValue().getBrokerAddrs().get(MixAll.MASTER_ID);
                if (brokerAddr != null) {
                    firstBrokerAddr = brokerAddr;
                    break;
                }
            }
            if (firstBrokerAddr == null) {
                log.warn("[RocketMQ] No master broker found, skipping type check for '{}'", topic);
                return true;
            }

            TopicConfig existingConfig = admin.examineTopicConfig(firstBrokerAddr, topic);
            TopicMessageType currentType = existingConfig.getTopicMessageType();

            if (currentType == targetType) {
                log.debug("[RocketMQ] Topic '{}' type matched ({})", topic, currentType);
                return true;
            }

            log.warn(
                    "[RocketMQ] Topic '{}' type mismatch: current={}, expected={}",
                    topic,
                    currentType,
                    targetType);
            return false;
        } catch (Exception e) {
            log.warn("[RocketMQ] Failed to check topic type '{}': {}", topic, e.getMessage());
            return true;
        }
    }

    private void createTopic(DefaultMQAdminExt admin, String topic, TopicMessageType targetType) {
        createOrUpdateTopic(admin, topic, targetType);
    }

    private void updateTopic(DefaultMQAdminExt admin, String topic, TopicMessageType targetType) {
        createOrUpdateTopic(admin, topic, targetType);
    }

    private void createOrUpdateTopic(DefaultMQAdminExt admin, String topic, TopicMessageType targetType) {
        try {
            var clusterInfo = admin.examineBrokerClusterInfo();
            var brokerAddrTable = clusterInfo.getBrokerAddrTable();

            Map<String, String> attributes = new HashMap<>();
            attributes.put("+message.type", targetType.name());

            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setTopicName(topic);
            topicConfig.setReadQueueNums(DEFAULT_READ_QUEUE_NUM);
            topicConfig.setWriteQueueNums(DEFAULT_WRITE_QUEUE_NUM);
            topicConfig.setAttributes(attributes);

            for (var entry : brokerAddrTable.entrySet()) {
                String brokerAddr = entry.getValue().getBrokerAddrs().get(MixAll.MASTER_ID);
                if (brokerAddr == null) {
                    continue;
                }
                admin.createAndUpdateTopicConfig(brokerAddr, topicConfig);
                log.info(
                        "[RocketMQ] Topic '{}' ({}) created/updated on broker '{}'",
                        topic,
                        targetType,
                        entry.getKey());
            }
        } catch (Exception e) {
            log.warn("[RocketMQ] Failed to create/update topic '{}': {}", topic, e.getMessage(), e);
        }
    }

    // ======================== 消费者分组相关 ========================

    /** 检查消费者分组是否已在所有 Master Broker 上存在 */
    private boolean consumerGroupExists(DefaultMQAdminExt admin, String group) {
        try {
            var clusterInfo = admin.examineBrokerClusterInfo();
            var brokerAddrTable = clusterInfo.getBrokerAddrTable();

            for (var entry : brokerAddrTable.entrySet()) {
                String brokerAddr = entry.getValue().getBrokerAddrs().get(MixAll.MASTER_ID);
                if (brokerAddr == null) {
                    continue;
                }
                SubscriptionGroupConfig config =
                        admin.examineSubscriptionGroupConfig(brokerAddr, group);
                if (config == null) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.debug(
                    "[RocketMQ] Failed to check consumer group '{}': {}",
                    group,
                    e.getMessage());
            return false;
        }
    }

    /** 在所有 Master Broker 上创建消费者分组 */
    private void createConsumerGroup(DefaultMQAdminExt admin, String group) {
        try {
            var clusterInfo = admin.examineBrokerClusterInfo();
            var brokerAddrTable = clusterInfo.getBrokerAddrTable();

            SubscriptionGroupConfig groupConfig = new SubscriptionGroupConfig();
            groupConfig.setGroupName(group);

            for (var entry : brokerAddrTable.entrySet()) {
                String brokerAddr = entry.getValue().getBrokerAddrs().get(MixAll.MASTER_ID);
                if (brokerAddr == null) {
                    continue;
                }
                admin.createAndUpdateSubscriptionGroupConfig(brokerAddr, groupConfig);
                log.info(
                        "[RocketMQ] Consumer group '{}' created on broker '{}'",
                        group,
                        entry.getKey());
            }
        } catch (Exception e) {
            log.warn(
                    "[RocketMQ] Failed to create consumer group '{}': {}",
                    group,
                    e.getMessage(),
                    e);
        }
    }

    // ======================== 扫描发现 ========================

    private static TopicMessageType toTopicMessageType(TopicType topicType) {
        if (topicType == null) {
            return TopicMessageType.NORMAL;
        }
        return switch (topicType) {
            case FIFO -> TopicMessageType.FIFO;
            case DELAY -> TopicMessageType.DELAY;
            default -> TopicMessageType.NORMAL;
        };
    }

    /** 扫描所有 {@link RocketMQMessageListener} 注解，收集 Topic 列表及其类型 */
    private Map<String, TopicType> discoverTopics(Map<String, Object> listeners) {
        Map<String, TopicType> result = new HashMap<>();
        for (Object bean : listeners.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            RocketMQMessageListener anno = targetClass.getAnnotation(RocketMQMessageListener.class);
            if (anno != null) {
                String topic = anno.topic();
                result.putIfAbsent(topic, topicTypes.getOrDefault(topic, TopicType.NORMAL));
            }
        }
        return result;
    }

    /** 扫描所有 {@link RocketMQMessageListener} 注解，收集去重后的消费者分组列表 */
    private Set<String> discoverConsumerGroups(Map<String, Object> listeners) {
        Set<String> groups = new HashSet<>();
        for (Object bean : listeners.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            RocketMQMessageListener anno = targetClass.getAnnotation(RocketMQMessageListener.class);
            if (anno != null) {
                groups.add(anno.consumerGroup());
            }
        }
        return groups;
    }

    // ======================== Lifecycle ========================

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void stop(Runnable callback) {
        running = false;
        callback.run();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
