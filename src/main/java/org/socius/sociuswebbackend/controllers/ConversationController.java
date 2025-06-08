package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<List<ConversationMemberDto>> getConversationMembers(@PathVariable UUID conversationId) {
        List<ConversationMemberDto> members = conversationService.getConversationMembers(conversationId);
        return ResponseEntity.ok(members);
    }

    /**
     * Lấy cuộc trò chuyện của user với phân trang (existing method với cải tiến)
     */
    @GetMapping()
    public ResponseEntity<Page<ConversationResponseDto>> getUserConversations(Pageable pageable) {
        Page<ConversationResponseDto> conversations = conversationService.getUserConversations(pageable);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Lấy tất cả cuộc trò chuyện của user hiện tại
     */
    @GetMapping("/all")
    public ResponseEntity<List<ConversationResponseDto>> getAllUserConversations() {
        List<ConversationResponseDto> conversations = conversationService.getAllUserConversations();
        return ResponseEntity.ok(conversations);
    }

    /**
     * Tạo hoặc lấy cuộc trò chuyện với người dùng khác
     */
    @PostMapping("/direct/{otherUserId}")
    public ResponseEntity<ConversationResponseDto> createOrGetDirectConversation(@PathVariable UUID otherUserId) {
        ConversationResponseDto conversation = conversationService.getOrCreateDirectConversation(otherUserId);
        return ResponseEntity.ok(conversation);
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
        Page<MessageResponseDto> messages = messageService.getMessages(conversationId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Tìm kiếm tin nhắn trong cuộc trò chuyện theo từ khóa
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param keyword        Từ khóa tìm kiếm
     * @param pageable       Thông tin phân trang
     * @return ResponseEntity chứa danh sách tin nhắn tìm thấy
     */
    @GetMapping("/{conversationId}/search")
    public ResponseEntity<Page<MessageResponseDto>> searchMessages(
            @PathVariable UUID conversationId,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MessageResponseDto> messages = messageService.searchMessages(conversationId, keyword, pageable);
        return ResponseEntity.ok(messages);
    }
}