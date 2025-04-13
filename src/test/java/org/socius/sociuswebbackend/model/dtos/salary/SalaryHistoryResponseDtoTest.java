package org.socius.sociuswebbackend.model.dtos.salary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử cho SalaryHistoryResponseDto.
 * 
 * Các bài kiểm tra này xác minh logic tính toán được triển khai trong lớp SalaryHistoryResponseDto.
 * Các phép tính chính được kiểm tra là:
 * - Tính toán số tiền thay đổi (lương mới - lương cũ)
 * - Tính toán phần trăm thay đổi ((lương mới - lương cũ) / lương cũ * 100)
 * - Xử lý trường hợp đặc biệt khi lương cũ bằng 0
 */
class SalaryHistoryResponseDtoTest {

    /**
     * Kiểm tra tính toán các trường dẫn xuất (số tiền thay đổi và phần trăm) trong SalaryHistoryResponseDto.
     * 
     * Bài kiểm tra này sử dụng nhiều tổ hợp đầu vào để xác minh rằng:
     * 1. Số tiền thay đổi được tính đúng là (lương mới - lương cũ)
     * 2. Phần trăm thay đổi được tính đúng là ((lương mới - lương cũ) / lương cũ * 100)
     * 
     * Các trường hợp kiểm thử (được định nghĩa qua @CsvSource):
     * - Lương tăng: từ 1000 lên 1200 (mong đợi: +200, +20%)
     * - Lương giảm: từ 5000 xuống 4500 (mong đợi: -500, -10%)
     * - Không đổi: từ 2500 lên 2500 (mong đợi: 0, 0%)
     * 
     * Đầu vào: Các tổ hợp khác nhau của lương cũ và lương mới
     * Kết quả mong đợi: Các giá trị số tiền thay đổi và phần trăm được tính đúng
     */
    @ParameterizedTest
    @CsvSource({
        "1000.00, 1200.00, 200.00, 20.00",
        "5000.00, 4500.00, -500.00, -10.00",
        "2500.00, 2500.00, 0.00, 0.00"
    })
    @DisplayName("Should correctly calculate change amount and percentage")
    void shouldCalculateDerivedFields(String previousSalary, String newSalary, String expectedChangeAmount, String expectedPercentage) {
        // Khởi tạo - Tạo DTO lịch sử lương thử nghiệm với người dùng và ngày
        SalaryHistoryResponseDto dto = new SalaryHistoryResponseDto();
        UserResponseDto user = new UserResponseDto();
        user.setId(UUID.randomUUID());
        dto.setUser(user);
        dto.setEffectiveDate(LocalDate.now());
        
        // Khi - Thiết lập các giá trị lương sẽ kích hoạt tính toán
        dto.setPreviousSalary(new BigDecimal(previousSalary));
        dto.setNewSalary(new BigDecimal(newSalary));
        
        // Thì - Xác minh các giá trị dẫn xuất được tính toán chính xác
        assertEquals(0, dto.getChangeAmount().compareTo(new BigDecimal(expectedChangeAmount)));
        assertEquals(0, dto.getPercentageChange().compareTo(new BigDecimal(expectedPercentage)));
    }
    
    /**
     * Kiểm tra xử lý trường hợp đặc biệt khi lương cũ bằng không.
     * 
     * Bài kiểm tra này xác minh rằng:
     * 1. Số tiền thay đổi vẫn được tính chính xác ngay cả khi lương cũ bằng không
     * 2. Phần trăm thay đổi được đặt là null (vì phép chia cho 0 là không xác định về mặt toán học)
     * 
     * Đầu vào: Lương cũ = 0, Lương mới = 1000
     * Kết quả mong đợi:
     * - Số tiền thay đổi = 1000
     * - Phần trăm thay đổi = null (vì liên quan đến phép chia cho 0)
     */
    @Test
    @DisplayName("Should handle zero previous salary")
    void shouldHandleZeroPreviousSalary() {
        // Khởi tạo - Tạo DTO lịch sử lương thử nghiệm với người dùng và ngày
        SalaryHistoryResponseDto dto = new SalaryHistoryResponseDto();
        UserResponseDto user = new UserResponseDto();
        user.setId(UUID.randomUUID());
        dto.setUser(user);
        dto.setEffectiveDate(LocalDate.now());
        
        // Khi - Thiết lập lương cũ bằng 0
        dto.setPreviousSalary(BigDecimal.ZERO);
        dto.setNewSalary(new BigDecimal("1000.00"));
        
        // Thì - Xác minh xử lý trường hợp đặc biệt
        assertEquals(0, dto.getChangeAmount().compareTo(new BigDecimal("1000.00")));
        // Phần trăm thay đổi không xác định khi lương cũ bằng 0
        assertNull(dto.getPercentageChange());
    }
}
