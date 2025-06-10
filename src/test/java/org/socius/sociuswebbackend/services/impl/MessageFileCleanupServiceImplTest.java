package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.MessageRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageFileCleanupServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private MessageFileCleanupServiceImpl messageFileCleanupService;

    private MessageEntity fileMessage1;
    private MessageEntity fileMessage2;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu test
        UserEntity sender = AuthTestDataUtil.createTestAdminUser();
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();

        fileMessage1 = ChatTestDataUtil.createMessageEntity(conversation, sender);
        fileMessage1.setId(UUID.randomUUID());
        fileMessage1.setMessageType(MessageType.FILE);
        fileMessage1.setContent("/uploads/files/test1.pdf");
        fileMessage1.setDeleted(true);
        // Đảm bảo phù hợp với điều kiện trong findDeletedMessagesWithMedia
        fileMessage1.setFileUrl("/uploads/files/test1.pdf");
        fileMessage1.setMediaCleanedUp(false);

        fileMessage2 = ChatTestDataUtil.createMessageEntity(conversation, sender);
        fileMessage2.setId(UUID.randomUUID());
        fileMessage2.setMessageType(MessageType.IMAGE);
        fileMessage2.setContent("/uploads/images/test2.jpg");
        fileMessage2.setDeleted(true);
        // Đảm bảo phù hợp với điều kiện trong findDeletedMessagesWithMedia
        fileMessage2.setFileUrl("/uploads/images/test2.jpg");
        fileMessage2.setMediaCleanedUp(false);

        // Cấu hình mock
        when(configService.getInt(eq("message.file.cleanup.days"), anyInt())).thenReturn(30);
    }

    @Test
    @DisplayName("Dọn dẹp file đính kèm của tin nhắn đã xóa")
    void cleanupDeletedMessagesFiles() throws IOException {
        // Mock repository để trả về các tin nhắn đã xóa có file đính kèm
        List<MessageEntity> deletedMessages = Arrays.asList(fileMessage1, fileMessage2);
        // Sửa lại sử dụng phương thức hiện có trong repository
        when(messageRepository.findDeletedMessagesForCleanup(anyInt())).thenReturn(deletedMessages);

        // Thực thi phương thức cần test
        messageFileCleanupService.cleanupDeletedMessagesFiles();

        // Kiểm tra xem FileStorageService có được gọi để xóa các file không
        verify(fileStorageService).deleteFile(fileMessage1.getFileUrl()); // Sử dụng fileUrl thay vì content
        verify(fileStorageService).deleteFile(fileMessage2.getFileUrl()); // Sử dụng fileUrl thay vì content

        // Kiểm tra xem nội dung tin nhắn có được cập nhật không
        verify(messageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("Xử lý trường hợp không có file cần dọn dẹp")
    void handleNoFilesToCleanup() throws IOException {
        // Mock repository để trả về danh sách trống
        when(messageRepository.findDeletedMessagesWithMedia()).thenReturn(List.of());

        // Thực thi phương thức cần test
        messageFileCleanupService.cleanupDeletedMessagesFiles();

        // Kiểm tra không có tương tác với FileStorageService và không lưu lại tin nhắn
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("Xử lý lỗi khi xóa file")
    void handleErrorWhenDeletingFile() throws IOException {
        // Mock repository để trả về tin nhắn cần xóa file
        when(messageRepository.findDeletedMessagesWithMedia()).thenReturn(List.of(fileMessage1));

        // Giả lập lỗi khi xóa file
        doThrow(new RuntimeException("File deletion error")).when(fileStorageService).deleteFile(anyString());

        // Thực thi - không nên ném ngoại lệ ra ngoài
        messageFileCleanupService.cleanupDeletedMessagesFiles();

        // Kiểm tra KHÔNG có gọi save() trong trường hợp lỗi
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }
}