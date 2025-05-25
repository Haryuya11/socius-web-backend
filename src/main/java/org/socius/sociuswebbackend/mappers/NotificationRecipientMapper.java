package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientRequestDto;
import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientId;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.NotificationRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Mapper for NotificationRecipient entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class NotificationRecipientMapper extends BaseEntityMapper implements
        GenericMapper<NotificationRecipientEntity, NotificationRecipientDto, NotificationRecipientRequestDto> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Convert NotificationRecipientEntity to NotificationRecipientDto
     */
    @Override
    @Mapping(source = "id.notificationId", target = "notificationId")
    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "isRead", target = "isRead")
    @Mapping(source = "readAt", target = "readAt")
    public abstract NotificationRecipientDto entityToDto(NotificationRecipientEntity entity);

    /**
     * Convert list of NotificationRecipientEntity to list of NotificationRecipientDto
     */
    @Override
    public abstract List<NotificationRecipientDto> entitiesToDtos(List<NotificationRecipientEntity> entities);

    /**
     * Convert NotificationRecipientRequestDto to NotificationRecipientEntity
     */
    @Override
    public NotificationRecipientEntity requestDtoToEntity(NotificationRecipientRequestDto dto) {
        if (dto == null) {
            return null;
        }

        NotificationRecipientId id = new NotificationRecipientId(dto.getNotificationId(), dto.getUserId());

        NotificationEntity notification = notificationRepository.findById(dto.getNotificationId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + dto.getNotificationId()));

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + dto.getUserId()));

        return NotificationRecipientEntity.builder()
                .id(id)
                .notification(notification)
                .user(user)
                .isRead(dto.getIsRead())
                .build();
    }
}
