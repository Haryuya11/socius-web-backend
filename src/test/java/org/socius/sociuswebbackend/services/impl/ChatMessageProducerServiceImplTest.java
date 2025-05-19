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
import org.socius.sociuswebbackend.services.PendingMessagesService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChatMessageProducerServiceImplTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PendingMessagesService pendingMessagesService;

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
                any(MessagePostProcessor.class)
        );

        // Kiểm tra giá trị các tham số
        assertEquals(RabbitMQKeyBuilder.getChatExchange(), exchangeCaptor.getValue());
        assertEquals(RabbitMQKeyBuilder.getPrivateRoutingKeyPattern(conversationId), routingKeyCaptor.getValue());
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
        assertEquals(RabbitMQKeyBuilder.getChatExchange(), exchangeCaptor.getValue());
        assertEquals(RabbitMQKeyBuilder.getGroupRoutingKeyPattern(conversationId), routingKeyCaptor.getValue());
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

        verify(pendingMessagesService).addPendingMessage(
                any(UUID.class),
                eq(messageResponseDto)
        );
    }

    @Test
    @DisplayName("Gửi read receipt thành công")
    void sendReadReceiptSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID lastReadMessageId = UUID.randomUUID();

        // Thực thi
        chatMessageProducerService.sendReadReceipt(userId, conversationId, lastReadMessageId);

        // Kiểm tra
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKeyBuilder.getChatExchange()),
                anyString(),
                any(Map.class),
                any(MessagePostProcessor.class)
        );
    }
}