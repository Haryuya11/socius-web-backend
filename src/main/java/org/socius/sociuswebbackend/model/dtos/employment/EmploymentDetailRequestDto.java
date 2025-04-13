package org.socius.sociuswebbackend.model.dtos.employment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EmploymentDetailRequestDto extends BaseEmploymentDto {
    @NotNull(message = "Working status must not be null")
    private WorkingStatus workingStatus;
}
