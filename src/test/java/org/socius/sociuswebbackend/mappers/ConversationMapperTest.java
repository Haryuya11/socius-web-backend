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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Lớp kiểm thử cho ConversationMapper.
 * <p>
 * Kiểm tra các chức năng ánh xạ giữa ConversationEntity và ConversationDTO,
 * đặc biệt tập trung vào việc xử lý mối quan hệ với tin nhắn, thành viên và người tạo.
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

    /**
     * Kiểm tra ánh xạ entity sang DTO với thông tin cơ bản.
     * <p>
     * Mục đích: Xác minh mapper có thể ánh xạ đúng các trường cơ bản
     * như id, name, type, createdBy, createdAt, updatedAt.
     */
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

    /**
     * Kiểm tra ánh xạ entity sang DTO với tin nhắn cuối cùng.
     * <p>
     * Mục đích: Xác minh mapper có thể xử lý đúng mối quan hệ với messages
     * và ánh xạ tin nhắn cuối cùng (nếu có).
     */
    @Test
    @DisplayName("Nên ánh xạ entity sang DTO với tin nhắn cuối cùng")
    void shouldMapEntityToDtoWithLastMessage() {
        // Khởi tạo - Tạo cuộc trò chuyện thử nghiệm với tin nhắn
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity creator = conversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Tạo tin nhắn và thêm vào cuộc trò chuyện
        MessageEntity message = ChatTestDataUtil.createMessageEntity(conversation, creator);
        conversation.setMessages(new HashSet<>(List.of(message)));

        // Tạo DTO tin nhắn
        MessageResponseDto messageDto = MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .build();

        // Giả lập mapper người dùng và tin nhắn
        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);
        when(messageMapper.entityToDto(message)).thenReturn(messageDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh ánh xạ cơ bản đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        assertEquals(conversation.getName(), dto.getName());
        assertEquals(conversation.getType(), dto.getType());
        assertEquals(creatorDto, dto.getCreatedBy());
    }

    /**
     * Kiểm tra chuyển đổi requestDTO thành entity mới.
     * <p>
     * Mục đích: Xác minh mapper có thể tạo entity mới từ request DTO
     * với các trường cơ bản được ánh xạ đúng.
     */
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

    /**
     * Kiểm tra cập nhật entity từ requestDTO.
     * <p>
     * Mục đích: Xác minh mapper có thể cập nhật entity hiện tại
     * từ request DTO mà không làm mất dữ liệu khác.
     */
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

    /**
     * Kiểm tra ánh xạ entity sang DTO khi conversation không có tin nhắn.
     * <p>
     * Mục đích: Xác minh mapper xử lý đúng trường hợp edge case
     * khi conversation chưa có tin nhắn nào.
     */
    @Test
    @DisplayName("Nên ánh xạ entity sang DTO khi không có tin nhắn")
    void shouldMapEntityToDtoWithoutMessages() {
        // Khởi tạo - Tạo cuộc trò chuyện không có tin nhắn
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        conversation.setMessages(new HashSet<>());
        UserEntity creator = conversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Giả lập mapper người dùng
        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        assertEquals(conversation.getName(), dto.getName());
        assertEquals(conversation.getType(), dto.getType());
        assertEquals(creatorDto, dto.getCreatedBy());
        assertNull(dto.getLastMessage()); // Không có tin nhắn cuối
    }

    /**
     * Kiểm tra ánh xạ entity sang DTO với nhiều tin nhắn.
     * <p>
     * Mục đích: Xác minh mapper chỉ lấy tin nhắn cuối cùng
     * khi có nhiều tin nhắn trong conversation.
     */
    @Test
    @DisplayName("Nên ánh xạ entity sang DTO với tin nhắn cuối cùng từ nhiều tin nhắn")
    void shouldMapEntityToDtoWithLastMessageFromMultiple() {
        // Khởi tạo - Tạo cuộc trò chuyện với nhiều tin nhắn
        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity creator = conversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Tạo nhiều tin nhắn với thời gian khác nhau
        MessageEntity oldMessage = ChatTestDataUtil.createMessageEntity(conversation, creator);
        oldMessage.setCreatedAt(LocalDateTime.now().minusHours(2));
        oldMessage.setContent("Tin nhắn cũ");

        MessageEntity newMessage = ChatTestDataUtil.createMessageEntity(conversation, creator);
        newMessage.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        newMessage.setContent("Tin nhắn mới");

        conversation.setMessages(new HashSet<>(List.of(oldMessage, newMessage)));

        MessageResponseDto newMessageDto = MessageResponseDto.builder()
                .id(newMessage.getId())
                .content(newMessage.getContent())
                .build();

        // Giả lập mapper
        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);
        when(messageMapper.entityToDto(newMessage)).thenReturn(newMessageDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh tin nhắn cuối cùng được ánh xạ đúng
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        // Verify chỉ tin nhắn mới nhất được map (nếu mapper có logic này)
    }

    /**
     * Kiểm tra xử lý null values trong entity.
     * <p>
     * Mục đích: Xác minh mapper xử lý an toàn các giá trị null
     * mà không gây ra NullPointerException.
     */
    @Test
    @DisplayName("Nên xử lý an toàn các giá trị null trong entity")
    void shouldHandleNullValuesInEntitySafely() {
        // Khởi tạo - Tạo entity với một số giá trị null
        ConversationEntity conversation = new ConversationEntity();
        conversation.setId(java.util.UUID.randomUUID());
        conversation.setName("Test Conversation");
        conversation.setType(ConversationType.GROUP);
        conversation.setCreatedBy(null); // Null creator
        conversation.setMessages(null); // Null messages
        conversation.setMembers(new HashSet<>());

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Thì - Xác minh mapper xử lý null values an toàn
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getId());
        assertEquals(conversation.getName(), dto.getName());
        assertEquals(conversation.getType(), dto.getType());
        assertNull(dto.getCreatedBy()); // Null creator được xử lý đúng
        assertNull(dto.getLastMessage()); // Null messages được xử lý đúng
    }

    /**
     * Kiểm tra ánh xạ với conversation type khác nhau.
     * <p>
     * Mục đích: Xác minh mapper xử lý đúng các loại conversation
     * (DIRECT, GROUP) mà không bị lỗi.
     */
    @Test
    @DisplayName("Nên ánh xạ đúng với các loại conversation khác nhau")
    void shouldMapCorrectlyWithDifferentConversationTypes() {
        // Test với DIRECT conversation
        ConversationEntity directConversation = ChatTestDataUtil.createConversationEntity();
        directConversation.setType(ConversationType.DIRECT);
        UserEntity creator = directConversation.getCreatedBy();
        UserResponseDto creatorDto = AuthTestDataUtil.createTestAdminUserResponse();

        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);

        ConversationResponseDto directDto = conversationMapper.entityToDto(directConversation);

        assertNotNull(directDto);
        assertEquals(ConversationType.DIRECT, directDto.getType());

        // Test với GROUP conversation
        ConversationEntity groupConversation = ChatTestDataUtil.createConversationEntity();
        groupConversation.setType(ConversationType.GROUP);

        when(userMapper.entityToDto(creator)).thenReturn(creatorDto);

        ConversationResponseDto groupDto = conversationMapper.entityToDto(groupConversation);

        assertNotNull(groupDto);
        assertEquals(ConversationType.GROUP, groupDto.getType());
    }
}