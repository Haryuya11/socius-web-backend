package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.socius.sociuswebbackend.services.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);
    final private ConversationRepository conversationRepository;
    final private UserRepository userRepository;
    final private ConversationMemberRepository conversationMemberRepository;
    final private UnreadCountRepository unreadCountRepository;
    final private ConversationMapper conversationMapper;
    final private MessageRepository messageRepository;
    final private MessageMapper messageMapper;
    final private ChatMessageProducerService chatMessageProducerService;
    final private ConversationMemberMapper conversationMemberMapper;

    @Override
    @Transactional
    public ConversationResponseDto createGroupConversation(String name, UUID creatorId, Set<UUID> memberIds) {

        if (creatorId == null) {
            throw new RuntimeException("Creator ID không thể null");
        }

        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + creatorId));

        Set<UserEntity> validMembers = new HashSet<>();
        validMembers.add(creator);

        if (memberIds != null && !memberIds.isEmpty()) {
            for (UUID memberId : memberIds) {
                if (!memberId.equals(creatorId)) {
                    UserEntity member = userRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + memberId));
                    validMembers.add(member);
                }
            }
        }

        ConversationEntity conversation = ConversationEntity.builder()
                .name(name)
                .type(ConversationType.GROUP)
                .createdByUser(creator)
                .build();

        conversation = conversationRepository.save(conversation);
        conversationRepository.flush();

        List<ConversationMemberEntity> members = new ArrayList<>();
        members.add(createMemberEntity(conversation, creator, MemberRole.ADMIN));

        for (UserEntity member : validMembers) {
            if (!member.getId().equals(creatorId)) {
                members.add(createMemberEntity(conversation, member, MemberRole.MEMBER));
            }
        }

        conversationMemberRepository.saveAll(members);
        ConversationEntity finalConversation = conversation;
        List<UnreadCountEntity> unreadCounts = members.stream()
                .map(member -> createUnreadCountEntity(finalConversation, member.getUser()))
                .toList();

        unreadCountRepository.saveAll(unreadCounts);

        return conversationMapper.entityToDto(conversation);
    }

    @Override
    @Transactional
    public void deleteGroupConversation(UUID conversationId) {
        logger.info("Xóa cuộc trò chuyện nhóm với ID: {}", conversationId);

        try {
            Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);
            if (conversationOpt.isPresent()) {
                ConversationEntity conversationEntity = conversationOpt.get();

                if (conversationEntity.getType() != ConversationType.GROUP) {
                    throw new RuntimeException("Chức năng này chỉ áp dụng cho cuộc trò chuyện nhóm");
                }

                messageRepository.deleteByConversation_Id(conversationId);
                unreadCountRepository.deleteByConversation_Id(conversationId);
                conversationMemberRepository.deleteByConversation_Id(conversationId);
                conversationRepository.delete(conversationEntity);

                logger.info("Đã xóa cuộc trò chuyện nhóm với ID: {}", conversationId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xóa cuộc trò chuyện nhóm: {}", e.getMessage());
            throw new RuntimeException("Không thể xóa cuộc trò chuyện nhóm: " + e.getMessage());
        }
    }

    @Override
    public Page<ConversationResponseDto> getUserConversations(Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        UUID userId = user.getId();

        // Kiểm tra người dùng có tồn tại không
        if (!userRepository.existsById(userId)) {
            logger.info("Không tìm thấy người dùng với ID: {}", userId);
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
        }

        Page<ConversationEntity> conversations = conversationRepository
                .findActiveConversationsByUserId(userId, pageable);

        return conversations.map(conversation -> {
            ConversationResponseDto dto = conversationMapper.entityToDto(conversation);

            // Chỉ lấy active members
            List<ConversationMemberEntity> activeMembers = conversationMemberRepository
                    .findActiveMembers(conversation.getId());
            dto.setMembers(activeMembers.stream()
                    .map(conversationMemberMapper::entityToDto)
                    .collect(Collectors.toSet()));
            return dto;
        });
    }


    @Override
    @Transactional
    public void addMember(UUID conversationId, UUID memberId) {
        ConversationEntity conversation = getGroupConversation(conversationId);

        UserEntity member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + memberId));

        Optional<ConversationMemberEntity> existingMemberOpt = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, memberId));

        if (existingMemberOpt.isPresent()) {
            throw new RuntimeException("Người dùng đã là thành viên của cuộc trò chuyện");
        } else {
            // Thêm thành viên mới
            ConversationMemberEntity newMember = createMemberEntity(conversation, member, MemberRole.MEMBER);
            conversationMemberRepository.save(newMember);

            // Khởi tạo unread count
            UnreadCountEntity unreadCount = createUnreadCountEntity(conversation, member);
            unreadCountRepository.save(unreadCount);
        }

        String content = member.getFullName() + " đã tham gia cuộc trò chuyện";
        createSystemMessage(conversation, content);
    }

    @Override
    @Transactional
    public void addMembers(UUID conversationId, Set<UUID> memberIds) {
        for (UUID memberId : memberIds) {
            addMember(conversationId, memberId);
        }
    }

    @Override
    @Transactional
    public void removeMembers(UUID conversationId, Set<UUID> memberIds) {
        for (UUID memberId : memberIds) {
            removeMember(conversationId, memberId);

        }
    }

    @Override
    @Transactional
    public void removeMember(UUID conversationId, UUID memberId) {

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new RuntimeException("Chỉ có thể xóa thành viên khỏi cuộc trò chuyện nhóm");
        }

        ConversationMemberEntity memberToRemove = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, memberId))
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong cuộc trò chuyện"));

        if (memberToRemove.getLeftAt() != null) {
            throw new RuntimeException("Thành viên này đã rời khỏi cuộc trò chuyện");
        }

        // Đánh dấu thành viên đã rời đi
        memberToRemove.setLeftAt(LocalDateTime.now());
        conversationMemberRepository.save(memberToRemove);

        // Tạo system message
        String content = memberToRemove.getUser().getFirstName() + " " +
                memberToRemove.getUser().getLastName() + " đã rời khỏi cuộc trò chuyện";
        createSystemMessage(conversation, content);
    }

    @Override
    @Transactional
    public ConversationResponseDto getOrCreateDirectConversation(UUID otherUserId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            UserEntity user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
            UUID userId = user.getId();

            if (userId.equals(otherUserId)) {
                throw new IllegalArgumentException("Không thể tạo cuộc trò chuyện với chính mình");
            }

            // Kiểm tra xem đã có cuộc trò chuyện trực tiếp giữa 2 người dùng này chưa
            Optional<ConversationEntity> existingConversation = conversationRepository.findDirectConversationBetweenUsers(userId, otherUserId);

            if (existingConversation.isPresent()) {
                logger.info("Đã tìm thấy cuộc trò chuyện trực tiếp giữa {} và {}", userId, otherUserId);
                return conversationMapper.entityToDto(existingConversation.get());
            }

            UserEntity otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + otherUserId));

            // Tạo cuộc trò chuyện mới
            ConversationEntity conversation = ConversationEntity.builder()
                    .name(generateDirectConversationName(user, otherUser))
                    .type(ConversationType.DIRECT)
                    .createdByUser(user)
                    .build();

            ConversationEntity savedConversation = conversationRepository.save(conversation);
            conversationRepository.flush();

            // Thêm cả 2 người dùng vào cuộc trò chuyện
            List<ConversationMemberEntity> members = Arrays.asList(
                    createMemberEntity(savedConversation, user, MemberRole.ADMIN),
                    createMemberEntity(savedConversation, otherUser, MemberRole.ADMIN)
            );

            conversationMemberRepository.saveAll(members);

            // Khởi tạo unread counts
            List<UnreadCountEntity> unreadCounts = Arrays.asList(
                    createUnreadCountEntity(savedConversation, user),
                    createUnreadCountEntity(savedConversation, otherUser)
            );

            unreadCountRepository.saveAll(unreadCounts);
            logger.info("Đã tạo cuộc trò chuyện trực tiếp giữa {} và {}", userId, otherUserId);

            return conversationMapper.entityToDto(savedConversation);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hoặc lấy cuộc trò chuyện trực tiếp: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo hoặc lấy cuộc trò chuyện trực tiếp: " + e.getMessage());
        }
    }

    @Override
    public List<ConversationMemberDto> getConversationMembers(UUID conversationId) {
        logger.info("Lấy danh sách thành viên của cuộc trò chuyện: {} ", conversationId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID không hợp lệ trong session");
        }

        // Kiểm tra cuộc trò chuyện có tồn tại không
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Kiểm tra người dùng có phải là thành viên không
        ConversationMemberEntity requesterMember = conversationMemberRepository
                .findById(new ConversationMemberId(conversationId, user.getId()))
                .orElseThrow(() -> new RuntimeException("Bạn không phải là thành viên của cuộc trò chuyện này"));

        if (requesterMember.getLeftAt() != null) {
            throw new RuntimeException("Bạn đã rời khỏi cuộc trò chuyện này");
        }

        // Lấy danh sách thành viên hiện tại (chưa rời khỏi group)
        List<ConversationMemberEntity> activeMembers = conversationMemberRepository
                .findActiveMembers(conversationId);

        return activeMembers.stream()
                .map(conversationMemberMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationResponseDto> getAllUserConversations() {
        logger.info("Lấy tất cả cuộc trò chuyện của người dùng");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        UUID userId = user.getId();

        // Kiểm tra người dùng có tồn tại không
        if (!userRepository.existsById(userId)) {
            logger.warn("Không tìm thấy người dùng với ID: {}", userId);
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
        }

        // Lấy tất cả cuộc trò chuyện của user
        List<ConversationEntity> conversations = conversationRepository
                .findAllActiveConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> {
                    ConversationResponseDto dto = mapToConversationDto(conversation, userId);

                    // Thêm thông tin members cho từng conversation
                    List<ConversationMemberEntity> activeMembers = conversationMemberRepository
                            .findActiveMembers(conversation.getId());
                    dto.setMembers(activeMembers.stream()
                            .map(conversationMemberMapper::entityToDto)
                            .collect(Collectors.toSet()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponseDto findById(UUID conversationId) {
        // Kiểm tra cuộc trò chuyện có tồn tại không
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        // Trả về DTO của cuộc trò chuyện
        return conversationMapper.entityToDto(conversation);
    }

    @Override
    @Transactional
    public void updateConversationName(UUID conversationId, String newName) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Chỉ có thể cập nhật tên cho group conversation");
        }

        String oldName = conversation.getName();
        conversation.setName(newName);
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        // Gửi tin nhắn hệ thống thông báo về việc đổi tên
        try {
            MessageEntity systemMessage = MessageEntity.builder()
                    .conversation(conversation)
                    .content(String.format("Tên nhóm đã được thay đổi từ '%s' thành '%s'", oldName, newName))
                    .messageType(MessageType.SYSTEM)
                    .build();

            messageRepository.save(systemMessage);

            // Gửi thông báo qua WebSocket
            MessageResponseDto messageDto = messageMapper.entityToDto(systemMessage);
            chatMessageProducerService.sendChatMessage(messageDto, ConversationType.GROUP, conversationId);

            logger.info("Đã cập nhật tên cuộc trò chuyện {} từ '{}' sang '{}'",
                    conversationId, oldName, newName);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo đổi tên group chat {}: {}", conversationId, e.getMessage());
        }
    }

    private ConversationMemberEntity createMemberEntity(ConversationEntity conversation, UserEntity user, MemberRole role) {
        return ConversationMemberEntity.builder()
                .id(new ConversationMemberId(conversation.getId(), user.getId()))
                .conversation(conversation)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private UnreadCountEntity createUnreadCountEntity(ConversationEntity conversation, UserEntity user) {
        return UnreadCountEntity.builder()
                .id(new UnreadCountId(conversation.getId(), user.getId()))
                .conversation(conversation)
                .user(user)
                .unreadCount(0)
                .build();
    }

    private ConversationResponseDto mapToConversationDto(ConversationEntity conversation, UUID userId) {
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
    }

    private ConversationEntity getGroupConversation(UUID conversationId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện với ID: " + conversationId));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new RuntimeException("Chức năng này chỉ áp dụng cho cuộc trò chuyện nhóm");
        }

        return conversation;
    }

    private void createSystemMessage(ConversationEntity conversation, String content) {
        MessageEntity systemMessage = MessageEntity.builder()
                .conversation(conversation)
                .content(content)
                .messageType(MessageType.SYSTEM)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sender(conversation.getCreatedByUser()) // Người tạo cuộc trò chuyện là người gửi
                .build();

        messageRepository.save(systemMessage);

        // Gửi tin nhắn qua RabbitMQ
        MessageResponseDto messageDto = messageMapper.entityToDto(systemMessage);
        chatMessageProducerService.sendChatMessage(messageDto, conversation.getType(), conversation.getId());
    }

    private String generateDirectConversationName(UserEntity user1, UserEntity user2) {
        return user1.getFullName() + " & " + user2.getFullName();
    }
}
