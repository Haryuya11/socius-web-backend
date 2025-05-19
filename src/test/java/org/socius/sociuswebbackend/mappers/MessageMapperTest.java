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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Lớp kiểm thử cho MessageMapper.
 * <p>
 * Các bài kiểm tra này xác minh chức năng ánh xạ giữa các thực thể Message và các DTO,
 * với trọng tâm đặc biệt vào việc xử lý các mối quan hệ với người gửi và cuộc trò chuyện.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageMapperTest {

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MessageMapperImpl messageMapper;

    // Khai báo mock tĩnh ở cấp lớp
    private MockedStatic<ApplicationContextHelper> mockedStatic;

    /**
     * Phương thức thiết lập được thực hiện trước mỗi bài kiểm tra.
     */
    @BeforeEach
    void setUp() {
        // Khởi tạo mock tĩnh cho ApplicationContextHelper
        mockedStatic = mockStatic(ApplicationContextHelper.class);

        // Cấu hình mock trả về các bean giả lập của chúng ta khi được yêu cầu
        mockedStatic.when(() -> ApplicationContextHelper.getBean(UserMapper.class))
                .thenReturn(userMapper);
        mockedStatic.when(() -> ApplicationContextHelper.getBean(EntityMappingUtil.class))
                .thenReturn(entityMappingUtil);
    }

    /**
     * Phương thức dọn dẹp thực hiện sau mỗi bài kiểm tra.
     */
    @AfterEach
    void tearDown() {
        // Đóng mock sau mỗi bài kiểm tra để ngăn rò rỉ
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    @DisplayName("Nên ánh xạ entity sang DTO với thông tin đầy đủ")
    void shouldMapEntityToDtoWithFullInfo() {
        // Khởi tạo - Tạo tin nhắn thử nghiệm
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity sender = AuthTestDataUtil.createTestAdminUser();
        MessageEntity message = ChatTestDataUtil.createMessageEntity(conversation, sender);

        UserResponseDto senderDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Giả lập mapper người dùng
        when(userMapper.entityToDto(sender)).thenReturn(senderDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        MessageResponseDto dto = messageMapper.entityToDto(message);

        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(message.getId(), dto.getId());
        assertEquals(conversation.getId(), dto.getConversationId());
        assertEquals(message.getContent(), dto.getContent());
        assertEquals(senderDto, dto.getSender());
        assertEquals(message.getCreatedAt(), dto.getCreatedAt());
        assertEquals(message.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Nên chuyển đổi requestDTO thành entity mới")
    void shouldConvertRequestDtoToNewEntity() {
        // Khởi tạo - Tạo DTO yêu cầu tạo tin nhắn
        MessageRequestDto requestDto = ChatTestDataUtil.createMessageRequestDto();
        UUID conversationId = requestDto.getConversationId();
        UUID senderId = UUID.randomUUID();

        // Tạo các entity để mô phỏng tìm kiếm
        ConversationEntity conversation = new ConversationEntity();
        conversation.setId(conversationId);

        UserEntity sender = new UserEntity();
        sender.setId(senderId);

        // Giả lập EntityMappingUtil
        when(entityMappingUtil.mapIdToEntity(conversationId, ConversationEntity.class))
                .thenReturn(conversation);

        // Khi - Thực hiện chuyển đổi từ DTO sang thực thể mới
        MessageEntity entity = messageMapper.requestDtoToEntity(requestDto);

        // Thì - Xác minh chuyển đổi đã được thực hiện chính xác
        assertNotNull(entity);
        assertEquals(conversation, entity.getConversation());
        assertEquals(requestDto.getContent(), entity.getContent());
        assertEquals(MessageType.TEXT, entity.getMessageType());
    }

    @Test
    @DisplayName("Nên cập nhật entity từ requestDTO")
    void shouldUpdateEntityFromRequestDto() {
        // Khởi tạo - Tạo tin nhắn hiện có và DTO yêu cầu cập nhật
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity sender = AuthTestDataUtil.createTestAdminUser();
        MessageEntity existingMessage = ChatTestDataUtil.createMessageEntity(conversation, sender);
        existingMessage.setContent("Nội dung cũ");

        MessageRequestDto updateDto = ChatTestDataUtil.createMessageRequestDto();
        updateDto.setContent("Nội dung mới");
        UUID newConversationId = UUID.randomUUID();
        updateDto.setConversationId(newConversationId);

        ConversationEntity newConversation = new ConversationEntity();
        newConversation.setId(newConversationId);

        // Giả lập EntityMappingUtil
        when(entityMappingUtil.mapIdToEntity(newConversationId, ConversationEntity.class))
                .thenReturn(newConversation);

        // Khi - Thực hiện cập nhật thực thể từ DTO
        messageMapper.updateEntityFromDto(updateDto, existingMessage);

        // Thì - Xác minh thực thể đã được cập nhật chính xác
        assertEquals(updateDto.getContent(), existingMessage.getContent());
//        assertEquals(newConversation, existingMessage.getConversation());
    }
}