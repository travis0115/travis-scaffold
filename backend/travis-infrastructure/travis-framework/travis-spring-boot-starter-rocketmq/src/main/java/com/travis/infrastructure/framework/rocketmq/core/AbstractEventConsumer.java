package com.travis.infrastructure.framework.rocketmq.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * RocketMQ 事件消费者抽象基类，封装消息体读取、JSON 反序列化和异常处理的通用模板。
 *
 * <p>子类只需实现 {@link #onEvent(Object)} 方法编写业务逻辑，泛型参数 {@code T} 对应的消息体类型会自动从类签名中解析。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Component
 * @RocketMQMessageListener(
 *         topic = "system-event",
 *         tag = "user-login",
 *         consumerGroup = "system-user-login-consumer")
 * @RequiredArgsConstructor
 * public class LoginLogEventConsumer extends AbstractEventConsumer<UserLoginPayload> {
 *
 *     private final SysLoginLogService loginLogService;
 *
 *     @Override
 *     protected void onEvent(UserLoginPayload payload) {
 *         loginLogService.recordLoginLog(payload.getUsername(), payload.getStatus(), payload.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @param <T> 消息体类型
 * @author travis
 * @see RocketMQListener
 */
@Slf4j
public abstract class AbstractEventConsumer<T> implements RocketMQListener {

    @Autowired
    private ObjectMapper objectMapper;

    /** 从子类泛型签名中解析出消息体类型 Class，用于 JSON 反序列化 */
    private final Class<T> payloadType;

    /** 自动解析泛型参数，子类直接继承即可，无需手动指定消息体类型 */
    @SuppressWarnings("unchecked")
    protected AbstractEventConsumer() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            this.payloadType = (Class<T>) pt.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException(
                    "必须通过泛型参数指定消息体类型，如 extends AbstractEventConsumer<MyPayload>");
        }
    }

    /**
     * 消费消息的模板方法，自动完成消息体读取、JSON 反序列化，然后委托给 {@link #onEvent(Object)} 处理。
     *
     * <p>默认开启事务，确保 {@link #onEvent(Object)} 中的数据库操作在同一事务中执行。
     *
     * @param messageView RocketMQ 消息视图
     * @return 消费结果
     */
    @Override
    @Transactional
    public ConsumeResult consume(MessageView messageView) {
        try {
            ByteBuffer buf = messageView.getBody();
            byte[] body = new byte[buf.remaining()];
            buf.get(body);
            T payload = objectMapper.readValue(body, payloadType);
            onEvent(payload);
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("消费事件失败", e);
            return ConsumeResult.FAILURE;
        }
    }

    /**
     * 处理已反序列化的消息体。子类实现此方法编写具体的业务逻辑。
     *
     * @param payload 反序列化后的消息体对象，即发送端调用 {@code messagePublisher.publish(event, payload)} 时传入的 payload
     * @throws Exception 业务处理异常
     */
    protected abstract void onEvent(T payload) throws Exception;
}
