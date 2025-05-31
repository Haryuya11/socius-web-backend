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
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ConversationController conversationController;

    private UUID conversationId;
    private UUID userId;
    private String userKey;
    private List<ConversationMemberDto> mockMembers;
    private Page<ConversationResponseDto> mockConversationsPage;
    private List<ConversationResponseDto> mockConversationsList;

    @BeforeEach
    void setUp() {

        // Setup test data
        conversationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userKey = "test-user-key";

        setupMockData();
    }

    private void setupMockData() {
        // Mock conversation members
        UserResponseDto user1 = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        UserResponseDto user2 = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        ConversationMemberDto member1 = ConversationMemberDto.builder()
                .conversationId(conversationId)
                .user(user1)
                .role(MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .build();

        ConversationMemberDto member2 = ConversationMemberDto.builder()
                .conversationId(conversationId)
                .user(user2)
                .role(MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        mockMembers = Arrays.asList(member1, member2);

        // Mock conversations
        ConversationResponseDto conversation1 = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Team Discussion")
                .type(ConversationType.GROUP)
                .createdBy(user1)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        ConversationResponseDto conversation2 = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Project Chat")
                .type(ConversationType.GROUP)
                .createdBy(user2)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        mockConversationsList = Arrays.asList(conversation1, conversation2);
        mockConversationsPage = new PageImpl<>(mockConversationsList, PageRequest.of(0, 10), 2);
    }

    @Test
    @DisplayName("Phải trả về danh sách thành viên cuộc trò chuyện khi có session hợp lệ")
    void getConversationMembers_ValidSession_ShouldReturnMembers() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getConversationMembers(conversationId, userId))
                    .thenReturn(mockMembers);

            // When
            ResponseEntity<?> response = conversationController.getConversationMembers(conversationId, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            List<ConversationMemberDto> responseBody = (List<ConversationMemberDto>) response.getBody();
            assertEquals(2, responseBody.size());
            assertEquals(mockMembers.getFirst().getUser().getEmail(), responseBody.getFirst().getUser().getEmail());

            verify(conversationService).getConversationMembers(conversationId, userId);
        }
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi session là null")
    void getConversationMembers_NullSession_ShouldReturnUnauthorized() {
        // Given
        when(request.getSession(false)).thenReturn(null);

        // When
        ResponseEntity<?> response = conversationController.getConversationMembers(conversationId, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Session không hợp lệ hoặc đã hết hạn", responseBody.get("error"));

        verify(conversationService, never()).getConversationMembers(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi userId là null")
    void getConversationMembers_NullUserId_ShouldReturnUnauthorized() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(null);

            // When
            ResponseEntity<?> response = conversationController.getConversationMembers(conversationId, request);

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertEquals("User ID không hợp lệ trong session", responseBody.get("error"));

            verify(conversationService, never()).getConversationMembers(any(UUID.class), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Phải trả về BAD_REQUEST khi service ném ra ngoại lệ")
    void getConversationMembers_ServiceThrowsException_ShouldReturnBadRequest() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getConversationMembers(conversationId, userId))
                    .thenThrow(new RuntimeException("Conversation not found"));

            // When
            ResponseEntity<?> response = conversationController.getConversationMembers(conversationId, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(conversationService).getConversationMembers(conversationId, userId);
        }
    }

    @Test
    @DisplayName("Phải trả về danh sách cuộc trò chuyện của người dùng với phân trang khi có session hợp lệ")
    void getUserConversations_ValidSession_ShouldReturnPagedConversations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getUserConversations(userId, pageable))
                    .thenReturn(mockConversationsPage);

            // When
            ResponseEntity<?> response = conversationController.getUserConversations(pageable, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            Page<ConversationResponseDto> responseBody = (Page<ConversationResponseDto>) response.getBody();
            assertEquals(2, responseBody.getContent().size());
            assertEquals(2, responseBody.getTotalElements());
            assertEquals("Team Discussion", responseBody.getContent().getFirst().getName());

            verify(conversationService).getUserConversations(userId, pageable);
        }
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi session là null cho getUserConversations")
    void getUserConversations_NullSession_ShouldReturnUnauthorized() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(request.getSession(false)).thenReturn(null);

        // When
        ResponseEntity<?> response = conversationController.getUserConversations(pageable, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Session không hợp lệ hoặc đã hết hạn", responseBody.get("error"));

        verify(conversationService, never()).getUserConversations(any(UUID.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi userId là null cho getUserConversations")
    void getUserConversations_NullUserId_ShouldReturnUnauthorized() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(null);

            // When
            ResponseEntity<?> response = conversationController.getUserConversations(pageable, request);

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertEquals("User ID không hợp lệ trong session", responseBody.get("error"));

            verify(conversationService, never()).getUserConversations(any(UUID.class), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Phải trả về BAD_REQUEST khi service ném ra ngoại lệ cho getUserConversations")
    void getUserConversations_ServiceThrowsException_ShouldReturnBadRequest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getUserConversations(userId, pageable))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            ResponseEntity<?> response = conversationController.getUserConversations(pageable, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(conversationService).getUserConversations(userId, pageable);
        }
    }

    @Test
    @DisplayName("Phải trả về danh sách tất cả cuộc trò chuyện của người dùng khi có session hợp lệ")
    void getAllUserConversations_ValidSession_ShouldReturnAllConversations() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getAllUserConversations(userId))
                    .thenReturn(mockConversationsList);

            // When
            ResponseEntity<?> response = conversationController.getAllUserConversations(request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            List<ConversationResponseDto> responseBody = (List<ConversationResponseDto>) response.getBody();
            assertEquals(2, responseBody.size());
            assertEquals("Team Discussion", responseBody.get(0).getName());
            assertEquals("Project Chat", responseBody.get(1).getName());

            verify(conversationService).getAllUserConversations(userId);
        }
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi session là null cho getAllUserConversations")
    void getAllUserConversations_NullSession_ShouldReturnUnauthorized() {
        // Given
        when(request.getSession(false)).thenReturn(null);

        // When
        ResponseEntity<?> response = conversationController.getAllUserConversations(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Session không hợp lệ hoặc đã hết hạn", responseBody.get("error"));

        verify(conversationService, never()).getAllUserConversations(any(UUID.class));
    }

    @Test
    @DisplayName("Phải trả về UNAUTHORIZED khi userId là null cho getAllUserConversations")
    void getAllUserConversations_NullUserId_ShouldReturnUnauthorized() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(null);

            // When
            ResponseEntity<?> response = conversationController.getAllUserConversations(request);

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertEquals("User ID không hợp lệ trong session", responseBody.get("error"));

            verify(conversationService, never()).getAllUserConversations(any(UUID.class));
        }
    }

    @Test
    @DisplayName("Phải trả về BAD_REQUEST khi service ném ra ngoại lệ cho getAllUserConversations")
    void getAllUserConversations_ServiceThrowsException_ShouldReturnBadRequest() {
        // Given
        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getAllUserConversations(userId))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // When
            ResponseEntity<?> response = conversationController.getAllUserConversations(request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(conversationService).getAllUserConversations(userId);
        }
    }

    @Test
    @DisplayName("Phải trả về danh sách rỗng khi không có cuộc trò chuyện nào cho người dùng")
    void getAllUserConversations_EmptyList_ShouldReturnEmptyList() {
        // Given
        List<ConversationResponseDto> emptyList = new ArrayList<>();

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getAllUserConversations(userId)).thenReturn(emptyList);

            // When
            ResponseEntity<?> response = conversationController.getAllUserConversations(request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            List<ConversationResponseDto> responseBody = (List<ConversationResponseDto>) response.getBody();
            assertTrue(responseBody.isEmpty());

            verify(conversationService).getAllUserConversations(userId);
        }
    }

    @Test
    @DisplayName("Phải trả về trang rỗng khi không có cuộc trò chuyện nào cho người dùng với phân trang")
    void getUserConversations_EmptyPage_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationResponseDto> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getUserConversations(userId, pageable)).thenReturn(emptyPage);

            // When
            ResponseEntity<?> response = conversationController.getUserConversations(pageable, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            Page<ConversationResponseDto> responseBody = (Page<ConversationResponseDto>) response.getBody();
            assertTrue(responseBody.getContent().isEmpty());
            assertEquals(0, responseBody.getTotalElements());

            verify(conversationService).getUserConversations(userId, pageable);
        }
    }

    @Test
    @DisplayName("Phải trả về danh sách thành viên rỗng khi không có thành viên nào trong cuộc trò chuyện")
    void getConversationMembers_EmptyMembersList_ShouldReturnEmptyList() {
        // Given
        List<ConversationMemberDto> emptyMembers = new ArrayList<>();

        try (MockedStatic<RedisKeyBuilder> mockedRedisKeyBuilder = mockStatic(RedisKeyBuilder.class)) {
            mockedRedisKeyBuilder.when(RedisKeyBuilder::userIdAttributeKey).thenReturn(userKey);

            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute(userKey)).thenReturn(userId);
            when(conversationService.getConversationMembers(conversationId, userId))
                    .thenReturn(emptyMembers);

            // When
            ResponseEntity<?> response = conversationController.getConversationMembers(conversationId, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            List<ConversationMemberDto> responseBody = (List<ConversationMemberDto>) response.getBody();
            assertTrue(responseBody.isEmpty());

            verify(conversationService).getConversationMembers(conversationId, userId);
        }
    }
}