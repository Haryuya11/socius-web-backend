package org.socius.sociuswebbackend.jobs;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeadLetterQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueProcessor.class);

    final private RabbitTemplate rabbitTemplate;
    final private ChatMessageProducerService chatMessageProducerService;
    final private ConfigService configService;

    @RabbitListener(queues = "#{deadLetterQueue.name}")
    public void processDLQ(Message failedMessage) {
        try {
            MessageResponseDto message = (MessageResponseDto) rabbitTemplate.getMessageConverter()
                    .fromMessage(failedMessage);

            logger.info("Xử lý tin nhắn từ DLQ: {}", message);

            // Lấy thông tin từ headers
            Map<String, Object> headers = failedMessage.getMessageProperties().getHeaders();
            UUID conversationId = UUID.fromString(headers.get("original_conversation_id").toString());
            ConversationType conversationType = ConversationType.valueOf(
                    headers.get("original_conversation_type").toString());
            long failedAt = (long) headers.get("failed_at");

            // Kiểm tra thời gian thất bại để quyết định có nên thử lại
            long currentTime = System.currentTimeMillis();
            long failureAgeMinutes = (currentTime - failedAt) / (1000 * 60);

            long retryWindowMinutes = configService.getInt("rabbitmq.dlq.retry.window.minutes", 360);

            if (failureAgeMinutes < retryWindowMinutes) {
                chatMessageProducerService.sendChatMessage(message, conversationType, conversationId);
                logger.info("Đã thử gửi lại tin nhắn từ DLQ: {}", message.getId());
            } else {
                logger.warn("Tin nhắn từ DLQ đã quá thời gian thử lại ({}p > {}p), không xử lý: {}",
                        failureAgeMinutes, retryWindowMinutes, message.getId());
            }
        } catch (Exception e) {
            logger.error("Lỗi xử lý tin nhắn từ DLQ: {}", e.getMessage(), e);
        }
    }
}
