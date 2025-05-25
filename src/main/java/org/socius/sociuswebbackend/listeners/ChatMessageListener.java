package org.socius.sociuswebbackend.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.repositories.ConversationMemberRepository;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);

    final private SimpMessagingTemplate messagingTemplate;
    final private OnlineUserService onlineUserService;
    final private OfflineMessageService offlineMessageService;
    final private ConversationMemberRepository conversationMemberRepository;

    // Lắng nghe tin nhắn cá nhân
    @RabbitListener(queues = "#{privateMessageQueue.name}")
    public void handlePrivateMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn cá nhân từ RabbitMQ: {}", message);

        // Xác định người nhận
        UUID recipientId = determineRecipientId(message);
        if (recipientId != null) {
            processChatMessage(message, recipientId);
        } else {
            logger.warn("Không thể xác định người nhận cho tin nhắn: {}", message.getId());
        }
    }

    // Lắng nghe tin nhắn nhóm
    @RabbitListener(queues = "#{groupMessageQueue.name}")
    public void handleGroupMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn nhóm từ RabbitMQ: {}", message);
        processChatMessage(message, null);
    }

    // Lắng nghe thông báo đã đọc
    @RabbitListener(queues = "#{readReceiptQueue.name}")
    public void handleReadReceipt(Map<String, Object> readReceipt) {
        logger.info("Nhận được read receipt từ RabbitMQ: {}", readReceipt);

        UUID conversationId = UUID.fromString(readReceipt.get("conversationId").toString());
        UUID userId = UUID.fromString(readReceipt.get("userId").toString());
        UUID lastReadMessageId = UUID.fromString(readReceipt.get("lastReadMessageId").toString());

        // Gửi thông báo đã đọc qua WebSocket
        String destination = "/topic/conversations/" + conversationId + "/receipts";
        messagingTemplate.convertAndSend(destination, readReceipt);

        // Gửi thông báo đến người dùng
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/receipts", readReceipt);
    }

    private UUID determineRecipientId(MessageResponseDto message) {
        if (message.getConversationId() == null || message.getSender() == null || message.getSender().getId() == null) {
            return null;
        }

        // Lấy danh sách thành viên trong cuộc trò chuyện
        List<ConversationMemberEntity> members = conversationMemberRepository.findActiveMembers(message.getConversationId());

        // Tìm người nhận là thành viên khác với người gửi trong cuộc trò chuyện 1-1
        return members.stream()
                .filter(member -> !member.getUser().getId().equals(message.getSender().getId()))
                .map(member -> member.getUser().getId())
                .findFirst()
                .orElse(null);
    }

    private void processChatMessage(MessageResponseDto message, UUID specificRecipientId) {
        // Nếu có người nhận cụ thể
        if (specificRecipientId != null) {
            boolean isOnline = onlineUserService.isUserOnline(specificRecipientId);
            if (isOnline) {
                sendMessageToUser(specificRecipientId, message);
            } else {
                offlineMessageService.storeOfflineMessage(specificRecipientId, message);
                logger.info("Người dùng {} không trực tuyến, đã lưu tin nhắn offline: {}", specificRecipientId, message.getId());
            }
        }

        // Gửi tin nhắn đến topic của cuộc trò chuyện
        String destination = "/topic/conversations/" + message.getConversationId();
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendMessageToUser(UUID userId, MessageResponseDto message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/private-messages",
                message
        );
        logger.info("Đã gửi tin nhắn đến người dùng {}, messageId: {}", userId, message.getId());
    }
}
