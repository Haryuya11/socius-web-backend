package org.socius.sociuswebbackend.utils;

import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.PasswordChangeRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.LoginHistoryEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.Gender;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class providing test data for authentication-related tests
 */
public final class AuthTestDataUtil {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthTestDataUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a test admin user with ID
     */
    public static UserEntity createTestAdminUser() {
        UserEntity user = UserEntity.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@socius.org")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.male)
                .phoneNumber("1234567890")
                .hireDate(LocalDate.of(2020, 1, 1))
                .build();

        user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return user;
    }

    /**
     * Creates a test regular user with ID
     */
    public static UserEntity createTestRegularUser() {
        UserEntity user = UserEntity.builder()
                .firstName("Regular")
                .lastName("User")
                .email("user@socius.org")
                .birthDate(LocalDate.of(1995, 5, 5))
                .gender(Gender.female)
                .phoneNumber("0987654321")
                .hireDate(LocalDate.of(2021, 5, 5))
                .build();

        user.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        return user;
    }

    /**
     * Creates a test account for admin user
     */
    public static AccountEntity createTestAdminAccount(UserEntity adminUser) {
        AccountEntity account = AccountEntity.builder()
                .user(adminUser)
                .password(passwordEncoder.encode("Admin@123"))
                .isActive(true)
                .isDefaultPassword(false)
                .lastLogin(LocalDateTime.now().minusDays(1))
                .build();

        account.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        return account;
    }

    /**
     * Creates a test account for regular user
     */
    public static AccountEntity createTestRegularAccount(UserEntity regularUser) {
        AccountEntity account = AccountEntity.builder()
                .user(regularUser)
                .password(passwordEncoder.encode("User@123"))
                .isActive(true)
                .isDefaultPassword(true)
                .lastLogin(LocalDateTime.now().minusDays(2))
                .build();

        account.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        return account;
    }

    /**
     * Creates a test admin role
     */
    public static RoleEntity createTestAdminRole() {
        RoleEntity role = new RoleEntity();
        role.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        role.setName("ADMIN");
        role.setDescription("Administrator role with full system access");
        return role;
    }

    /**
     * Creates a test user role
     */
    public static RoleEntity createTestUserRole() {
        RoleEntity role = new RoleEntity();
        role.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        role.setName("USER");
        role.setDescription("Regular user with limited access");
        return role;
    }

    /**
     * Creates a test employment detail for admin user
     */
    public static EmploymentDetailEntity createTestAdminEmploymentDetail(UserEntity adminUser, RoleEntity adminRole) {
        EmploymentDetailEntity employmentDetail = EmploymentDetailEntity.builder()
                .user(adminUser)
                .role(adminRole)
                .startDate(LocalDate.of(2020, 1, 1))
                .salary(BigDecimal.valueOf(10000))
                .workingStatus(WorkingStatus.active)
                .build();

        employmentDetail.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        return employmentDetail;
    }

    /**
     * Creates a test employment detail for regular user
     */
    public static EmploymentDetailEntity createTestRegularEmploymentDetail(UserEntity regularUser, RoleEntity userRole) {
        EmploymentDetailEntity employmentDetail = EmploymentDetailEntity.builder()
                .user(regularUser)
                .role(userRole)
                .startDate(LocalDate.of(2021, 5, 5))
                .salary(BigDecimal.valueOf(5000))
                .workingStatus(WorkingStatus.active)
                .build();

        employmentDetail.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        return employmentDetail;
    }

    // DTOs

    /**
     * Creates a login request for admin user
     */
    public static LoginRequestDto createAdminLoginRequest() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("admin@socius.org");
        loginRequest.setPassword("Admin@123");
        return loginRequest;
    }

    /**
     * Creates a login request for regular user
     */
    public static LoginRequestDto createRegularUserLoginRequest() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("user@socius.org");
        loginRequest.setPassword("User@123");
        return loginRequest;
    }

    /**
     * Creates a login request with invalid credentials
     */
    public static LoginRequestDto createInvalidLoginRequest() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("nonexistent@socius.org");
        loginRequest.setPassword("WrongPassword");
        return loginRequest;
    }

    /**
     * Creates a password change request
     */
    public static PasswordChangeRequestDto createPasswordChangeRequest() {
        PasswordChangeRequestDto passwordChangeRequest = new PasswordChangeRequestDto();
        passwordChangeRequest.setCurrentPassword("User@123");
        passwordChangeRequest.setNewPassword("NewPass@456");
        passwordChangeRequest.setConfirmPassword("NewPass@456");
        return passwordChangeRequest;
    }

    /**
     * Creates a test user permissions DTO for admin
     */
    public static UserPermissionsDto createAdminPermissionsDto() {
        Set<String> permissions = new HashSet<>();
        permissions.add("USER_CREATE");
        permissions.add("USER_READ");
        permissions.add("USER_UPDATE");
        permissions.add("USER_DELETE");
        permissions.add("ADMIN_DASHBOARD");

        return UserPermissionsDto.builder()
                .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .roleId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .roleName("ADMIN")
                .permissions(permissions)
                .build();
    }

    /**
     * Creates a test user permissions DTO for regular user
     */
    public static UserPermissionsDto createRegularUserPermissionsDto() {
        Set<String> permissions = new HashSet<>();
        permissions.add("USER_READ");

        return UserPermissionsDto.builder()
                .userId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .roleId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .roleName("USER")
                .permissions(permissions)
                .build();
    }

    /**
     * Creates a test login history entity for admin
     */
    public static LoginHistoryEntity createAdminLoginHistory(UserEntity adminUser) {
        LoginHistoryEntity loginHistory = LoginHistoryEntity.builder()
                .user(adminUser)
                .loginTime(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .deviceInfo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/96.0.4664.110")
                .build();

        loginHistory.setId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
        return loginHistory;
    }

    /**
     * Creates a test login history request DTO for admin
     */
    public static LoginHistoryRequestDto createAdminLoginHistoryRequest() {
        LoginHistoryRequestDto request = new LoginHistoryRequestDto();
        request.setUserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        request.setIpAddress("192.168.1.1");
        request.setDeviceInfo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/96.0.4664.110");
        return request;
    }

    /**
     * Creates a test role response DTO for admin
     */
    public static RoleResponseDto createAdminRoleResponseDto() {
        Set<PermissionResponseDto> permissions = new HashSet<>();
        permissions.add(PermissionResponseDto.builder()
                .id(UUID.fromString("11111111-0000-0000-0000-000000000000"))
                .name("USER_CREATE")
                .description("Create users")
                .build());
        permissions.add(PermissionResponseDto.builder()
                .id(UUID.fromString("22222222-0000-0000-0000-000000000000"))
                .name("USER_READ")
                .description("Read users")
                .build());

        return RoleResponseDto.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .name("ADMIN")
                .description("Administrator role with full system access")
                .permissions(permissions)
                .build();
    }

    /**
     * Creates a test role response DTO for regular user
     */
    public static RoleResponseDto createUserRoleResponseDto() {
        Set<PermissionResponseDto> permissions = new HashSet<>();
        permissions.add(PermissionResponseDto.builder()
                .id(UUID.fromString("22222222-0000-0000-0000-000000000000"))
                .name("USER_READ")
                .description("Read users")
                .build());

        return RoleResponseDto.builder()
                .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .name("USER")
                .description("Regular user with limited access")
                .permissions(permissions)
                .build();
    }

}