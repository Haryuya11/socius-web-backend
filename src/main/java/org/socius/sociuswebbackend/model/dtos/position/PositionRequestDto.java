package org.socius.sociuswebbackend.model.dtos.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequestDto {
    private String name;
    
    private String description;
}
