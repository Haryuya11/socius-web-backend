package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.ConversationMapper;
import org.socius.sociuswebbackend.mappers.MessageMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.ConversationType;
import org.socius.sociuswebbackend.model.enums.MemberRole;
import org.socius.sociuswebbackend.model.enums.MessageType;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ChatMessageProducerService;
import org.socius.sociuswebbackend.services.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    final private ConversationRepository conversationRepository;
    final private UserRepository userRepository;
    final private ConversationMemberRepository conversationMemberRepository;
    final private UnreadCountRepository unreadCountRepository;
    final private ConversationMapper conversationMapper;
    final private MessageRepository messageRepository;
    final private MessageMapper messageMapper;
    final private ChatMessageProducerService chatMessageProducerService;

    @Override
    @Transactional
    public ConversationResponseDto createConversation(UUID creatorId, ConversationRequestDto requestDto) {
        // Kiểm tra người tạo có tồn tại không
        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + creatorId));

        // Tạo cuộc trò chuyện mới
        ConversationEntity conversation = conversationMapper.requestDtoToEntity(requestDto);
        conversation.setCreatedBy(creator);
        conversation = conversationRepository.save(conversation);

        // Thêm người tạo vào cuộc trò chuyện với vai trò là admin
        ConversationMemberEntity creatorMember = ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversation.getId(), creatorId))
                .conversation(conversation)
                .user(creator)
                .role(MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        List<ConversationMemberEntity> members = new ArrayList<>();
        members.add(creatorMember);

        // Thêm các thành viên khác
        for (UUID memberId : requestDto.getMemberIds()) {
            if (memberId.equals(creatorId)) continue; // Bỏ qua người tạo vì đã thêm

            UserEntity member = userRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + memberId));

            ConversationMemberEntity memberEntity = ConversationMemberEntity.builder()
                    .id(new ConversationMemberId(conversation.getId(), memberId))
                    .conversation(conversation)
                    .user(member)
                    .role(MemberRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build();

            members.add(memberEntity);
        }

        // Lưu danh sách thành viên vào cơ sở dữ liệu
        conversationMemberRepository.saveAll(members);

        // Tạo và lưu UnreadCount cho từng thành viên
        ConversationEntity finalConversation = conversation;
        List<UnreadCountEntity> unreadCounts = members.stream()
                .map(member -> UnreadCountEntity.builder()
                        .id(new UnreadCountId(finalConversation.getId(), member.getId().getUserId()))
                        .conversation(finalConversation)
                        .user(member.getUser())
                        .unreadCount(0)
                        .build())
                .toList();

        unreadCountRepository.saveAll(unreadCounts);

        return getConversation(creatorId, conversation.getId());
    }

    @Override
    public Page<ConversationResponseDto> getUserConversations(UUID userId, Pageable pageable) {
        // Kiểm tra người dùng có tồn tại không
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
        }

        // Lấy danh sách cuộc trò chuyện của người dùng theo phân trang
        Page<ConversationEntity> conversations = conversationRepository.findConversationsByUserId(userId, pageable);

        // Chuyển đổi danh sách cuộc trò chuyện thành DTO
        return conversations.map(conversation -> {
            ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

            // Cập nhật số tin nhắn chưa đọc
            Optional<UnreadCountEntity> unreadCount = unreadCountRepository
                    .findByIdConversationIdAndIdUserId(conversation.getId(), userId);
            unreadCount.ifPresent(count -> dto.setUnreadCount(count.getUnreadCount()));

            // Nếu là cuộc trò chuyện trực tiếp, cập nhật tên và ảnh
            if (conversation.getType() == ConversationType.DIRECT) {
                Optional<ConversationMemberEntity> otherMember = conversation.getMembers().stream()
                        .filter(member -> !member.getId().getUserId().equals(userId) && member.getLeftAt() == null)
                        .findFirst();

                if (otherMember.isPresent()) {
                    UserEntity otherUser = otherMember.get().getUser();
                    dto.setName(otherUser.getFirstName() + " " + otherUser.getLastName());
                    dto.setImageUrl(otherUser.getImageUrl());
                }
            }

            return dto;
        });
    }

    @Override
    public ConversationResponseDto getConversation(UUID userId, UUID conversationId) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người dùng có phải là thành viên không
        boolean isMember = conversationMemberRepository.existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(conversationId, userId);
        if (!isMember) {
            throw new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện");
        }

        ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

        // Cập nhật số tin nhắn chưa đọc
        Optional<UnreadCountEntity> unreadCount = unreadCountRepository
                .findByIdConversationIdAndIdUserId(conversationId, userId);
        unreadCount.ifPresent(count -> dto.setUnreadCount(count.getUnreadCount()));

        // Nếu là cuộc trò chuyện trực tiếp, cập nhật tên và ảnh
        if (conversation.getType() == ConversationType.DIRECT) {
            Optional<ConversationMemberEntity> otherMember = conversation.getMembers().stream()
                    .filter(member -> !member.getId().getUserId().equals(userId) && member.getLeftAt() == null)
                    .findFirst();

            if (otherMember.isPresent()) {
                UserEntity otherUser = otherMember.get().getUser();
                dto.setName(otherUser.getFirstName() + " " + otherUser.getLastName());
                dto.setImageUrl(otherUser.getImageUrl());
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public ConversationResponseDto addMember(UUID userId, UUID conversationId, UUID memberId) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người thêm có phải là admin của cuộc trò chuyện không
        ConversationMemberEntity userMember = conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        if (userMember.getRole() != MemberRole.ADMIN || userMember.getLeftAt() != null) {
            throw new RuntimeException("Người dùng không có quyền thêm thành viên");
        }

        // Kiểm tra nếu là cuộc trò chuyện trực tiếp
        if (conversation.getType() == ConversationType.DIRECT) {
            throw new RuntimeException("Không thể thêm thành viên vào cuộc trò chuyện trực tiếp");
        }

        // Kiểm tra thành viên cần thêm có tồn tại không
        UserEntity member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + memberId));

        // Kiểm tra xem thành viên đã trong cuộc trò chuyện chưa
        Optional<ConversationMemberEntity> existingMember = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, memberId));

        if (existingMember.isPresent()) {
            // Nếu đã rời đi trước đó, cập nhật lại
            if (existingMember.get().getLeftAt() != null) {
                existingMember.get().setLeftAt(null);
                existingMember.get().setJoinedAt(LocalDateTime.now());
                conversationMemberRepository.save(existingMember.get());
            } else {
                throw new RuntimeException("Người dùng đã là thành viên của cuộc trò chuyện");
            }
        } else {
            // Thêm thành viên mới
            ConversationMemberEntity newMember = ConversationMemberEntity.builder()
                    .id(new ConversationMemberId(conversationId, memberId))
                    .conversation(conversation)
                    .user(member)
                    .role(MemberRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build();

            conversationMemberRepository.save(newMember);

            // Khởi tạo unread count cho thành viên mới
            UnreadCountEntity unreadCount = UnreadCountEntity.builder()
                    .id(new UnreadCountId(conversationId, memberId))
                    .conversation(conversation)
                    .user(member)
                    .unreadCount(0)
                    .build();

            unreadCountRepository.save(unreadCount);

            String content = member.getFirstName() + " " + member.getLastName() + " đã tham gia cuộc trò chuyện";
            createSystemMessage(conversation, content);
        }

        return getConversation(userId, conversationId);
    }

    @Override
    @Transactional
    public ConversationResponseDto removeMember(UUID userId, UUID conversationId, UUID memberId) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người xóa có phải là admin của cuộc trò chuyện không
        ConversationMemberEntity userMember = conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        if (userMember.getRole() != MemberRole.ADMIN || userMember.getLeftAt() != null) {
            throw new RuntimeException("Người dùng không có quyền xóa thành viên");
        }

        // Kiểm tra nếu là cuộc trò chuyện trực tiếp
        if (conversation.getType() == ConversationType.DIRECT) {
            throw new RuntimeException("Không thể xóa thành viên khỏi cuộc trò chuyện trực tiếp");
        }

        // Không thể xóa chính mình
        if (userId.equals(memberId)) {
            throw new RuntimeException("Không thể xóa chính mình khỏi cuộc trò chuyện");
        }

        // Kiểm tra thành viên cần xóa
        ConversationMemberEntity memberToRemove = conversationMemberRepository.findById(new ConversationMemberId(conversationId, memberId))
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong cuộc trò chuyện"));

        if (memberToRemove.getLeftAt() != null) {
            throw new RuntimeException("Thành viên này đã rời khỏi cuộc trò chuyện");
        }

        // Đánh dấu thành viên đã rời đi
        memberToRemove.setLeftAt(LocalDateTime.now());
        conversationMemberRepository.save(memberToRemove);

        String content = memberToRemove.getUser().getFirstName() + " " + memberToRemove.getUser().getLastName() + " đã rời khỏi cuộc trò chuyện";
        createSystemMessage(conversation, content);

        return getConversation(userId, conversationId);
    }

    @Override
    @Transactional
    public ConversationResponseDto getOrCreateDirectConversation(UUID userId1, UUID userId2) {
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("Không thể tạo cuộc trò chuyện với chính mình");
        }

        // Kiểm tra xem đã có cuộc trò chuyện trực tiếp giữa 2 người dùng này chưa
        Optional<ConversationEntity> existingConversation = conversationRepository.findDirectConversationBetweenUsers(userId1, userId2);

        if (existingConversation.isPresent()) {
            return getConversation(userId1, existingConversation.get().getId());
        }

        // Lấy thông tin người dùng
        UserEntity user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId1));

        UserEntity user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId2));

        // Tạo cuộc trò chuyện mới
        ConversationEntity conversation = ConversationEntity.builder()
                .name(user2.getFirstName() + " " + user2.getLastName()) // Đặt tên là tên của người đối diện
                .type(ConversationType.DIRECT)
                .createdBy(user1)
                .build();

        conversation = conversationRepository.save(conversation);

        // Thêm cả 2 người dùng vào cuộc trò chuyện
        ConversationMemberEntity member1 = ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversation.getId(), userId1))
                .conversation(conversation)
                .user(user1)
                .role(MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        ConversationMemberEntity member2 = ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversation.getId(), userId2))
                .conversation(conversation)
                .user(user2)
                .role(MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        conversationMemberRepository.saveAll(Arrays.asList(member1, member2));

        // Khởi tạo unread counts
        UnreadCountEntity unreadCount1 = UnreadCountEntity.builder()
                .id(new UnreadCountId(conversation.getId(), userId1))
                .conversation(conversation)
                .user(user1)
                .unreadCount(0)
                .build();

        UnreadCountEntity unreadCount2 = UnreadCountEntity.builder()
                .id(new UnreadCountId(conversation.getId(), userId2))
                .conversation(conversation)
                .user(user2)
                .unreadCount(0)
                .build();

        unreadCountRepository.saveAll(Arrays.asList(unreadCount1, unreadCount2));

        return getConversation(userId1, conversation.getId());
    }

    @Override
    @Transactional
    public ConversationResponseDto updateConversation(UUID userId, UUID conversationId, ConversationRequestDto requestDto) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người dùng có quyền cập nhật không
        ConversationMemberEntity member = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        if (member.getRole() != MemberRole.ADMIN || member.getLeftAt() != null) {
            throw new RuntimeException("Người dùng không có quyền cập nhật cuộc trò chuyện");
        }

        // Chỉ cho phép cập nhật cuộc trò chuyện nhóm
        if (conversation.getType() == ConversationType.DIRECT) {
            throw new RuntimeException("Không thể cập nhật thông tin cuộc trò chuyện trực tiếp");
        }

        // Cập nhật thông tin
        if (requestDto.getName() != null && !requestDto.getName().trim().isEmpty()) {
            conversation.setName(requestDto.getName());
        }
        // Không cập nhật type và memberIds vì có thể gây xung đột

        conversationRepository.save(conversation);

        return getConversation(userId, conversationId);
    }

    @Override
    @Transactional
    public boolean leaveConversation(UUID userId, UUID conversationId) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người dùng có phải là thành viên không
        ConversationMemberEntity member = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, userId))
                .orElseThrow(() -> new RuntimeException("Người dùng không phải là thành viên của cuộc trò chuyện"));

        if (member.getLeftAt() != null) {
            throw new RuntimeException("Người dùng đã rời khỏi cuộc trò chuyện này");
        }

        // Không cho phép rời cuộc trò chuyện trực tiếp
        if (conversation.getType() == ConversationType.DIRECT) {
            throw new RuntimeException("Không thể rời khỏi cuộc trò chuyện trực tiếp");
        }

        // Nếu là admin và là admin duy nhất, cần chỉ định admin mới
        if (member.getRole() == MemberRole.ADMIN) {
            long adminCount = conversation.getMembers().stream()
                    .filter(m -> m.getRole() == MemberRole.ADMIN && m.getLeftAt() == null
                            && !m.getId().getUserId().equals(userId))
                    .count();

            if (adminCount == 0) {
                // Tìm thành viên khác để làm admin
                Optional<ConversationMemberEntity> newAdmin = conversation.getMembers().stream()
                        .filter(m -> m.getLeftAt() == null && !m.getId().getUserId().equals(userId))
                        .findFirst();

                if (newAdmin.isPresent()) {
                    newAdmin.get().setRole(MemberRole.ADMIN);
                    conversationMemberRepository.save(newAdmin.get());
                }
            }
        }

        // Đánh dấu thành viên đã rời đi
        member.setLeftAt(LocalDateTime.now());
        conversationMemberRepository.save(member);
        return true;
    }

    private void createSystemMessage(ConversationEntity conversation, String content) {
        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .content(content)
                .messageType(MessageType.AUDIO) // Thêm SYSTEM vào enum MessageType
                .build();
        messageRepository.save(message);

        // Gửi tin nhắn qua RabbitMQ
        MessageResponseDto messageDto = messageMapper.entityToDto(message);
        chatMessageProducerService.sendChatMessage(messageDto, conversation.getType(), conversation.getId());
    }
}
