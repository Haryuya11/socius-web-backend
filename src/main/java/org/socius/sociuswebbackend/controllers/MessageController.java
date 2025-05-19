package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.ReadReceiptDto;
import org.socius.sociuswebbackend.model.dtos.message.SyncMessagesRequestDto;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.socius.sociuswebbackend.services.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    final private MessageService messageService;
    final private FileStorageService fileStorageService;

    /**
     * Gửi tin nhắn
     *
     * @param requestDto DTO chứa thông tin tin nhắn
     * @return ResponseEntity chứa thông tin phản hồi
     */
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody MessageRequestDto requestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID senderId = UUID.fromString(auth.getName());

        MessageResponseDto responseDto = messageService.sendMessage(senderId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Gửi tin nhắn với tệp tin đính kèm
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param content        Nội dung tin nhắn
     * @param type           Loại tin nhắn
     * @param file           Tệp tin đính kèm
     * @return ResponseEntity chứa thông tin phản hồi
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponseDto> sendMessageWithFile(
            @RequestParam("conversationId") UUID conversationId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "type") MessageType type,
            @RequestParam("file") MultipartFile file) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID senderId = UUID.fromString(auth.getName());

            // Lưu tệp tin
            String directory = "messages/" + conversationId;
            String mediaUrl = fileStorageService.storeFile(file, directory);

            // Tạo DTO tin nhắn
            MessageRequestDto requestDto = MessageRequestDto.builder()
                    .conversationId(conversationId)
                    .content(content == null ? "" : content)
                    .messageType(type)
                    .mediaUrl(mediaUrl)
                    .build();

            // Gửi tin nhắn
            MessageResponseDto responseDto = messageService.sendMessage(senderId, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn với tệp tin: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
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
        UUID userId = UUID.fromString(auth.getName());

        Page<MessageResponseDto> messages = messageService.getMessages(userId, conversationId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Dánh dấu tin nhắn đã đọc
     *
     * @param readReceiptDto Thông tin về tin nhắn cuối cùng đã đọc
     * @return ResponseEntity chứa số lượng tin nhắn đã đánh dấu
     */
    @PostMapping("/read")
    public ResponseEntity<Integer> markAsRead(@Valid @RequestBody ReadReceiptDto readReceiptDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        int count = messageService.markAsRead(userId, readReceiptDto);
        return ResponseEntity.ok(count);
    }

    /**
     * Đồng bộ tin nhắn mới sau khi mất kết nối
     *
     * @param syncRequest Thông tin đồng bộ
     * @return ResponseEntity chứa danh sách tin nhắn đã đồng bộ
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<UUID, List<MessageResponseDto>>> syncMessages(
            @Valid @RequestBody SyncMessagesRequestDto syncRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        Map<UUID, List<MessageResponseDto>> syncedMessages = messageService.syncMessages(userId, syncRequest);
        return ResponseEntity.ok(syncedMessages);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Boolean>> deleteMessage(@PathVariable UUID messageId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        boolean result = messageService.deleteMessage(userId, messageId);
        Map<String, Boolean> response = Collections.singletonMap("success", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/search")
    public ResponseEntity<Page<MessageResponseDto>> searchMessages(
            @PathVariable UUID conversationId,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        Page<MessageResponseDto> messages = messageService.searchMessages(userId, conversationId, keyword, pageable);
        return ResponseEntity.ok(messages);
    }
}
