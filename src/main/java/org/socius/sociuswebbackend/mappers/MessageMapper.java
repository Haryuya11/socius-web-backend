package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MessageMapper extends BaseEntityMapper, GenericMapper<MessageEntity, MessageResponseDto, MessageRequestDto> {

    @Override
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(target = "isRead", ignore = true)
    MessageResponseDto entityToDto(MessageEntity entity);


    @Override
    default MessageEntity requestDtoToEntity(MessageRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = new EntityMappingUtil();
        ConversationEntity conversationEntity = mappingUtil.mapConversationIdToEntity(dto.getConversationId());

        return MessageEntity.builder()
                .conversation(conversationEntity)
                .content(dto.getContent())
                .messageType(dto.getMessageType())
                .isEdited(false)
                .isDeleted(false)
                .build();
    }

    @Override
    default void updateEntityFromDto(MessageRequestDto dto, @MappingTarget MessageEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
            entity.setEdited(true);
        }

        if (dto.getMessageType() != null) {
            entity.setMessageType(dto.getMessageType());
        }
    }



}
