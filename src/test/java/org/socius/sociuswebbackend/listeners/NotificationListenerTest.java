package org.socius.sociuswebbackend.listeners;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationListener, testing RabbitMQ message processing and WebSocket sending.
 */
@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationListener notificationListener;

    private ObjectMapper objectMapper;
    private NotificationResponseDto responseDto;
    private NotificationRecipientDto recipientDto;
    private UserResponseDto userDto;
    private UserResponseDto senderDto;
    private UUID userId;
    private UUID notificationId;
    private UUID senderId;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Setup test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper with custom serializers/deserializers
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(module);

        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        // Setup UserResponseDto for recipient
        userDto = UserResponseDto.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("testuser@example.com")
                .build();

        // Setup UserResponseDto for sender
        senderDto = UserResponseDto.builder()
                .id(senderId)
                .firstName("Sender")
                .lastName("User")
                .email("sender@example.com")
                .build();

        // Setup NotificationRecipientDto
        recipientDto = NotificationRecipientDto.builder()
                .notificationId(notificationId)
                .userId(userId)
                .user(userDto)
                .isRead(false)
                .readAt(null)
                .build();

        // Setup NotificationResponseDto with mutable list
        responseDto = NotificationResponseDto.builder()
                .id(notificationId)
                .title("Test Notification")
                .sender(senderDto)
                .message("This is a test notification")
                .expiryDate(LocalDate.now().plusDays(1))
                .type(NotificationType.info)
                .isUrgent(false)
                .recipients(new ArrayList<>(Arrays.asList(recipientDto)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Custom serializer for LocalDate
     */
    private static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DATE_FORMATTER));
        }
    }

    /**
     * Custom deserializer for LocalDate
     */
    private static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String date = p.getText();
            return LocalDate.parse(date, DATE_FORMATTER);
        }
    }

    /**
     * Custom serializer for LocalDateTime
     */
    private static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DATE_TIME_FORMATTER));
        }
    }

    /**
     * Custom deserializer for LocalDateTime
     */
    private static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateTime = p.getText();
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        }
    }

    /**
     * Test receiveNotification sends WebSocket message to recipient.
     */
    @Test
    void testReceiveNotification() throws Exception {
        // Serialize and deserialize to simulate RabbitMQ message
        String jsonMessage = objectMapper.writeValueAsString(responseDto);
        NotificationResponseDto receivedDto = objectMapper.readValue(jsonMessage, NotificationResponseDto.class);

        // Call the listener method
        notificationListener.receiveNotification(receivedDto);

        // Verify WebSocket message sent to recipient
        verify(messagingTemplate).convertAndSendToUser(
                eq(userId.toString()),
                eq("/queue/notifications"),
                eq(receivedDto)
        );
    }

    /**
     * Test receiveNotification with empty recipients does not send WebSocket message.
     */
    @Test
    void testReceiveNotificationEmptyRecipients() throws Exception {
        // Create responseDto with empty recipients
        responseDto.setRecipients(new ArrayList<>());
        String jsonMessage = objectMapper.writeValueAsString(responseDto);
        NotificationResponseDto receivedDto = objectMapper.readValue(jsonMessage, NotificationResponseDto.class);

        // Call the listener method
        notificationListener.receiveNotification(receivedDto);

        // Verify no WebSocket message sent
        verifyNoInteractions(messagingTemplate);
    }
}