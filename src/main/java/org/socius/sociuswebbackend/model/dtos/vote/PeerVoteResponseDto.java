package org.socius.sociuswebbackend.model.dtos.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.period.PeriodResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.VoteType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PeerVoteResponseDto extends BaseDto {
    private UserResponseDto voter;
    private UserResponseDto votedEmployee;
    private PeriodResponseDto period;
    private String reason;
    private VoteType voteType;
}
