package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientRequestDto;
import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientId;
import org.socius.sociuswebbackend.model.entities.UserEntity;

import java.util.List;

/**
 * Mapper for NotificationRecipient entities and DTOs
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class, NotificationMapper.class })
public interface NotificationRecipientMapper extends BaseEntityMapper {

    /**
     * Convert NotificationRecipientEntity to NotificationRecipientDto
     */
    @Mapping(source = "id.notificationId", target = "notificationId")
    @Mapping(source = "id.userId", target = "userId")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "isRead", target = "isRead")
    @Mapping(source = "readAt", target = "readAt")
    NotificationRecipientDto entityToDto(NotificationRecipientEntity entity);

    /**
     * Convert list of NotificationRecipientEntity to list of
     * NotificationRecipientDto
     */
    List<NotificationRecipientDto> entitiesToDtos(List<NotificationRecipientEntity> entities);

    /**
     * Convert NotificationRecipientRequestDto to NotificationRecipientEntity
     */
    default NotificationRecipientEntity requestDtoToEntity(NotificationRecipientRequestDto dto) {
        if (dto == null) {
            return null;
        }

        NotificationRecipientId id = new NotificationRecipientId(dto.getNotificationId(), dto.getUserId());

        NotificationEntity notification = new NotificationEntity();
        notification.setId(dto.getNotificationId());

        UserEntity user = new UserEntity();
        user.setId(dto.getUserId());

        return NotificationRecipientEntity.builder()
                .id(id)
                .notification(notification)
                .user(user)
                .isRead(dto.getIsRead())
                .build();
    }
}
