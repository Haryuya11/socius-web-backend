package org.socius.sociuswebbackend.model.dtos.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TeamWithMembersDto extends BaseDto {
    private String name;
    private UserResponseDto leader;
    
    @Builder.Default
    private List<UserResponseDto> members = new ArrayList<>();
    
    private int memberCount;
}
