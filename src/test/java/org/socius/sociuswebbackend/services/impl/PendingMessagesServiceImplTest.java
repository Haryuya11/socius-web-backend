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
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PendingMessagesServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ConfigService configService;

    @Mock
    private OfflineMessageService offlineMessageService;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PendingMessagesServiceImpl pendingMessagesService;

    private final UUID userId = UUID.randomUUID();
    private MessageResponseDto messageResponseDto;
    private MessageResponseDto anotherMessageDto;
    private String pendingMessagesKey;

    @BeforeEach
    void setUp() {
        messageResponseDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .content("Test pending message")
                .conversationId(UUID.randomUUID())
                .build();

        anotherMessageDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .content("Another offline message")
                .conversationId(UUID.randomUUID())
                .build();

        pendingMessagesKey = RabbitMQKeyBuilder.getPendingMessagesKey(userId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);

    }

    @Test
    @DisplayName("Khởi tạo buffer tin nhắn thành công")
    void initializeBufferSuccessfully() {
        // Thực thi
        pendingMessagesService.initializeBuffer(userId);

        // Kiểm tra
        verify(listOperations).rightPushAll(eq(pendingMessagesKey), eq(new ArrayList<>()));

    }

    @Test
    @DisplayName("Thêm tin nhắn vào buffer khi buffer đã tồn tại")
    void addMessageToBufferWhenBufferExists() {
        // Giả lập dữ liệu
        List<MessageResponseDto> existingMessages = Arrays.asList(messageResponseDto);
        when(listOperations.range(eq(pendingMessagesKey), eq(0L), eq(-1L)))
                .thenReturn(Collections.singletonList(existingMessages));

        // Thực thi
        pendingMessagesService.addPendingMessage(userId, anotherMessageDto);

        // Kiểm tra
        verify(listOperations).rightPush(eq(pendingMessagesKey), eq(anotherMessageDto));
    }

    @Test
    @DisplayName("Thêm tin nhắn vào buffer khi buffer chưa tồn tại")
    void addPendingMessageWhenBufferNotExists() {
        // Giả lập buffer chưa tồn tại
        when(listOperations.range(eq(pendingMessagesKey), eq(0L), eq(-1L)))
                .thenReturn(null);

        // Thực thi
        pendingMessagesService.addPendingMessage(userId, messageResponseDto);

        // Kiểm tra
        verify(listOperations).rightPush(eq(pendingMessagesKey), eq(messageResponseDto));
    }

    @Test
    @DisplayName("Lấy và xóa tin nhắn đang chờ")
    void getPendingMessagesAndClearBuffer() {

        when(listOperations.size(pendingMessagesKey)).thenReturn(1L);

        // Giả lập dữ liệu
        when(listOperations.range(eq(pendingMessagesKey), eq(0L), eq(-1L)))
                .thenReturn(Collections.singletonList(messageResponseDto));

        when(redisTemplate.delete(pendingMessagesKey)).thenReturn(true);


        // SỬA: Thay thế offlineMessageService bằng pendingMessagesService
        List<MessageResponseDto> result = pendingMessagesService.getPendingMessages(userId);
        pendingMessagesService.clearBuffer(userId);

        // Kiểm tra
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisTemplate).delete(pendingMessagesKey);
    }
}
