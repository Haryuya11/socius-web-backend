package org.socius.sociuswebbackend.model.dtos.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateRequestDto {

    private String firstName;

    private String lastName;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate birthDate;

    private Gender gender;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    private String nationality;

    private String address;

    @PastOrPresent(message = "Ngày bắt đầu làm việc không được là tương lai")
    private LocalDate hireDate;

    // Employment details
    private UUID departmentId;
    private UUID teamId;
    private UUID positionId;
    private UUID roleId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Lương phải lớn hơn 0")
    private BigDecimal salary;

    private String description;
}