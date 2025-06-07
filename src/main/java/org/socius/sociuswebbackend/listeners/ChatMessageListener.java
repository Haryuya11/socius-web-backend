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

import java.util.HashMap;
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

    @RabbitListener(queues = "#{privateMessageQueue.name}")
    public void handlePrivateMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn cá nhân từ RabbitMQ: {}", message);
        processPrivateMessage(message);
    }

    @RabbitListener(queues = "#{groupMessageQueue.name}")
    public void handleGroupMessage(MessageResponseDto message) {
        logger.info("Nhận được tin nhắn nhóm từ RabbitMQ: {}", message);
        processGroupMessage(message);
    }

    @RabbitListener(queues = "#{readReceiptQueue.name}")
    public void handleReadReceipt(Map<String, Object> readReceipt) {
        logger.info("Nhận được read receipt từ RabbitMQ: {}", readReceipt);

        UUID conversationId = UUID.fromString(readReceipt.get("conversationId").toString());

        // Gửi read receipt đến conversation topic
        String destination = "/topic/conversations/" + conversationId + "/receipts";
        messagingTemplate.convertAndSend(destination, readReceipt);
    }

    private void processPrivateMessage(MessageResponseDto message) {
        // Lấy danh sách thành viên trong cuộc trò chuyện
        List<ConversationMemberEntity> members = conversationMemberRepository
                .findActiveMembers(message.getConversationId());

        for (ConversationMemberEntity member : members) {
            UUID userId = member.getUser().getId();

            // Tạo message wrapper với structure mà frontend expect
            Map<String, Object> messageWrapper = createMessageWrapper(message);

            // Kiểm tra user có online không
            boolean isOnline = onlineUserService.isUserOnline(userId);

            if (isOnline) {
                // Gửi đến queue của từng user - QUAN TRỌNG: sửa destination cho khớp với frontend
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/messages", // Khớp với frontend subscription
                        messageWrapper
                );
                logger.info("Đã gửi tin nhắn realtime đến user {}", userId);
            } else {
                // Lưu offline message
                offlineMessageService.storeOfflineMessage(userId, message);
                logger.info("User {} offline, đã lưu tin nhắn", userId);
            }
        }

        // Gửi đến topic conversation để sync UI
        Map<String, Object> conversationMessage = createConversationMessage(message);
        String destination = "/topic/conversations/" + message.getConversationId();
        messagingTemplate.convertAndSend(destination, conversationMessage);
    }

    private void processGroupMessage(MessageResponseDto message) {
        // Tương tự như private message nhưng gửi đến tất cả members
        List<ConversationMemberEntity> members = conversationMemberRepository
                .findActiveMembers(message.getConversationId());

        for (ConversationMemberEntity member : members) {
            UUID userId = member.getUser().getId();

            // Không gửi lại cho người gửi
            if (userId.equals(message.getSender().getId())) {
                continue;
            }

            Map<String, Object> messageWrapper = createMessageWrapper(message);

            boolean isOnline = onlineUserService.isUserOnline(userId);

            if (isOnline) {
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/messages",
                        messageWrapper
                );
            } else {
                offlineMessageService.storeOfflineMessage(userId, message);
            }
        }

        // Gửi đến topic conversation
        Map<String, Object> conversationMessage = createConversationMessage(message);
        String destination = "/topic/conversations/" + message.getConversationId();
        messagingTemplate.convertAndSend(destination, conversationMessage);
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
        data.put("read", false); // Mặc định chưa đọc

        wrapper.put("data", data);
        return wrapper;
    }

    private Map<String, Object> createConversationMessage(MessageResponseDto message) {
        Map<String, Object> conversationMsg = new HashMap<>();
        conversationMsg.put("type", "CONVERSATION_MESSAGE");
        conversationMsg.put("messageId", message.getId());
        conversationMsg.put("conversationId", message.getConversationId());
        conversationMsg.put("senderId", message.getSender().getId());
        conversationMsg.put("timestamp", message.getCreatedAt());
        return conversationMsg;
    }

    // Giữ nguyên method determineRecipientId cũ nếu cần dùng
    private UUID determineRecipientId(MessageResponseDto message) {
        if (message.getConversationId() == null || message.getSender() == null || message.getSender().getId() == null) {
            return null;
        }

        List<ConversationMemberEntity> members = conversationMemberRepository.findActiveMembers(message.getConversationId());

        return members.stream()
                .filter(member -> !member.getUser().getId().equals(message.getSender().getId()))
                .map(member -> member.getUser().getId())
                .findFirst()
                .orElse(null);
    }
}