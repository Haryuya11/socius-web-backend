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
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
        if (departmentRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Phòng ban đã tồn tại");
        }

        UUID departmentId = UUID.randomUUID();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        DepartmentEntity department = departmentMapper.requestDtoToEntity(requestDto);
        department.setId(departmentId);

        department = departmentRepository.save(department);

        conversationService.createGroupConversation(
                departmentId,
                "Phòng " + department.getName(),
                creator.getId(),
                new HashSet<>()
        );

        return departmentMapper.entityToDto(department);
    }

    @Override
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

        // Tìm chi tiết việc làm của nhân viên
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        employmentDetail.setDepartment(department);
        employmentDetailRepository.save(employmentDetail);

        // Tự động thêm nhân viên vào group chat của phòng ban
        try {
            // Thêm nhân viên vào group chat
            conversationService.addMember(departmentId, employeeId);
            logger.info("Đã thêm nhân viên {} vào group chat của phòng ban {}", employeeId, departmentId);
        } catch (Exception e) {
            // Log lỗi nhưng không ảnh hưởng đến quá trình thêm nhân viên vào phòng ban
            logger.error("Không thể thêm nhân viên vào group chat của phòng ban: {}", e.getMessage());

        }

        return departmentMapper.entityToDto(department);
    }

    @Override
    public List<DepartmentResponseDto> addEmployees(UUID departmentId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(departmentId, employeeId);
        }
        return findAll();
    }

    @Override
    public List<DepartmentResponseDto> removeEmployees(UUID departmentId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(departmentId, employeeId);
        }
        return findAll();
    }

    @Override
    public DepartmentResponseDto removeEmployee(UUID departmentId, UUID employeeId) {
        // Tìm phòng ban theo ID
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        // Tìm nhân viên theo ID
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Tìm chi tiết việc làm của nhân viên
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc phòng ban không
        if (employmentDetail.getDepartment() == null || !employmentDetail.getDepartment().getId().equals(departmentId)) {
            throw new RuntimeException("Nhân viên không thuộc phòng ban này");
        }

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
                .description("Đã rời khỏi phòng ban" + department.getName())
                .build();

        employmentHistoryRepository.save(history);

        // Xóa nhân viên khỏi phòng ban
        employmentDetail.setDepartment(null);
        employmentDetailRepository.save(employmentDetail);

        // Xóa nhân viên khỏi group chat của phòng ban
        try {
            conversationService.removeMember(departmentId, employeeId);
            logger.info("Đã xóa nhân viên {} khỏi group chat của phòng ban {}", employeeId, departmentId);
        } catch (Exception e) {
            logger.error("Không thể xóa nhân viên khỏi group chat của phòng ban: {}", e.getMessage());
        }
        return departmentMapper.entityToDto(department);
    }
}
