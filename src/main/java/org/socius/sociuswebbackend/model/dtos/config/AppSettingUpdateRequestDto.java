package org.socius.sociuswebbackend.model.dtos.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSettingUpdateRequestDto {
    @NotBlank(message = "Giá trị không được để trống")
    private String value;
    private String description;
}