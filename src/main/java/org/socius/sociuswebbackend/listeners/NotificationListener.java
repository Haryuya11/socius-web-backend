package org.socius.sociuswebbackend.listeners;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.config.RabbitMQConfig;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listener xử lý thông báo từ RabbitMQ queue và gửi qua WebSocket tới các recipient.
 */
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final SimpMessagingTemplate messagingTemplate;

    final String SESSION_INVALIDATION_QUEUE = RabbitMQKeyBuilder.getSessionInvalidationQueue();


    /**
     * Nhận thông báo từ RabbitMQ queue và gửi qua WebSocket tới từng recipient.
     *
     * @param responseDto DTO chứa thông tin thông báo và danh sách recipient.
     */
    @RabbitListener(queues = RabbitMQKeyBuilder.SESSION_INVALIDATION_QUEUE)
    public void receiveNotification(NotificationResponseDto responseDto) {
        // Gửi thông báo qua WebSocket tới từng recipient
        responseDto.getRecipients().forEach(recipient ->
                messagingTemplate.convertAndSendToUser(
                        recipient.getUser().getId().toString(),
                        "/queue/notifications",
                        responseDto
                )
        );
    }
}
