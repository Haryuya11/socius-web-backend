package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.vote.PeerVoteRequestDto;
import org.socius.sociuswebbackend.model.dtos.vote.PeerVoteResponseDto;
import org.socius.sociuswebbackend.model.entities.PeerVoteEntity;
import org.socius.sociuswebbackend.model.entities.PeriodEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for PeerVote entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, PeriodMapper.class})
public abstract class PeerVoteMapper extends BaseEntityMapper implements
        GenericMapper<PeerVoteEntity, PeerVoteResponseDto, PeerVoteRequestDto> {
    
    @Override
    public abstract PeerVoteResponseDto entityToDto(PeerVoteEntity entity);
    
    @Override
    public PeerVoteEntity requestDtoToEntity(PeerVoteRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity voter = mappingUtil.mapUserIdToEntity(dto.getVoterId());
        UserEntity votedEmployee = mappingUtil.mapUserIdToEntity(dto.getVotedEmployeeId());
        PeriodEntity period = mappingUtil.mapPeriodIdToEntity(dto.getPeriodId());
        
        return PeerVoteEntity.builder()
            .voter(voter)
            .votedEmployee(votedEmployee)
            .period(period)
            .reason(dto.getReason())
            .voteType(dto.getVoteType())
            .build();
    }
    
    @Override
    public void updateEntityFromDto(PeerVoteRequestDto dto, @MappingTarget PeerVoteEntity entity) {
        if (dto == null) {
            return;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        if (dto.getVoterId() != null) {
            entity.setVoter(mappingUtil.mapUserIdToEntity(dto.getVoterId()));
        }
        
        if (dto.getVotedEmployeeId() != null) {
            entity.setVotedEmployee(mappingUtil.mapUserIdToEntity(dto.getVotedEmployeeId()));
        }
        
        if (dto.getPeriodId() != null) {
            entity.setPeriod(mappingUtil.mapPeriodIdToEntity(dto.getPeriodId()));
        }
        
        if (dto.getReason() != null) {
            entity.setReason(dto.getReason());
        }
        
        if (dto.getVoteType() != null) {
            entity.setVoteType(dto.getVoteType());
        }
    }
}
