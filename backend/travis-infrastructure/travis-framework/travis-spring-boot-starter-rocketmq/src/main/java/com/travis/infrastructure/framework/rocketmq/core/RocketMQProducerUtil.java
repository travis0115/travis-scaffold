package com.travis.infrastructure.framework.rocketmq.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 {@link RocketMQClientTemplate} (v5 gRPC) 的消息发送工具类，提供静态方法封装常用操作。
 *
 * <p>适配 rocketmq-v5-client-spring-boot-starter 2.3.x 版本，基于 gRPC 协议通信。
 *
 * <p>支持的消息发送方式：
 *
 * <ul>
 *   <li>{@link #syncSendNormalMessage(String, Object)} — 同步发送普通消息
 *   <li>{@link #syncSendFifoMessage(String, Object, String)} — 同步发送 FIFO 顺序消息
 *   <li>{@link #syncSendDelayMessage(String, Object, Duration)} — 同步发送延迟消息
 *   <li>{@link #asyncSendNormalMessage(String, Object)} — 异步发送普通消息
 *   <li>{@link #asyncSendFifoMessage(String, Object, String)} — 异步发送 FIFO 顺序消息
 *   <li>{@link #asyncSendDelayMessage(String, Object, Duration)} — 异步发送延迟消息
 * </ul>
 *
 * <p>destination 格式：{@code topic:tags}，例如 {@code order-topic:pay}。
 *
 * <p>注意：v5 客户端基于 gRPC 协议，不再支持 sendOneWay（单向发送）模式。 如需即发即弃语义，可使用 {@link
 * #asyncSendNormalMessage(String, Object)} 并忽略返回值。
 *
 * @author travis
 */
@Slf4j
public class RocketMQProducerUtil {

    private static RocketMQClientTemplate rocketMQClientTemplate;

    public void setRocketMQClientTemplate(RocketMQClientTemplate rocketMQClientTemplate) {
        RocketMQProducerUtil.rocketMQClientTemplate = rocketMQClientTemplate;
    }

    // ==================== 同步发送 ====================

    /**
     * 同步发送普通消息
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @return 发送回执
     */
    public static SendReceipt syncSendNormalMessage(String destination, Object payload) {
        try {
            SendReceipt sendReceipt =
                    rocketMQClientTemplate.syncSendNormalMessage(destination, payload);
            validateSendReceipt(destination, sendReceipt);
            return sendReceipt;
        } catch (MessagingException e) {
            log.warn("rocketmq syncSendNormalMessage failed, destination={}", destination, e);
            throw new IllegalStateException(
                    "rocketmq syncSendNormalMessage failed: " + destination, e);
        }
    }

    /**
     * 同步发送普通消息（Spring Message 形式）
     *
     * @param destination 目的地，格式为 topic:tags
     * @param message Spring Message 对象
     * @return 发送回执
     */
    public static SendReceipt syncSendNormalMessage(String destination, Message<?> message) {
        try {
            SendReceipt sendReceipt =
                    rocketMQClientTemplate.syncSendNormalMessage(destination, message);
            validateSendReceipt(destination, sendReceipt);
            return sendReceipt;
        } catch (MessagingException e) {
            log.warn("rocketmq syncSendNormalMessage failed, destination={}", destination, e);
            throw new IllegalStateException(
                    "rocketmq syncSendNormalMessage failed: " + destination, e);
        }
    }

    /**
     * 同步发送 FIFO 顺序消息
     *
     * <p>相同 {@code messageGroup} 的消息会被发送到同一个队列，保证消费顺序。
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @param messageGroup 消息组名，相同 messageGroup 的消息保证顺序
     * @return 发送回执
     */
    public static SendReceipt syncSendFifoMessage(
            String destination, Object payload, String messageGroup) {
        try {
            SendReceipt sendReceipt =
                    rocketMQClientTemplate.syncSendFifoMessage(destination, payload, messageGroup);
            validateSendReceipt(destination, sendReceipt);
            return sendReceipt;
        } catch (MessagingException e) {
            log.warn(
                    "rocketmq syncSendFifoMessage failed, destination={}, messageGroup={}",
                    destination,
                    messageGroup,
                    e);
            throw new IllegalStateException(
                    "rocketmq syncSendFifoMessage failed: " + destination, e);
        }
    }

    /**
     * 同步发送延迟消息
     *
     * <p>注意：目标 Topic 需要在 RocketMQ 服务端创建为「定时/延时消息」类型。
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @param delayTime 延迟时间，支持任意 {@link Duration}
     * @return 发送回执
     */
    public static SendReceipt syncSendDelayMessage(
            String destination, Object payload, Duration delayTime) {
        try {
            SendReceipt sendReceipt =
                    rocketMQClientTemplate.syncSendDelayMessage(destination, payload, delayTime);
            validateSendReceipt(destination, sendReceipt);
            return sendReceipt;
        } catch (MessagingException e) {
            log.warn(
                    "rocketmq syncSendDelayMessage failed, destination={}, delayTime={}",
                    destination,
                    delayTime,
                    e);
            throw new IllegalStateException(
                    "rocketmq syncSendDelayMessage failed: " + destination, e);
        }
    }

    // ==================== 异步发送 ====================

    /**
     * 异步发送普通消息
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @return 发送结果的 {@link CompletableFuture}
     */
    public static CompletableFuture<SendReceipt> asyncSendNormalMessage(
            String destination, Object payload) {
        try {
            return rocketMQClientTemplate.asyncSendNormalMessage(
                    destination, payload, new CompletableFuture<>());
        } catch (MessagingException e) {
            log.warn("rocketmq asyncSendNormalMessage failed, destination={}", destination, e);
            throw new IllegalStateException(
                    "rocketmq asyncSendNormalMessage failed: " + destination, e);
        }
    }

    /**
     * 异步发送 FIFO 顺序消息
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @param messageGroup 消息组名，相同 messageGroup 的消息保证顺序
     * @return 发送结果的 {@link CompletableFuture}
     */
    public static CompletableFuture<SendReceipt> asyncSendFifoMessage(
            String destination, Object payload, String messageGroup) {
        try {
            return rocketMQClientTemplate.asyncSendFifoMessage(
                    destination, payload, messageGroup, new CompletableFuture<>());
        } catch (MessagingException e) {
            log.warn(
                    "rocketmq asyncSendFifoMessage failed, destination={}, messageGroup={}",
                    destination,
                    messageGroup,
                    e);
            throw new IllegalStateException(
                    "rocketmq asyncSendFifoMessage failed: " + destination, e);
        }
    }

    /**
     * 异步发送延迟消息
     *
     * @param destination 目的地，格式为 topic:tags
     * @param payload 消息体
     * @param delayTime 延迟时间
     * @return 发送结果的 {@link CompletableFuture}
     */
    public static CompletableFuture<SendReceipt> asyncSendDelayMessage(
            String destination, Object payload, Duration delayTime) {
        try {
            return rocketMQClientTemplate.asyncSendDelayMessage(
                    destination, payload, delayTime, new CompletableFuture<>());
        } catch (MessagingException e) {
            log.warn(
                    "rocketmq asyncSendDelayMessage failed, destination={}, delayTime={}",
                    destination,
                    delayTime,
                    e);
            throw new IllegalStateException(
                    "rocketmq asyncSendDelayMessage failed: " + destination, e);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 校验发送回执，v5 Client 内部吞掉了发送异常并返回 null，需要在此兜底
     *
     * @param destination 目的地
     * @param sendReceipt 发送回执
     */
    private static void validateSendReceipt(String destination, SendReceipt sendReceipt) {
        if (sendReceipt == null) {
            throw new IllegalStateException(
                    "rocketmq send failed (sendReceipt is null): " + destination);
        }
    }
}
