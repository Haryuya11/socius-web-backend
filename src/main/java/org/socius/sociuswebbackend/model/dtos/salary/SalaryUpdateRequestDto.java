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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryUpdateRequestDto {
    @NotNull(message = "Mức lương mới không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Mức lương phải lớn hơn 0")
    private BigDecimal newSalary;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    private String reason;
}
