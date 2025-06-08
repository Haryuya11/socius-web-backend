package org.socius.sociuswebbackend.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.exception.GlobalExceptionHandler;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageService messageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private MockMvc mockMvc;

    @InjectMocks
    private ConversationController conversationController;

    private UUID conversationId;
    private UUID otherUserId;
    private UserEntity mockUser;
    private ConversationResponseDto conversationResponseDto;
    private ConversationMemberDto memberDto;
    private MessageResponseDto messageResponseDto;


    @BeforeEach
    void setUp() {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        // Thiết lập MockMvc với resolver
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController)
                .setControllerAdvice(globalExceptionHandler)
                .setCustomArgumentResolvers(resolver)
                .build();

        conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        // Setup mock user
        mockUser = new UserEntity();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");

        // Setup mock DTOs
        conversationResponseDto = ConversationResponseDto.builder()
                .id(conversationId)
                .name("Test Conversation")
                .type(ConversationType.DIRECT)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        memberDto = ConversationMemberDto.builder()
                .conversationId(conversationId)
                .user(userResponseDto)
                .joinedAt(LocalDateTime.now())
                .build();

        messageResponseDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .sender(userResponseDto)
                .content("Test message")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupValidAuthentication() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    }

    private void setupInvalidAuthentication() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    }

    private void setupNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    // Test getConversationMembers
    @Test
    @DisplayName("Lấy thành viên conversation thành công")
    void getConversationMembers_ValidRequest_ShouldReturnMembers() {
        // Given
        setupValidAuthentication();
        List<ConversationMemberDto> members = List.of(memberDto);
        when(conversationService.getConversationMembers(conversationId)).thenReturn(members);

        // When
        ResponseEntity<?> response = conversationController.getConversationMembers(conversationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(members, response.getBody());
        verify(conversationService).getConversationMembers(conversationId);
    }

    @Test
    @DisplayName("Lấy thành viên conversation - không có authentication")
    void getConversationMembers_NoAuthentication_ShouldReturnBadRequest() throws Exception {
        // Given
        setupNoAuthentication();
        when(conversationService.getConversationMembers(conversationId))
                .thenThrow(new IllegalArgumentException("User không được xác thực"));

//        // When
//        ResponseEntity<List<ConversationMemberDto>> response = conversationController.getConversationMembers(conversationId);
//
//        // Then
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//
//        assertNotNull(response.getBody());

        // When & Then
        mockMvc.perform(get("/api/conversations/{conversationId}/members", conversationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User không được xác thực"));
    }

    @Test
    @DisplayName("Lấy thành viên conversation - user không tồn tại")
    void getConversationMembers_UserNotFound_ShouldReturnBadRequest() throws Exception {
        // Given
        UUID conversationId = UUID.randomUUID();
        when(conversationService.getConversationMembers(conversationId))
                .thenThrow(new IllegalArgumentException("Người dùng không tồn tại"));

        // When & Then
        mockMvc.perform(get("/api/conversations/{conversationId}/members", conversationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Người dùng không tồn tại"));
    }

    // Test getUserConversations
    @Test
    @DisplayName("Lấy conversations của user với phân trang thành công")
    void getUserConversations_ValidRequest_ShouldReturnConversations() {
        // Given
        setupValidAuthentication();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationResponseDto> conversations = new PageImpl<>(List.of(conversationResponseDto));
        when(conversationService.getUserConversations(pageable)).thenReturn(conversations);

        // When
        ResponseEntity<?> response = conversationController.getUserConversations(pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversations, response.getBody());
        verify(conversationService).getUserConversations(pageable);
    }

    @Test
    @DisplayName("Lấy conversations - không có authentication")
    void getUserConversations_NoAuthentication_ShouldReturnBadRequest() throws Exception {
        // Given
        setupNoAuthentication();
        Pageable pageable = PageRequest.of(0, 10);
        when(conversationService.getUserConversations(pageable))
                .thenThrow(new IllegalArgumentException("User không được xác thực"));
//
//        // When
//        ResponseEntity<Page<ConversationResponseDto>> response = conversationController.getUserConversations(pageable);
//
//        // Then
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());

        // When & Then
        mockMvc.perform(get("/api/conversations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User không được xác thực"));
    }

    @Test
    @DisplayName("Lấy conversations - user không tồn tại")
    void getUserConversations_UserNotFound_ShouldReturnBadRequest() throws Exception {
        // Given
        when(conversationService.getUserConversations(any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Người dùng không tồn tại"));

        // When & Then
        mockMvc.perform(get("/api/conversations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Người dùng không tồn tại"));
    }

    @Test
    @DisplayName("Tạo direct conversation - user không tồn tại")
    void createOrGetDirectConversation_UserNotFound_ShouldReturnBadRequest() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenThrow(new IllegalArgumentException("Người dùng không tồn tại"));

        // When & Then
        mockMvc.perform(post("/api/conversations/direct/{otherUserId}", otherUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Người dùng không tồn tại"));
    }


    // Test getAllUserConversations
    @Test
    @DisplayName("Lấy tất cả conversations của user thành công")
    void getAllUserConversations_ValidRequest_ShouldReturnAllConversations() {
        // Given
        setupValidAuthentication();
        List<ConversationResponseDto> conversations = List.of(conversationResponseDto);
        when(conversationService.getAllUserConversations()).thenReturn(conversations);

        // When
        ResponseEntity<?> response = conversationController.getAllUserConversations();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversations, response.getBody());
        verify(conversationService).getAllUserConversations();
    }

    @Test
    @DisplayName("Lấy tất cả conversations - không có authentication")
    void getAllUserConversations_NoAuthentication_ShouldReturnBadRequest() throws Exception {
        // Given
        setupNoAuthentication();
        when(conversationService.getAllUserConversations())
                .thenThrow(new IllegalArgumentException("User không được xác thực"));

//        // When
//        ResponseEntity<List<ConversationResponseDto>> response = conversationController.getAllUserConversations();
//
//        // Then
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());

        // When & Then
        mockMvc.perform(get("/api/conversations/all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User không được xác thực"));
    }

    @Test
    @DisplayName("Lấy tất cả conversations - user không tồn tại")
    void getAllUserConversations_UserNotFound_ShouldReturnBadRequest() throws Exception {
        // Given
        when(conversationService.getAllUserConversations())
                .thenThrow(new IllegalArgumentException("Người dùng không tồn tại"));

        // When & Then
        mockMvc.perform(get("/api/conversations/all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Người dùng không tồn tại"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Người dùng không tồn tại"));
    }

    // Test createOrGetDirectConversation
    @Test
    @DisplayName("Tạo hoặc lấy direct conversation thành công")
    void createOrGetDirectConversation_ValidRequest_ShouldReturnConversation() {
        // Given
        setupValidAuthentication();
        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenReturn(conversationResponseDto);

        // When
        ResponseEntity<?> response = conversationController.createOrGetDirectConversation(otherUserId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversationResponseDto, response.getBody());
        verify(conversationService).getOrCreateDirectConversation(otherUserId);
    }

    @Test
    @DisplayName("Tạo direct conversation - không có authentication")
    void createOrGetDirectConversation_NoAuthentication_ShouldReturnBadRequest() throws Exception {
        // Given
        setupNoAuthentication();
        when(conversationService.getOrCreateDirectConversation(otherUserId))
                .thenThrow(new IllegalArgumentException("User không được xác thực"));

        // When
//        ResponseEntity<ConversationResponseDto> response = conversationController.createOrGetDirectConversation(otherUserId);
//
//        // Then
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());

        // When & Then
        mockMvc.perform(post("/api/conversations/direct/{otherUserId}", otherUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User không được xác thực"));
    }

    // Test getMessages
    @Test
    @DisplayName("Lấy messages thành công")
    void getMessages_ValidRequest_ShouldReturnMessages() {
        // Given
        setupValidAuthentication();
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> messages = new PageImpl<>(List.of(messageResponseDto));
        when(messageService.getMessages(conversationId, pageable)).thenReturn(messages);

        // When
        ResponseEntity<Page<MessageResponseDto>> response = conversationController.getMessages(conversationId, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messages, response.getBody());
        verify(messageService).getMessages(conversationId, pageable);
    }

    @Test
    @DisplayName("Lấy messages - không có messages")
    void getMessages_NoMessages_ShouldReturnEmptyPage() {
        // Given
        setupValidAuthentication();
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageService.getMessages(conversationId, pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<MessageResponseDto>> response = conversationController.getMessages(conversationId, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        verify(messageService).getMessages(conversationId, pageable);
    }

    @Test
    @DisplayName("Lấy messages với phân trang lớn")
    void getMessages_LargePagination_ShouldReturnPagedMessages() {
        // Given
        setupValidAuthentication();
        Pageable pageable = PageRequest.of(0, 50);
        Page<MessageResponseDto> messages = new PageImpl<>(List.of(messageResponseDto));
        when(messageService.getMessages(conversationId, pageable)).thenReturn(messages);

        // When
        ResponseEntity<Page<MessageResponseDto>> response = conversationController.getMessages(conversationId, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(messageService).getMessages(conversationId, pageable);
    }

    // Test searchMessages
    @Test
    @DisplayName("Tìm kiếm messages thành công")
    void searchMessages_ValidRequest_ShouldReturnMessages() {
        // Given
        setupValidAuthentication();
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> messages = new PageImpl<>(List.of(messageResponseDto));
        when(messageService.searchMessages(conversationId, keyword, pageable)).thenReturn(messages);

        // When
        ResponseEntity<Page<MessageResponseDto>> response = conversationController
                .searchMessages(conversationId, keyword, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(messages, response.getBody());
        verify(messageService).searchMessages(conversationId, keyword, pageable);
    }

    @Test
    @DisplayName("Tìm kiếm messages - không tìm thấy")
    void searchMessages_NotFound_ShouldReturnEmptyPage() {
        // Given
        setupValidAuthentication();
        String keyword = "nonexistent";
        Pageable pageable = PageRequest.of(0, 20);
        Page<MessageResponseDto> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageService.searchMessages(conversationId, keyword, pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<MessageResponseDto>> response = conversationController
                .searchMessages(conversationId, keyword, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        verify(messageService).searchMessages(conversationId, keyword, pageable);
    }

    @Test
    @DisplayName("Tìm kiếm tin nhắn trong cuộc trò chuyện")
    void searchMessages() throws Exception {
        UUID conversationId = UUID.randomUUID();
        String keyword = "test";

        // Sử dụng ArrayList thay vì List.of()
        List<MessageResponseDto> messageList = new ArrayList<>();
        Page<MessageResponseDto> mockPage = new PageImpl<>(messageList, PageRequest.of(0, 20), 0);

        when(messageService.searchMessages(eq(conversationId), eq(keyword), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/conversations/{conversationId}/search", conversationId)
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("Tìm kiếm tin nhắn thành công với kết quả")
    void searchMessagesWithResults() throws Exception {
        UUID conversationId = UUID.randomUUID();
        String keyword = "test";

        // Tạo mock message response
        MessageResponseDto messageDto = MessageResponseDto.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .content("This is a test message")
                .messageType(MessageType.TEXT)
                .isEdited(false)
                .isDeleted(false)
                .isRead(false)
                .build();

        List<MessageResponseDto> messageList = new ArrayList<>();
        messageList.add(messageDto);

        Page<MessageResponseDto> mockPage = new PageImpl<>(messageList, PageRequest.of(0, 20), 1);

        when(messageService.searchMessages(eq(conversationId), eq(keyword), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/conversations/{conversationId}/search", conversationId)
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].content").value("This is a test message"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}