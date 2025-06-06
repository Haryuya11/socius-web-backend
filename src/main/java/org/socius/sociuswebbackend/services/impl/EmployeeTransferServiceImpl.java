package org.socius.sociuswebbackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.events.RBACEvent;
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.EmployeeTransferService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeTransferServiceImpl implements EmployeeTransferService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeTransferServiceImpl.class);
    private final EmploymentDetailRepository employmentDetailRepository;
    private final EmploymentHistoryRepository employmentHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ConversationService conversationService;
    private final EmploymentDetailMapper employmentDetailMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EmploymentDetailResponseDto transferDepartment(UUID employeeId, UUID newDepartmentId) {
        logger.info("Bắt đầu chuyển phòng ban cho nhân viên {} sang phòng ban {}", employeeId, newDepartmentId);

        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        DepartmentEntity oldDepartment = employmentDetail.getDepartment();
        DepartmentEntity newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban mới với ID: " + newDepartmentId));

        String reason = oldDepartment != null
                ? "Chuyển phòng ban từ " + oldDepartment.getName() + " sang " + newDepartment.getName()
                : "Gán vào phòng ban " + newDepartment.getName();

        saveEmploymentHistory(employmentDetail, reason);

        // Xử lý chuyển group chat phòng ban
        handleDepartmentChatTransfer(employeeId, oldDepartment, newDepartment);

        employmentDetail.setDepartment(newDepartment);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);

        logger.info("Đã chuyển phòng ban cho nhân viên {} thành công", employeeId);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    public EmploymentDetailResponseDto transferTeam(UUID employeeId, UUID newTeamId) {
        logger.info("Bắt đầu chuyển team cho nhân viên {} sang team {}", employeeId, newTeamId);

        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        TeamEntity oldTeam = employmentDetail.getTeam();
        TeamEntity newTeam = teamRepository.findById(newTeamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team mới với ID: " + newTeamId));

        String reason = oldTeam != null
                ? "Chuyển team từ " + oldTeam.getName() + " sang " + newTeam.getName()
                : "Gán vào team " + newTeam.getName();

        saveEmploymentHistory(employmentDetail, reason);

        // Xử lý chuyển group chat team
        handleTeamChatTransfer(employeeId, oldTeam, newTeam);

        employmentDetail.setTeam(newTeam);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);

        logger.info("Đã chuyển team cho nhân viên {} thành công", employeeId);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    public EmploymentDetailResponseDto transferPosition(UUID employeeId, UUID newPositionId) {
        logger.info("Bắt đầu chuyển vị trí cho nhân viên {} sang vị trí {}", employeeId, newPositionId);

        EmploymentDetailEntity employmentDetail = getEmploymentDetail(employeeId);
        PositionEntity oldPosition = employmentDetail.getPosition();
        PositionEntity newPosition = positionRepository.findById(newPositionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí mới với ID: " + newPositionId));

        String reason = oldPosition != null
                ? "Chuyển vị trí từ " + oldPosition.getName() + " sang " + newPosition.getName()
                : "Gán vào vị trí " + newPosition.getName();

        saveEmploymentHistory(employmentDetail, reason);

        employmentDetail.setPosition(newPosition);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);

        logger.info("Đã chuyển vị trí cho nhân viên {} thành công", employeeId);
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    public EmploymentDetailResponseDto transferEmployeeRole(UUID employeeId, UUID newRoleId) {
        logger.info("Bắt đầu chuyển role cho nhân viên {} sang role {}", employeeId, newRoleId);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        RoleEntity newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + newRoleId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        RoleEntity currentRole = employmentDetail.getRole();

        if (currentRole != null && currentRole.getId().equals(newRoleId)) {
            throw new RuntimeException("Nhân viên đã có role này rồi");
        }

        String historyDescription = currentRole != null
                ? "Chuyển role từ " + currentRole.getName() + " sang " + newRole.getName()
                : "Gán role " + newRole.getName();

        // Lưu lịch sử
        saveEmploymentHistory(employmentDetail, historyDescription);

        // Cập nhật role mới
        employmentDetail.setRole(newRole);
        employmentDetail.setUpdatedAt(LocalDateTime.now());

        employmentDetail = employmentDetailRepository.save(employmentDetail);

        // Phát sự kiện cập nhật RBAC bất đồng bộ
        CompletableFuture.runAsync(() -> {
            try {
                if (currentRole != null) {
                    eventPublisher.publishEvent(new RBACEvent(this, currentRole.getId(), RBACEvent.EventType.ROLE_UPDATED));
                }
                eventPublisher.publishEvent(new RBACEvent(this, newRoleId, RBACEvent.EventType.ROLE_UPDATED));
            } catch (Exception e) {
                logger.error("Lỗi khi phát sự kiện RBAC: {}", e.getMessage());
            }
        });

        logger.info("Đã chuyển role cho nhân viên {} thành công", employee.getEmail());
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    private EmploymentDetailEntity getEmploymentDetail(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        return employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin công việc của nhân viên với ID: " + employeeId));
    }

    private void saveEmploymentHistory(EmploymentDetailEntity employmentDetail, String reason) {
        try {
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
            logger.debug("Đã lưu lịch sử việc làm: {}", reason);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu lịch sử việc làm: {}", e.getMessage());
            throw new RuntimeException("Không thể lưu lịch sử việc làm: " + e.getMessage());
        }
    }

    private void handleDepartmentChatTransfer(UUID employeeId, DepartmentEntity oldDepartment, DepartmentEntity newDepartment) {
        try {
            // Xóa khỏi group chat phòng ban cũ
            if (oldDepartment != null && oldDepartment.getGroupChatId() != null) {
                logger.debug("Xóa nhân viên {} khỏi group chat phòng ban cũ: {}", employeeId, oldDepartment.getName());
                conversationService.removeMember(oldDepartment.getGroupChatId(), employeeId);
            }

            // Thêm vào group chat phòng ban mới
            if (newDepartment != null && newDepartment.getGroupChatId() != null) {
                logger.debug("Thêm nhân viên {} vào group chat phòng ban mới: {}", employeeId, newDepartment.getName());
                conversationService.addMember(newDepartment.getGroupChatId(), employeeId);
            } else if (newDepartment != null) {
                logger.warn("Phòng ban {} chưa có group chat được thiết lập", newDepartment.getName());
            }
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển group chat phòng ban cho nhân viên {}: {}", employeeId, e.getMessage());
            // Không throw exception để không làm fail toàn bộ transaction chuyển phòng ban
        }
    }

    private void handleTeamChatTransfer(UUID employeeId, TeamEntity oldTeam, TeamEntity newTeam) {
        try {
            // Xóa khỏi group chat team cũ
            if (oldTeam != null && oldTeam.getGroupChatId() != null) {
                logger.debug("Xóa nhân viên {} khỏi group chat team cũ: {}", employeeId, oldTeam.getName());
                conversationService.removeMember(oldTeam.getGroupChatId(), employeeId);
            }

            // Thêm vào group chat team mới
            if (newTeam != null && newTeam.getGroupChatId() != null) {
                logger.debug("Thêm nhân viên {} vào group chat team mới: {}", employeeId, newTeam.getName());
                conversationService.addMember(newTeam.getGroupChatId(), employeeId);
            } else if (newTeam != null) {
                logger.warn("Team {} chưa có group chat được thiết lập", newTeam.getName());
            }
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển group chat team cho nhân viên {}: {}", employeeId, e.getMessage());
            // Không throw exception để không làm fail toàn bộ transaction chuyển team
        }
    }
}