package org.socius.sociuswebbackend.model.dtos.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TeamRequestDto {
    @NotBlank(message = "Team name must not be empty")
    private String name;
    
    private UUID leaderId;
}
