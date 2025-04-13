package org.socius.sociuswebbackend.model.dtos.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DepartmentRequestDto {
    @NotBlank(message = "Department name must not be empty")
    private String name;
    
    private String description;
}
