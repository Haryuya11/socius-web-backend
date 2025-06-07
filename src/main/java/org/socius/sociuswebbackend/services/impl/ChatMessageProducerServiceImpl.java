package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.PendingMessagesService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageProducerServiceImpl implements ChatMessageProducerService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageProducerServiceImpl.class);

    final private RabbitTemplate rabbitTemplate;
    final private ConfigService configService;
    final private PendingMessagesService pendingMessagesService;

    @Override
    @Retryable(
            retryFor = {AmqpException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public void sendChatMessage(MessageResponseDto message, ConversationType conversationType, UUID conversationId) {
        try {
            String exchange = RabbitMQKeyBuilder.getChatExchange();
            String routingKey;

            // Xác định routing key dựa trên loại cuộc trò chuyện
            if (conversationType == ConversationType.DIRECT) {
                routingKey = RabbitMQKeyBuilder.getPrivateRoutingKeyPattern(conversationId);
            } else {
                routingKey = RabbitMQKeyBuilder.getGroupRoutingKeyPattern(conversationId);
            }

            logger.info("Gửi tin nhắn đến exchange {}, routing key {}: {}", exchange, routingKey, message);

            // Thêm thông tin vào message
            rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
                message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message1.getMessageProperties().setTimestamp(new Date());
                message1.getMessageProperties().getHeaders().put("retry_count", 0);
                message1.getMessageProperties().getHeaders().put("conversation_type", conversationType.name());
                message1.getMessageProperties().getHeaders().put("conversation_id", conversationId.toString());
                return message1;
            });

        } catch (AmqpException e) {
            logger.error("Lỗi khi gửi tin nhắn qua RabbitMQ (sẽ thử lại): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không thể khôi phục khi gửi tin nhắn: {}", e.getMessage(), e);
            if (message.getSender() != null && message.getSender().getId() != null) {
                pendingMessagesService.addPendingMessage(message.getSender().getId(), message);
            }
        }
    }

    @Recover
    public void recoverSendChatMessage(AmqpException e, MessageResponseDto message, ConversationType conversationType, UUID conversationId) {
        logger.error("Đã thử gửi tin nhắn tối đa số lần thử lại, chuyển sang xử lý khác: {}", e.getMessage(), e);

        try {
            // Lưu tin nhắn vào Dead Letter Queue (DLQ) hoặc DB để xử lý sau
            String dlxExchange = RabbitMQKeyBuilder.getDeadLetterExchange();

            rabbitTemplate.convertAndSend(dlxExchange, RabbitMQKeyBuilder.getDeadLetterRoutingKey(), message, message1 -> {
                message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message1.getMessageProperties().getHeaders().put("original_conversation_id", conversationId);
                message1.getMessageProperties().getHeaders().put("original_conversation_type", conversationType.name());
                message1.getMessageProperties().getHeaders().put("failed_at", System.currentTimeMillis());
                message1.getMessageProperties().setExpiration(configService.getString("rabbitmq.dlx.message.ttl", "86400000")); // Mặc định 24h
                return message1;
            });

            // Lưu vào pending buffer để gửi lại khi WebSocket kết nối lại
            if (message.getSender() != null && message.getSender().getId() != null) {
                pendingMessagesService.addPendingMessage(message.getSender().getId(), message);
            }

            logger.info("Đã lưu tin nhắn vào DLX và pending buffer: {}", message);

        } catch (Exception dlxError) {
            logger.error("Không thể gửi tin nhắn đến DLX: {}", dlxError.getMessage(), dlxError);
        }
    }

    @Override
    @Retryable(
            retryFor = {AmqpException.class},
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void sendReadReceipt(UUID userId, UUID conversationId, UUID lastReadMessageId) {
        try {
            String routingKey = RabbitMQKeyBuilder.getReadReceiptRoutingKeyPattern(conversationId);

            Map<String, Object> readReceipt = Map.of(
                    "userId", userId,
                    "conversationId", conversationId,
                    "lastReadMessageId", lastReadMessageId,
                    "timestamp", System.currentTimeMillis()
            );

            logger.info("Gửi read receipt cho userId {} trong conversationId {}, lastReadMessageId: {}",
                    userId, conversationId, lastReadMessageId);
            rabbitTemplate.convertAndSend(RabbitMQKeyBuilder.getChatExchange(), routingKey, readReceipt, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
        } catch (AmqpException e) {
            logger.error("Lỗi khi gửi thông báo đã đọc qua RabbitMQ (sẽ thử lại): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không thể khôi phục khi gửi thông báo đã đọc: {}", e.getMessage(), e);
        }
    }

    @Recover
    public void recoverSendReadReceipt(Exception e, UUID userId, UUID conversationId, UUID lastReadMessageId) {
        logger.error("Đã thử gửi read receipt tối đa số lần thử lại: {}", e.getMessage(), e);
        try {
            // Lưu read receipt vào cache/DB để xử lý sau
            String key = RedisKeyBuilder.failedReadReceiptKey(userId, conversationId);
            // Implement appropriate storage mechanism
        } catch (Exception storageError) {
            logger.error("Không thể lưu read receipt: {}", storageError.getMessage());
        }
    }
}