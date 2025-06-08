package org.socius.sociuswebbackend.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.repositories.ConversationMemberRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineUserService onlineUserService;
    private final OfflineMessageService offlineMessageService;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConfigService configService;

    @RabbitListener(queues = "#{privateMessageQueue.name}")
    public void handlePrivateMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn cá nhân từ RabbitMQ: {}", message);
        processMessage(message, true);
    }

    @RabbitListener(queues = "#{groupMessageQueue.name}")
    public void handleGroupMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn nhóm từ RabbitMQ: {}", message);
        processMessage(message, false);
    }

    private void processMessage(MessageResponseDto message, boolean isPrivateMessage) {
        try {
            // Lấy danh sách thành viên trong conversation
            List<ConversationMemberEntity> members = conversationMemberRepository
                    .findByConversation_Id(message.getConversationId());

            logger.info("Tìm thấy {} thành viên trong conversation {}",
                    members.size(), message.getConversationId());

            for (ConversationMemberEntity member : members) {
                UUID userId = member.getUser().getId();

                // Không gửi lại cho người gửi
                if (userId.equals(message.getSender().getId())) {
                    continue;
                }

                // Kiểm tra user có online không
                boolean isOnline = onlineUserService.isUserOnline(userId);
                logger.info("User {} online status: {}", userId, isOnline);

                if (isOnline) {
                    sendRealtimeMessage(message, userId, isPrivateMessage);
                } else {
                    // Lưu vào offline messages
                    offlineMessageService.storeOfflineMessage(userId, message);
                    logger.info("Saved offline message for user {} in conversation {}",
                            userId, message.getConversationId());
                }
            }

            // Gửi đến topic conversation để sync UI cho tất cả members
            sendToConversationTopic(message);

        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    private void sendRealtimeMessage(MessageResponseDto message, UUID userId, boolean isPrivateMessage) {
        try {
            // Tạo message wrapper
            Map<String, Object> messageWrapper = createMessageWrapper(message);

            // Gửi đến queue riêng của user
            String userDestination = "/user/" + userId + "/queue/messages";
            messagingTemplate.convertAndSend(userDestination, messageWrapper);

            logger.info("Sent realtime message to user {} via {}", userId, userDestination);

            // Thống kê gửi thành công
            boolean enableStats = configService.getBoolean("websocket.message.stats.enabled", false);
            if (enableStats) {
                // Log thống kê nếu cần
                logger.debug("Message delivery stats: userId={}, messageId={}, success=true",
                        userId, message.getId());
            }

        } catch (Exception e) {
            logger.error("Failed to send realtime message to user {}: {}", userId, e.getMessage(), e);

            // Fallback: lưu vào offline messages nếu gửi realtime thất bại
            try {
                offlineMessageService.storeOfflineMessage(userId, message);
                logger.info("Saved message as offline due to realtime failure for user {}", userId);
            } catch (Exception offlineError) {
                logger.error("Failed to save offline message for user {}: {}",
                        userId, offlineError.getMessage());
            }
        }
    }

    private void sendToConversationTopic(MessageResponseDto message) {
        try {
            Map<String, Object> conversationMessage = createConversationMessage(message);
            String destination = "/topic/conversations/" + message.getConversationId();
            messagingTemplate.convertAndSend(destination, conversationMessage);

            logger.info("Sent conversation update to topic: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send conversation topic message: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> createMessageWrapper(MessageResponseDto message) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("type", "CHAT_MESSAGE");

        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("content", message.getContent());
        data.put("timestamp", message.getCreatedAt());
        data.put("senderId", message.getSender().getId());
        data.put("senderName", message.getSender().getFirstName() + " " + message.getSender().getLastName());
        data.put("conversationId", message.getConversationId());
        data.put("messageType", message.getMessageType());
        data.put("fileUrl", message.getFileUrl());
        data.put("isRead", false);

        wrapper.put("data", data);
        wrapper.put("timestamp", System.currentTimeMillis());

        return wrapper;
    }

    private Map<String, Object> createConversationMessage(MessageResponseDto message) {
        Map<String, Object> conversationMsg = new HashMap<>();
        conversationMsg.put("type", "CONVERSATION_UPDATE");
        conversationMsg.put("messageId", message.getId());
        conversationMsg.put("conversationId", message.getConversationId());
        conversationMsg.put("senderId", message.getSender().getId());
        conversationMsg.put("lastMessage", message.getContent());
        conversationMsg.put("timestamp", message.getCreatedAt());
        conversationMsg.put("messageType", message.getMessageType());

        return conversationMsg;
    }

    @RabbitListener(queues = "#{readReceiptQueue.name}")
    public void handleReadReceipt(Map<String, Object> readReceipt) {
        logger.info("Nhận được read receipt từ RabbitMQ: {}", readReceipt);

        try {
            UUID conversationId = UUID.fromString(readReceipt.get("conversationId").toString());
            UUID userId = UUID.fromString(readReceipt.get("userId").toString());

            // Gửi read receipt đến conversation topic
            String destination = "/topic/conversations/" + conversationId + "/receipts";
            messagingTemplate.convertAndSend(destination, readReceipt);

            logger.info("Sent read receipt to topic: {}", destination);

            // Gửi đến user queue để cập nhật UI
            String userDestination = "/user/" + userId + "/queue/receipts";
            messagingTemplate.convertAndSend(userDestination, readReceipt);

        } catch (Exception e) {
            logger.error("Error processing read receipt: {}", e.getMessage(), e);
        }
    }
}