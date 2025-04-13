package org.socius.sociuswebbackend.model.dtos.employee;

import lombok.Data;
import org.socius.sociuswebbackend.model.enums.Gender;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeOnboardingRequestDto {
    // Thông tin cá nhân
    @NotBlank(message = "First name must not be empty")
    private String firstName;
    
    @NotBlank(message = "Last name must not be empty")
    private String lastName;
    
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotNull(message = "Birth date must not be empty")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private Gender gender;
    
    private String nationality;
    
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    @NotBlank(message = "Phone number must not be empty")
    private String phoneNumber;
    
    private String address;
    
    // Thông tin công việc
    @NotNull(message = "Hỉe date must not be empty")
    @PastOrPresent(message = "Hire date must be today or in the past")
    private LocalDate hireDate;
    
    @NotNull(message = "Position ID must not be empty")
    private UUID positionId;
    
    @NotNull(message = "Department ID must not be empty")
    private UUID departmentId;
    
    private UUID teamId;
    
    @NotNull(message = "Role ID must not be empty")
    private UUID roleId;
    
    @NotNull(message = "Salary must not be empty")
    @DecimalMin(value = "0.00", message = "Salary must be a positive number")
    private BigDecimal salary;
    
    private WorkingStatus workingStatus = WorkingStatus.active;
    
    // Thông tin tài khoản
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
