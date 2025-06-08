package org.socius.sociuswebbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientId;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper for Notification entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, NotificationRecipientMapper.class})
@RequiredArgsConstructor
public abstract class NotificationMapper extends BaseEntityMapper implements
        GenericMapper<NotificationEntity, NotificationResponseDto, NotificationRequestDto> {

    protected NotificationRecipientMapper recipientMapper;
    protected EntityMappingUtil entityMappingUtil;


    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract NotificationResponseDto entityToDto(NotificationEntity entity);

    /**
     * Process recipients after mapping the main entity
     */
    @AfterMapping
    public void mapRecipients(@MappingTarget NotificationResponseDto dto, NotificationEntity entity) {
        // Khởi tạo recipients để đảm bảo tải từ database
        if (entity != null && entity.getRecipients() != null) {
            Hibernate.initialize(entity.getRecipients());
        }

        if (entity != null && entity.getRecipients() != null && !entity.getRecipients().isEmpty()) {
            dto.setRecipients(entity.getRecipients().stream()
                    .map(recipientMapper::entityToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setRecipients(new ArrayList<>());
        }
    }

    @Override
    public NotificationEntity requestDtoToEntity(NotificationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity sender = entityMappingUtil.mapUserIdToEntity(dto.getSenderId());

        return NotificationEntity.builder()
                .title(dto.getTitle())
                .sender(sender)
                .message(dto.getMessage())
                .expiryDate(dto.getExpiryDate())
                .type(dto.getType())
                .isUrgent(dto.getIsUrgent())
                .recipients(new HashSet<>())
                .build();
    }

    @Override
    public void updateEntityFromDto(NotificationRequestDto dto, @MappingTarget NotificationEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getSenderId() != null) {
            entity.setSender(entityMappingUtil.mapUserIdToEntity(dto.getSenderId()));
        }

        if (dto.getMessage() != null) {
            entity.setMessage(dto.getMessage());
        }

        if (dto.getExpiryDate() != null) {
            entity.setExpiryDate(dto.getExpiryDate());
        }

        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }

        if (dto.getIsUrgent() != null) {
            entity.setIsUrgent(dto.getIsUrgent());
        }
    }

    /**
     * Post-processing after entity creation to handle recipients
     */
    @AfterMapping
    public void updateRecipients(NotificationRequestDto dto, @MappingTarget NotificationEntity entity) {
        if (dto.getRecipientIds() != null) {
            if (entity.getRecipients() == null) {
                entity.setRecipients(new HashSet<>());
            } else {
                entity.getRecipients().clear();
            }
        }
    }

    /**
     * Add recipients to an already saved NotificationEntity
     */
    public void addRecipientsToEntity(NotificationRequestDto dto, NotificationEntity entity) {
        if (dto.getRecipientIds() != null && entity.getId() != null) {
            entity.getRecipients().clear(); // Xóa recipients cũ nếu có

            dto.getRecipientIds().stream()
                    .filter(Objects::nonNull)
                    .forEach(userId -> {
                        try {
                            UserEntity user = entityMappingUtil.mapUserIdToEntity(userId);
                            NotificationRecipientId id = new NotificationRecipientId(entity.getId(), userId);
                            NotificationRecipientEntity recipient = NotificationRecipientEntity.builder()
                                    .id(id)
                                    .notification(entity)
                                    .user(user)
                                    .isRead(false)
                                    .build();

                            entity.getRecipients().add(recipient);

                        } catch (IllegalArgumentException e) {
                            System.err.println("Failed to add recipient with user ID: " + userId + ". Error: " + e.getMessage());
                        }
                    });
        }
    }
}