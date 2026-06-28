package com.travis.infrastructure.common.message;

/**
 * MQ 异步发布完成回调，用于感知发送结果。
 *
 * <p>成功时 {@code ex} 为 {@code null}，失败时携带异常。回调参数携带完整的发布上下文，便于日志记录或重试。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * messagePublisher.asyncPublish(SystemMessage.USER_LOGIN, payload,
 *         (message, p, options, ex) -> {
 *             if (ex != null) {
 *                 log.error("发送失败, message={}, payload={}", message, p, ex);
 *             } else {
 *                 log.info("发送成功, message={}", message);
 *             }
 *         });
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher#asyncPublish(Message, Object, MessagePublishCallback)
 * @see MessagePublisher#asyncPublish(Message, Object, MessagePublishOptions,
 *     MessagePublishCallback)
 */
@FunctionalInterface
public interface MessagePublishCallback {

    /**
     * MQ 异步发布完成通知
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param options 发布选项（无选项时为 {@code null}）
     * @param ex 发送异常，成功时为 {@code null}
     */
    void onCompleted(Message message, Object payload, MessagePublishOptions options, Throwable ex);
}
