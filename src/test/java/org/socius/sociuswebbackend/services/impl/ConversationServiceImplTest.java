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
import org.socius.sociuswebbackend.mappers.ConversationMapper;
import org.socius.sociuswebbackend.mappers.ConversationMemberMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnreadCountRepository unreadCountRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationMemberMapper conversationMemberMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private UUID userId;
    private UUID groupId;
    private UUID otherUserId;
    private UUID conversationId;
    private UserEntity user;
    private UserEntity otherUser;
    private ConversationEntity conversation;
    private ConversationResponseDto conversationResponseDto;
    private ConversationMemberEntity userMember;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        user = new UserEntity();
        user.setId(userId);
        user.setFirstName("Test");
        user.setLastName("User");

        otherUser = new UserEntity();
        otherUser.setId(otherUserId);
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");

        conversation = new ConversationEntity();
        conversation.setId(conversationId);
        conversation.setName("Test Conversation");
        conversation.setType(ConversationType.GROUP);
        conversation.setCreatedByUser(user);

        conversationResponseDto = ConversationResponseDto.builder()
                .id(conversationId)
                .name("Test Conversation")
                .type(ConversationType.GROUP)
                .build();

        userMember = new ConversationMemberEntity();
        userMember.setUser(user);
        userMember.setConversation(conversation);
        userMember.setRole(MemberRole.ADMIN);
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm thành công")
    void createGroupConversationSuccessfully() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        // Mock các dependencies
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.createGroupConversation(
                groupId, groupName, userId, memberIds);

        // Kiểm tra
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
        verify(conversationMapper).entityToDto(conversation);
    }



    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện nhóm với creator không tồn tại")
    void failToCreateGroupConversationWhenCreatorNotExists() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> conversationService.createGroupConversation(groupId, groupName, userId, memberIds));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện nhóm với thành viên không tồn tại")
    void failToCreateGroupConversationWhenMemberNotExists() {
        // Thiết lập dữ liệu test
        UUID invalidMemberId = UUID.randomUUID();
        Set<UUID> memberIds = Set.of(userId, invalidMemberId); // Thêm một ID không tồn tại
        String groupName = "Test Group";

        // Mock creator tồn tại
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Mock member không tồn tại
        when(userRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra - exception phải được throw trước khi save conversation
        RuntimeException exception = assertThrows(RuntimeException.class, () -> conversationService.createGroupConversation(groupId, groupName, userId, memberIds));

        // Kiểm tra message exception
        assertTrue(exception.getMessage().contains("Không tìm thấy người dùng với ID"));

        // Verify rằng conversation không được save do validation failed
        verify(conversationRepository, never()).save(any());
        verify(conversationMemberRepository, never()).saveAll(any());
        verify(unreadCountRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với danh sách thành viên rỗng")
    void createGroupConversationWithEmptyMemberList() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = new HashSet<>();
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.createGroupConversation(
                groupId, groupName, userId, memberIds);

        // Kiểm tra - chỉ có creator được thêm vào nhóm
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với creator trong danh sách members")
    void createGroupConversationWithCreatorInMemberList() {
        // Thiết lập dữ liệu test - creator cũng có trong memberIds
        Set<UUID> memberIds = Set.of(userId, otherUserId); // userId là creator và cũng trong memberIds
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.createGroupConversation(
                groupId, groupName, userId, memberIds);

        // Kiểm tra
        assertNotNull(result);

        // Verify chỉ có 2 members được tạo (không duplicate creator)
        verify(conversationMemberRepository).saveAll(argThat(members -> {
            List<ConversationMemberEntity> memberList = (List<ConversationMemberEntity>) members;
            return memberList.size() == 2; // Creator + 1 other member
        }));
    }

    @Test
    @DisplayName("Kiểm tra các thành viên được thêm vào với quyền đúng")
    void verifyMembersAddedWithCorrectRoles() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(otherUserId); // Chỉ thêm otherUser, không duplicate creator
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        conversationService.createGroupConversation(groupId, groupName, userId, memberIds);

        // Verify members được save với role đúng
        verify(conversationMemberRepository).saveAll(argThat(members -> {
            List<ConversationMemberEntity> memberList = (List<ConversationMemberEntity>) members;

            // Phải có đúng 2 members
            if (memberList.size() != 2) return false;

            // Creator phải có role ADMIN
            boolean hasCreatorAsAdmin = memberList.stream()
                    .anyMatch(member -> member.getUser().getId().equals(userId) &&
                            member.getRole() == MemberRole.ADMIN);

            // Other member phải có role MEMBER
            boolean hasOtherAsMember = memberList.stream()
                    .anyMatch(member -> member.getUser().getId().equals(otherUserId) &&
                            member.getRole() == MemberRole.MEMBER);

            return hasCreatorAsAdmin && hasOtherAsMember;
        }));
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với tên null hoặc rỗng")
    void createGroupConversationWithNullOrEmptyName() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi với tên null
        ConversationResponseDto result1 = conversationService.createGroupConversation(
                groupId, null, userId, memberIds);

        // Thực thi với tên rỗng
        ConversationResponseDto result2 = conversationService.createGroupConversation(
                groupId, "", userId, memberIds);

        // Kiểm tra - cả hai trường hợp đều được xử lý
        assertNotNull(result1);
        assertNotNull(result2);
        verify(conversationRepository, times(2)).save(any(ConversationEntity.class));
    }

    @Test
    @DisplayName("Lỗi khi groupId là null")
    void failToCreateGroupConversationWithNullGroupId() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> conversationService.createGroupConversation(null, groupName, userId, memberIds));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi creatorId là null")
    void failToCreateGroupConversationWithNullCreatorId() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> conversationService.createGroupConversation(groupId, groupName, null, memberIds));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với memberIds null")
    void createGroupConversationWithNullMemberIds() {
        // Thiết lập dữ liệu test
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.createGroupConversation(
                groupId, groupName, userId, null);

        // Kiểm tra - chỉ có creator được thêm vào nhóm
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Kiểm tra conversation entity được tạo với thông tin đúng")
    void verifyConversationEntityCreationWithCorrectInfo() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        conversationService.createGroupConversation(groupId, groupName, userId, memberIds);

        // Verify conversation entity được save với thông tin đúng
        verify(conversationRepository).save(argThat(conv ->
                conv.getName().equals(groupName) &&
                        conv.getType() == ConversationType.GROUP &&
                        conv.getCreatedByUser().getId().equals(userId)
        ));
    }

    @Test
    @DisplayName("Kiểm tra unread count được tạo cho tất cả thành viên")
    void verifyUnreadCountCreatedForAllMembers() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Collections.singletonList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(List.of());
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        conversationService.createGroupConversation(groupId, groupName, userId, memberIds);

        // Verify unread count được tạo cho tất cả thành viên
        verify(unreadCountRepository).saveAll(argThat(unreadCounts -> {
            List<UnreadCountEntity> unreadCountList = (List<UnreadCountEntity>) unreadCounts;
            return unreadCountList.size() == 2 && // 2 thành viên
                    unreadCountList.stream().allMatch(uc -> uc.getUnreadCount() == 0);
        }));
    }

    @Test
    @DisplayName("Lấy danh sách conversations của user thành công")
    void getUserConversationsSuccessfully() {
        // Setup
        Pageable pageable = Pageable.ofSize(10);
        List<ConversationEntity> conversations = Arrays.asList(conversation);
        Page<ConversationEntity> conversationPage = new PageImpl<>(conversations);

        // Sửa lại mock để khớp với implementation
        when(conversationRepository.findActiveConversationsByUserId(userId, pageable))
                .thenReturn(conversationPage);
        when(conversationMapper.entityToDto(conversation))
                .thenReturn(conversationResponseDto);
        when(userRepository.existsById(userId)).thenReturn(true);

        // Mock thêm cho conversationMemberRepository và conversationMemberMapper
        when(conversationMemberRepository.findActiveMembers(conversationId))
                .thenReturn(Collections.singletonList(userMember));
        when(conversationMemberMapper.entityToDto(userMember))
                .thenReturn(ConversationMemberDto.builder()
                        .conversationId(conversationId)
                        .role(MemberRole.ADMIN)
                        .build());

        // Execute
        Page<ConversationResponseDto> result = conversationService.getUserConversations(userId, pageable);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(conversationResponseDto, result.getContent().get(0));
        verify(conversationRepository).findActiveConversationsByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Tìm conversation theo ID thành công")
    void findConversationByIdSuccessfully() {
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));
        when(conversationMapper.entityToDto(conversation))
                .thenReturn(conversationResponseDto);

        ConversationResponseDto result = conversationService.findById(conversationId);

        assertNotNull(result);
        assertEquals(conversationResponseDto, result);
        verify(conversationRepository).findById(conversationId);
    }

    @Test
    @DisplayName("Tìm conversation không tồn tại")
    void findConversationByIdNotFound() {
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            conversationService.findById(conversationId);
        });
    }

}