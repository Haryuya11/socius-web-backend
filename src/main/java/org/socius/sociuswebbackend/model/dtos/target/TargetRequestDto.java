package org.socius.sociuswebbackend.model.dtos.target;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.TargetStatus;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetRequestDto {
    @NotBlank(message = "Target name must not be empty")
    private String name;
    
    private String description;
    
    @NotNull(message = "Deadline must not be null")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;
    
    @NotNull(message = "Status must not be null")
    private TargetStatus status;
    
    private UUID assignedToId;
}
