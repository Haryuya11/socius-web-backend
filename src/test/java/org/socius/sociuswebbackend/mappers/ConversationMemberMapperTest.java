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
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Lớp kiểm thử cho ConversationMemberMapper.
 * <p>
 * Các bài kiểm tra này xác minh chức năng ánh xạ giữa các thực thể ConversationMember và các DTO,
 * với trọng tâm đặc biệt vào việc xử lý composite key và mối quan hệ với người dùng và cuộc trò chuyện.
 */
@ExtendWith(MockitoExtension.class)
class ConversationMemberMapperTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ConversationMemberMapperImpl conversationMemberMapper;

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
    void shouldMapEntityToDto() {
        // Khởi tạo - Tạo thành viên cuộc trò chuyện thử nghiệm

        ConversationEntity conversation = ChatTestDataUtil.createConversationEntity();
        UserEntity user = AuthTestDataUtil.createTestAdminUser();

        LocalDateTime joinedAt = LocalDateTime.now();

        ConversationMemberEntity member = ChatTestDataUtil.createConversationMemberEntity(conversation, user);
        member.setJoinedAt(joinedAt);

        UserResponseDto userDto = AuthTestDataUtil.createTestAdminUserResponse();

        // Giả lập mapper người dùng
        when(userMapper.entityToDto(user)).thenReturn(userDto);

        // Khi - Thực hiện ánh xạ từ thực thể sang DTO
        ConversationMemberDto dto = conversationMemberMapper.entityToDto(member);

        // Thì - Xác minh ánh xạ đã được thực hiện chính xác
        assertNotNull(dto);
        assertEquals(conversation.getId(), dto.getConversationId());
        assertEquals(userDto, dto.getUser());
        assertEquals(joinedAt, dto.getJoinedAt());
        assertEquals(MemberRole.MEMBER, dto.getRole());
    }
}