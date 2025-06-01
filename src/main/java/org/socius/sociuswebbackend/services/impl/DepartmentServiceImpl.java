package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.DepartmentMapper;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.DepartmentRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);
    final private DepartmentRepository departmentRepository;
    final private DepartmentMapper departmentMapper;
    final private UserRepository userRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private ConversationService conversationService;
    final private EmploymentHistoryRepository employmentHistoryRepository;


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
                throw new RuntimeException("Phòng ban đã tồn tại");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            UserEntity creator = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            DepartmentEntity department = DepartmentEntity.builder()
                    .name(requestDto.getName())
                    .description(requestDto.getDescription())
                    .build();


            DepartmentEntity savedDepartment = departmentRepository.save(department);

            conversationService.createGroupConversation(
                    savedDepartment.getId(),
                    "Phòng " + savedDepartment.getName(),
                    creator.getId(),
                    new HashSet<>()
            );

            return departmentMapper.entityToDto(savedDepartment);
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

        if (!department.getName().equals(requestDto.getName()) &&
                departmentRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Phòng ban với tên này đã tồn tại");
        }

        departmentMapper.updateEntityFromDto(requestDto, department);
        department = departmentRepository.save(department);
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
    public DepartmentResponseDto addEmployee(UUID departmentId, UUID employeeId) {

        // Tìm phòng ban theo ID
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        // Tìm nhân viên theo ID
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));


        // Kiểm tra xem nhân viên đã thuộc phòng ban chưa
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc phòng ban không
        if (employmentDetail.getDepartment() != null &&
                employmentDetail.getDepartment().getId().equals(departmentId)) {
            throw new RuntimeException("Nhân viên đã thuộc phòng ban này");
        }
        DepartmentResponseDto result = addEmployeeToDatabase(departmentId, employeeId);

        // Thêm vào group chat trong transaction riêng biệt
        CompletableFuture.runAsync(() -> addToGroupChatSafely(departmentId, employeeId));

        return result;
    }

    @Transactional
    public DepartmentResponseDto addEmployeeToDatabase(UUID departmentId, UUID employeeId) {
        // Logic thêm nhân viên vào database
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getDepartment() != null &&
                !employmentDetail.getDepartment().getId().equals(departmentId)) {
            throw new RuntimeException("Nhân viên đã thuộc phòng ban khác");
        }

        employmentDetail.setDepartment(department);
        employmentDetailRepository.save(employmentDetail);

        return departmentMapper.entityToDto(department);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void addToGroupChatSafely(UUID departmentId, UUID employeeId) {
        try {
            conversationService.addMember(departmentId, employeeId);
            logger.info("Đã thêm nhân viên {} vào group chat của phòng ban {}", employeeId, departmentId);
        } catch (Exception e) {
            logger.error("Không thể thêm nhân viên vào group chat của phòng ban: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<DepartmentResponseDto> addEmployees(UUID departmentId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(departmentId, employeeId);
        }
        return findAll();
    }

    @Override
    @Transactional
    public List<DepartmentResponseDto> removeEmployees(UUID departmentId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(departmentId, employeeId);
        }
        return findAll();
    }

    @Override
    public DepartmentResponseDto removeEmployee(UUID departmentId, UUID employeeId) {
        // Kiểm tra điều kiện trước khi thực hiện
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc phòng ban không
        if (employmentDetail.getDepartment() == null || !employmentDetail.getDepartment().getId().equals(departmentId)) {
            throw new RuntimeException("Nhân viên không thuộc phòng ban này");
        }

        // Thực hiện cập nhật database trong transaction riêng biệt
        DepartmentResponseDto result = removeEmployeeFromDatabase(departmentId, employeeId);

        // Xóa khỏi group chat trong transaction riêng biệt
        CompletableFuture.runAsync(() -> removeFromGroupChatSafely(departmentId, employeeId));

        return result;
    }

    @Transactional
    public DepartmentResponseDto removeEmployeeFromDatabase(UUID departmentId, UUID employeeId) {
        try {
            // Tìm phòng ban theo ID
            DepartmentEntity department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

            // Tìm nhân viên theo ID
            UserEntity employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

            // Tìm chi tiết việc làm của nhân viên
            EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

            // Lưu lịch sử làm việc
            EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
                    .user(employee)
                    .position(employmentDetail.getPosition())
                    .department(employmentDetail.getDepartment())
                    .team(employmentDetail.getTeam())
                    .role(employmentDetail.getRole())
                    .startDate(employmentDetail.getStartDate())
                    .endDate(LocalDate.now())
                    .salary(employmentDetail.getSalary())
                    .description("Đã rời khỏi phòng ban " + department.getName())
                    .build();

            employmentHistoryRepository.save(history);

            // Xóa nhân viên khỏi phòng ban
            employmentDetail.setDepartment(null);
            employmentDetailRepository.save(employmentDetail);

            return departmentMapper.entityToDto(department);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa nhân viên {} khỏi phòng ban {}: {}",
                    employeeId, departmentId, e.getMessage());
            throw new RuntimeException("Không thể xóa nhân viên khỏi phòng ban: " + e.getMessage(), e);
        }
    }

    protected void removeFromGroupChatSafely(UUID departmentId, UUID employeeId) {
        try {
            conversationService.removeMember(departmentId, employeeId);
            logger.info("Đã xóa nhân viên {} khỏi group chat của phòng ban {}", employeeId, departmentId);
        } catch (Exception e) {
            logger.error("Không thể xóa nhân viên {} khỏi group chat của phòng ban {}: {}",
                    employeeId, departmentId, e.getMessage());
            // Không throw exception để không ảnh hưởng đến quá trình chính
        }
    }

    @Override
    public Map<String, Object> getDepartmentWithMembers(UUID departmentId, Pageable pageable) {
        DepartmentEntity department = departmentRepository.findDepartmentWithMembers(departmentId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        return departmentMapper.entityToDtoWithMembers(department, pageable);
    }
}
