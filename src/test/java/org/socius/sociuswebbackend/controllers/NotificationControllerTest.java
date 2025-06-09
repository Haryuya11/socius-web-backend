package org.socius.sociuswebbackend.controllers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import org.socius.sociuswebbackend.services.NotificationService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for NotificationController, testing REST endpoints.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;
    private UUID notificationId;
    private UUID senderId;
    private NotificationRequestDto requestDto;
    private NotificationResponseDto responseDto;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Setup test environment and data before each test.
     */
    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper with custom LocalDate serializer/deserializer
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        objectMapper.registerModule(module);
        // Disable features that may modify immutable collections
        objectMapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        objectMapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        // Setup UserResponseDto for recipient
        UserResponseDto userDto = UserResponseDto.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("testuser@example.com")
                .build();

        // Setup UserResponseDto for sender
        UserResponseDto senderDto = UserResponseDto.builder()
                .id(senderId)
                .firstName("Sender")
                .lastName("User")
                .email("sender@example.com")
                .build();

        // Setup NotificationRequestDto
        requestDto = NotificationRequestDto.builder()
                .title("Test Notification")
                .senderId(senderId)
                .message("This is a test notification")
                .expiryDate(LocalDate.now().plusDays(1))
                .type(NotificationType.info)
                .isUrgent(false)
                .recipientIds(Collections.singletonList(userId))
                .build();

        // Setup NotificationRecipientDto
        NotificationRecipientDto recipientDto = NotificationRecipientDto.builder()
                .notificationId(notificationId)
                .userId(userId)
                .user(userDto)
                .isRead(false)
                .readAt(null)
                .build();

        // Setup NotificationResponseDto
        responseDto = NotificationResponseDto.builder()
                .id(notificationId)
                .title("Test Notification")
                .sender(senderDto)
                .message("This is a test notification")
                .expiryDate(LocalDate.now().plusDays(1))
                .type(NotificationType.info)
                .isUrgent(false)
                .recipients(Collections.singletonList(recipientDto))
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
     * Test POST /api/notification creates a notification successfully.
     */
    @Test
    void testCreateNotification() throws Exception {
        when(notificationService.createNotification(any(NotificationRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.title").value("Test Notification"))
                .andExpect(jsonPath("$.message").value("This is a test notification"))
                .andExpect(jsonPath("$.type").value("info"))
                .andExpect(jsonPath("$.isUrgent").value(false))
                .andExpect(jsonPath("$.sender.id").value(senderId.toString()))
                .andExpect(jsonPath("$.recipients[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.recipients[0].isRead").value(false));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQKeyBuilder.SESSION_MANAGEMENT_EXCHANGE),
                eq(RabbitMQKeyBuilder.INVALIDATE_SESSION_ROUTING_KEY),
                eq(responseDto)
        );
        verify(notificationService).createNotification(any(NotificationRequestDto.class));
    }

    /**
     * Test POST /api/notification with invalid input returns 400.
     */
    @Test
    void testCreateNotificationInvalidInput() throws Exception {
        requestDto.setTitle("");
        requestDto.setExpiryDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService, rabbitTemplate);
    }

    /**
     * Test PUT /api/notification/{notificationId}/read marks notification as read.
     */
    @Test
    void testMarkNotificationAsRead() throws Exception {
        doNothing().when(notificationService).markNotificationAsRead(notificationId, userId);

        mockMvc.perform(put("/api/notification/{notificationId}/read", notificationId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService).markNotificationAsRead(notificationId, userId);
    }

    /**
     * Test PUT /api/notification/{notificationId}/read with missing userId returns 400.
     */
    @Test
    void testMarkNotificationAsReadMissingUserId() throws Exception {
        mockMvc.perform(put("/api/notification/{notificationId}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService);
    }

    /**
     * Test PUT /api/notification/{notificationId}/read with non-existing notificationId returns 404.
     */
    @Test
    void testMarkNotificationAsReadNotificationNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Notification not found")).when(notificationService).markNotificationAsRead(notificationId, userId);

        mockMvc.perform(put("/api/notification/{notificationId}/read", notificationId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(notificationService).markNotificationAsRead(notificationId, userId);
    }

    /**
     * Test PUT /api/notification/{notificationId}/read with non-recipient userId.
     */
    @Test
    void testMarkNotificationAsReadUserNotRecipient() throws Exception {
        doThrow(new IllegalArgumentException("User is not a recipient")).when(notificationService).markNotificationAsRead(notificationId, userId);

        mockMvc.perform(put("/api/notification/{notificationId}/read", notificationId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    Throwable cause = result.getResolvedException();
                    System.out.println("Resolved exception: " + (cause != null ? cause.getClass().getName() : "null"));
                    if (cause == null) {
                        assertEquals(400, result.getResponse().getStatus(), "Expected HTTP 400 Bad Request");
                    } else {
                        assertInstanceOf(IllegalArgumentException.class, cause, "Expected IllegalArgumentException, got " + cause.getClass().getName());
                        assertEquals("User is not a recipient", cause.getMessage());
                    }
                });

        verify(notificationService).markNotificationAsRead(notificationId, userId);
    }
}