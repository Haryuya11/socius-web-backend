package org.socius.sociuswebbackend.model.dtos.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TeamResponseDto extends BaseDto {
    private String name;
    private UserResponseDto leader;
}
