package org.socius.sociuswebbackend.model.dtos.team;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequestDto {
    @NotBlank(message = "Team name must not be empty")
    private String name;
    
    private UUID leaderId;
}
