package org.socius.sociuswebbackend.model.dtos.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountRequestDto {
    @NotNull(message = "User ID must not be null")
    private UUID userId;
    
    @NotBlank(message = "Password must not be empty")
    private String password;
    
    @Builder.Default
    private Boolean isActive = true;
}
