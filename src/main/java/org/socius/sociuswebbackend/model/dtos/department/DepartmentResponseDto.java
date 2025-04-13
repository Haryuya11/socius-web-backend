package org.socius.sociuswebbackend.model.dtos.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DepartmentResponseDto extends BaseDto {
    private String name;
    private String description;
}
