package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.MessageMapper;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private MessageStatusRepository messageStatusRepository;

    @Mock
    private UnreadCountRepository unreadCountRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ChatMessageProducerService chatMessageProducerService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private UserEntity sender;
    private ConversationEntity conversation;
    private MessageEntity message;
    private MessageRequestDto messageRequestDto;
    private MessageResponseDto messageResponseDto;
    private UUID senderId;
    private UUID conversationId;
    private UUID messageId;
    private ConversationMemberEntity conversationMember;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu test
        senderId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        sender = AuthTestDataUtil.createTestAdminUser();
        sender.setId(senderId);

        conversation = ChatTestDataUtil.createConversationEntity();
        conversation.setId(conversationId);

        message = ChatTestDataUtil.createMessageEntity(conversation, sender);
        message.setId(messageId);
        message.setContent("Test message");
        message.setMessageType(MessageType.TEXT);
        message.setConversation(conversation);

        messageRequestDto = ChatTestDataUtil.createMessageRequestDto();
        messageRequestDto.setConversationId(conversationId);
        messageRequestDto.setContent("Test message");

        messageResponseDto = ChatTestDataUtil.createMessageResponseDto();

        // Tạo conversation member
        conversationMember = ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversationId, senderId))
                .conversation(conversation)
                .user(sender)
                .build();

    }

    @Test
    @DisplayName("Gửi tin nhắn văn bản thành công")
    void sendTextMessageSuccessfully() {
        // Thiết lập mock
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(
                eq(conversationId), eq(senderId))).thenReturn(true);
        when(messageMapper.requestDtoToEntity(messageRequestDto)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);

        // Thực thi
        MessageResponseDto result = messageService.sendMessage(senderId, messageRequestDto);

        // Kiểm tra
        assertNotNull(result);
        verify(messageRepository).save(any(MessageEntity.class));
        verify(chatMessageProducerService).sendChatMessage(any(MessageResponseDto.class),
                any(), any(UUID.class));
    }

    @Test
    @DisplayName("Gửi tin nhắn khi không phải thành viên của cuộc trò chuyện")
    void sendMessageWhenNotMemberOfConversation() {
        // Thiết lập mock
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(eq(conversationId), eq(senderId)))
                .thenReturn(false);
        // Thực thi và kiểm tra ngoại lệ
        Exception exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(senderId, messageRequestDto);
        });

        // Kiểm tra thông báo lỗi
        assertTrue(exception.getMessage().contains("không phải là thành viên"));
    }

    @Test
    @DisplayName("Lấy tin nhắn theo cuộc trò chuyện")
    void getMessagesByConversation() {
        // Thiết lập mock
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(eq(conversationId), eq(senderId)))
                .thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(message)));
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);

        // Thực thi
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageResponseDto> results = messageService.getMessages(senderId, conversationId, pageable);

        // Kiểm tra
        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Đánh dấu tin nhắn đã đọc")
    void markMessagesAsRead() {
        // Tạo dữ liệu ReadReceiptDto
        UUID lastReadMessageId = UUID.randomUUID();
        ReadReceiptDto readReceiptDto = ReadReceiptDto.builder()
                .conversationId(conversationId)
                .lastReadMessageId(lastReadMessageId)
                .build();

        // Thiết lập mock
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(eq(conversationId), eq(senderId)))
                .thenReturn(true);
        when(messageStatusRepository.markMessagesAsRead(eq(conversationId), eq(senderId), eq(lastReadMessageId), any()))
                .thenReturn(5); // Giả sử đánh dấu 5 tin nhắn

        // Thực thi
        int count = messageService.markAsRead(senderId, readReceiptDto);

        // Kiểm tra
        assertEquals(5, count, "Số lượng tin nhắn đã đánh dấu không đúng");
        verify(unreadCountRepository).updateUnreadCount(
                eq(conversationId),
                eq(senderId),
                eq(0),  // Đặt lại số lượng tin nhắn chưa đọc thành 0
                eq(lastReadMessageId)
        );
    }

    @Test
    @DisplayName("Đồng bộ tin nhắn thành công")
    void syncMessagesSuccessfully() {
        // Tạo dữ liệu SyncMessagesRequestDto
        Map<UUID, UUID> lastMessageIds = new HashMap<>();
        lastMessageIds.put(conversationId, messageId);
        SyncMessagesRequestDto syncRequest = SyncMessagesRequestDto.builder()
                .lastMessageIds(lastMessageIds)
                .build();

        // Thiết lập mock
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(eq(conversationId), eq(senderId)))
                .thenReturn(true);
        when(messageRepository.findNewerMessages(eq(conversationId), any(UUID.class)))
                .thenReturn(Collections.singletonList(message));
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);

        // Thực thi
        Map<UUID, List<MessageResponseDto>> result = messageService.syncMessages(senderId, syncRequest);

        // Kiểm tra
        assertNotNull(result);
        assertTrue(result.containsKey(conversationId));
        assertEquals(1, result.get(conversationId).size());
    }

    @Test
    @DisplayName("Tìm kiếm tin nhắn thành công")
    void searchMessagesSuccessfully() {
        String searchTerm = "test";

        // Thiết lập mock
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(conversationId, senderId))
                .thenReturn(true);
        when(messageRepository.searchMessages(eq(conversationId), eq(searchTerm), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(message)));
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // Thực thi
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> results = messageService.searchMessages(senderId, conversationId, searchTerm, pageable);

        // Kiểm tra
        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Xóa tin nhắn thành công")
    void deleteMessageSuccessfully() {

        MessageResponseDto spyMessageResponseDto = spy(messageResponseDto);

        // Thiết lập mock
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageMapper.entityToDto(message)).thenReturn(spyMessageResponseDto);

        // Thực thi
        messageService.deleteMessage(senderId, messageId);

        // Kiểm tra
        verify(messageRepository).save(message);
        assertTrue(message.isDeleted());
        verify(spyMessageResponseDto).setDeleted(true);
    }

    @Test
    @DisplayName("Cập nhật tin nhắn thành công")
    void updateMessageSuccessfully() {
        // Thiết lập dữ liệu
        messageRequestDto.setContent("Updated content");

        // Thiết lập mock
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(message);
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);

        // Thực thi
        MessageResponseDto result = messageService.updateMessage(senderId, messageId, messageRequestDto);

        // Kiểm tra
        assertNotNull(result);
        verify(messageRepository).save(message);
        verify(messageMapper).updateEntityFromDto(any(), any());
    }
}