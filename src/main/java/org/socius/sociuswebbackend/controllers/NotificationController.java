package org.socius.sociuswebbackend.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.services.NotificationService;
import org.socius.sociuswebbackend.config.RabbitMQConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller xử lý các yêu cầu liên quan đến thông báo.
 * Cung cấp các endpoint để tạo, lấy danh sách, và đánh dấu thông báo đã đọc.
 */
@RestController
@RequestMapping("api/notification")
@RequiredArgsConstructor
@Validated
public class NotificationController {
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Tạo một thông báo mới và gửi nó qua RabbitMQ để thông báo qua WebSocket.
     *
     * @param requestDto DTO chứa nội dung thông báo và danh sách recipient IDs.
     * @return ResponseEntity chứa NotificationResponseDto với thông tin thông báo đã tạo.
     */
    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationRequestDto requestDto) {
        NotificationResponseDto responseDto = notificationService.createNotification(requestDto);

        // Gửi thông báo đến RabbitMQ queue để xử lý qua WebSocket
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SESSION_MANAGEMENT_EXCHANGE,
                RabbitMQConfig.INVALIDATE_SESSION_ROUTING_KEY,
                responseDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Lấy danh sách thông báo phân trang theo userId.
     * Tự động đánh dấu các thông báo chưa đọc trong trang hiện tại là đã đọc.
     *
     * @param userId ID của người dùng nhận thông báo.
     * @param page Số trang (mặc định: 0).
     * @param size Số thông báo mỗi trang (mặc định: 10).
     * @return ResponseEntity chứa Page<NotificationResponseDto> với danh sách thông báo.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đánh dấu thông báo là đã đọc cho người dùng cụ thể.
     *
     * @param notificationId ID của thông báo.
     * @param userId ID của người dùng (query param).
     * @return ResponseEntity với status 200 nếu thành công, 400 nếu user không phải recipient, hoặc 404 nếu notification không tồn tại.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable UUID notificationId,
            @RequestParam UUID userId
    ) {
        try {
            notificationService.markNotificationAsRead(notificationId, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}