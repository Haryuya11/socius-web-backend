package org.socius.sociuswebbackend.model.dtos.employment;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EmploymentHistoryRequestDto extends BaseEmploymentDto {
    @NotNull(message = "End date must not be null")
    private LocalDate endDate;
    
    private String description;
}
