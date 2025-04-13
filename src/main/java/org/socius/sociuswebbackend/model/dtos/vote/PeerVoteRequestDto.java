package org.socius.sociuswebbackend.model.dtos.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.VoteType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PeerVoteRequestDto {
    @NotNull(message = "Voter ID must not be null")
    private UUID voterId;
    
    @NotNull(message = "Voted employee ID must not be null")
    private UUID votedEmployeeId;
    
    @NotNull(message = "Period ID must not be null")
    private UUID periodId;
    
    private String reason;
    
    @NotNull(message = "Vote type must not be null")
    private VoteType voteType;
}
