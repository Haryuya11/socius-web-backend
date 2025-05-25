package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.model.entities.ConversationMemberId;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ConversationMemberMapper extends BaseEntityMapper {

    @Mapping(source = "id.conversationId", target = "conversationId")
    @Mapping(source = "user", target = "user")
    ConversationMemberDto entityToDto(ConversationMemberEntity entity);

    default ConversationMemberEntity dtoToEntity(ConversationMemberDto dto) {
        if (dto == null) {
            return null;
        }

        return ConversationMemberEntity.builder()
                .id(new ConversationMemberId(dto.getConversationId(), dto.getUser().getId()))
                .joinedAt(dto.getJoinedAt())
                .role(dto.getRole())
                .build();
    }
}
