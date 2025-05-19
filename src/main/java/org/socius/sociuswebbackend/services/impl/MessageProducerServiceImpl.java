package org.socius.sociuswebbackend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.enums.InvalidationReason;
import org.socius.sociuswebbackend.model.messages.SessionInvalidationMessage;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.MessageProducerService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class MessageProducerServiceImpl implements MessageProducerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProducerServiceImpl.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendSessionInvalidationMessage(UUID roleId, InvalidationReason reason, String message) {
        try {
            SessionInvalidationMessage invalidationMessage = SessionInvalidationMessage.builder()
                    .roleId(roleId)
                    .reason(reason)
                    .message(message)
                    .forceAllUsersWithRole(true)
                    .build();

            logger.info("Gửi thông điệp hủy phiên cho vai trò {}: {}", roleId, invalidationMessage);
            rabbitTemplate.convertAndSend(
                    RabbitMQKeyBuilder.getSessionManagementExchange(),
                    RabbitMQKeyBuilder.getInvalidateSessionRoutingKey(), invalidationMessage
            );
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông điệp hủy phiên: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendSpecificSessionInvalidationMessage(Set<String> sessionIds, InvalidationReason reason, String message) {
        try {
            SessionInvalidationMessage invalidationMessage = SessionInvalidationMessage.builder()
                    .sessionIds(sessionIds)
                    .reason(reason)
                    .message(message)
                    .forceAllUsersWithRole(false)
                    .build();

            logger.info("Gửi thông điệp hủy phiên cụ thể cho các session {}: {}", sessionIds, invalidationMessage);
            rabbitTemplate.convertAndSend(
                    RabbitMQKeyBuilder.getSessionManagementExchange(),
                    RabbitMQKeyBuilder.getInvalidateSessionRoutingKey(), invalidationMessage
            );
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông điệp hủy phiên cụ thể: {}", e.getMessage(), e);
        }
    }
}
