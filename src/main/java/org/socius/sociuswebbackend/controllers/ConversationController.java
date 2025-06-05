package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.MessageService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<?> getConversationMembers(
            @PathVariable UUID conversationId,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session không hợp lệ hoặc đã hết hạn"));
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) session.getAttribute(userKey);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID không hợp lệ trong session"));
        }

        try {
            List<ConversationMemberDto> members = conversationService.getConversationMembers(conversationId, userId);
            return ResponseEntity.ok(members);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy cuộc trò chuyện của user với phân trang (existing method với cải tiến)
     */
    @GetMapping()
    public ResponseEntity<?> getUserConversations(
            Pageable pageable,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session không hợp lệ hoặc đã hết hạn"));
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) session.getAttribute(userKey);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID không hợp lệ trong session"));
        }

        try {
            Page<ConversationResponseDto> conversations = conversationService.getUserConversations(userId, pageable);
            return ResponseEntity.ok(conversations);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả cuộc trò chuyện của user hiện tại
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUserConversations(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session không hợp lệ hoặc đã hết hạn"));
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) session.getAttribute(userKey);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID không hợp lệ trong session"));
        }

        try {
            List<ConversationResponseDto> conversations = conversationService.getAllUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo hoặc lấy cuộc trò chuyện với người dùng khác
     */
    @PostMapping("/direct/{otherUserId}")
    public ResponseEntity<?> createOrGetDirectConversation(
            @PathVariable UUID otherUserId,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session không hợp lệ hoặc đã hết hạn"));
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) session.getAttribute(userKey);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID không hợp lệ trong session"));
        }
        try {
            ConversationResponseDto conversation = conversationService.getOrCreateDirectConversation(userId, otherUserId);
            return ResponseEntity.ok(conversation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tin nhắn trong cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param pageable       Thông tin phân trang
     * @return ResponseEntity chứa danh sách tin nhắn
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<Page<MessageResponseDto>> getMessages(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        UUID userId = user.getId();

        Page<MessageResponseDto> messages = messageService.getMessages(userId, conversationId, pageable);
        return ResponseEntity.ok(messages);
    }
}