package org.socius.sociuswebbackend.model.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordChangeRequestDto {
    @NotBlank(message = "Current password must not be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", 
             message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character")
    private String currentPassword;
    
    @NotBlank(message = "New password must not be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", 
             message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character")
    private String newPassword;
    
    @NotBlank(message = "Confirm password must not be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", 
             message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character")
    private String confirmPassword;
}
