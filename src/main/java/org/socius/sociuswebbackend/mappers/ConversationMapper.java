package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
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
public interface ConversationMapper extends BaseEntityMapper, GenericMapper<ConversationEntity, ConversationResponseDto, ConversationRequestDto> {

    @Override
    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponseDto entityToDto(ConversationEntity entity);

    @AfterMapping
    default void mapLastMessageAndUnreadCount(@MappingTarget ConversationResponseDto dto, ConversationEntity entity) {
        // Tìm kiếm tin nhắn cuối cùng
        Optional<MessageEntity> lastMessage = entity.getMessages().stream()
                .filter(msg -> !msg.isDeleted())
                .max((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));

        if (lastMessage.isPresent()) {
            MessageMapper messageMapper = ApplicationContextHelper.getBean(MessageMapper.class);
            dto.setLastMessage(messageMapper.entityToDto(lastMessage.get()));
        }

        // Set lại số lượng tin nhắn chưa đọc
        dto.setUnreadCount(0);
    }

    @Override
    default ConversationEntity requestDtoToEntity(ConversationRequestDto dto) {
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
    default void updateEntityFromDto(ConversationRequestDto dto, @MappingTarget ConversationEntity entity) {
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