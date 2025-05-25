package org.socius.sociuswebbackend.mappers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRecipientDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Lớp kiểm thử cho NotificationMapper.
 *
 * Các bài kiểm tra này xác minh chức năng ánh xạ giữa các thực thể Notification và các DTO,
 * với trọng tâm đặc biệt vào việc xử lý các mối quan hệ người nhận.
 *
 * Lớp kiểm thử này sử dụng MockedStatic để giả lập các phương thức tĩnh trong ApplicationContextHelper,
 * điều này là cần thiết để kiểm tra cơ chế giải quyết phụ thuộc của mapper.
 */
@ExtendWith(MockitoExtension.class)
class NotificationMapperTest {

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationRecipientMapper recipientMapper;

    @InjectMocks
    private NotificationMapperImpl notificationMapper;

    // Khai báo mock tĩnh ở cấp lớp
    private MockedStatic<ApplicationContextHelper> mockedStatic;

    /**
     * Phương thức thiết lập được thực hiện trước mỗi bài kiểm tra.
     *
     * Phương thức này cấu hình các mock tĩnh cần thiết để mô phỏng ngữ cảnh Spring
     * và quá trình tiêm phụ thuộc mà mapper dựa vào.
     */
    @BeforeEach
    void setUp() {
        // Khởi tạo mock tĩnh cho ApplicationContextHelper
        mockedStatic = mockStatic(ApplicationContextHelper.class);

        // Cấu hình mock trả về các bean giả lập của chúng ta khi được yêu cầu
        mockedStatic.when(() -> ApplicationContextHelper.getBean(NotificationRecipientMapper.class))
                  .thenReturn(recipientMapper);
        mockedStatic.when(() -> ApplicationContextHelper.getBean(EntityMappingUtil.class))
                  .thenReturn(entityMappingUtil);
    }

    /**
     * Phương thức dọn dẹp thực hiện sau mỗi bài kiểm tra.
     *
     * Đóng đúng cách mock tĩnh để ngăn rò rỉ bộ nhớ và nhiễu kiểm tra.
     */
    @AfterEach
    void tearDown() {
        // Đóng mock sau mỗi bài kiểm tra để ngăn rò rỉ
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    /**
     * Kiểm tra chức năng ánh xạ từ thực thể sang DTO cho thông báo có người nhận.
     *
     * Bài kiểm tra này xác minh rằng mapper đúng ánh xạ một NotificationEntity có người nhận liên kết
     * thành một NotificationResponseDto, giữ nguyên tất cả dữ liệu và mối quan hệ liên quan.
     *
     * Đầu vào:
     * - Một NotificationEntity với ID, tiêu đề, nội dung, loại, cờ khẩn cấp, và hai người nhận
     *   (một đã đọc, một chưa đọc)
     *
     * Kết quả mong đợi:
     * - Một NotificationResponseDto có cùng ID, tiêu đề, nội dung, loại, và cờ khẩn cấp
     * - Hai người nhận được ánh xạ đúng thành các NotificationRecipientDto
     */
    @Test
    @DisplayName("Should map entity to DTO with recipients")
    void shouldMapEntityToDtoWithRecipients() {
        // Khởi tạo - Tạo thông báo thử nghiệm với người nhận
        UUID notificationId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        UserEntity sender = new UserEntity();
        sender.setId(UUID.randomUUID());

        NotificationEntity notification = new NotificationEntity();
        notification.setId(notificationId);
        notification.setTitle("Important Notice");
        notification.setMessage("This is an important message");
        notification.setType(NotificationType.info);
        notification.setIsUrgent(true);
        notification.setExpiryDate(LocalDate.now().plusDays(7));
        notification.setSender(sender);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        Set<NotificationRecipientEntity> recipients = new HashSet<>();

        UserEntity user1 = new UserEntity();
        user1.setId(userId1);

        UserEntity user2 = new UserEntity();
        user2.setId(userId2);

        NotificationRecipientEntity nr1 = new NotificationRecipientEntity();
        nr1.setId(new NotificationRecipientId(notificationId, userId1));
        nr1.setNotification(notification);
        nr1.setUser(user1);
        nr1.setIsRead(false);

        NotificationRecipientEntity nr2 = new NotificationRecipientEntity();
        nr2.setId(new NotificationRecipientId(notificationId, userId2));
        nr2.setNotification(notification);
        nr2.setUser(user2);
        nr2.setIsRead(true);
        nr2.setReadAt(LocalDateTime.now());

        recipients.add(nr1);
        recipients.add(nr2);
        notification.setRecipients(recipients);

        // Giả lập mapper người nhận để trả về các DTO thử nghiệm của chúng ta
        NotificationRecipientDto recipientDto1 = new NotificationRecipientDto();
        recipientDto1.setNotificationId(notificationId);
        recipientDto1.setUserId(userId1);
        recipientDto1.setIsRead(false);

        NotificationRecipientDto recipientDto2 = new NotificationRecipientDto();
        recipientDto2.setNotificationId(notificationId);
        recipientDto2.setUserId(userId2);
        recipientDto2.setIsRead(true);
        recipientDto2.setReadAt(LocalDateTime.now());

        when(recipientMapper.entityToDto(nr1)).thenReturn(recipientDto1);
        when(recipientMapper.entityToDto(nr2)).thenReturn(recipientDto2);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        NotificationResponseDto dto = notificationMapper.entityToDto(notification);
        notificationMapper.mapRecipients(dto, notification);

        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(notificationId, dto.getId());
        assertEquals("Important Notice", dto.getTitle());
        assertEquals("This is an important message", dto.getMessage());
        assertEquals(NotificationType.info, dto.getType());
        assertTrue(dto.getIsUrgent());
        assertEquals(2, dto.getRecipients().size());
    }

    /**
     * Kiểm tra chức năng cập nhật thực thể từ DTO với người nhận.
     *
     * Bài kiểm tra này xác minh rằng mapper đúng cập nhật một NotificationEntity từ một NotificationRequestDto,
     * đặc biệt tập trung vào cách xử lý các liên kết người nhận.
     *
     * Đầu vào:
     * - Một NotificationEntity với trạng thái ban đầu
     * - Một NotificationRequestDto với các giá trị đã cập nhật và ID người nhận
     *
     * Kết quả mong đợi:
     * - NotificationEntity được cập nhật với giá trị từ DTO
     * - Người nhận thông báo được tạo lại chính xác dựa trên ID người nhận trong DTO
     */
    @Test
    @DisplayName("Should update entity recipients from DTO")
    void shouldUpdateEntityRecipientsFromDto() {
        // Khởi tạo - Tạo thông báo thử nghiệm và DTO với người nhận
        UUID notificationId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        NotificationEntity notification = new NotificationEntity();
        notification.setId(notificationId);
        notification.setTitle("Team Meeting");
        notification.setMessage("Team meeting scheduled");
        notification.setExpiryDate(LocalDate.now().plusDays(1));
        notification.setType(NotificationType.reminder);
        notification.setIsUrgent(false);
        notification.setRecipients(new HashSet<>());

        UserEntity sender = new UserEntity();
        sender.setId(UUID.randomUUID());
        notification.setSender(sender);

        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setTitle("Team Meeting Updated");
        dto.setMessage("Team meeting rescheduled");
        dto.setRecipientIds(List.of(userId1, userId2));

        UserEntity user1 = new UserEntity();
        user1.setId(userId1);

        UserEntity user2 = new UserEntity();
        user2.setId(userId2);

        // Giả lập công cụ ánh xạ thực thể để trả về người dùng thử nghiệm của chúng ta
        when(entityMappingUtil.mapUserIdToEntity(userId1)).thenReturn(user1);
        when(entityMappingUtil.mapUserIdToEntity(userId2)).thenReturn(user2);

        // Khi - Thực hiện hoạt động cập nhật
        notificationMapper.updateEntityFromDto(dto, notification);
        notificationMapper.updateRecipients(dto, notification);
        notificationMapper.addRecipientsToEntity(dto, notification);

        // Thì - Xác minh thực thể đã được cập nhật chính xác
        assertEquals("Team Meeting Updated", notification.getTitle());
        assertEquals("Team meeting rescheduled", notification.getMessage());
        assertEquals(2, notification.getRecipients().size());

        // Xác minh rằng cả hai người nhận đã được liên kết chính xác với thông báo
        boolean foundUser1 = false;
        boolean foundUser2 = false;

        for (NotificationRecipientEntity nr : notification.getRecipients()) {
            if (nr.getId().getUserId().equals(userId1)) {
                foundUser1 = true;
                assertEquals(notification, nr.getNotification());
                assertEquals(user1, nr.getUser());
                assertFalse(nr.getIsRead());
            }
            if (nr.getId().getUserId().equals(userId2)) {
                foundUser2 = true;
                assertEquals(notification, nr.getNotification());
                assertEquals(user2, nr.getUser());
                assertFalse(nr.getIsRead());
            }
        }

        assertTrue(foundUser1, "Người dùng 1 phải nằm trong những người nhận thông báo");
        assertTrue(foundUser2, "Người dùng 2 phải nằm trong những người nhận thông báo");
    }
}
