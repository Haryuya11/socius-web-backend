package org.socius.sociuswebbackend.model.dtos.department;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequestDto {
    @NotBlank(message = "Department name must not be empty")
    private String name;
    
    private String description;
}
