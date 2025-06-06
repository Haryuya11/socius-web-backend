package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
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
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
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
    private UserRepository userRepository;

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
        // Setup SecurityContext cho test
        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock UserRepository
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tạo conversation thành công")
    void createOrGetDirectConversation_Success() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        ConversationResponseDto expectedConversation = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .build();

        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenReturn(expectedConversation);

        // When
        ResponseEntity<?> response = conversationController
                .createOrGetDirectConversation(otherUserId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedConversation, response.getBody());
        verify(conversationService).getOrCreateDirectConversation(otherUserId);
    }

    @Test
    @DisplayName("User không tồn tại - trả về 400")
    void createOrGetDirectConversation_UserNotFound_ReturnsBadRequest() {
        // Given
        UUID otherUserId = UUID.randomUUID();

        // Mock user không tồn tại
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenThrow(new IllegalArgumentException("Người dùng không tồn tại"));

        // When
        ResponseEntity<?> response = conversationController
                .createOrGetDirectConversation(otherUserId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Người dùng không tồn tại", body.get("error"));
    }

    @Test
    @DisplayName("Authentication null - trả về 400")
    void createOrGetDirectConversation_NoAuthentication_ReturnsBadRequest() {
        // Given
        UUID otherUserId = UUID.randomUUID();

        // Clear SecurityContext
        SecurityContextHolder.clearContext();

        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenThrow(new IllegalArgumentException("User không được xác thực"));

        // When
        ResponseEntity<?> response = conversationController
                .createOrGetDirectConversation(otherUserId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}