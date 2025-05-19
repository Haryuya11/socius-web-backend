package org.socius.sociuswebbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.socius.sociuswebbackend.services.MessageService;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;
    private UUID conversationId;
    private MessageRequestDto messageRequestDto;
    private MessageResponseDto messageResponseDto;

    @BeforeEach
    void setUp() {
        // Thiết lập PageableHandlerMethodArgumentResolver để xử lý tham số Pageable
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        // Thiết lập MockMvc với resolver
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setCustomArgumentResolvers(resolver)
                .build();

        objectMapper = new ObjectMapper();

        // Thiết lập dữ liệu test
        userId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        messageRequestDto = ChatTestDataUtil.createMessageRequestDto();
        messageRequestDto.setConversationId(conversationId);

        messageResponseDto = ChatTestDataUtil.createMessageResponseDto();

        // Thiết lập RequestContextHolder để có thể lấy userId từ session
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("USER_ID", userId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Gửi tin nhắn văn bản thành công")
    void sendTextMessageSuccessfully() throws Exception {
        // Thiết lập mock
        when(messageService.sendMessage(eq(userId), any(MessageRequestDto.class)))
                .thenReturn(messageResponseDto);

        // Thiết lập SecurityContext với Authentication giả lập
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Thực thi và kiểm tra
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Gửi tin nhắn với file đính kèm")
    void sendMessageWithFile() throws Exception {
        // Thiết lập mock file và response
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );

        when(fileStorageService.storeFile(any(), anyString())).thenReturn("/uploads/files/test.txt");
        when(messageService.sendMessage(eq(userId), any(MessageRequestDto.class)))
                .thenReturn(messageResponseDto);

        // Thiết lập SecurityContext
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Sửa đường dẫn và tham số cho đúng
        mockMvc.perform(multipart("/api/messages/file")
                        .file(file)
                        .param("conversationId", conversationId.toString())
                        .param("content", "Test message")
                        .param("type", "TEXT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Lấy tin nhắn theo cuộc trò chuyện")
    void getMessagesByConversation() throws Exception {
        // Thiết lập mock
        Page<MessageResponseDto> messagePage = new PageImpl<>(
                Collections.singletonList(messageResponseDto),
                PageRequest.of(0, 20),
                1
        );

        when(messageService.getMessages(eq(userId), eq(conversationId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Thiết lập SecurityContext
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/messages/{conversationId}", conversationId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.totalElements").exists());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Đánh dấu tin nhắn đã đọc")
    void markMessagesAsRead() throws Exception {
        // Tạo ReadReceiptDto
        ReadReceiptDto readReceiptDto = ReadReceiptDto.builder()
                .conversationId(conversationId)
                .lastReadMessageId(UUID.randomUUID())
                .build();

        // Thiết lập SecurityContext với Authentication giả lập
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Thực thi và kiểm tra
        mockMvc.perform(post("/api/messages/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readReceiptDto)))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();

    }

    @Test
    @DisplayName("Đồng bộ tin nhắn")
    void syncMessages() throws Exception {
        // Tạo SyncMessagesRequestDto
        Map<UUID, UUID> lastMessageIds = new HashMap<>();
        lastMessageIds.put(conversationId, UUID.randomUUID());
        SyncMessagesRequestDto syncRequest = SyncMessagesRequestDto.builder()
                .lastMessageIds(lastMessageIds)
                .build();

        // Thiết lập SecurityContext với Authentication giả lập
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Thiết lập mock
        Map<UUID, List<MessageResponseDto>> syncResponse = new HashMap<>();
        syncResponse.put(conversationId, Collections.singletonList(messageResponseDto));
        when(messageService.syncMessages(eq(userId), any(SyncMessagesRequestDto.class)))
                .thenReturn(syncResponse);

        // Thực thi và kiểm tra
        mockMvc.perform(post("/api/messages/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + conversationId).exists());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tìm kiếm tin nhắn")
    void searchMessages() throws Exception {
        // Tạo dữ liệu phân trang để mock
        Page<MessageResponseDto> messagePage = new PageImpl<>(
                Collections.singletonList(messageResponseDto),
                PageRequest.of(0, 20),
                1
        );

        String keyword = "test";

        // Thiết lập mock với đúng signature
        when(messageService.searchMessages(
                eq(userId),
                eq(conversationId),
                eq(keyword),
                any(Pageable.class)
        )).thenReturn(messagePage);

        // Thiết lập SecurityContext
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Sửa đường dẫn đúng
        mockMvc.perform(get("/api/messages/conversations/{conversationId}/search", conversationId)
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.totalElements").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Xóa tin nhắn")
    void deleteMessage() throws Exception {
        // Thiết lập messageId
        UUID messageId = UUID.randomUUID();

        // Thiết lập SecurityContext với Authentication giả lập
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Thực thi và kiểm tra
        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();

    }
}