package org.socius.sociuswebbackend.model.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lớp kiểm thử cho EmploymentHistoryEntity.
 * 
 * Các bài kiểm tra này xác minh các quy tắc nghiệp vụ và logic xác thực dữ liệu được triển khai trong lớp EmploymentHistoryEntity.
 * Xác thực chính được kiểm tra là xác thực phạm vi ngày (ngày kết thúc phải không trước ngày bắt đầu).
 */
class EmploymentHistoryEntityTest {

    /**
     * Kiểm tra logic xác thực ngày tháng trong EmploymentHistoryEntity.
     * 
     * Bài kiểm tra này xác minh rằng phương thức validateDates đúng thực thi quy tắc nghiệp vụ
     * yêu cầu ngày kết thúc của thời gian làm việc không được sớm hơn ngày bắt đầu.
     * 
     * Các trường hợp kiểm thử:
     * 1. Ngày kết thúc trước ngày bắt đầu - phải ném IllegalArgumentException
     * 2. Ngày kết thúc trùng với ngày bắt đầu - phải vượt qua xác thực (trường hợp biên)
     * 3. Ngày kết thúc sau ngày bắt đầu - phải vượt qua xác thực (trường hợp bình thường)
     * 
     * Đầu vào: Một thực thể lịch sử việc làm với các tổ hợp ngày bắt đầu và kết thúc khác nhau
     * Kết quả mong đợi: 
     * - Ngoại lệ khi ngày kết thúc trước ngày bắt đầu
     * - Không có ngoại lệ khi ngày kết thúc trùng với hoặc sau ngày bắt đầu
     */
    @Test
    @DisplayName("Should validate that end date is after start date")
    void shouldValidateDates() {
        // Khởi tạo - Tạo thực thể lịch sử việc làm với các trường cần thiết
        EmploymentHistoryEntity history = new EmploymentHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setSalary(BigDecimal.valueOf(5000));

        // Thiết lập các quan hệ cần thiết
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        history.setUser(user);

        PositionEntity position = new PositionEntity();
        position.setId(UUID.randomUUID());
        history.setPosition(position);

        DepartmentEntity department = new DepartmentEntity();
        department.setId(UUID.randomUUID());
        history.setDepartment(department);

        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        history.setRole(role);

        // Khi & Thì
        // Trường hợp 1: Ngày kết thúc trước ngày bắt đầu (không hợp lệ)
        history.setStartDate(LocalDate.of(2023, 5, 1));
        history.setEndDate(LocalDate.of(2023, 4, 30));
        Exception exception = assertThrows(IllegalArgumentException.class, history::validateDates);
        assertEquals("End date must be after start date", exception.getMessage());

        // Trường hợp 2: Ngày kết thúc trùng với ngày bắt đầu (trường hợp biên, hợp lệ)
        history.setStartDate(LocalDate.of(2023, 5, 1));
        history.setEndDate(LocalDate.of(2023, 5, 1));
        assertDoesNotThrow(history::validateDates);

        // Trường hợp 3: Ngày kết thúc sau ngày bắt đầu (trường hợp bình thường, hợp lệ)
        history.setStartDate(LocalDate.of(2023, 5, 1));
        history.setEndDate(LocalDate.of(2023, 6, 30));
        assertDoesNotThrow(history::validateDates);
    }
}
