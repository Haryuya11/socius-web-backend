package org.socius.sociuswebbackend.model.dtos.salary;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryHistoryRequestDto {
    @NotNull(message = "User ID must not be null")
    private UUID userId;

    @NotNull(message = "Previous salary must not be null")
    @DecimalMin(value = "0.0", message = "Previous salary cannot be negative")
    private BigDecimal previousSalary;

    @NotNull(message = "New salary must not be null")
    @DecimalMin(value = "0.0", message = "New salary cannot be negative")
    private BigDecimal newSalary;

    @NotNull(message = "Effective date must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    private String reason;
}
