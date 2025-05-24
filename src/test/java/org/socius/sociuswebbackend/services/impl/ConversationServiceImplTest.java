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
import org.socius.sociuswebbackend.mappers.MessageMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.socius.sociuswebbackend.utils.ChatTestDataUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private MessageRepository messageRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ChatMessageProducerService chatMessageProducerService;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private UUID userId;
    private UUID otherUserId;
    private UUID conversationId;
    private UserEntity user;
    private UserEntity otherUser;
    private ConversationEntity conversation;
    private ConversationRequestDto conversationRequestDto;
    private ConversationResponseDto conversationResponseDto;
    private ConversationMemberEntity userMember;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        user = AuthTestDataUtil.createTestAdminUser();
        user.setId(userId);

        otherUser = AuthTestDataUtil.createTestRegularUser();
        otherUser.setId(otherUserId);

        conversation = ChatTestDataUtil.createConversationEntity();
        conversation.setId(conversationId);
        conversation.setType(ConversationType.GROUP);

        conversationRequestDto = ChatTestDataUtil.createConversationRequestDto();
        conversationResponseDto = ChatTestDataUtil.createConversationResponseDto();

        userMember = ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversationId, userId))
                .conversation(conversation)
                .user(user)
                .role(MemberRole.ADMIN)
                .leftAt(null)
                .build();
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện nhóm thành công")
    void createGroupConversationSuccessfully() {
        // Thiết lập dữ liệu test
        Set<UUID> memberIds = Set.of(userId, otherUserId);
        conversationRequestDto.setMemberIds(memberIds);
        conversationRequestDto.setType(ConversationType.GROUP);

        // Mock các dependencies
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationMapper.requestDtoToEntity(conversationRequestDto)).thenReturn(conversation);
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Arrays.asList(userMember));
        when(unreadCountRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // Mock getConversation method dependencies
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(true);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);
        when(unreadCountRepository.findByIdConversationIdAndIdUserId(conversationId, userId))
                .thenReturn(Optional.empty());

        // Thực thi
        ConversationResponseDto result = conversationService.createConversation(userId, conversationRequestDto);

        // Kiểm tra
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Tạo cuộc trò chuyện trực tiếp thành công")
    void createDirectConversationSuccessfully() {
        // Thiết lập dữ liệu test
        conversation.setType(ConversationType.DIRECT);

        // Mock dependencies
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(conversationRepository.findDirectConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(ConversationEntity.class))).thenReturn(conversation);
        when(conversationMemberRepository.saveAll(anyList())).thenReturn(Arrays.asList());
        when(unreadCountRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // Mock getConversation method dependencies
        when(conversationRepository.findById(any(UUID.class))).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(any(UUID.class), eq(userId)))
                .thenReturn(true);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.getOrCreateDirectConversation(userId, otherUserId);

        // Kiểm tra
        assertNotNull(result);
        verify(conversationRepository).save(any(ConversationEntity.class));
        verify(conversationMemberRepository).saveAll(anyList());
        verify(unreadCountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Trả về cuộc trò chuyện trực tiếp đã tồn tại")
    void returnExistingDirectConversation() {
        // Thiết lập dữ liệu test
        when(conversationRepository.findDirectConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.of(conversation));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(true);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);

        // Thực thi
        ConversationResponseDto result = conversationService.getOrCreateDirectConversation(userId, otherUserId);

        // Kiểm tra
        assertNotNull(result);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lấy danh sách cuộc trò chuyện của người dùng")
    void getUserConversationsSuccessfully() {
        // Thiết lập dữ liệu test
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversationEntity> conversationPage = new PageImpl<>(Arrays.asList(conversation));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(conversationRepository.findConversationsByUserId(eq(userId), eq(pageable)))
                .thenReturn(conversationPage);
        when(conversationMapper.entityToDto(conversation)).thenReturn(conversationResponseDto);
        when(unreadCountRepository.findByIdConversationIdAndIdUserId(conversationId, userId))
                .thenReturn(Optional.empty());

        // Thực thi
        Page<ConversationResponseDto> result = conversationService.getUserConversations(userId, pageable);

        // Kiểm tra
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(conversationResponseDto, result.getContent().get(0));
    }

    @Test
    @DisplayName("Lỗi khi user không tồn tại để lấy conversations")
    void failWhenUserNotExistsForGetConversations() {
        // Thiết lập dữ liệu test
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(userId)).thenReturn(false);

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.getUserConversations(userId, pageable);
        });

        verify(conversationRepository, never()).findConversationsByUserId(any(), any());
    }

    @Test
    @DisplayName("Thêm thành viên vào cuộc trò chuyện nhóm thành công")
    void addMembersToGroupConversationSuccessfully() {
        // Thiết lập dữ liệu test
        UUID newMemberId = UUID.randomUUID();
        Set<UUID> newMemberIds = Set.of(newMemberId);
        UserEntity newMember = AuthTestDataUtil.createTestRegularUser();
        newMember.setId(newMemberId);

        conversation.setType(ConversationType.GROUP);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)))
                .thenReturn(Optional.of(userMember));
        when(userRepository.findById(newMemberId)).thenReturn(Optional.of(newMember));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, newMemberId)))
                .thenReturn(Optional.empty());
        when(conversationMemberRepository.save(any(ConversationMemberEntity.class)))
                .thenReturn(new ConversationMemberEntity());
        when(unreadCountRepository.save(any(UnreadCountEntity.class)))
                .thenReturn(new UnreadCountEntity());
        when(messageRepository.save(any(MessageEntity.class)))
                .thenReturn(new MessageEntity());

        // Thực thi
        conversationService.addMembers(userId, conversationId, newMemberIds);

        // Kiểm tra
        verify(conversationMemberRepository).save(any(ConversationMemberEntity.class));
        verify(unreadCountRepository).save(any(UnreadCountEntity.class));
        verify(messageRepository).save(any(MessageEntity.class));
    }

    @Test
    @DisplayName("Lỗi khi thêm thành viên vào cuộc trò chuyện trực tiếp")
    void failToAddMembersToDirectConversation() {
        // Thiết lập dữ liệu test
        Set<UUID> newMemberIds = Set.of(UUID.randomUUID());
        conversation.setType(ConversationType.DIRECT);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)))
                .thenReturn(Optional.of(userMember));

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.addMembers(userId, conversationId, newMemberIds);
        });

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi user không phải admin để thêm thành viên")
    void failToAddMembersWhenNotAdmin() {
        // Thiết lập dữ liệu test
        Set<UUID> newMemberIds = Set.of(UUID.randomUUID());
        userMember.setRole(MemberRole.MEMBER); // Không phải admin

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)))
                .thenReturn(Optional.of(userMember));

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.addMembers(userId, conversationId, newMemberIds);
        });

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rời khỏi cuộc trò chuyện thành công")
    void leaveConversationSuccessfully() {
        // Thiết lập dữ liệu test
        conversation.setType(ConversationType.GROUP);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)))
                .thenReturn(Optional.of(userMember));
        when(conversationMemberRepository.save(any(ConversationMemberEntity.class)))
                .thenReturn(userMember);

        // Thực thi
        conversationService.leaveConversation(userId, conversationId);

        // Kiểm tra
        assertNotNull(userMember.getLeftAt());
        verify(conversationMemberRepository).save(userMember);
    }

    @Test
    @DisplayName("Lỗi khi rời khỏi cuộc trò chuyện trực tiếp")
    void failToLeaveDirectConversation() {
        // Thiết lập dữ liệu test
        conversation.setType(ConversationType.DIRECT);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)))
                .thenReturn(Optional.of(userMember));

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.leaveConversation(userId, conversationId);
        });

        verify(conversationMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lỗi khi conversation không tồn tại")
    void failWhenConversationNotExists() {
        // Thiết lập mock
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.getConversation(userId, conversationId);
        });
    }

    @Test
    @DisplayName("Lỗi khi user không phải thành viên")
    void failWhenUserNotMember() {
        // Thiết lập mock
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(conversationId, userId))
                .thenReturn(false);

        // Thực thi và kiểm tra
        assertThrows(RuntimeException.class, () -> {
            conversationService.getConversation(userId, conversationId);
        });
    }

    @Test
    @DisplayName("Lỗi khi tạo cuộc trò chuyện với chính mình")
    void failToCreateConversationWithSelf() {
        // Thực thi và kiểm tra
        assertThrows(IllegalArgumentException.class, () -> {
            conversationService.getOrCreateDirectConversation(userId, userId);
        });
    }
}