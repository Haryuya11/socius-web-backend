package org.socius.sociuswebbackend.mappers;

import org.hibernate.Hibernate;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UnreadCountEntity;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {UserMapper.class, MessageMapper.class, ConversationMemberMapper.class})
public abstract class ConversationMapper extends BaseEntityMapper implements GenericMapper<ConversationEntity, ConversationResponseDto, ConversationRequestDto> {

    private static final Logger logger = LoggerFactory.getLogger(ConversationMapper.class);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "members", ignore = true)
    public abstract ConversationResponseDto entityToDto(ConversationEntity entity);

    @AfterMapping
    public void mapLastMessageAndUnreadCount(@MappingTarget ConversationResponseDto dto, ConversationEntity entity) {
        try {
            // Chỉ xử lý lastMessage nếu messages đã được load
            if (Hibernate.isInitialized(entity.getMessages()) && !entity.getMessages().isEmpty()) {
                Optional<MessageEntity> lastMessage = entity.getMessages().stream()
                        .filter(msg -> !msg.isDeleted())
                        .max((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));

                if (lastMessage.isPresent()) {
                    MessageMapper messageMapper = ApplicationContextHelper.getBean(MessageMapper.class);
                    dto.setLastMessage(messageMapper.entityToDto(lastMessage.get()));
                }
            }

            // Set default unread count
            dto.setUnreadCount(0);
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không làm crash mapper
            logger.warn("Lỗi khi map lastMessage: {}", e.getMessage());
            dto.setUnreadCount(0);
        }
    }

    @Override
    public ConversationEntity requestDtoToEntity(ConversationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return ConversationEntity.builder()
                .name(dto.getName())
                .type(dto.getType())
                .build();

        // Members sẽ được xử lý riêng trong service
    }

    @Override
    public void updateEntityFromDto(ConversationRequestDto dto, @MappingTarget ConversationEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }

        // Members sẽ được xử lý riêng trong service
    }
}