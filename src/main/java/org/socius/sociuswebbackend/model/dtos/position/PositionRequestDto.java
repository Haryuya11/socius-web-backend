package org.socius.sociuswebbackend.model.dtos.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequestDto {
    @NotBlank(message = "Position name must not be empty")
    private String name;
    
    private String description;
}
