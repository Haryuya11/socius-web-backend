package org.socius.sociuswebbackend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.EmploymentDetailMapper;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.AdminService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.ConversationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    // Repositories
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final EmploymentDetailRepository employmentDetailRepository;
    private final AccountRepository accountRepository;

    // Services
    private final ConversationService conversationService;
    private final ConfigService configService;
    private final PasswordEncoder passwordEncoder;

    // Mappers
    private final EmploymentDetailMapper employmentDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmploymentDetailResponseDto createEmployee(EmployeeCreationRequestDto requestDto) {
        logger.info("Starting employee creation process for email: {}", requestDto.getEmail());

        // Validate email uniqueness
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống: " + requestDto.getEmail());
        }

        // Validate references
        validateReferences(requestDto);

        try {
            // 1. Create User
            UserEntity user = createUser(requestDto);
            logger.info("Successfully created user with ID: {}", user.getId());

            // 2. Create Employment Detail
            EmploymentDetailEntity employmentDetail = createEmploymentDetail(user, requestDto);
            logger.info("Successfully created employment detail for user: {}", user.getId());

            // 3. Create Account
            createAccount(user);
            logger.info("Successfully created account for user: {}", user.getId());

            // 4. Schedule post-creation tasks (after transaction commits)
            schedulePostCreationTasks(user.getId(), requestDto.getDepartmentId(), requestDto.getTeamId());

            logger.info("Employee creation completed successfully for email: {}", requestDto.getEmail());
            return employmentDetailMapper.entityToDto(employmentDetail);

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating employee {}: {}", requestDto.getEmail(), e.getMessage());
            throw new IllegalArgumentException("Dữ liệu không hợp lệ hoặc đã tồn tại: " + e.getMessage(), e);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found while creating employee {}: {}", requestDto.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating employee for email {}: {}", requestDto.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Không thể tạo nhân viên: " + e.getMessage(), e);
        }
    }

    private void validateReferences(EmployeeCreationRequestDto requestDto) {
        // Validate department
        if (requestDto.getDepartmentId() != null && !departmentRepository.existsById(requestDto.getDepartmentId())) {
            throw new EntityNotFoundException("Không tìm thấy phòng ban với ID: " + requestDto.getDepartmentId());
        }

        // Validate position
        if (requestDto.getPositionId() != null && !positionRepository.existsById(requestDto.getPositionId())) {
            throw new EntityNotFoundException("Không tìm thấy vị trí với ID: " + requestDto.getPositionId());
        }

        // Validate role
        if (requestDto.getRoleId() != null && !roleRepository.existsById(requestDto.getRoleId())) {
            throw new EntityNotFoundException("Không tìm thấy role với ID: " + requestDto.getRoleId());
        }

        // Validate team if provided
        if (requestDto.getTeamId() != null && !teamRepository.existsById(requestDto.getTeamId())) {
            throw new EntityNotFoundException("Không tìm thấy team với ID: " + requestDto.getTeamId());
        }
    }

    private UserEntity createUser(EmployeeCreationRequestDto requestDto) {
        UserEntity user = UserEntity.builder()
                .firstName(requestDto.getFirstName().trim())
                .lastName(requestDto.getLastName().trim())
                .email(requestDto.getEmail().trim().toLowerCase())
                .phoneNumber(requestDto.getPhoneNumber())
                .birthDate(requestDto.getBirthDate())
                .gender(requestDto.getGender())
                .address(requestDto.getAddress())
                .nationality(requestDto.getNationality())
                .hireDate(requestDto.getHireDate())
                .imageUrl(requestDto.getImageUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private EmploymentDetailEntity createEmploymentDetail(UserEntity user, EmployeeCreationRequestDto requestDto) {
        // Fetch entities
        DepartmentEntity department = departmentRepository.findById(requestDto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phòng ban với ID: " + requestDto.getDepartmentId()));

        PositionEntity position = positionRepository.findById(requestDto.getPositionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy vị trí với ID: " + requestDto.getPositionId()));

        RoleEntity role = roleRepository.findById(requestDto.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy role với ID: " + requestDto.getRoleId()));

        TeamEntity team = null;
        if (requestDto.getTeamId() != null) {
            team = teamRepository.findById(requestDto.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy team với ID: " + requestDto.getTeamId()));
        }

        EmploymentDetailEntity employmentDetail = EmploymentDetailEntity.builder()
                .user(user)
                .department(department)
                .position(position)
                .role(role)
                .team(team)
                .salary(requestDto.getSalary())
                .startDate(requestDto.getHireDate())
                .workingStatus(requestDto.getWorkingStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return employmentDetailRepository.save(employmentDetail);
    }

    private void createAccount(UserEntity user) {
        // Check if account already exists
        if (accountRepository.findByUser(user).isPresent()) {
            logger.warn("Account already exists for user: {}", user.getId());
            return;
        }

        String defaultPassword = configService.getString("default.password", "1");

        AccountEntity account = AccountEntity.builder()
                .user(user)
                .password(passwordEncoder.encode(defaultPassword))
                .isActive(true)
                .isDefaultPassword(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accountRepository.save(account);
        logger.info("Default account created successfully for user: {}", user.getId());
    }

    /**
     * Schedule tasks to run after transaction commits
     */
    private void schedulePostCreationTasks(UUID userId, UUID departmentId, UUID teamId) {
        logger.info("Scheduling post-creation tasks for user: {}, department: {}, team: {}",
                userId, departmentId, teamId);

        // Use TransactionSynchronization to ensure these run after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executePostCreationTasks(userId, departmentId, teamId);
            }
        });
    }

    /**
     * Execute tasks after transaction has been committed
     */
    private void executePostCreationTasks(UUID userId, UUID departmentId, UUID teamId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Add to department group chat
                if (departmentId != null) {
                    addToDepartmentGroupChat(userId, departmentId);
                }

                // Add to team group chat
                if (teamId != null) {
                    addToTeamGroupChat(userId, teamId);
                }

                logger.info("Post-creation tasks completed for user: {}", userId);
            } catch (Exception e) {
                logger.error("Error in post-creation tasks for user {}: {}", userId, e.getMessage(), e);
            }
        });
    }

    private void addToDepartmentGroupChat(UUID userId, UUID departmentId) {
        try {
            // Verify user exists in database
            if (!userRepository.existsById(userId)) {
                logger.warn("User {} not found, cannot add to department group chat", userId);
                return;
            }

            DepartmentEntity department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) {
                logger.warn("Department {} not found", departmentId);
                return;
            }

            UUID groupChatId = department.getGroupChatId();
            if (groupChatId != null) {
                conversationService.addMember(groupChatId, userId);
                logger.info("Successfully added user {} to department {} group chat {}",
                        userId, department.getName(), groupChatId);
            } else {
                logger.warn("Department {} does not have a group chat", department.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to add user {} to department {} group chat: {}",
                    userId, departmentId, e.getMessage(), e);
        }
    }

    private void addToTeamGroupChat(UUID userId, UUID teamId) {
        try {
            // Verify user exists in database
            if (!userRepository.existsById(userId)) {
                logger.warn("User {} not found, cannot add to team group chat", userId);
                return;
            }

            TeamEntity team = teamRepository.findById(teamId).orElse(null);
            if (team == null) {
                logger.warn("Team {} not found", teamId);
                return;
            }

            UUID groupChatId = team.getGroupChatId();
            if (groupChatId != null) {
                conversationService.addMember(groupChatId, userId);
                logger.info("Successfully added user {} to team {} group chat {}",
                        userId, team.getName(), groupChatId);
            } else {
                logger.warn("Team {} does not have a group chat", team.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to add user {} to team {} group chat: {}",
                    userId, teamId, e.getMessage(), e);
        }
    }
}