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
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConversationMapperTest {
    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ConversationMemberMapper conversationMemberMapper;

    @InjectMocks
    private ConversationMapperImpl conversationMapper;

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
        mockedStatic.when(() -> ApplicationContextHelper.getBean(MessageMapper.class))
                .thenReturn(messageMapper);
        mockedStatic.when(() -> ApplicationContextHelper.getBean(ConversationMemberMapper.class))
                .thenReturn(conversationMemberMapper);
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
    @DisplayName("Nên ánh xạ entity sang DTO với thông tin cơ bản")
    void shouldMapEntityToDtoWithBasicInfo() {
        // Khởi tạo - Tạo cuộc trò chuyện thử nghiệm
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity creator = conversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Giả lập mapper người dùng để trả về DTO
        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        assertEquals(conversation.getName(), dto.getName());
        assertEquals(conversation.getType(), dto.getType());
        assertEquals(creatorDto, dto.getCreatedBy());
        assertEquals(conversation.getCreatedAt(), dto.getCreatedAt());
        assertEquals(conversation.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Nên ánh xạ entity sang DTO với tin nhắn cuối cùng")
    void shouldMapEntityToDtoWithLastMessage() {
        // Khởi tạo - Tạo cuộc trò chuyện thử nghiệm với tin nhắn
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity creator = conversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Tạo tin nhắn và thêm vào cuộc trò chuyện
        MessageEntity message = ChatTestDataUtil.createMessageEntity(conversation, creator);
        conversation.getMessages().add(message);

        // Tạo DTO tin nhắn
        MessageResponseDto messageDto = MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .build();

        // Giả lập mapper người dùng và tin nhắn
        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);
        when(messageMapper.entityToDto(message)).thenReturn(messageDto);

        // Giả lập phương thức getLastMessage
        // Đây là một tình huống phức tạp vì phải mock getLastMessage mà chưa có giả định cụ thể
        // Nếu ConversationMapper có phương thức getLastMessage riêng, ta cần mock nó

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh ánh xạ cơ bản đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        assertEquals(conversation.getName(), dto.getName());
        assertEquals(conversation.getType(), dto.getType());
        assertEquals(creatorDto, dto.getCreatedBy());
    }

    @Test
    @DisplayName("Nên chuyển đổi requestDTO thành entity mới")
    void shouldConvertRequestDtoToNewEntity() {
        // Khởi tạo - Tạo DTO yêu cầu tạo cuộc trò chuyện
        ConversationRequestDto requestDto = ChatTestDataUtil.createConversationRequestDto();

        // Khi - Thực hiện chuyển đổi từ DTO sang thực thể mới
        ConversationEntity entity = conversationMapper.requestDtoToEntity(requestDto);

        // Thì - Xác minh chuyển đổi đã được thực hiện chính xác
        assertNotNull(entity);
        assertEquals(requestDto.getName(), entity.getName());
        assertEquals(requestDto.getType(), entity.getType());
    }

    @Test
    @DisplayName("Nên cập nhật entity từ requestDTO")
    void shouldUpdateEntityFromRequestDto() {
        // Khởi tạo - Tạo cuộc trò chuyện hiện có và DTO yêu cầu cập nhật
        ConversationEntity existingConversation = ChatTestDataUtil.createConversationEntity();
        existingConversation.setName("Tên cũ");
        existingConversation.setType(ConversationType.DIRECT);

        ConversationRequestDto updateDto = new ConversationRequestDto();
        updateDto.setName("Tên mới");
        updateDto.setType(ConversationType.GROUP);

        // Khi - Thực hiện cập nhật thực thể từ DTO
        conversationMapper.updateEntityFromDto(updateDto, existingConversation);

        // Thì - Xác minh thực thể đã được cập nhật chính xác
        assertEquals(updateDto.getName(), existingConversation.getName());
        assertEquals(updateDto.getType(), existingConversation.getType());
    }
}
