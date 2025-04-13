package org.socius.sociuswebbackend.model.dtos.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.RankingCriteria;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmployeeRankingRequestDto {
    @NotNull(message = "Employee ID must not be null")
    private UUID employeeId;
    
    @NotNull(message = "Period ID must not be null")
    private UUID periodId;
    
    @NotNull(message = "Rank must not be null")
    @DecimalMin(value = "0.0", message = "Rank must be at least 0")
    @DecimalMax(value = "10.0", message = "Rank must be at most 10")
    private BigDecimal rank;
    
    @NotNull(message = "Criteria must not be null")
    private RankingCriteria criteria;
}
