package org.socius.sociuswebbackend.model.dtos.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PerformanceReviewRequestDto {
    @NotNull(message = "Employee ID must not be null")
    private UUID employeeId;
    
    @NotNull(message = "Reviewer ID must not be null")
    private UUID reviewerId;
    
    @NotNull(message = "Period ID must not be null")
    private UUID periodId;
    
    @NotNull(message = "Rating must not be null")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10")
    private BigDecimal rating;
    
    private String comment;
}
