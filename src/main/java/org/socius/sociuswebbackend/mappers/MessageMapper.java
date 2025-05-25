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
public abstract class MessageMapper extends BaseEntityMapper implements GenericMapper<MessageEntity, MessageResponseDto, MessageRequestDto> {
    @Override
    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "fileOriginalName", target = "fileOriginalName")
    @Mapping(source = "fileContentType", target = "fileContentType")
    @Mapping(source = "fileSize", target = "fileSize")
    public abstract MessageResponseDto entityToDto(MessageEntity entity);


    @Override
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "statusList", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "fileOriginalName", target = "fileOriginalName")
    @Mapping(source = "fileContentType", target = "fileContentType")
    @Mapping(source = "fileSize", target = "fileSize")
    public MessageEntity requestDtoToEntity(MessageRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = new EntityMappingUtil();
        ConversationEntity conversationEntity = mappingUtil.mapConversationIdToEntity(dto.getConversationId());

        return MessageEntity.builder()
                .conversation(conversationEntity)
                .content(dto.getContent())
                .messageType(dto.getMessageType())
                .fileOriginalName(dto.getFileOriginalName())
                .fileContentType(dto.getFileContentType())
                .fileSize(dto.getFileSize())
                .fileUrl(dto.getFileUrl())
                .isEdited(false)
                .isDeleted(false)
                .build();
    }

    @Override
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "statusList", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public void updateEntityFromDto(MessageRequestDto dto, @MappingTarget MessageEntity entity) {
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
