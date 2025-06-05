package org.socius.sociuswebbackend.model.dtos.salary;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryHistoryResponseDto extends BaseDto {
    private UserResponseDto user;
    private BigDecimal previousSalary;
    private BigDecimal newSalary;
    private BigDecimal changeAmount;
    private BigDecimal percentageChange;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    private String reason;

    public void setPreviousSalary(BigDecimal previousSalary) {
        this.previousSalary = previousSalary;
        calculateDerivedFields();
    }

    public void setNewSalary(BigDecimal newSalary) {
        this.newSalary = newSalary;
        calculateDerivedFields();
    }

    private void calculateDerivedFields() {
        if (previousSalary != null && newSalary != null) {
            this.changeAmount = newSalary.subtract(previousSalary);

            if (previousSalary.compareTo(BigDecimal.ZERO) != 0) {
                this.percentageChange = this.changeAmount
                        .divide(previousSalary, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }
    }
}
