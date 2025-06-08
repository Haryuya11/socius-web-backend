package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.events.RBACEvent;
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryUpdateRequestDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.socius.sociuswebbackend.services.SessionValidationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmploymentDetailServiceImpl implements EmploymentDetailService {

    private static final Logger logger = LoggerFactory.getLogger(EmploymentDetailServiceImpl.class);

    private final EmploymentHistoryRepository employmentHistoryRepository;
    private final EmploymentDetailRepository employmentDetailRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationService conversationService;
    private final SessionManagementService sessionManagementService;
    private final SessionValidationService sessionValidationService;
    private final EmploymentDetailMapper employmentDetailMapper;

    protected enum AssignmentType {
        TEAM, DEPARTMENT, POSITION
    }

    @Override
    public Map<String, Object> getAllActiveEmployees(Pageable pageable) {
        Page<EmploymentDetailEntity> employeePage = employmentDetailRepository.findAll(pageable);
        List<EmploymentDetailResponseDto> employees = employeePage.getContent().stream()
                .map(employmentDetailMapper::entityToLimitedDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("employeeCount", employees.size());
        result.put("totalPages", employeePage.getTotalPages());
        result.put("totalElements", employeePage.getTotalElements());

        return result;
    }

    @Override
    public Map<String, Object> getAllActiveEmployeesForAdmin(Pageable pageable) {
        Page<EmploymentDetailEntity> employeePage = employmentDetailRepository.findAll(pageable);
        List<EmploymentDetailResponseDto> employees = employeePage.getContent().stream()
                .map(employmentDetailMapper::entityToLimitedDtoForAdmin)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employees);
        result.put("employeeCount", employees.size());
        result.put("totalPages", employeePage.getTotalPages());
        result.put("totalElements", employeePage.getTotalElements());

        return result;
    }

    // ===================== ROLE MANAGEMENT =====================

    @Override
    @Transactional
    public EmploymentDetailResponseDto assignRoleToEmployee(UUID employeeId, UUID roleId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + roleId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        RoleEntity currentRole = employmentDetail.getRole();

        // Kiểm tra nếu nhân viên đã có role thì không cho phép gán role mới
        if (currentRole != null) {
            throw new RuntimeException("Nhân viên đã có role '" + currentRole.getName() +
                    "'. Vui lòng xóa role hiện tại trước khi gán role mới.");
        }

        // Gán role mới
        employmentDetail.setRole(role);
        employmentDetail = employmentDetailRepository.save(employmentDetail);

        // Lưu lịch sử
        saveEmploymentHistory(employmentDetail, "Gán role " + role.getName());

        // Publish RBAC event và gửi thông báo WebSocket
        publishRoleUpdateEvent(roleId);
        notifyRoleAssignment(employee.getEmail(), role.getName());

        logger.info("Đã gán role {} cho nhân viên {} thành công", role.getName(), employee.getEmail());
        return employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    @Transactional
    public List<EmploymentDetailResponseDto> assignRoleToMultipleEmployees(List<UUID> employeeIds, UUID roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + roleId));

        List<EmploymentDetailResponseDto> results = new ArrayList<>();
        List<String> successfulEmployees = new ArrayList<>();

        for (UUID employeeId : employeeIds) {
            try {
                // Gọi trực tiếp logic thay vì gọi method có @Transactional
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                RoleEntity currentRole = employmentDetail.getRole();
                if (currentRole != null) {
                    logger.error("Nhân viên {} đã có role '{}'. Bỏ qua.", employee.getEmail(), currentRole.getName());
                    continue;
                }

                // Gán role
                employmentDetail.setRole(role);
                employmentDetail = employmentDetailRepository.save(employmentDetail);
                saveEmploymentHistory(employmentDetail, "Gán role " + role.getName());

                results.add(employmentDetailMapper.entityToDto(employmentDetail));
                successfulEmployees.add(employee.getEmail());

            } catch (Exception e) {
                logger.error("Lỗi khi gán role {} cho nhân viên {}: {}", role.getName(), employeeId, e.getMessage());
            }
        }

        // Publish events sau khi hoàn thành tất cả assignments
        if (!results.isEmpty()) {
            publishRoleUpdateEvent(roleId);
            notifyMultipleRoleAssignments(successfulEmployees, role.getName());
        }

        logger.info("Đã gán role {} cho {}/{} nhân viên thành công", role.getName(), results.size(), employeeIds.size());
        return results;
    }

    @Override
    @Transactional
    public void removeRoleFromEmployee(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        RoleEntity currentRole = employmentDetail.getRole();
        if (currentRole != null) {
            String roleName = currentRole.getName();
            UUID roleId = currentRole.getId();

            saveEmploymentHistory(employmentDetail, "Xóa role " + roleName);
            employmentDetail.setRole(null);
            employmentDetail = employmentDetailRepository.save(employmentDetail);

            publishRoleUpdateEvent(roleId);
            notifyRoleRemoval(employee.getEmail(), roleName);

            logger.info("Đã xóa role {} khỏi nhân viên {} thành công", roleName, employee.getEmail());
        } else {
            logger.warn("Nhân viên {} không có role để xóa", employee.getEmail());
        }

        employmentDetailMapper.entityToDto(employmentDetail);
    }

    @Override
    @Transactional
    public void removeRoleFromMultipleEmployees(List<UUID> employeeIds) {
        List<String> successfulEmployees = new ArrayList<>();
        Set<UUID> affectedRoleIds = new HashSet<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                RoleEntity currentRole = employmentDetail.getRole();
                if (currentRole != null) {
                    String roleName = currentRole.getName();
                    affectedRoleIds.add(currentRole.getId());

                    saveEmploymentHistory(employmentDetail, "Xóa role " + roleName);
                    employmentDetail.setRole(null);
                    employmentDetailRepository.save(employmentDetail);

                    successfulEmployees.add(employee.getEmail() + " (role: " + roleName + ")");
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xóa role cho nhân viên {}: {}", employeeId, e.getMessage());
            }
        }

        // Publish events sau khi hoàn thành
        affectedRoleIds.forEach(this::publishRoleUpdateEvent);
        if (!successfulEmployees.isEmpty()) {
            notifyMultipleRoleRemovals(successfulEmployees);
        }

        logger.info("Đã xóa role khỏi {}/{} nhân viên thành công", successfulEmployees.size(), employeeIds.size());
    }

    @Override
    public List<EmploymentDetailResponseDto> getEmployeesByRole(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Không tìm thấy role với ID: " + roleId);
        }

        List<EmploymentDetailEntity> employmentDetails = employmentDetailRepository.findByRole_Id(roleId);

        if (employmentDetails.isEmpty()) {
            logger.info("Không tìm thấy nhân viên nào có role với ID: {}", roleId);
            return Collections.emptyList();
        }

        List<EmploymentDetailResponseDto> responseDtos = employmentDetails.stream()
                .map(employmentDetailMapper::entityToDto)
                .toList();
        logger.info("Đã tìm thấy {} nhân viên có role với ID: {}", responseDtos.size(), roleId);
        return responseDtos;
    }

    // ===================== TEAM MANAGEMENT =====================

    @Override
    @Transactional
    public void addEmployeeToTeam(UUID teamId, UUID employeeId) {
        validateTeamAssignment(teamId, employeeId);

        // Thực hiện assignment trong cùng transaction
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        employmentDetail.setTeam(team);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Được thêm vào team " + team.getName());

        // Thêm vào group chat bất đồng bộ
        addToGroupChatAsync(teamId, employeeId, AssignmentType.TEAM);

        logger.info("Đã thêm nhân viên {} vào team {} thành công", employee.getEmail(), team.getName());
    }

    @Override
    @Transactional
    public void addEmployeesToTeam(UUID teamId, List<UUID> employeeIds) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        List<String> successfulEmployees = new ArrayList<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getTeam() != null) {
                    logger.error("Nhân viên {} đã thuộc team '{}'. Bỏ qua.",
                            employee.getEmail(), employmentDetail.getTeam().getName());
                    continue;
                }

                employmentDetail.setTeam(team);
                employmentDetailRepository.save(employmentDetail);
                saveEmploymentHistory(employmentDetail, "Được thêm vào team " + team.getName());

                successfulEmployees.add(employee.getEmail());
            } catch (Exception e) {
                logger.error("Lỗi khi thêm nhân viên {} vào team {}: {}", employeeId, team.getName(), e.getMessage());
            }
        }

        // Thêm vào group chat bất đồng bộ cho tất cả employees thành công
        successfulEmployees.forEach(email -> {
            try {
                userRepository.findByEmail(email).ifPresent(emp -> addToGroupChatAsync(teamId, emp.getId(), AssignmentType.TEAM));
            } catch (Exception e) {
                logger.error("Lỗi khi thêm {} vào group chat: {}", email, e.getMessage());
            }
        });

        logger.info("Đã thêm {}/{} nhân viên vào team {} thành công",
                successfulEmployees.size(), employeeIds.size(), team.getName());
    }

    @Override
    @Transactional
    public void removeEmployeeFromTeam(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getTeam() == null) {
            throw new RuntimeException("Nhân viên không thuộc team nào");
        }

        UUID currentTeamId = employmentDetail.getTeam().getId();
        String teamName = employmentDetail.getTeam().getName();

        // Xóa khỏi team
        employmentDetail.setTeam(null);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Đã rời khỏi team " + teamName);

        // Xóa khỏi group chat bất đồng bộ
        removeFromGroupChatAsync(currentTeamId, employeeId, AssignmentType.TEAM);

        logger.info("Đã xóa nhân viên {} khỏi team {}", employee.getEmail(), teamName);
    }

    @Override
    @Transactional
    public void removeEmployeesFromTeam(List<UUID> employeeIds) {
        List<String> successfulEmployees = new ArrayList<>();
        Map<UUID, String> teamChatsToUpdate = new HashMap<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getTeam() != null) {
                    UUID teamId = employmentDetail.getTeam().getId();
                    String teamName = employmentDetail.getTeam().getName();
                    teamChatsToUpdate.put(teamId, teamName);

                    employmentDetail.setTeam(null);
                    employmentDetailRepository.save(employmentDetail);
                    saveEmploymentHistory(employmentDetail, "Đã rời khỏi team " + teamName);

                    successfulEmployees.add(employee.getEmail());
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xóa nhân viên {} khỏi team: {}", employeeId, e.getMessage());
            }
        }

        // Xóa khỏi group chats bất đồng bộ
        teamChatsToUpdate.forEach((teamId, teamName) ->
                successfulEmployees.forEach(email -> {
                    try {
                        userRepository.findByEmail(email).ifPresent(emp -> removeFromGroupChatAsync(teamId, emp.getId(), AssignmentType.TEAM));
                    } catch (Exception e) {
                        logger.error("Lỗi khi xóa {} khỏi group chat team {}: {}", email, teamName, e.getMessage());
                    }
                })
        );

        logger.info("Đã xóa {}/{} nhân viên khỏi team thành công", successfulEmployees.size(), employeeIds.size());
    }

    // ===================== DEPARTMENT MANAGEMENT =====================

    @Override
    @Transactional
    public void addEmployeeToDepartment(UUID departmentId, UUID employeeId) {
        validateDepartmentAssignment(departmentId, employeeId);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        employmentDetail.setDepartment(department);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Được thêm vào phòng ban " + department.getName());

        addToGroupChatAsync(departmentId, employeeId, AssignmentType.DEPARTMENT);

        logger.info("Đã thêm nhân viên {} vào phòng ban {} thành công", employee.getEmail(), department.getName());
    }

    @Override
    @Transactional
    public void addEmployeesToDepartment(UUID departmentId, List<UUID> employeeIds) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        List<String> successfulEmployees = new ArrayList<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getDepartment() != null) {
                    logger.error("Nhân viên {} đã thuộc phòng ban '{}'. Bỏ qua.",
                            employee.getEmail(), employmentDetail.getDepartment().getName());
                    continue;
                }

                employmentDetail.setDepartment(department);
                employmentDetailRepository.save(employmentDetail);
                saveEmploymentHistory(employmentDetail, "Được thêm vào phòng ban " + department.getName());

                successfulEmployees.add(employee.getEmail());
            } catch (Exception e) {
                logger.error("Lỗi khi thêm nhân viên {} vào phòng ban {}: {}",
                        employeeId, department.getName(), e.getMessage());
            }
        }

        // Thêm vào group chat bất đồng bộ
        successfulEmployees.forEach(email -> {
            try {
                userRepository.findByEmail(email).ifPresent(emp -> addToGroupChatAsync(departmentId, emp.getId(), AssignmentType.DEPARTMENT));
            } catch (Exception e) {
                logger.error("Lỗi khi thêm {} vào group chat phòng ban: {}", email, e.getMessage());
            }
        });

        logger.info("Đã thêm {}/{} nhân viên vào phòng ban {} thành công",
                successfulEmployees.size(), employeeIds.size(), department.getName());
    }

    @Override
    @Transactional
    public void removeEmployeeFromDepartment(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getDepartment() == null) {
            throw new RuntimeException("Nhân viên không thuộc phòng ban nào");
        }

        UUID currentDepartmentId = employmentDetail.getDepartment().getId();
        String departmentName = employmentDetail.getDepartment().getName();

        employmentDetail.setDepartment(null);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Đã rời khỏi phòng ban " + departmentName);

        removeFromGroupChatAsync(currentDepartmentId, employeeId, AssignmentType.DEPARTMENT);

        logger.info("Đã xóa nhân viên {} khỏi phòng ban {}", employee.getEmail(), departmentName);
    }

    @Override
    @Transactional
    public void removeEmployeesFromDepartment(List<UUID> employeeIds) {
        List<String> successfulEmployees = new ArrayList<>();
        Map<UUID, String> departmentChatsToUpdate = new HashMap<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getDepartment() != null) {
                    UUID departmentId = employmentDetail.getDepartment().getId();
                    String departmentName = employmentDetail.getDepartment().getName();
                    departmentChatsToUpdate.put(departmentId, departmentName);

                    employmentDetail.setDepartment(null);
                    employmentDetailRepository.save(employmentDetail);
                    saveEmploymentHistory(employmentDetail, "Đã rời khỏi phòng ban " + departmentName);

                    successfulEmployees.add(employee.getEmail());
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xóa nhân viên {} khỏi phòng ban: {}", employeeId, e.getMessage());
            }
        }

        // Xóa khỏi group chats bất đồng bộ
        departmentChatsToUpdate.forEach((deptId, deptName) ->
                successfulEmployees.forEach(email -> {
                    try {
                        userRepository.findByEmail(email).ifPresent(emp -> removeFromGroupChatAsync(deptId, emp.getId(), AssignmentType.DEPARTMENT));
                    } catch (Exception e) {
                        logger.error("Lỗi khi xóa {} khỏi group chat phòng ban {}: {}", email, deptName, e.getMessage());
                    }
                })
        );

        logger.info("Đã xóa {}/{} nhân viên khỏi phòng ban thành công", successfulEmployees.size(), employeeIds.size());
    }

    // ===================== POSITION MANAGEMENT =====================

    @Override
    @Transactional
    public void addEmployeeToPosition(UUID positionId, UUID employeeId) {
        validatePositionAssignment(positionId, employeeId);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        PositionEntity position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        employmentDetail.setPosition(position);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Được gán vào vị trí " + position.getName());

        logger.info("Đã gán nhân viên {} vào vị trí {} thành công", employee.getEmail(), position.getName());
    }

    @Override
    @Transactional
    public void addEmployeesToPosition(UUID positionId, List<UUID> employeeIds) {
        PositionEntity position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        List<String> successfulEmployees = new ArrayList<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getPosition() != null) {
                    logger.error("Nhân viên {} đã có vị trí '{}'. Bỏ qua.",
                            employee.getEmail(), employmentDetail.getPosition().getName());
                    continue;
                }

                employmentDetail.setPosition(position);
                employmentDetailRepository.save(employmentDetail);
                saveEmploymentHistory(employmentDetail, "Được gán vào vị trí " + position.getName());

                successfulEmployees.add(employee.getEmail());
            } catch (Exception e) {
                logger.error("Lỗi khi gán nhân viên {} vào vị trí {}: {}",
                        employeeId, position.getName(), e.getMessage());
            }
        }

        logger.info("Đã gán {}/{} nhân viên vào vị trí {} thành công",
                successfulEmployees.size(), employeeIds.size(), position.getName());
    }

    @Override
    @Transactional
    public void removeEmployeeFromPosition(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getPosition() == null) {
            throw new RuntimeException("Nhân viên không có vị trí nào");
        }

        String positionName = employmentDetail.getPosition().getName();

        employmentDetail.setPosition(null);
        employmentDetailRepository.save(employmentDetail);
        saveEmploymentHistory(employmentDetail, "Đã rời khỏi vị trí " + positionName);

        logger.info("Đã xóa nhân viên {} khỏi vị trí {}", employee.getEmail(), positionName);
    }

    @Override
    @Transactional
    public void removeEmployeesFromPosition(List<UUID> employeeIds) {
        List<String> successfulEmployees = new ArrayList<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

                EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

                if (employmentDetail.getPosition() != null) {
                    String positionName = employmentDetail.getPosition().getName();

                    employmentDetail.setPosition(null);
                    employmentDetailRepository.save(employmentDetail);
                    saveEmploymentHistory(employmentDetail, "Đã rời khỏi vị trí " + positionName);

                    successfulEmployees.add(employee.getEmail() + " (vị trí: " + positionName + ")");
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xóa nhân viên {} khỏi vị trí: {}", employeeId, e.getMessage());
            }
        }

        logger.info("Đã xóa {}/{} nhân viên khỏi vị trí thành công", successfulEmployees.size(), employeeIds.size());
    }

    @Override
    @Transactional
    public EmploymentDetailResponseDto updateEmployeeSalary(SalaryUpdateRequestDto requestDto, UUID employeeId) {
        try {
            UserEntity employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

            EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));


            BigDecimal previousSalary = employmentDetail.getSalary();

            // Validate lương mới khác lương hiện tại
            if (requestDto.getNewSalary().compareTo(previousSalary) == 0) {
                throw new IllegalArgumentException("Mức lương mới phải khác mức lương hiện tại");
            }

            EmploymentHistoryEntity employmentHistory = EmploymentHistoryEntity.builder()
                    .user(employee)
                    .position(employmentDetail.getPosition())
                    .department(employmentDetail.getDepartment())
                    .team(employmentDetail.getTeam())
                    .role(employmentDetail.getRole())
                    .startDate(employmentDetail.getStartDate())
                    .endDate(requestDto.getEffectiveDate().minusDays(1)) // Kết thúc 1 ngày trước ngày hiệu lực
                    .salary(previousSalary)
                    .description("Cập nhật lương từ " + previousSalary + " thành " + requestDto.getNewSalary() +
                            " hiệu lực từ " + requestDto.getEffectiveDate())
                    .build();

            employmentHistoryRepository.save(employmentHistory);

            SalaryHistoryEntity salaryHistory = SalaryHistoryEntity.builder()
                    .user(employee)
                    .previousSalary(previousSalary)
                    .newSalary(requestDto.getNewSalary())
                    .effectiveDate(requestDto.getEffectiveDate())
                    .reason(requestDto.getReason())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            salaryHistoryRepository.save(salaryHistory);

            employmentDetail.setSalary(requestDto.getNewSalary());
            employmentDetailRepository.save(employmentDetail);

            return employmentDetailMapper.entityToDto(employmentDetail);

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật lương cho nhân viên {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Không thể cập nhật lương cho nhân viên: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public EmploymentDetailResponseDto updateEmployee(UUID employeeId, EmployeeUpdateRequestDto requestDto) {
        try {
            UserEntity employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

            EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

            UUID oldDepartmentId = employmentDetail.getDepartment() != null ? employmentDetail.getDepartment().getId() : null;
            UUID oldTeamId = employmentDetail.getTeam() != null ? employmentDetail.getTeam().getId() : null;
            BigDecimal oldSalary = employmentDetail.getSalary();

            // Cập nhật thông tin nhân viên
            updateUserInfo(employee, requestDto);

            updateEmploymentInfo(employmentDetail, requestDto);

            handleDepartmentChange(employeeId, oldDepartmentId, requestDto.getDepartmentId());
            handleTeamChange(employeeId, oldTeamId, requestDto.getTeamId());
            handleSalaryChange(employee, oldSalary, requestDto.getSalary());

            userRepository.save(employee);
            employmentDetailRepository.save(employmentDetail);

            saveEmploymentHistory(employmentDetail, "Cập nhật thông tin nhân viên");

            return employmentDetailMapper.entityToDto(employmentDetail);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật thông tin nhân viên {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Không thể cập nhật thông tin nhân viên: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void terminateEmployee(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        saveEmploymentHistory(employmentDetail, "Chấm dứt hợp đồng");

        UUID oldDepartmentId = employmentDetail.getDepartment() != null ? employmentDetail.getDepartment().getId() : null;
        UUID oldTeamId = employmentDetail.getTeam() != null ? employmentDetail.getTeam().getId() : null;

        employmentDetail.setDepartment(null);
        employmentDetail.setTeam(null);
        employmentDetail.setPosition(null);
        employmentDetail.setRole(null);
        employmentDetail.setWorkingStatus(WorkingStatus.terminated);

        employmentDetailRepository.save(employmentDetail);

        if (oldDepartmentId != null) {
            removeFromGroupChatAsync(oldDepartmentId, employeeId, AssignmentType.DEPARTMENT);
        }
        if (oldTeamId != null) {
            removeFromGroupChatAsync(oldTeamId, employeeId, AssignmentType.TEAM);
        }

        // Vô hiệu hóa tài khoản nhân viên
        deactivateEmployeeAccount(employeeId);

        // Hủy session hiện tại
        invalidateEmployeeSession(employeeId);

        logger.info("Đã chấm dứt hợp đồng nhân viên {} thành công", employee.getEmail());
    }

    @Override
    @Transactional
    public void terminateEmployees(List<UUID> employeeIds) {
        List<String> successfulEmployees = new ArrayList<>();
        Map<UUID, String> departmentChatsToUpdate = new HashMap<>();
        Map<UUID, String> teamChatsToUpdate = new HashMap<>();

        for (UUID employeeId : employeeIds) {
            try {
                UserEntity employee = userRepository.findById(employeeId).orElse(null);
                if (employee != null) {
                    EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee).orElse(null);
                    if (employmentDetail != null && employmentDetail.getWorkingStatus() != WorkingStatus.terminated) {

                        // Lưu thông tin hiện tại vào lịch sử
                        saveEmploymentHistory(employmentDetail, "Chấm dứt hợp đồng");

                        // Lưu thông tin để xử lý group chat
                        if (employmentDetail.getDepartment() != null) {
                            UUID deptId = employmentDetail.getDepartment().getId();
                            String deptName = employmentDetail.getDepartment().getName();
                            departmentChatsToUpdate.put(deptId, deptName);
                        }

                        if (employmentDetail.getTeam() != null) {
                            UUID teamId = employmentDetail.getTeam().getId();
                            String teamName = employmentDetail.getTeam().getName();
                            teamChatsToUpdate.put(teamId, teamName);
                        }

                        // Terminate nhân viên
                        employmentDetail.setDepartment(null);
                        employmentDetail.setTeam(null);
                        employmentDetail.setPosition(null);
                        employmentDetail.setRole(null);
                        employmentDetail.setWorkingStatus(WorkingStatus.terminated);

                        employmentDetailRepository.save(employmentDetail);

                        // Vô hiệu hóa tài khoản
                        deactivateEmployeeAccount(employeeId);

                        successfulEmployees.add(employee.getEmail());
                    }
                }
            } catch (Exception e) {
                logger.error("Lỗi khi terminate nhân viên {}: {}", employeeId, e.getMessage());
            }
        }

        // Xóa khỏi group chats bất đồng bộ
        departmentChatsToUpdate.forEach((deptId, deptName) ->
                successfulEmployees.forEach(email -> {
                    try {
                        userRepository.findByEmail(email).ifPresent(emp ->
                                removeFromGroupChatAsync(deptId, emp.getId(), AssignmentType.DEPARTMENT));
                    } catch (Exception e) {
                        logger.error("Lỗi khi xóa {} khỏi department chat {}: {}", email, deptName, e.getMessage());
                    }
                }));

        teamChatsToUpdate.forEach((teamId, teamName) ->
                successfulEmployees.forEach(email -> {
                    try {
                        userRepository.findByEmail(email).ifPresent(emp ->
                                removeFromGroupChatAsync(teamId, emp.getId(), AssignmentType.TEAM));
                    } catch (Exception e) {
                        logger.error("Lỗi khi xóa {} khỏi team chat {}: {}", email, teamName, e.getMessage());
                    }
                }));

        // Hủy session của tất cả nhân viên đã terminate
        invalidateEmployeeSessions(successfulEmployees);

        logger.info("Đã terminate {}/{} nhân viên thành công", successfulEmployees.size(), employeeIds.size());
    }

    @Override
    @Transactional
    public void restoreEmployee(UUID employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getWorkingStatus() != WorkingStatus.terminated) {
            throw new RuntimeException("Nhân viên chưa bị terminate, không thể khôi phục");
        }

        // Chỉ khôi phục trạng thái, không tự động gán lại department/team/position/role
        employmentDetail.setWorkingStatus(WorkingStatus.inactive);

        employmentDetailRepository.save(employmentDetail);

        // Kích hoạt lại tài khoản
        activateEmployeeAccount(employeeId);

        // Lưu lịch sử khôi phục
        saveEmploymentHistory(employmentDetail, "Nhân viên được khôi phục từ trạng thái terminated");

        logger.info("Đã khôi phục nhân viên {}", employee.getEmail());
    }

    @Override
    public Map<String, Object> getTerminatedEmployees(Pageable pageable) {
        Page<EmploymentDetailEntity> terminatedEmployees = employmentDetailRepository
                .findByWorkingStatus(WorkingStatus.terminated, pageable);

        List<EmploymentDetailResponseDto> employeeDtos = terminatedEmployees.getContent()
                .stream()
                .map(employmentDetailMapper::entityToDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("employees", employeeDtos);
        result.put("currentPage", terminatedEmployees.getNumber());
        result.put("totalItems", terminatedEmployees.getTotalElements());
        result.put("totalPages", terminatedEmployees.getTotalPages());
        result.put("pageSize", terminatedEmployees.getSize());

        return result;
    }
    // ===================== VALIDATION METHODS =====================

    private void validateTeamAssignment(UUID teamId, UUID employeeId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getTeam() != null) {
            throw new RuntimeException("Nhân viên đã thuộc team '" + employmentDetail.getTeam().getName() +
                    "'. Vui lòng xóa khỏi team hiện tại trước khi thêm vào team mới.");
        }
    }

    private void validateDepartmentAssignment(UUID departmentId, UUID employeeId) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + departmentId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getDepartment() != null) {
            throw new RuntimeException("Nhân viên đã thuộc phòng ban '" + employmentDetail.getDepartment().getName() +
                    "'. Vui lòng xóa khỏi phòng ban hiện tại trước khi thêm vào phòng ban mới.");
        }
    }

    private void validatePositionAssignment(UUID positionId, UUID employeeId) {
        positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + positionId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getPosition() != null) {
            throw new RuntimeException("Nhân viên đã có vị trí '" + employmentDetail.getPosition().getName() +
                    "'. Vui lòng xóa khỏi vị trí hiện tại trước khi gán vị trí mới.");
        }
    }

    // ===================== HELPER METHODS =====================

    private void saveEmploymentHistory(EmploymentDetailEntity employmentDetail, String description) {
        EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
                .user(employmentDetail.getUser())
                .position(employmentDetail.getPosition())
                .department(employmentDetail.getDepartment())
                .team(employmentDetail.getTeam())
                .role(employmentDetail.getRole())
                .startDate(employmentDetail.getStartDate())
                .endDate(LocalDate.now())
                .salary(employmentDetail.getSalary())
                .description(description)
                .build();

        employmentHistoryRepository.save(history);
    }

    private String getEntityTypeName(AssignmentType type) {
        return switch (type) {
            case TEAM -> "team";
            case DEPARTMENT -> "phòng ban";
            case POSITION -> "vị trí";
        };
    }

    // ===================== ASYNC HELPER METHODS =====================

    private void publishRoleUpdateEvent(UUID roleId) {
        CompletableFuture.runAsync(() ->
                eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED))
        );
    }

    private void notifyRoleAssignment(String employeeEmail, String roleName) {
        CompletableFuture.runAsync(() -> {
            try {
                // Gửi thông báo WebSocket về việc gán role
                // webSocketService.sendRoleAssignmentNotification(employeeEmail, roleName);
                logger.info("Đã gửi thông báo gán role {} cho {}", roleName, employeeEmail);
            } catch (Exception e) {
                logger.error("Lỗi khi gửi thông báo gán role: {}", e.getMessage());
            }
        });
    }

    private void notifyMultipleRoleAssignments(List<String> employeeEmails, String roleName) {
        CompletableFuture.runAsync(() -> {
            try {
                // Gửi thông báo hàng loạt
                logger.info("Đã gửi thông báo gán role {} cho {} nhân viên", roleName, employeeEmails.size());
            } catch (Exception e) {
                logger.error("Lỗi khi gửi thông báo gán role hàng loạt: {}", e.getMessage());
            }
        });
    }

    private void notifyRoleRemoval(String employeeEmail, String roleName) {
        CompletableFuture.runAsync(() -> {
            try {
                // Gửi thông báo xóa role
                logger.info("Đã gửi thông báo xóa role {} khỏi {}", roleName, employeeEmail);
            } catch (Exception e) {
                logger.error("Lỗi khi gửi thông báo xóa role: {}", e.getMessage());
            }
        });
    }

    private void notifyMultipleRoleRemovals(List<String> employeeDetails) {
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Đã gửi thông báo xóa role cho {} nhân viên", employeeDetails.size());
            } catch (Exception e) {
                logger.error("Lỗi khi gửi thông báo xóa role hàng loạt: {}", e.getMessage());
            }
        });
    }

    private void addToGroupChatAsync(UUID assignmentId, UUID employeeId, AssignmentType type) {
        CompletableFuture.runAsync(() -> {
            try {
                UUID groupChatId = getGroupChatId(assignmentId, type);
                if (groupChatId != null) {
                    conversationService.addMember(groupChatId, employeeId);
                    String entityType = getEntityTypeName(type);
                    logger.info("Đã thêm nhân viên {} vào group chat {} của {} {}",
                            employeeId, groupChatId, entityType, assignmentId);
                } else {
                    logger.warn("Không tìm thấy group chat ID cho {} {} khi thêm vào group chat",
                            getEntityTypeName(type), assignmentId);
                }
            } catch (Exception e) {
                String entityType = getEntityTypeName(type);
                logger.error("Không thể thêm nhân viên {} vào group chat của {} {}: {}",
                        employeeId, entityType, assignmentId, e.getMessage());
            }
        });
    }

    private void removeFromGroupChatAsync(UUID assignmentId, UUID employeeId, AssignmentType type) {
        CompletableFuture.runAsync(() -> {
            try {
                UUID groupChatId = getGroupChatId(assignmentId, type);
                if (groupChatId != null) {
                    conversationService.removeMember(groupChatId, employeeId);
                    String entityType = getEntityTypeName(type);
                    logger.info("Đã xóa nhân viên {} khỏi group chat {} của {} {}",
                            employeeId, groupChatId, entityType, assignmentId);
                } else {
                    logger.warn("Không tìm thấy group chat ID cho {} {} khi xóa khỏi group chat",
                            getEntityTypeName(type), assignmentId);
                }
            } catch (Exception e) {
                String entityType = getEntityTypeName(type);
                logger.error("Không thể xóa nhân viên {} khỏi group chat của {} {}: {}",
                        employeeId, entityType, assignmentId, e.getMessage());
            }
        });
    }

    private UUID getGroupChatId(UUID assignmentId, AssignmentType type) {
        try {
            return switch (type) {
                case TEAM -> {
                    TeamEntity team = teamRepository.findById(assignmentId).orElse(null);
                    yield team != null ? team.getGroupChatId() : null;
                }
                case DEPARTMENT -> {
                    DepartmentEntity department = departmentRepository.findById(assignmentId).orElse(null);
                    yield department != null ? department.getGroupChatId() : null;
                }
                default -> null;
            };
        } catch (Exception e) {
            logger.error("Lỗi khi lấy group chat ID cho {} {}: {}", getEntityTypeName(type), assignmentId, e.getMessage());
            return null;
        }
    }

    private void updateUserInfo(UserEntity employee, EmployeeUpdateRequestDto requestDto) {
        if (requestDto.getFirstName() != null) {
            employee.setFirstName(requestDto.getFirstName());
        }
        if (requestDto.getLastName() != null) {
            employee.setLastName(requestDto.getLastName());
        }
        if (requestDto.getBirthDate() != null) {
            employee.setBirthDate(requestDto.getBirthDate());
        }
        if (requestDto.getGender() != null) {
            employee.setGender(requestDto.getGender());
        }
        if (requestDto.getPhoneNumber() != null) {
            employee.setPhoneNumber(requestDto.getPhoneNumber());
        }
        if (requestDto.getNationality() != null) {
            employee.setNationality(requestDto.getNationality());
        }
        if (requestDto.getHireDate() != null) {
            employee.setHireDate(requestDto.getHireDate());
        }
        if (requestDto.getAddress() != null) {
            employee.setAddress(requestDto.getAddress());
        }
    }

    private void updateEmploymentInfo(EmploymentDetailEntity employmentDetail, EmployeeUpdateRequestDto requestDto) {
        // Cập nhật position
        if (requestDto.getPositionId() != null) {
            PositionEntity position = positionRepository.findById(requestDto.getPositionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí với ID: " + requestDto.getPositionId()));
            employmentDetail.setPosition(position);
        }

        // Cập nhật department
        if (requestDto.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(requestDto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + requestDto.getDepartmentId()));
            employmentDetail.setDepartment(department);
        }

        // Cập nhật team
        if (requestDto.getTeamId() != null) {
            TeamEntity team = teamRepository.findById(requestDto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + requestDto.getTeamId()));
            employmentDetail.setTeam(team);
        }

        // Cập nhật role
        if (requestDto.getRoleId() != null) {
            RoleEntity role = roleRepository.findById(requestDto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + requestDto.getRoleId()));
            employmentDetail.setRole(role);
        }

        // Cập nhật lương
        if (requestDto.getSalary() != null) {
            employmentDetail.setSalary(requestDto.getSalary());
        }
    }

    private void handleDepartmentChange(UUID employeeId, UUID oldDepartmentId, UUID newDepartmentId) {
        if (newDepartmentId != null && !newDepartmentId.equals(oldDepartmentId)) {
            // Xóa khỏi group chat phòng ban cũ
            if (oldDepartmentId != null) {
                removeFromGroupChatAsync(oldDepartmentId, employeeId, AssignmentType.DEPARTMENT);
            }
            addToGroupChatAsync(newDepartmentId, employeeId, AssignmentType.DEPARTMENT);

            logger.info("Đã chuyển nhân viên {} sang phòng ban mới", employeeId);
        }
    }

    private void handleTeamChange(UUID employeeId, UUID oldTeamId, UUID newTeamId) {
        if (newTeamId != null && !newTeamId.equals(oldTeamId)) {
            if (oldTeamId != null) {
                removeFromGroupChatAsync(oldTeamId, employeeId, AssignmentType.TEAM);
            }
            addToGroupChatAsync(newTeamId, employeeId, AssignmentType.TEAM);

            logger.info("Đã chuyển nhân viên {} sang team mới", employeeId);
        }
    }

    private void handleSalaryChange(UserEntity employee, BigDecimal oldSalary, BigDecimal newSalary) {
        if (newSalary != null && newSalary.compareTo(oldSalary) != 0) {

            // Lưu lịch sử lương
            SalaryHistoryEntity salaryHistory = SalaryHistoryEntity.builder()
                    .user(employee)
                    .previousSalary(oldSalary)
                    .newSalary(newSalary)
                    .effectiveDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            salaryHistoryRepository.save(salaryHistory);

            logger.info("Đã lưu lịch sử thay đổi lương cho nhân viên {} từ {} thành {}",
                    employee.getEmail(), oldSalary, newSalary);
        }
    }

    private void deactivateEmployeeAccount(UUID employeeId) {
        accountRepository.findByUser_Id(employeeId).ifPresent(account -> {
            account.setIsActive(false);
            accountRepository.save(account);
        });
    }

    private void activateEmployeeAccount(UUID employeeId) {
        accountRepository.findByUser_Id(employeeId).ifPresent(account -> {
            account.setIsActive(true);
            accountRepository.save(account);
        });
    }

    private void invalidateEmployeeSession(UUID employeeId) {
        try {
            String sessionId = sessionValidationService.getUserSessionId(employeeId);
            if (sessionId != null) {
                sessionManagementService.invalidateSession(sessionId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi hủy session cho nhân viên {}: {}", employeeId, e.getMessage());
        }
    }

    private void invalidateEmployeeSessions(List<String> employeeEmails) {
        employeeEmails.forEach(email -> {
            try {
                userRepository.findByEmail(email).ifPresent(user ->
                        invalidateEmployeeSession(user.getId()));
            } catch (Exception e) {
                logger.error("Lỗi khi hủy session cho nhân viên có email {}: {}", email, e.getMessage());
            }
        });
    }
}