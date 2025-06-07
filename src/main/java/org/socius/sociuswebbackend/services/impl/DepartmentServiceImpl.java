package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.DepartmentMapper;
import org.socius.sociuswebbackend.mappers.DepartmentMappingHelper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.DepartmentRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    final private DepartmentRepository departmentRepository;
    final private DepartmentMapper departmentMapper;
    final private UserRepository userRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private ConversationService conversationService;
    final private DepartmentMappingHelper departmentMapperHelper;


    @Override
    public List<DepartmentResponseDto> findAll() {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        return departments.stream()
                .map(departmentMapper::entityToDto)
                .toList();
    }

    @Override
    public DepartmentResponseDto findById(UUID id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        return departmentMapper.entityToDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDto create(DepartmentRequestDto requestDto) {
        try {
            if (departmentRepository.existsByName(requestDto.getName())) {
                throw new IllegalArgumentException("Phòng ban đã tồn tại");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            UserEntity creator = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            DepartmentEntity department = DepartmentEntity.builder()
                    .name(requestDto.getName())
                    .description(requestDto.getDescription())
                    .build();


            ConversationResponseDto groupChat = conversationService.createGroupConversation(
                    department.getName(),
                    creator.getId(),
                    new HashSet<>()
            );

            department.setGroupChatId(groupChat.getId());

            DepartmentEntity savedDepartment = departmentRepository.save(department);
            return departmentMapper.entityToDto(savedDepartment);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể tạo phòng ban vì ràng buộc dữ liệu", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo phòng ban: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DepartmentResponseDto update(UUID id, DepartmentRequestDto requestDto) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        String oldName = department.getName();
        boolean nameChanged = !oldName.equals(requestDto.getName());

        if (nameChanged && departmentRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Phòng ban với tên này đã tồn tại");
        }

        departmentMapper.updateEntityFromDto(requestDto, department);
        department = departmentRepository.save(department);

        // Cập nhật tên group chat nếu tên phòng ban thay đổi
        if (nameChanged && department.getGroupChatId() != null) {
            try {
                conversationService.updateConversationName(department.getGroupChatId(), requestDto.getName());
                logger.info("Đã cập nhật tên group chat của phòng ban {} từ '{}' sang '{}'",
                        id, oldName, requestDto.getName());
            } catch (Exception e) {
                logger.error("Lỗi khi cập nhật tên group chat của phòng ban {}: {}", id, e.getMessage());
                throw new RuntimeException("Không thể cập nhật tên group chat của phòng ban: " + e.getMessage(), e);
            }
        }

        return departmentMapper.entityToDto(department);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy phòng ban với ID: " + id);
        }

        long count = employmentDetailRepository.countByDepartmentId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa phòng ban vì vẫn còn " + count + " nhân viên thuộc phòng ban này");
        }

        // Xóa group chat của phòng ban
        try {
            conversationService.deleteGroupConversation(id);
            logger.info("Đã xóa group chat của phòng ban {}", id);
        } catch (Exception e) {
            logger.error("Không thể xóa group chat của phòng ban: {}", e.getMessage());
        }

        departmentRepository.deleteById(id);
        logger.info("Đã xóa phòng ban với ID: {}", id);
    }

    @Override
    public Map<String, Object> getDepartmentWithMembers(UUID departmentId, Pageable pageable) {
        DepartmentEntity department = departmentRepository.findDepartmentWithMembers(departmentId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        return departmentMapperHelper.entityToDtoWithMembers(department);
    }
}
