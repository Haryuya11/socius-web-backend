package org.socius.sociuswebbackend.model.dtos.employment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Base class for employment-related DTOs that share common fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEmploymentDto {
    @NotNull(message = "User ID must not be null")
    private UUID userId;
    
    @NotNull(message = "Position ID must not be null")
    private UUID positionId;
    
    @NotNull(message = "Department ID must not be null")
    private UUID departmentId;
    
    private UUID teamId;
    
    @NotNull(message = "Role ID must not be null")
    private UUID roleId;
    
    @NotNull(message = "Start date must not be null")
    @PastOrPresent(message = "Start date must be in the past or present")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @NotNull(message = "Salary must not be null")
    @DecimalMin(value = "0.00", message = "Salary must be a positive number")
    private BigDecimal salary;
}
