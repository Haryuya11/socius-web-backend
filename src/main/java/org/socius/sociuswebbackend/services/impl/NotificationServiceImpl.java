package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.NotificationMapper;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.socius.sociuswebbackend.repositories.NotificationRepository;
import org.socius.sociuswebbackend.services.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * Triển khai dịch vụ thông báo, xử lý tạo, lấy danh sách, và đánh dấu thông báo đã đọc.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Tạo thông báo mới từ DTO, lưu vào cơ sở dữ liệu và thêm recipient.
     *
     * @param requestDto DTO chứa nội dung thông báo và danh sách recipient IDs.
     * @return NotificationResponseDto chứa thông tin thông báo đã tạo.
     * @throws IllegalStateException nếu không thể map DTO sang entity.
     */
    @Override
    @Transactional
    public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
        // Chuyển đổi DTO thành Entity
        NotificationEntity entity = notificationMapper.requestDtoToEntity(requestDto);
        if (entity == null) {
            throw new IllegalStateException("Failed to map requestDto to entity: entityMappingUtil or input may be invalid");
        }

        // Lưu entity để sinh ID
        NotificationEntity savedEntity = notificationRepository.save(entity);

        // Thêm recipient vào entity đã lưu
        notificationMapper.addRecipientsToEntity(requestDto, savedEntity);

        // Lưu lại entity với recipient
        savedEntity = notificationRepository.save(savedEntity);

        // Chuyển đổi entity sang DTO để trả về
        return notificationMapper.entityToDto(savedEntity);
    }

    /**
     * Lấy danh sách thông báo phân trang theo userId.
     *
     * @param userId ID của người dùng nhận thông báo.
     * @param pageable Thông tin phân trang (page, size).
     * @return Page<NotificationResponseDto> chứa danh sách thông báo phân trang.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByUserId(UUID userId, Pageable pageable) {
        // Lấy danh sách thông báo từ repository và chuyển đổi sang DTO
        Page<NotificationEntity> notificationPage = notificationRepository.findByRecipientUserId(userId, pageable);
        return notificationPage.map(notificationMapper::entityToDto);
    }

    /**
     * Đánh dấu thông báo là đã đọc cho người dùng cụ thể.
     *
     * @param notificationId ID của thông báo.
     * @param userId ID của người dùng.
     */
    @Override
    @Transactional
    public void markNotificationAsRead(UUID notificationId, UUID userId) {
        // Gọi repository để cập nhật trạng thái is_read và read_at
        notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
    }
}