package org.socius.sociuswebbackend.model.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.socius.sociuswebbackend.model.enums.PeriodStatus;
import org.socius.sociuswebbackend.model.enums.PeriodType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử cho PeriodEntity.
 * 
 * Các bài kiểm tra này xác minh các quy tắc nghiệp vụ và logic xác thực dữ liệu được triển khai trong lớp PeriodEntity.
 * Xác thực chính được kiểm tra là xác thực phạm vi ngày (ngày kết thúc không được trước ngày bắt đầu).
 */
class PeriodEntityTest {

    /**
     * Kiểm tra logic xác thực ngày tháng trong PeriodEntity.
     * 
     * Bài kiểm tra này xác minh rằng phương thức validateDates đúng thực thi quy tắc nghiệp vụ
     * yêu cầu ngày kết thúc của kỳ không được sớm hơn ngày bắt đầu.
     * 
     * Các trường hợp kiểm thử:
     * 1. Ngày kết thúc trước ngày bắt đầu - phải ném IllegalArgumentException
     * 2. Ngày kết thúc trùng với ngày bắt đầu - phải vượt qua xác thực (trường hợp biên)
     * 3. Ngày kết thúc sau ngày bắt đầu - phải vượt qua xác thực (trường hợp bình thường)
     * 
     * Đầu vào: Một thực thể kỳ với các tổ hợp ngày bắt đầu và kết thúc khác nhau
     * Kết quả mong đợi: 
     * - Ngoại lệ khi ngày kết thúc trước ngày bắt đầu
     * - Không có ngoại lệ khi ngày kết thúc trùng với hoặc sau ngày bắt đầu
     */
    @Test
    @DisplayName("Should validate that end date is after start date")
    void shouldValidateDates() {
        // Khởi tạo - Tạo kỳ thử nghiệm với thông tin cơ bản
        PeriodEntity period = new PeriodEntity();
        period.setId(UUID.randomUUID());
        period.setName("Q1 2023");
        period.setType(PeriodType.monthly);
        period.setStatus(PeriodStatus.active);
        
        // Khi & Thì
        // Trường hợp 1: Ngày kết thúc trước ngày bắt đầu (không hợp lệ)
        period.setStartDate(LocalDate.of(2023, 3, 1));
        period.setEndDate(LocalDate.of(2023, 2, 28));
        Exception exception = assertThrows(IllegalArgumentException.class, period::validateDates);
        assertEquals("End date must be after start date", exception.getMessage());
        
        // Trường hợp 2: Ngày kết thúc trùng với ngày bắt đầu (trường hợp biên, hợp lệ)
        period.setStartDate(LocalDate.of(2023, 3, 1));
        period.setEndDate(LocalDate.of(2023, 3, 1));
        assertDoesNotThrow(period::validateDates);
        
        // Trường hợp 3: Ngày kết thúc sau ngày bắt đầu (trường hợp bình thường, hợp lệ)
        period.setStartDate(LocalDate.of(2023, 3, 1));
        period.setEndDate(LocalDate.of(2023, 3, 31));
        assertDoesNotThrow(period::validateDates);
    }
}
