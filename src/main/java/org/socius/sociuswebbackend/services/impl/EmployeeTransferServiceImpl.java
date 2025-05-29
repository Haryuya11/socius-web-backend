package org.socius.sociuswebbackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.EmployeeTransferService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeTransferServiceImpl implements EmployeeTransferService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeTransferServiceImpl.class);
    private final EmploymentDetailRepository employmentDetailRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final EmploymentDetailMapper employmentDetailMapper;
    private final EmploymentHistoryRepository employmentHistoryRepository;

    @Override
    public EmploymentDetailResponseDto transferDepartment(UUID employeeId, UUID newDepartmentId) {
        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        DepartmentEntity oldDepartment = employmentDetail.getDepartment();
        DepartmentEntity newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban mới"));

        String reason = "Chuyển phòng ban từ " + oldDepartment.getName() + " sang " + newDepartment.getName();
        saveEmploymentHistory(employmentDetail, reason);

        handleDepartmentChatTransfer(employeeId, oldDepartment, newDepartment);

        employmentDetail.setDepartment(newDepartment);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    public EmploymentDetailResponseDto transferTeam(UUID employeeId, UUID newTeamId) {
        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        TeamEntity oldTeam = employmentDetail.getTeam();
        TeamEntity newTeam = teamRepository.findById(newTeamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team mới"));

        String reason = "Chuyển team từ " + oldTeam.getName() + " sang " + newTeam.getName();
        saveEmploymentHistory(employmentDetail, reason);

        handleTeamChatTransfer(employeeId, oldTeam, newTeam);

        employmentDetail.setTeam(newTeam);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    public EmploymentDetailResponseDto transferPosition(UUID employeeId, UUID newPositionId) {
        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        PositionEntity oldPosition = employmentDetail.getPosition();
        PositionEntity newPosition = positionRepository.findById(newPositionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí mới"));

        String reason = "Chuyển vị trí từ " + oldPosition.getName() + " sang " + newPosition.getName();
        saveEmploymentHistory(employmentDetail, reason);

        employmentDetail.setPosition(newPosition);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    private EmploymentDetailEntity getEmploymentDetail(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        return employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin công việc của nhân viên"));
    }

    private void saveEmploymentHistory(EmploymentDetailEntity employmentDetail, String reason) {
        EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
                .user(employmentDetail.getUser())
                .position(employmentDetail.getPosition())
                .department(employmentDetail.getDepartment())
                .team(employmentDetail.getTeam())
                .role(employmentDetail.getRole())
                .startDate(employmentDetail.getStartDate())
                .endDate(LocalDate.now())
                .salary(employmentDetail.getSalary())
                .description(reason)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        employmentHistoryRepository.save(history);
    }

    private void handleDepartmentChatTransfer(UUID employeeId, DepartmentEntity oldDepartment, DepartmentEntity newDepartment) {
        try {
            // Xóa khỏi group chat phòng ban cũ
            if (oldDepartment != null) {
                conversationService.removeMember(oldDepartment.getId(), employeeId);
            }

            // Thêm vào group chat phòng ban mới
            if (newDepartment != null) {
                conversationService.addMember(newDepartment.getId(), employeeId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển group chat phòng ban: {}", e.getMessage());
        }
    }

    private void handleTeamChatTransfer(UUID employeeId, TeamEntity oldTeam, TeamEntity newTeam) {
        try {
            // Xóa khỏi group chat team cũ
            if (oldTeam != null) {
                conversationService.removeMember(oldTeam.getId(), employeeId);
            }

            // Thêm vào group chat team mới
            if (newTeam != null) {
                conversationService.addMember(newTeam.getId(), employeeId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển group chat team: {}", e.getMessage());
        }
    }
}
