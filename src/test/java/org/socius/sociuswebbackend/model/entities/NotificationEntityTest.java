package org.socius.sociuswebbackend.model.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.socius.sociuswebbackend.model.enums.NotificationType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử cho NotificationEntity.
 * 
 * Các bài kiểm tra này xác minh các quy tắc nghiệp vụ và logic xác thực dữ liệu được triển khai trong lớp NotificationEntity.
 * Xác thực chính được kiểm tra là xác thực ngày hết hạn (ngày hết hạn phải là ngày hiện tại hoặc trong tương lai).
 */
class NotificationEntityTest {

    /**
     * Kiểm tra logic xác thực ngày hết hạn trong NotificationEntity.
     * 
     * Bài kiểm tra này xác minh rằng phương thức validateExpiryDate đúng thực thi quy tắc nghiệp vụ
     * yêu cầu ngày hết hạn thông báo không được là ngày trong quá khứ.
     * 
     * Các trường hợp kiểm thử:
     * 1. Ngày hết hạn trong quá khứ - phải ném IllegalArgumentException
     * 2. Ngày hết hạn là ngày hiện tại - phải vượt qua xác thực (trường hợp biên)
     * 3. Ngày hết hạn trong tương lai - phải vượt qua xác thực (trường hợp bình thường)
     * 
     * Đầu vào: Một thực thể thông báo với các ngày hết hạn khác nhau
     * Kết quả mong đợi: 
     * - Ngoại lệ khi ngày hết hạn trong quá khứ
     * - Không có ngoại lệ khi ngày hết hạn là ngày hiện tại hoặc trong tương lai
     */
    @Test
    @DisplayName("Should validate that expiry date is in the future")
    void shouldValidateExpiryDate() {
        // Khởi tạo - Tạo thông báo thử nghiệm với thông tin cơ bản
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID());
        notification.setTitle("Important Notification");
        notification.setMessage("This is an important notification");
        notification.setType(NotificationType.info);
        notification.setIsUrgent(false);
        
        // Thiết lập người gửi giả lập cho thông báo
        UserEntity sender = new UserEntity();
        sender.setId(UUID.randomUUID());
        notification.setSender(sender);
        
        // Khi & Thì
        // Trường hợp 1: Ngày hết hạn trong quá khứ (không hợp lệ)
        notification.setExpiryDate(LocalDate.now().minusDays(1));
        Exception exception = assertThrows(IllegalArgumentException.class, notification::validateExpiryDate);
        assertEquals("Expiry date must be in the future", exception.getMessage());
        
        // Trường hợp 2: Ngày hết hạn là ngày hiện tại (trường hợp biên, hợp lệ)
        notification.setExpiryDate(LocalDate.now());
        assertDoesNotThrow(notification::validateExpiryDate);
        
        // Trường hợp 3: Ngày hết hạn trong tương lai (trường hợp bình thường, hợp lệ)
        notification.setExpiryDate(LocalDate.now().plusDays(7));
        assertDoesNotThrow(notification::validateExpiryDate);
    }
}
