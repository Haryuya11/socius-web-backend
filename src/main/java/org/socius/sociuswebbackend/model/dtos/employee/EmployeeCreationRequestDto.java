package org.socius.sociuswebbackend.model.dtos.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.Gender;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;

import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCreationRequestDto {
    // Thông tin cá nhân
    @NotBlank(message = "Họ không được để trống")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate birthDate;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    private String nationality;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số diện thoại không hợp lệ")
    private String phoneNumber;

    private String address;

    private String imageUrl;

    // Thông tin công việc
    @NotNull(message = "Ngày bắt đầu làm việc không được để trống")
    @PastOrPresent(message = "Ngày bắt đầu làm việc phải là ngày hôm nay hoặc trong quá khứ")
    private LocalDate hireDate;

    @NotNull(message = "ID chức vụ không được để trống")
    private UUID positionId;

    @NotNull(message = "ID phòng ban không được để trống")
    private UUID departmentId;

    private UUID teamId;

    @NotNull(message = "ID role không được để trống")
    private UUID roleId;

    @NotNull(message = "Lương không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Lương phải lớn hơn 0")
    private BigDecimal salary;

    @Builder.Default
    private WorkingStatus workingStatus = WorkingStatus.active;

}
