package org.socius.sociuswebbackend.utils;

import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.model.enums.MessageType;

import java.time.LocalDateTime;
import java.util.*;

public class ChatTestDataUtil {

    private ChatTestDataUtil() {
    }

    /**
     * Tạo Một ConversationEntity mẫu
     */
    public static ConversationEntity createConversationEntity() {
        return ConversationEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Conversation")
                .type(ConversationType.GROUP)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdByUser(AuthTestDataUtil.createTestAdminUser())
                .members(new HashSet<>())
                .messages(new HashSet<>())
                .build();
    }

    /**
     * Tạo một MessageEntity mẫu
     */
    public static MessageEntity createMessageEntity(ConversationEntity conversation, UserEntity sender) {
        return MessageEntity.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .content("Test message content")
                .messageType(MessageType.TEXT)
                .mediaCleanedUp(false)
                .statusList(new HashSet<>())
                .build();
    }

    /**
     * Tạo một ConversationMemberEntity mẫu
     */
    public static ConversationMemberEntity createConversationMemberEntity(ConversationEntity conversation, UserEntity user) {
        return ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversation.getId(), user.getId()))
                .conversation(conversation)
                .user(user)
                .role(MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo một ConversationRequestDto mẫu
     */
    public static ConversationRequestDto createConversationRequestDto() {
        return ConversationRequestDto.builder()
                .name("Nhóm mới")
                .type(ConversationType.GROUP)
                .build();
    }

    /**
     * Tạo một MessageRequestDto mẫu
     */
    public static MessageRequestDto createMessageRequestDto() {
        return MessageRequestDto.builder()
                .conversationId(UUID.randomUUID())
                .content("Tin nhắn mẫu")
                .build();
    }

    /**
     * Tạo một MessageResponseDto mẫu
     */
    public static MessageResponseDto createMessageResponseDto() {
        return MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .conversationId(UUID.randomUUID())
                .sender(AuthTestDataUtil.createTestAdminUserResponse())
                .content("Nội dung tin nhắn mẫu")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo một ConversationResponseDto mẫu
     */
    public static ConversationResponseDto createConversationResponseDto() {
        return ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Cuộc trò chuyện mẫu")
                .type(ConversationType.GROUP)
                .createdByUser(AuthTestDataUtil.createTestAdminUserResponse())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo một ReadReceiptDto mẫu
     */
    public static ReadReceiptDto createReadReceiptDto() {
        return ReadReceiptDto.builder()
                .conversationId(UUID.randomUUID())
                .lastReadMessageId(UUID.randomUUID())
                .build();
    }

    /**
     * Tạo một SyncMessagesRequestDto mẫu
     */
    public static SyncMessagesRequestDto createSyncMessagesRequestDto() {
        Map<UUID, UUID> lastMessageIds = new HashMap<>();
        lastMessageIds.put(UUID.randomUUID(), UUID.randomUUID());

        return SyncMessagesRequestDto.builder()
                .lastMessageIds(lastMessageIds)
                .build();
    }

    /**
     * Tạo một UnreadCountEntity mẫu
     */
    public static UnreadCountEntity createUnreadCountEntity(ConversationEntity conversation, UserEntity user) {
        UnreadCountId id = new UnreadCountId(conversation.getId(), user.getId());

        MessageEntity lastReadMessage = createMessageEntity(conversation, user);

        return UnreadCountEntity.builder()
                .id(id)
                .conversation(conversation)
                .user(user)
                .unreadCount(5)
                .lastReadMessage(lastReadMessage)
                .build();
    }

    /**
     * Tạo một MessageStatusEntity mẫu
     */
    public static MessageStatusEntity createMessageStatusEntity(MessageEntity message, UserEntity user) {
        MessageStatusId id = new MessageStatusId(message.getId(), user.getId());

        return MessageStatusEntity.builder()
                .id(id)
                .message(message)
                .user(user)
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();
    }

    /**
     * Tạo danh sách các thành viên cuộc trò chuyện
     */
    public static Set<ConversationMemberEntity> createConversationMemberEntities(ConversationEntity conversation, int memberCount) {
        Set<ConversationMemberEntity> members = new HashSet<>();

        // Thêm người tạo là admin với vai trò ADMIN
        ConversationMemberEntity creator = createConversationMemberEntity(conversation, conversation.getCreatedByUser());
        creator.setRole(MemberRole.ADMIN);
        members.add(creator);

        // Thêm các thành viên khác
        for (int i = 0; i < memberCount - 1; i++) {
            UserEntity user = AuthTestDataUtil.createTestRegularUser();
            // Đặt ID khác nhau cho mỗi người dùng
            user.setId(UUID.randomUUID());
            members.add(createConversationMemberEntity(conversation, user));
        }

        return members;
    }

    /**
     * Tạo danh sách tin nhắn cho cuộc trò chuyện
     */
    public static List<MessageEntity> createMessageList(ConversationEntity conversation, int messageCount) {
        List<MessageEntity> messages = new ArrayList<>();

        LocalDateTime baseTime = LocalDateTime.now().minusHours(1);
        UserEntity sender = conversation.getCreatedByUser();

        for (int i = 0; i < messageCount; i++) {
            MessageEntity message = createMessageEntity(conversation, sender);
            message.setId(UUID.randomUUID());
            message.setContent("Tin nhắn số " + (i + 1));
            message.setCreatedAt(baseTime.plusMinutes(i * 5L));
            message.setUpdatedAt(baseTime.plusMinutes(i * 5L));
            messages.add(message);
        }

        return messages;
    }
}
