package org.socius.sociuswebbackend.model.dtos.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.Gender;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    @NotBlank(message = "First name must not be empty")
    private String firstName;
    
    @NotBlank(message = "Last name must not be empty")
    private String lastName;
    
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email is not valid")
    private String email;
    
    @NotNull(message = "Birth date must not be empty")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private String imageUrl;
    
    private Gender gender;
    
    private String nationality;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;
    
    @NotNull(message = "Hire date must not be empty")
    private LocalDate hireDate;
    
    private String address;
}
