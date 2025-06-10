package org.socius.sociuswebbackend.services.impl;

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
import org.socius.sociuswebbackend.mappers.ConversationMapper;
import org.socius.sociuswebbackend.mappers.ConversationMemberMapper;
import org.socius.sociuswebbackend.mappers.MessageMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
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
    private MessageRepository messageRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationMemberMapper conversationMemberMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ChatMessageProducerService chatMessageProducerService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;


    @InjectMocks
    private ConversationServiceImpl conversationService;

    private UUID userId;
    private UUID otherUserId;
    private UUID conversationId;
    private UserEntity user;
    private UserEntity otherUser;
    private ConversationEntity conversation;
    private ConversationResponseDto conversationResponseDto;
    private ConversationMemberEntity userMember;
    private MockedStatic<SecurityContextHolder> securityContextHolder;

    @BeforeEach
    void setUp() {
        try {
            if (securityContextHolder != null) {
                securityContextHolder.close();
            }
        } catch (Exception e) {
            // Ignore nếu chưa có static mock nào
        }

        // Setup SecurityContext
        securityContextHolder = mockStatic(SecurityContextHolder.class);
        securityContextHolder.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        // Initialize test data
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        // Create test entities - QUAN TRỌNG: Khởi tạo đầy đủ
        user = AuthTestDataUtil.createTestAdminUser();
        user.setId(userId);
        user.setEmail("test@example.com");

        otherUser = AuthTestDataUtil.createTestAdminUser();
        otherUser.setId(otherUserId);
        otherUser.setEmail("other@example.com");

        conversation = ChatTestDataUtil.createConversationEntity();
        conversation.setId(conversationId);
        conversation.setType(ConversationType.GROUP);
        conversation.setCreatedByUser(user); // QUAN TRỌNG: Set creator

        // Create conversation member
        userMember = new ConversationMemberEntity();
        userMember.setUser(user);
        userMember.setConversation(conversation);
        userMember.setRole(MemberRole.ADMIN);

        // Create response DTO
        conversationResponseDto = ConversationResponseDto.builder()
                .id(conversationId)
                .name("Test Conversation")
                .type(ConversationType.GROUP)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock repository calls - SỬA CHÍNH TẠI ĐÂY
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user)); // Đảm bảo user không null

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolder != null) {
            securityContextHolder.close();
            securityContextHolder = null;
        }
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm thành công")
    void createGroupConversationSuccessfully() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(otherUserId);
        String groupName = "Test Group";

        // Mock các dependencies
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.createGroupConversation(
                groupName, userId, memberIds);

        // Kiểm tra
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationRepository).flush();
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
        verify(conversationMapper).entityToDto(conversation);
    }

    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện nhóm với creator không tồn tại")
    void failToCreateGroupConversationWhenCreatorNotExists() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(otherUserId);
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () ->
                conversationService.createGroupConversation(groupName, userId, memberIds));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện nhóm với thành viên không tồn tại")
    void failToCreateGroupConversationWhenMemberNotExists() {
        // Thiết lập dữ liệu test
        UUID invalidMemberId = UUID.randomUUID();
        Set<UUID> memberIds = Set.of(invalidMemberId);
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                conversationService.createGroupConversation(groupName, userId, memberIds));

        assertTrue(exception.getMessage().contains("Không tìm thấy người dùng với ID"));
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi creatorId là null")
    void failToCreateGroupConversationWithNullCreatorId() {
        Set<UUID> memberIds = Set.of(otherUserId);
        String groupName = "Test Group";

        assertThrows(RuntimeException.class, () ->
                conversationService.createGroupConversation(groupName, null, memberIds));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với memberIds null")
    void createGroupConversationWithNullMemberIds() {
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        ConversationResponseDto result = conversationService.createGroupConversation(
                groupName, userId, null);

        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm với creator trong danh sách members")
    void createGroupConversationWithCreatorInMemberList() {
        Set<UUID> memberIds = Set.of(userId, otherUserId); // userId là creator và cũng trong memberIds
        String groupName = "Test Group";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        ConversationResponseDto result = conversationService.createGroupConversation(
                groupName, userId, memberIds);

        assertNotNull(result);

        // Verify chỉ có 2 members được tạo (Creator + 1 other member, không duplicate creator)
        verify(conversationMemberRepository).saveAll(argThat(members -> {
            List<ConversationMemberEntity> memberList = (List<ConversationMemberEntity>) members;
            return memberList.size() == 2;
        }));
    }

    @Test
    @DisplayName("Lấy danh sách conversations của user thành công")
    void getUserConversationsSuccessfully() {
        Pageable pageable = Pageable.ofSize(10);
        List<ConversationEntity> conversations = Arrays.asList(conversation);
        Page<ConversationEntity> conversationPage = new PageImpl<>(conversations);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(conversationRepository.findActiveConversationsByUserId(userId, pageable))
                .thenReturn(conversationPage);
        when(conversationMapper.entityToDto(conversation))
                .thenReturn(conversationResponseDto);
        when(conversationMemberRepository.findActiveMembers(conversationId))
                .thenReturn(Collections.singletonList(userMember));
        when(conversationMemberMapper.entityToDto(userMember))
                .thenReturn(ConversationMemberDto.builder()
                        .conversationId(conversationId)
                        .role(MemberRole.ADMIN)
                        .build());

        Page<ConversationResponseDto> result = conversationService.getUserConversations(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(conversationRepository).findActiveConversationsByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Lỗi khi lấy conversations của user không tồn tại")
    void failToGetUserConversationsWhenUserNotExists() {
        Pageable pageable = Pageable.ofSize(10);

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                conversationService.getUserConversations(pageable));
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

        assertThrows(RuntimeException.class, () ->
                conversationService.findById(conversationId));
    }

    @Test
    @DisplayName("Tạo hoặc lấy cuộc trò chuyện trực tiếp thành công")
    void getOrCreateDirectConversationSuccessfully() {
        when(conversationRepository.findDirectConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        ConversationEntity directConversation = new ConversationEntity();
        directConversation.setId(conversationId);
        directConversation.setType(ConversationType.DIRECT);
        directConversation.setCreatedByUser(user);

        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(directConversation);
        when(conversationMemberRepository.findActiveMember(conversationId, userId))
                .thenReturn(Optional.of(userMember));
        when(conversationMapper.entityToDto(any(ConversationEntity.class)))
                .thenReturn(conversationResponseDto);

        ConversationResponseDto result = conversationService.getOrCreateDirectConversation(otherUserId);

        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện trực tiếp với chính mình")
    void failToCreateDirectConversationWithSameUser() {
        assertThrows(IllegalArgumentException.class, () ->
                conversationService.getOrCreateDirectConversation(userId));
    }

    @Test
    @DisplayName("Thêm thành viên vào cuộc trò chuyện nhóm thành công")
    void addMemberToGroupConversationSuccessfully() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationMemberRepository.findById(any(ConversationMemberId.class)))
                .thenReturn(Optional.empty());

        MessageEntity systemMessage = new MessageEntity();
        systemMessage.setId(UUID.randomUUID());
        systemMessage.setContent("Test system message");
        systemMessage.setMessageType(MessageType.SYSTEM);

        when(messageRepository.save(any(MessageEntity.class))).thenReturn(systemMessage);
        when(messageMapper.entityToDto(any(MessageEntity.class)))
                .thenReturn(MessageResponseDto.builder().build());

        conversationService.addMember(conversationId, otherUserId);

        verify(conversationMemberRepository).save(any(ConversationMemberEntity.class));
        verify(unreadCountRepository).save(any(UnreadCountEntity.class));
        verify(messageRepository).save(any(MessageEntity.class));
        verify(chatMessageProducerService).sendChatMessage(any(), eq(ConversationType.GROUP), eq(conversationId));
    }

    @Test
    @DisplayName("Xóa thành viên khỏi cuộc trò chuyện nhóm thành công")
    void removeMemberFromGroupConversationSuccessfully() {
        ConversationMemberEntity memberToRemove = new ConversationMemberEntity();
        memberToRemove.setUser(otherUser);
        memberToRemove.setConversation(conversation);
        memberToRemove.setRole(MemberRole.MEMBER);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(any(ConversationMemberId.class)))
                .thenReturn(Optional.of(memberToRemove));

        MessageEntity systemMessage = new MessageEntity();
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(systemMessage);
        when(messageMapper.entityToDto(any(MessageEntity.class)))
                .thenReturn(MessageResponseDto.builder().build());

        conversationService.removeMember(conversationId, otherUserId);

        verify(conversationMemberRepository).save(argThat(member ->
                member.getLeftAt() != null));
        verify(messageRepository).save(any(MessageEntity.class));
        verify(chatMessageProducerService).sendChatMessage(any(), eq(ConversationType.GROUP), eq(conversationId));
    }

    @Test
    @DisplayName("Lấy danh sách thành viên cuộc trò chuyện thành công")
    void getConversationMembersSuccessfully() {
        ConversationMemberEntity requesterMember = new ConversationMemberEntity();
        requesterMember.setUser(user);
        requesterMember.setConversation(conversation);
        requesterMember.setRole(MemberRole.ADMIN);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(any(ConversationMemberId.class)))
                .thenReturn(Optional.of(requesterMember));
        when(conversationMemberRepository.findActiveMembers(conversationId))
                .thenReturn(Arrays.asList(requesterMember, userMember));
        when(conversationMemberMapper.entityToDto(any(ConversationMemberEntity.class)))
                .thenReturn(ConversationMemberDto.builder().build());

        List<ConversationMemberDto> result = conversationService.getConversationMembers(conversationId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(conversationMemberRepository).findActiveMembers(conversationId);
    }

    @Test
    @DisplayName("Lấy tất cả cuộc trò chuyện của user thành công")
    void getAllUserConversationsSuccessfully() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(conversationRepository.findAllActiveConversationsByUserId(userId))
                .thenReturn(Arrays.asList(conversation));
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);
        when(conversationMemberRepository.findActiveMembers(conversationId))
                .thenReturn(Collections.singletonList(userMember));
        when(conversationMemberMapper.entityToDto(userMember))
                .thenReturn(ConversationMemberDto.builder().build());

        List<ConversationResponseDto> result = conversationService.getAllUserConversations();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conversationRepository).findAllActiveConversationsByUserId(userId);
    }

    @Test
    @DisplayName("Xóa cuộc trò chuyện nhóm thành công")
    void deleteGroupConversationSuccessfully() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        conversationService.deleteGroupConversation(conversationId);

        verify(messageRepository).deleteByConversation_Id(conversationId);
        verify(unreadCountRepository).deleteByConversation_Id(conversationId);
        verify(conversationMemberRepository).deleteByConversation_Id(conversationId);
        verify(conversationRepository).delete(conversation);
    }

    @Test
    @DisplayName("Lỗi khi xóa cuộc trò chuyện không phải nhóm")
    void failToDeleteNonGroupConversation() {
        ConversationEntity directConversation = new ConversationEntity();
        directConversation.setType(ConversationType.DIRECT);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversation));

        assertThrows(RuntimeException.class, () ->
                conversationService.deleteGroupConversation(conversationId));
    }

    @Test
    @DisplayName("Cập nhật tên group conversation thành công")
    void updateConversationNameSuccessfully() {
        // Given
        UUID conversationId = UUID.randomUUID();
        String oldName = "Old Group Name";
        String newName = "New Group Name";

        ConversationEntity conversation = ConversationEntity.builder()
                .id(conversationId)
                .name(oldName)
                .type(ConversationType.GROUP)
                .build();

        MessageEntity systemMessage = MessageEntity.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .content(String.format("Tên nhóm đã được thay đổi từ '%s' thành '%s'", oldName, newName))
                .messageType(MessageType.SYSTEM)
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(systemMessage);
        when(messageMapper.entityToDto(any(MessageEntity.class))).thenReturn(MessageResponseDto.builder().build());

        // When
        conversationService.updateConversationName(conversationId, newName);

        // Then
        assertEquals(newName, conversation.getName());
        verify(conversationRepository).save(conversation);
        verify(messageRepository).save(any(MessageEntity.class));
        verify(chatMessageProducerService).sendChatMessage(any(), eq(ConversationType.GROUP), eq(conversationId));
    }

    @Test
    @DisplayName("Lỗi khi cập nhật tên direct conversation")
    void failToUpdateDirectConversationName() {
        // Given
        UUID conversationId = UUID.randomUUID();
        ConversationEntity conversation = ConversationEntity.builder()
                .id(conversationId)
                .type(ConversationType.DIRECT)
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> conversationService.updateConversationName(conversationId, "New Name")
        );

        assertEquals("Chỉ có thể cập nhật tên cho group conversation", exception.getMessage());
        verify(conversationRepository, never()).save(any());
    }
}