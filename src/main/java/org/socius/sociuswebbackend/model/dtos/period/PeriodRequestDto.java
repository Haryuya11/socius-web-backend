package org.socius.sociuswebbackend.model.dtos.period;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.PeriodStatus;
import org.socius.sociuswebbackend.model.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PeriodRequestDto {
    @NotBlank(message = "Period name must not be empty")
    private String name;
    
    @NotNull(message = "Period type must not be null")
    private PeriodType type;
    
    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;
    
    @NotNull(message = "End date must not be null")
    private LocalDate endDate;
    
    @NotNull(message = "Status must not be null")
    private PeriodStatus status;
    
    private String description;
}
