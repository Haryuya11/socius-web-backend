package org.socius.sociuswebbackend.model.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password must not be empty")
    private String password;
    
    private boolean rememberMe;
}
