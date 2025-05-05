package org.socius.sociuswebbackend.model.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.socius.sociuswebbackend.model.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử cho UserEntity.
 * <p>
 * Các bài kiểm tra này xác minh các quy tắc nghiệp vụ và logic xác thực dữ liệu được triển khai trong lớp UserEntity.
 * Các xác thực chính được kiểm tra bao gồm:
 * - Xác thực tuổi (người dùng phải ít nhất 18 tuổi)
 * - Chức năng nối họ và tên đầy đủ
 */
class UserEntityTest {

    /**
     * Kiểm tra logic xác thực tuổi trong UserEntity.
     * <p>
     * Bài kiểm tra này xác minh rằng phương thức validateAge đúng thực thi quy tắc nghiệp vụ
     * yêu cầu tất cả người dùng phải ít nhất 18 tuổi.
     * <p>
     * Các trường hợp kiểm thử:
     * 1. Người dùng dưới 18 tuổi - phải ném IllegalArgumentException
     * 2. Người dùng đúng 18 tuổi - phải vượt qua xác thực
     * 3. Người dùng trên 18 tuổi - phải vượt qua xác thực
     * <p>
     * Đầu vào: Một thực thể người dùng với các ngày sinh khác nhau
     * Kết quả mong đợi:
     * - Ngoại lệ đối với người dùng dưới 18 tuổi
     * - Không có ngoại lệ đối với người dùng từ 18 tuổi trở lên
     */
    @Test
    @DisplayName("Should validate that user is at least 18 years old")
    void shouldValidateUserAge() {
        // Khởi tạo - Tạo người dùng thử nghiệm với thông tin cơ bản
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setGender(Gender.male);
        user.setHireDate(LocalDate.now());

        // Khi & Thì
        // Trường hợp 1: Người dùng dưới 18 tuổi
        user.setBirthDate(LocalDate.now().minusYears(17));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // Gọi thủ công logic xác thực sẽ được kích hoạt bởi JPA
            user.validateAge();
        });
        assertEquals("User must be at least 18 years old", exception.getMessage());

        // Trường hợp 2: Người dùng đúng 18 tuổi
        user.setBirthDate(LocalDate.now().minusYears(18));
        assertDoesNotThrow(() -> user.validateAge());

        // Trường hợp 3: Người dùng trên 18 tuổi
        user.setBirthDate(LocalDate.now().minusYears(25));
        assertDoesNotThrow(() -> user.validateAge());
    }

    /**
     * Kiểm tra logic ghép họ và tên trong UserEntity.
     * <p>
     * Bài kiểm tra này xác minh rằng phương thức getFullName đúng ghép
     * tên và họ của người dùng với một khoảng trắng ở giữa.
     * <p>
     * Đầu vào: Một thực thể người dùng với tên "John" và họ "Doe"
     * Kết quả mong đợi: Tên đầy đủ là "John Doe"
     */
    @Test
    @DisplayName("Should correctly concatenate first and last name")
    void shouldConcatenateFullName() {
        // Khởi tạo - Tạo người dùng thử nghiệm với họ và tên
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");

        // Khi - Lấy tên đầy đủ
        String fullName = user.getFullName();

        // Thì - Xác minh logic ghép họ tên hoạt động chính xác
        assertEquals("John Doe", fullName);
    }
}
