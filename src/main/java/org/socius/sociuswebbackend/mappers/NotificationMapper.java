package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientId;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Mapper for Notification entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, NotificationRecipientMapper.class})
public interface NotificationMapper extends BaseEntityMapper, 
        GenericMapper<NotificationEntity, NotificationResponseDto, NotificationRequestDto> {
    
    @Override
    @Mapping(target = "recipients", ignore = true)
    NotificationResponseDto entityToDto(NotificationEntity entity);
    
    /**
     * Process recipients after mapping the main entity
     */
    @AfterMapping
    default void mapRecipients(@MappingTarget NotificationResponseDto dto, NotificationEntity entity) {
        if (entity.getRecipients() != null && !entity.getRecipients().isEmpty()) {
            NotificationRecipientMapper mapper = ApplicationContextHelper.getBean(NotificationRecipientMapper.class);
            dto.setRecipients(entity.getRecipients().stream()
                .map(mapper::entityToDto)
                .collect(Collectors.toList()));
        } else {
            dto.setRecipients(new ArrayList<>());
        }
    }
    
    @Override
    default NotificationEntity requestDtoToEntity(NotificationRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity sender = mappingUtil.mapUserIdToEntity(dto.getSenderId());
        
        NotificationEntity notificationEntity = NotificationEntity.builder()
            .title(dto.getTitle())
            .sender(sender)
            .message(dto.getMessage())
            .expiryDate(dto.getExpiryDate())
            .type(dto.getType())
            .isUrgent(dto.getIsUrgent())
            .recipients(new HashSet<>())
            .build();
            
        return notificationEntity;
    }
    
    @Override
    default void updateEntityFromDto(NotificationRequestDto dto, @MappingTarget NotificationEntity entity) {
        if (dto == null) {
            return;
        }
        
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        
        if (dto.getSenderId() != null) {
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            entity.setSender(mappingUtil.mapUserIdToEntity(dto.getSenderId()));
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
    default void updateRecipients(NotificationRequestDto dto, @MappingTarget NotificationEntity entity) {
        if (dto.getRecipientIds() != null) {
            if (entity.getRecipients() == null) {
                entity.setRecipients(new HashSet<>());
            } else {
                entity.getRecipients().clear();
            }
            
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            
            dto.getRecipientIds().stream()
                .filter(userId -> userId != null)
                .forEach(userId -> {
                    UserEntity user = mappingUtil.mapUserIdToEntity(userId);
                    
                    NotificationRecipientId id = new NotificationRecipientId(entity.getId(), userId);
                    NotificationRecipientEntity recipient = NotificationRecipientEntity.builder()
                        .id(id)
                        .notification(entity)
                        .user(user)
                        .isRead(false)
                        .build();
                        
                    entity.getRecipients().add(recipient);
                });
        }
    }
}
