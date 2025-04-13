package org.socius.sociuswebbackend.model.dtos.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.period.PeriodResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PerformanceReviewResponseDto extends BaseDto {
    private UserResponseDto employee;
    private UserResponseDto reviewer;
    private PeriodResponseDto period;
    private BigDecimal rating;
    private String comment;
}
