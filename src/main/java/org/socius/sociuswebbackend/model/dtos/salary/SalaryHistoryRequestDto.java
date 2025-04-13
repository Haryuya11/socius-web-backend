package org.socius.sociuswebbackend.model.dtos.salary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SalaryHistoryRequestDto {
    @NotNull(message = "User ID must not be null")
    private UUID userId;
    
    @NotNull(message = "Previous salary must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Previous salary cannot be negative")
    private BigDecimal previousSalary;
    
    @NotNull(message = "New salary must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "New salary cannot be negative")
    private BigDecimal newSalary;
    
    @NotNull(message = "Effective date must not be null")
    private LocalDate effectiveDate;
    
    private String reason;
}
