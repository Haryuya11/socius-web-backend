package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversationControllerCreateOrGetDirectConversationTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ConversationController conversationController;

    private UUID userId;
    private UUID otherUserId;
    private String userKey;
    private ConversationResponseDto conversationResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        userKey = "spring:session:user:id";

        // Setup ConversationResponseDto
        UserResponseDto createdBy = UserResponseDto.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        conversationResponseDto = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Direct Conversation")
                .type(ConversationType.DIRECT)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện trực tiếp thành công")
    void createOrGetDirectConversation_Success() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, otherUserId))
                    .thenReturn(conversationResponseDto);

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(conversationResponseDto, response.getBody());

            // Verify interactions
            verify(request).getSession(false);
            verify(session).getAttribute(userKey);
            verify(conversationService).getOrCreateDirectConversation(userId, otherUserId);
        }
    }

    @Test
    @DisplayName("Thất bại khi session null")
    void createOrGetDirectConversation_SessionNull_ReturnsUnauthorized() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(null);

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertNotNull(responseBody);
            assertEquals("Session không hợp lệ hoặc đã hết hạn", responseBody.get("error"));

            // Verify interactions
            verify(request).getSession(false);
            verify(conversationService, never()).getOrCreateDirectConversation(any(UUID.class), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Thất bại khi userId null trong session")
    void createOrGetDirectConversation_UserIdNull_ReturnsUnauthorized() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(null);

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertNotNull(responseBody);
            assertEquals("User ID không hợp lệ trong session", responseBody.get("error"));

            // Verify interactions
            verify(request).getSession(false);
            verify(session).getAttribute(userKey);
            verify(conversationService, never()).getOrCreateDirectConversation(any(UUID.class), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Thất bại khi service throw RuntimeException")
    void createOrGetDirectConversation_ServiceThrowsException_ReturnsBadRequest() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            String errorMessage = "Không thể tạo cuộc trò chuyện với chính mình";
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, otherUserId))
                    .thenThrow(new RuntimeException(errorMessage));

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertNotNull(responseBody);
            assertEquals(errorMessage, responseBody.get("error"));

            // Verify interactions
            verify(request).getSession(false);
            verify(session).getAttribute(userKey);
            verify(conversationService).getOrCreateDirectConversation(userId, otherUserId);
        }
    }

    @Test
    @DisplayName("Thất bại khi service throw IllegalArgumentException")
    void createOrGetDirectConversation_ServiceThrowsIllegalArgumentException_ReturnsBadRequest() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            String errorMessage = "Không thể tạo cuộc trò chuyện với chính mình";
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, otherUserId))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertNotNull(responseBody);
            assertEquals(errorMessage, responseBody.get("error"));

            // Verify interactions
            verify(request).getSession(false);
            verify(session).getAttribute(userKey);
            verify(conversationService).getOrCreateDirectConversation(userId, otherUserId);
        }
    }

    @Test
    @DisplayName("Kiểm tra tham số otherUserId được truyền đúng")
    void createOrGetDirectConversation_CorrectParametersPassed() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            UUID specificOtherUserId = UUID.fromString("12345678-1234-1234-1234-123456789012");
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, specificOtherUserId))
                    .thenReturn(conversationResponseDto);

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(specificOtherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Verify exact parameters
            verify(conversationService).getOrCreateDirectConversation(
                    eq(userId),
                    eq(specificOtherUserId)
            );
        }
    }

    @Test
    @DisplayName("Kiểm tra response body structure cho trường hợp thành công")
    void createOrGetDirectConversation_Success_ResponseBodyStructure() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, otherUserId))
                    .thenReturn(conversationResponseDto);

            // Act
            ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            ConversationResponseDto responseBody = (ConversationResponseDto) response.getBody();
            assertNotNull(responseBody);
            assertEquals(conversationResponseDto.getId(), responseBody.getId());
            assertEquals(conversationResponseDto.getName(), responseBody.getName());
            assertEquals(conversationResponseDto.getType(), responseBody.getType());
            assertEquals(conversationResponseDto.getCreatedBy().getId(), responseBody.getCreatedBy().getId());
        }
    }

    @Test
    @DisplayName("Kiểm tra RedisKeyBuilder được gọi đúng")
    void createOrGetDirectConversation_RedisKeyBuilderCalled() {
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            // Arrange
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getOrCreateDirectConversation(userId, otherUserId))
                    .thenReturn(conversationResponseDto);

            // Act
            conversationController.createOrGetDirectConversation(otherUserId, request);

            // Assert
            mockedRedisKeyBuilder.verify(RedisKeyBuilder::userIdAttributeKey, times(1));
        }
    }
}