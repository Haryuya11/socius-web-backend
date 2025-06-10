package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;

import java.util.UUID;

public interface ChatMessageProducerService {

    /**
     * Gửi tin nhắn đến hàng đợi RabbitMQ
     *
     * @param message Tin nhắn cần gửi
     * @param conversationType Loại cuộc hội thoại (DIRECT hoặc GROUP)
     * @param conversationId ID của cuộc hội thoại
     */
    void sendChatMessage(MessageResponseDto message, ConversationType conversationType, UUID conversationId);

    /**
     * Gửi thông báo trạng thái đã đọc
     *
     * @param userId ID người dùng đã đọc tin nhắn
     * @param conversationId ID cuộc hội thoại
     * @param lastReadMessageId ID tin nhắn cuối cùng đã đọc
     */
    void sendReadReceipt(UUID userId, UUID conversationId, UUID lastReadMessageId);
}