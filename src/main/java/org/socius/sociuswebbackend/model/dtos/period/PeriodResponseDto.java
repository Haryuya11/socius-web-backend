package org.socius.sociuswebbackend.model.dtos.period;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.enums.PeriodStatus;
import org.socius.sociuswebbackend.model.enums.PeriodType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PeriodResponseDto extends BaseDto {
    private String name;
    private PeriodType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodStatus status;
    private String description;
}
