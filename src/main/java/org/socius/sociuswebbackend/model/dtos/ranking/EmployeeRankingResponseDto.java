package org.socius.sociuswebbackend.model.dtos.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.period.PeriodResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.RankingCriteria;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EmployeeRankingResponseDto extends BaseDto {
    private UserResponseDto employee;
    private PeriodResponseDto period;
    private BigDecimal rank;
    private RankingCriteria criteria;
}
