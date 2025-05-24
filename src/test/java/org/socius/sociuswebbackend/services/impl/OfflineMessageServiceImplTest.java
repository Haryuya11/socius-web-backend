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
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OfflineMessageServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ConfigService configService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    @InjectMocks
    private OfflineMessageServiceImpl offlineMessageService;

    private final UUID userId = UUID.randomUUID();
    private MessageResponseDto messageResponseDto;
    private MessageResponseDto anotherMessageDto;
    private String offlineMessagesKey;

    @BeforeEach
    void setUp() {
        messageResponseDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .content("Test offline message")
                .conversationId(UUID.randomUUID())
                .build();

        anotherMessageDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .content("Another offline message")
                .conversationId(UUID.randomUUID())
                .build();

        offlineMessagesKey = RedisKeyBuilder.chatOfflineKey(userId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(configService.getInt(eq("offline.messages.expiry.days"), anyInt())).thenReturn(7);
    }

    @Test
    @DisplayName("Lưu trữ tin nhắn thành công")
    void storeOfflineMessageSuccessfully() {
        when(redisTemplate.opsForList().size(anyString())).thenReturn(0L);

        offlineMessageService.storeOfflineMessage(userId, messageResponseDto);

        verify(listOperations).rightPush(eq(offlineMessagesKey), eq(messageResponseDto));
    }

    @Test
    @DisplayName("Lấy danh sách tin nhắn offline thành công")
    void getOfflineMessagesSuccessfully() {

        when(listOperations.size(offlineMessagesKey)).thenReturn(2L);
        when(listOperations.range(eq(offlineMessagesKey), eq(0L), eq(1L)))
                .thenReturn(Arrays.asList(messageResponseDto, anotherMessageDto));

        List<MessageResponseDto> result = offlineMessageService.getOfflineMessages(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(messageResponseDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Lấy danh sách tin nhắn offline khi không có tin nhắn")
    void getOfflineMessagesWhenEmpty() {
        // Giả lập không có dữ liệu
        when(redisTemplate.opsForValue().get(offlineMessagesKey)).thenReturn(null);

        // Thực thi
        List<MessageResponseDto> result = offlineMessageService.getOfflineMessages(userId);

        // Kiểm tra
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Xóa tin nhắn offline sau khi lấy")
    void deleteOfflineMessagesAfterRetrieving() {
        // Thiết lập kích thước danh sách
        when(listOperations.size(offlineMessagesKey)).thenReturn(1L);

        // Mock range để trả về đúng messageResponseDto
        when(listOperations.range(eq(offlineMessagesKey), eq(0L), eq(0L)))
                .thenReturn(Collections.singletonList(messageResponseDto));

        when(redisTemplate.delete(offlineMessagesKey)).thenReturn(true);

        // Thực thi
        List<MessageResponseDto> result = offlineMessageService.getOfflineMessages(userId);
        offlineMessageService.clearOfflineMessages(userId);

        // Kiểm tra
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisTemplate).delete(offlineMessagesKey);
    }
}
