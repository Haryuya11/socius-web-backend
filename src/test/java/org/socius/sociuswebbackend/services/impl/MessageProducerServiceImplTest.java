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
import org.socius.sociuswebbackend.model.enums.InvalidationReason;
import org.socius.sociuswebbackend.model.messages.SessionInvalidationMessage;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageProducerServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private MessageProducerServiceImpl messageProducerService;

    private final UUID roleId = UUID.randomUUID();
    private final String exchangeName = "test.exchange";
    private final String routingKey = "test.routing.key";

    @BeforeEach
    void setUp() {
        when(configService.getString(eq("rabbitmq.exchange.session"), anyString())).thenReturn(exchangeName);
        when(configService.getString(eq("rabbitmq.routing.invalidate"), anyString())).thenReturn(routingKey);
    }

    @Test
    @DisplayName("Gửi thông điệp hủy phiên cho vai trò cụ thể thành công")
    void sendSessionInvalidationMessageForRoleShouldSucceed() {
        String message = "Vui lòng đăng nhập lại để cập nhật quyền hạn";
        InvalidationReason reason = InvalidationReason.ROLE_CHANGED;

        messageProducerService.sendSessionInvalidationMessage(roleId, reason, message);

        verify(rabbitTemplate).convertAndSend(eq(exchangeName), eq(routingKey),
                (Object) argThat(arg -> {
                    SessionInvalidationMessage msg = (SessionInvalidationMessage) arg;
                    return msg.getRoleId().equals(roleId) &&
                            msg.getReason() == reason &&
                            msg.getMessage().equals(message) &&
                            msg.isForceAllUsersWithRole();
                }));
    }

    @Test
    @DisplayName("Gửi thông điệp hủy phiên cụ thể thành công")
    void sendSpecificSessionInvalidationMessageShouldSucceed() {
        Set<String> sessiomIds = new HashSet<>();
        sessiomIds.add("sessionId1");
        sessiomIds.add("sessionId2");
        String message = "Phiên làm việc của bạn đã bị hủy";

        InvalidationReason reason = InvalidationReason.FORCE_LOGOUT;

        messageProducerService.sendSpecificSessionInvalidationMessage(sessiomIds, reason, message);

        verify(rabbitTemplate).convertAndSend(eq(exchangeName), eq(routingKey),
                (Object) argThat(arg -> {
                    SessionInvalidationMessage msg = (SessionInvalidationMessage) arg;
                    return msg.getSessionIds().equals(sessiomIds) &&
                            msg.getReason() == reason &&
                            msg.getMessage().equals(message) &&
                            !msg.isForceAllUsersWithRole();
                }));
    }

    @Test
    @DisplayName("Bắt lỗi và xử lý khi gửi thông điệp thất bại")
    void shouldHandleExceptionWhenSendingMessageFails() {
        doThrow(new RuntimeException("Lỗi kết nối")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(SessionInvalidationMessage.class));

        messageProducerService.sendSessionInvalidationMessage(roleId, InvalidationReason.ROLE_CHANGED, "test");
        messageProducerService.sendSpecificSessionInvalidationMessage(Set.of("session-1"), InvalidationReason.FORCE_LOGOUT, "test");

        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), any(SessionInvalidationMessage.class));
    }
}
