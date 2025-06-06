package org.socius.sociuswebbackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.MessageMapper;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.services.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    final private MessageRepository messageRepository;

    final private ConversationRepository conversationRepository;

    final private UserRepository userRepository;

    final private ConversationMemberRepository conversationMemberRepository;

    final private MessageStatusRepository messageStatusRepository;

    final private UnreadCountRepository unreadCountRepository;

    final private MessageMapper messageMapper;

    final private ChatMessageProducerService chatMessageProducerService;

    @Override
    public MessageResponseDto sendMessage(UUID senderId, MessageRequestDto requestDto) {

        if (!isUserMemberOfConversation(senderId, requestDto.getConversationId())) {
            throw new RuntimeException("Bạn không có quyền gửi tin nhắn trong cuộc trò chuyện này");
        }
        // Kiểm tra người gửi có tồn tại không
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + senderId));

        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(requestDto.getConversationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + requestDto.getConversationId()));

        // Tạo tin nhắn mới
        MessageEntity message = messageMapper.requestDtoToEntity(requestDto);
        message.setSender(sender);
        message.setConversation(conversation);
        message = messageRepository.save(message);

        // Lấy danh sách thành viên trong cuộc trò chuyện
        Set<UUID> memberIds = conversationMemberRepository.findActiveMemberIds(conversation.getId());

        // Tạo trạng thái tin nhắn cho tất cả thành viên trong cuộc trò chuyện
        List<MessageStatusEntity> messageStatuses = new ArrayList<>();
        for (UUID memberId : memberIds) {
            if (memberId.equals(senderId)) {
                // Nếu là người gửi, đánh dấu tin nhắn đã đọc
                UserEntity senderUser = UserEntity.builder()
                        .id(memberId)
                        .build();

                MessageStatusEntity senderStatus = MessageStatusEntity.builder()
                        .id(new MessageStatusId(message.getId(), senderId))
                        .message(message)
                        .user(senderUser)
                        .isRead(true)
                        .readAt(LocalDateTime.now())
                        .build();

                messageStatuses.add(senderStatus);
            } else {
                // Nếu không phải người gửi, đánh dấu tin nhắn chưa đọc
                UserEntity memberUser = UserEntity.builder()
                        .id(memberId)
                        .build();

                MessageStatusEntity memberStatus = MessageStatusEntity.builder()
                        .id(new MessageStatusId(message.getId(), memberId))
                        .message(message)
                        .user(memberUser)
                        .isRead(false)
                        .build();

                messageStatuses.add(memberStatus);

                // Cập nhật số lượng tin nhắn chưa đọc cho thành viên
                unreadCountRepository.incrementUnreadCount(conversation.getId(), memberId);
            }
        }
        messageStatusRepository.saveAll(messageStatuses);

        // Chuyển đổi tin nhắn thành DTO
        MessageResponseDto responseDto = messageMapper.entityToDto(message);
        responseDto.setRead(true); // Đánh dấu tin nhắn đã đọc cho người gửi

        // Publish tin nhắn đến RabbitMQ
        chatMessageProducerService.sendChatMessage(responseDto, conversation.getType(), conversation.getId());

        return responseDto;
    }

    @Override
    public Page<MessageResponseDto> getMessages(UUID conversationId, Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));


        if (!isUserMemberOfConversation(user.getId(), conversationId)) {
            throw new RuntimeException("Bạn không có quyền xem cuộc trò chuyện này");
        }

        // Lấy danh sách tin nhắn trong cuộc trò chuyện
        Page<MessageEntity> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);


        // Chuyển đổi danh sách tin nhắn thành danh sách DTO
        return messages.map(message -> {
            MessageResponseDto responseDto = messageMapper.entityToDto(message);

            // Kiểm tra trạng thái tin nhắn cho người dùng hiện tại
            Optional<MessageStatusEntity> messageStatus = messageStatusRepository.findById(
                    new MessageStatusId(message.getId(), user.getId()));
            responseDto.setRead(messageStatus.map(MessageStatusEntity::getIsRead).orElse(false));
            return responseDto;
        });
    }

    @Override
    @Transactional
    public int markAsRead(UUID userId, ReadReceiptDto readReceiptDto) {
        try {

            // Kiểm tra người dùng có quyền truy cập cuộc trò chuyện không
            if (!isUserMemberOfConversation(userId, readReceiptDto.getConversationId())) {
                throw new RuntimeException("Bạn không có quyền đánh dấu tin nhắn là đã đọc trong cuộc trò chuyện này");
            }

            // Cập nhật trạng thái tin nhắn đã đọc cho người dùng
            int markedCount = messageStatusRepository.markMessagesAsRead(
                    readReceiptDto.getConversationId(),
                    userId,
                    readReceiptDto.getLastReadMessageId(),
                    LocalDateTime.now()
            );

            // Cập nhật unread_count và last_read_message_id
            if (markedCount > 0) {
                unreadCountRepository.updateUnreadCount(
                        readReceiptDto.getConversationId(),
                        userId,
                        0, // Đặt lại số lượng tin nhắn chưa đọc
                        readReceiptDto.getLastReadMessageId()
                );

                // Gửi read receipt đến RabbitMQ
                chatMessageProducerService.sendReadReceipt(
                        userId,
                        readReceiptDto.getConversationId(),
                        readReceiptDto.getLastReadMessageId()
                );
            }
            return markedCount;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đánh dấu tin nhắn là đã đọc: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<UUID, List<MessageResponseDto>> syncMessages(UUID userId, SyncMessagesRequestDto syncRequest) {
        Map<UUID, List<MessageResponseDto>> messages = new HashMap<>();

        // Duyết qua các cuộc trò chuyện cần đồng bộ
        for (Map.Entry<UUID, UUID> entry : syncRequest.getLastMessageIds().entrySet()) {
            UUID conversationId = entry.getKey();
            UUID lastMessageId = entry.getValue();

            // Kiểm tra người dùng có quyền truy cập cuộc trò chuyện không
            boolean isMember = conversationMemberRepository.findActiveMember(conversationId, userId)
                    .isPresent();
            if (!isMember) {
                logger.warn("Người dùng {} không thuộc cuộc trò chuyện {}", userId, conversationId);
                continue;
            }

            // Lấy danh sách tin nhắn mới từ lastMessageId
            List<MessageEntity> newerMessages = messageRepository.findNewerMessages(conversationId, lastMessageId);

            // Chuyển đổi danh sách tin nhắn thành danh sách DTO
            List<MessageResponseDto> newerMessageDtos = newerMessages.stream()
                    .map(message -> {
                        MessageResponseDto responseDto = messageMapper.entityToDto(message);

                        // Kiểm tra trạng thái tin nhắn cho người dùng hiện tại
                        Optional<MessageStatusEntity> messageStatus = messageStatusRepository.findById(
                                new MessageStatusId(message.getId(), userId));
                        responseDto.setRead(messageStatus.map(MessageStatusEntity::getIsRead).orElse(false));
                        return responseDto;
                    })
                    .toList();

            if (!newerMessageDtos.isEmpty()) {
                messages.put(conversationId, newerMessageDtos);
            }
        }

        return messages;
    }

    @Override
    public boolean deleteMessage(UUID userId, UUID messageId) {
        // Kiểm tra tin nhắn có tồn tại không
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId));

        // Kiểm tra người dùng có quyền xóa tin nhắn không
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Người dùng không có quyền xóa tin nhắn này");
        }

        // Đánh dấu tin nhắn là đã xóa
        message.setDeleted(true);
        messageRepository.save(message);

        // Gửi thông báo tin nhắn đã bị xóa đến tất cả người dùng
        MessageResponseDto deletedMessageDto = messageMapper.entityToDto(message);
        deletedMessageDto.setDeleted(true);

        chatMessageProducerService.sendChatMessage(deletedMessageDto, message.getConversation().getType(),
                message.getConversation().getId());

        return true;
    }

    @Override
    @Transactional
    public MessageResponseDto updateMessage(UUID userId, UUID messageId, MessageRequestDto requestDto) {
        // Kiểm tra tin nhắn có tồn tại không
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId));

        // Kiểm tra người dùng có quyền sửa tin nhắn không
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Người dùng không có quyền xóa tin nhắn này");
        }

        // Cập nhật nội dung tin nhắn
        messageMapper.updateEntityFromDto(requestDto, message);
        message.setEdited(true);
        message = messageRepository.save(message);

        // Chuyển đổi tin nhắn thành DTO
        MessageResponseDto responseDto = messageMapper.entityToDto(message);

        // Publish tin nhắn đã cập nhật đến RabbitMQ
        chatMessageProducerService.sendChatMessage(
                responseDto,
                message.getConversation().getType(),
                message.getConversation().getId()
        );

        return responseDto;
    }

    @Override
    public Page<MessageResponseDto> searchMessages(UUID conversationId, String keyword, Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        UUID userId = user.getId();

        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người gửi có phải là thành viên của cuộc trò chuyện không
        boolean isMember = conversationMemberRepository.findActiveMember(conversationId, userId)
                .isPresent();
        if (!isMember) {
            throw new RuntimeException("Người gửi không phải là thành viên của cuộc trò chuyện");
        }

        // Tìm kiếm các tin nhắn theo từ khóa
        Page<MessageEntity> messages = messageRepository.searchMessages(conversationId, keyword, pageable);

        return messages.map(messageMapper::entityToDto);
    }

    private boolean isUserMemberOfConversation(UUID userId, UUID conversationId) {
        return conversationMemberRepository
                .findActiveMember(conversationId, userId)
                .isPresent();
    }
}
