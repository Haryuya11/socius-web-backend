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
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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


        when(conversationMemberRepository.findActiveMember(eq(conversationId), eq(senderId)))
                .thenReturn(Optional.of(mock(ConversationMemberEntity.class)));

        // Mock conversation exists
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // Mock user exists
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));

    }

    @Test
    @DisplayName("Gửi tin nhắn văn bản thành công")
    void sendTextMessageSuccessfully() {
        // Setup
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));


        when(messageMapper.requestDtoToEntity(any(MessageRequestDto.class))).thenReturn(message);
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(message);
        when(messageMapper.entityToDto(any(MessageEntity.class))).thenReturn(messageResponseDto);

        // Execute & Verify
        assertDoesNotThrow(() -> {
            MessageResponseDto result = messageService.sendMessage(senderId, messageRequestDto);
            assertNotNull(result);
            assertEquals(messageResponseDto.getId(), result.getId());
        });
    }

    @Test
    @DisplayName("Gửi tin nhắn khi không phải thành viên của cuộc trò chuyện")
    void sendMessageWhenNotMemberOfConversation() {
        // Setup
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        when(conversationMemberRepository.findActiveMember(eq(conversationId), eq(senderId)))
                .thenReturn(Optional.empty()); // User is NOT active member

        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () ->
                messageService.sendMessage(senderId, messageRequestDto));

        assertTrue(exception.getMessage().contains("không phải là thành viên") ||
                exception.getMessage().contains("không có quyền gửi tin nhắn"));
    }

    @Test
    @DisplayName("Lấy tin nhắn theo cuộc trò chuyện")
    void getMessagesByConversation() {
        // Setup
        when(conversationMemberRepository.findActiveMember(eq(conversationId), eq(senderId)))
                .thenReturn(Optional.of(mock(ConversationMemberEntity.class))); // User is member
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Execute - sửa thứ tự tham số
        assertDoesNotThrow(() -> messageService.getMessages(senderId, conversationId, Pageable.unpaged()));

        // Verify
        verify(messageRepository).findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any(Pageable.class));
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

    @Test
    @DisplayName("Gửi tin nhắn hình ảnh thành công")
    void sendImageMessageSuccessfully() {
        messageRequestDto.setMessageType(MessageType.IMAGE);
        messageRequestDto.setContent("Updated content");
        messageRequestDto.setFileUrl("/uploads/images/test.jpg");

        message.setMessageType(MessageType.IMAGE);
        message.setContent("Updated content");
        message.setFileUrl("/uploads/images/test.jpg");

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageMapper.requestDtoToEntity(messageRequestDto)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.entityToDto(message)).thenReturn(messageResponseDto);

        MessageResponseDto result = messageService.sendMessage(senderId, messageRequestDto);

        assertNotNull(result, "Kết quả không được null");
        verify(messageRepository).save(any(MessageEntity.class));
        verify(chatMessageProducerService).sendChatMessage(any(MessageResponseDto.class),
                any(), any(UUID.class));
    }

    @Test
    @DisplayName("Gửi tin nhắn thất bại khi người gửi không tồn tại")
    void sendMessageFailsWhenSenderNotFound() {
        // Setup
        when(userRepository.findById(senderId)).thenReturn(Optional.empty()); // User not found

        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () ->
                messageService.sendMessage(senderId, messageRequestDto));

        assertTrue(exception.getMessage().contains("Không tìm thấy người dùng"));
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("Gửi tin nhắn thất bại khi cuộc trò chuyện không tồn tại")
    void sendMessageFailsWhenConversationNotFound() {
        // Setup mock
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Execute & Verify
        Exception exception = assertThrows(RuntimeException.class, () ->
                messageService.sendMessage(senderId, messageRequestDto));

        assertTrue(exception.getMessage().contains("Không tìm thấy cuộc trò chuyện"));
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("Tìm kiếm tin nhắn với từ khóa có nhiều kết quả")
    void searchMessagesWithMultipleResults() {
        String searchTerm = "test";

        // Tạo nhiều tin nhắn để test
        MessageEntity message1 = ChatTestDataUtil.createMessageEntity(conversation, sender);
        message1.setId(UUID.randomUUID());
        message1.setContent("Test message one");

        MessageEntity message2 = ChatTestDataUtil.createMessageEntity(conversation, sender);
        message2.setId(UUID.randomUUID());
        message2.setContent("Test message two");

        MessageResponseDto messageResponseDto1 = ChatTestDataUtil.createMessageResponseDto();
        messageResponseDto1.setContent("Test message one");

        MessageResponseDto messageResponseDto2 = ChatTestDataUtil.createMessageResponseDto();
        messageResponseDto2.setContent("Test message two");

        // Thiết lập mock
        when(messageRepository.searchMessages(eq(conversationId), eq(searchTerm), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(message1, message2)));

        when(messageMapper.entityToDto(message1)).thenReturn(messageResponseDto1);
        when(messageMapper.entityToDto(message2)).thenReturn(messageResponseDto2);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // Thực thi
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> results = messageService.searchMessages(senderId, conversationId, searchTerm, pageable);

        // Kiểm tra
        assertNotNull(results);
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().get(0).getContent().toLowerCase().contains(searchTerm.toLowerCase()),
                "Tin nhắn đầu tiên phải chứa từ khóa tìm kiếm");
        assertTrue(results.getContent().get(1).getContent().toLowerCase().contains(searchTerm.toLowerCase()),
                "Tin nhắn thứ hai phải chứa từ khóa tìm kiếm");
    }

    @Test
    @DisplayName("Đồng bộ tin nhắn với nhiều cuộc trò chuyện")
    void syncMessagesWithMultipleConversations() {
        // Setup data
        UUID senderId = UUID.randomUUID();
        UUID conversationId1 = UUID.randomUUID();
        UUID conversationId2 = UUID.randomUUID();

        // Tạo SyncMessagesRequestDto
        Map<UUID, UUID> lastMessageIds = new HashMap<>();
        lastMessageIds.put(conversationId1, UUID.randomUUID());
        lastMessageIds.put(conversationId2, UUID.randomUUID());

        SyncMessagesRequestDto syncRequest = SyncMessagesRequestDto.builder()
                .lastMessageIds(lastMessageIds)
                .build();

        // Tạo entities
        UserEntity sender = AuthTestDataUtil.createTestAdminUser();
        sender.setId(senderId); // Đặt ID giống với senderId

        ConversationEntity conversation1 = ChatTestDataUtil.createConversationEntity();
        conversation1.setId(conversationId1);

        ConversationEntity conversation2 = ChatTestDataUtil.createConversationEntity();
        conversation2.setId(conversationId2);

        MessageEntity message1 = ChatTestDataUtil.createMessageEntity(conversation1, sender);
        message1.setId(UUID.randomUUID());

        MessageEntity message2 = ChatTestDataUtil.createMessageEntity(conversation2, sender);
        message2.setId(UUID.randomUUID());

        MessageResponseDto messageResponseDto = ChatTestDataUtil.createMessageResponseDto();

        // **QUAN TRỌNG: Mock để user thuộc về các conversations**
        when(conversationMemberRepository.findActiveMember(conversationId1, senderId))
                .thenReturn(Optional.of(mock(ConversationMemberEntity.class)));
        when(conversationMemberRepository.findActiveMember(conversationId2, senderId))
                .thenReturn(Optional.of(mock(ConversationMemberEntity.class)));

        // Thiết lập mock cho repository
        when(messageRepository.findNewerMessages(eq(conversationId1), any(UUID.class)))
                .thenReturn(Collections.singletonList(message1));
        when(messageRepository.findNewerMessages(eq(conversationId2), any(UUID.class)))
                .thenReturn(Collections.singletonList(message2));

        // Mock mapper
        when(messageMapper.entityToDto(message1)).thenReturn(messageResponseDto);
        when(messageMapper.entityToDto(message2)).thenReturn(messageResponseDto);

        // Thực thi
        Map<UUID, List<MessageResponseDto>> result = messageService.syncMessages(senderId, syncRequest);

        // Kiểm tra
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(conversationId1));
        assertTrue(result.containsKey(conversationId2));
        assertEquals(1, result.get(conversationId1).size());
        assertEquals(1, result.get(conversationId2).size());

        // Verify các method được gọi
        verify(conversationMemberRepository, times(2)).findActiveMember(any(UUID.class), eq(senderId));
        verify(messageRepository, times(2)).findNewerMessages(any(UUID.class), any(UUID.class));
    }
}