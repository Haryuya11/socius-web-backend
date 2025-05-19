package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChatMessageProducerServiceImplTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ChatMessageProducerServiceImpl chatMessageProducerService;

    @Captor
    private ArgumentCaptor<String> exchangeCaptor;

    @Captor
    private ArgumentCaptor<String> routingKeyCaptor;

    @Captor
    private ArgumentCaptor<MessageResponseDto> messageCaptor;

    private MessageResponseDto messageResponseDto;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu test
        conversationId = UUID.randomUUID();
        messageResponseDto = ChatTestDataUtil.createMessageResponseDto();
        messageResponseDto.setConversationId(conversationId);

        // Mock các giá trị cấu hình
        when(configService.getString(eq("rabbitmq.exchange.chat"), anyString()))
                .thenReturn("chat-exchange");
        when(configService.getString(eq("rabbitmq.direct-chat.routing-key"), anyString()))
                .thenReturn("direct");
        when(configService.getString(eq("rabbitmq.group-chat.routing-key"), anyString()))
                .thenReturn("group");
    }

    @Test
    @DisplayName("Gửi tin nhắn trực tiếp thành công")
    void sendDirectChatMessageSuccessfully() {
        // Thực thi
        chatMessageProducerService.sendChatMessage(messageResponseDto, ConversationType.DIRECT, conversationId);

        // Kiểm tra - sử dụng ArgumentCaptor để xác nhận các tham số
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)  // Chỉ định rõ loại của MessagePostProcessor
        );

        // Kiểm tra giá trị các tham số
        assertEquals("chat-exchange", exchangeCaptor.getValue());
        assertEquals("chat.message.private." + conversationId, routingKeyCaptor.getValue());
        assertEquals(messageResponseDto, messageCaptor.getValue());
    }

    @Test
    @DisplayName("Gửi tin nhắn nhóm thành công")
    void sendGroupChatMessageSuccessfully() {
        // Thực thi
        chatMessageProducerService.sendChatMessage(messageResponseDto, ConversationType.GROUP, conversationId);

        // Kiểm tra - sử dụng ArgumentCaptor để xác nhận các tham số
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)  // Chỉ định rõ loại của MessagePostProcessor
        );

        // Kiểm tra giá trị các tham số
        assertEquals("chat-exchange", exchangeCaptor.getValue());
        assertEquals("chat.message.group." + conversationId, routingKeyCaptor.getValue());
        assertEquals(messageResponseDto, messageCaptor.getValue());
    }

    @Test
    @DisplayName("Xử lý lỗi khi gửi tin nhắn")
    void handleErrorWhenSendingMessage() {
        // Giả lập lỗi
        doThrow(new RuntimeException("Test Error"))
                .when(rabbitTemplate).convertAndSend(
                        anyString(),
                        anyString(),
                        any(MessageResponseDto.class),
                        any(MessagePostProcessor.class)
                );

        // Thực thi - không nên ném ngoại lệ
        chatMessageProducerService.sendChatMessage(messageResponseDto, ConversationType.DIRECT, conversationId);

        // Kiểm tra ghi log lỗi (khó kiểm tra trực tiếp)
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(MessageResponseDto.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("Gửi read receipt thành công")
    void sendReadReceiptSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID lastReadMessageId = UUID.randomUUID();

        when(configService.getString(eq("rabbitmq.exchange.chat"), anyString()))
                .thenReturn("chat-exchange");

        // Thực thi
        chatMessageProducerService.sendReadReceipt(userId, conversationId, lastReadMessageId);

        // Kiểm tra - sử dụng cú pháp verify phù hợp với phương thức thực tế
        verify(rabbitTemplate).convertAndSend(
                eq("chat-exchange"),
                anyString(),
                any(Map.class)
        );
    }
}