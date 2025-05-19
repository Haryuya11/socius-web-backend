package org.socius.sociuswebbackend.utils;

import org.socius.sociuswebbackend.model.dtos.auth.LoginRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.PasswordChangeRequestDto;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
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
     * Tạo một người dùng admin mẫu
     */
    public static UserEntity createTestAdminUser() {

        return UserEntity.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .firstName("Admin")
                .lastName("User")
                .email("admin@socius.org")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.male)
                .phoneNumber("1234567890")
                .hireDate(LocalDate.of(2020, 1, 1))
                .build();
    }

    /**
     * Tạo một người dùng thường mẫu
     */
    public static UserEntity createTestRegularUser() {
        return UserEntity.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .firstName("Regular")
                .lastName("User")
                .email("user@socius.org")
                .birthDate(LocalDate.of(1995, 5, 5))
                .gender(Gender.female)
                .phoneNumber("0987654321")
                .hireDate(LocalDate.of(2021, 5, 5))
                .build();
    }

    /**
     * Tạo một tài khoản admin mẫu
     */
    public static AccountEntity createTestAdminAccount(UserEntity adminUser) {

        return AccountEntity.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .user(adminUser)
                .password(passwordEncoder.encode("Admin@123"))
                .isActive(true)
                .isDefaultPassword(false)
                .lastLogin(LocalDateTime.now().minusDays(1))
                .build();
    }

    /**
     * Tạo một tài khoản thường mẫu
     */
    public static AccountEntity createTestRegularAccount(UserEntity regularUser) {

        return AccountEntity.builder()
                .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .user(regularUser)
                .password(passwordEncoder.encode("User@123"))
                .isActive(true)
                .isDefaultPassword(true)
                .lastLogin(LocalDateTime.now().minusDays(2))
                .build();
    }

    /**
     * Tạo admin role mẫu
     */
    public static RoleEntity createTestAdminRole() {
        return RoleEntity.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .name("ADMIN")
                .description("Administrator role with full system access")
                .build();
    }

    /**
     * Tạo user role mẫu
     */
    public static RoleEntity createTestUserRole() {
        return RoleEntity.builder()
                .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .name("USER")
                .description("Regular user with limited access")
                .build();
    }

    /**
     * Tạo một chi tiết việc làm mẫu cho người dùng admin
     */
    public static EmploymentDetailEntity createTestAdminEmploymentDetail(UserEntity adminUser, RoleEntity adminRole) {
        return EmploymentDetailEntity.builder()
                .id(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .user(adminUser)
                .role(adminRole)
                .startDate(LocalDate.of(2020, 1, 1))
                .salary(BigDecimal.valueOf(10000))
                .workingStatus(WorkingStatus.active)
                .build();
    }

    /**
     * Tạo một chi tiết việc làm mẫu cho người dùng thường
     */
    public static EmploymentDetailEntity createTestRegularEmploymentDetail(UserEntity regularUser, RoleEntity userRole) {
        return EmploymentDetailEntity.builder()
                .id(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))
                .user(regularUser)
                .role(userRole)
                .startDate(LocalDate.of(2021, 5, 5))
                .salary(BigDecimal.valueOf(5000))
                .workingStatus(WorkingStatus.active)
                .build();
    }

    // DTOs

    /**
     * Tạo một yêu cầu đăng nhập mẫu cho admin
     */
    public static LoginRequestDto createAdminLoginRequest() {
        return LoginRequestDto.builder()
                .email("admin@socius.org")
                .password("Admin@123")
                .build();
    }

    /**
     * Tạo một yêu cầu đăng nhập mẫu cho người dùng thường
     */
    public static LoginRequestDto createRegularUserLoginRequest() {
        return LoginRequestDto.builder()
                .email("user@socius.org")
                .password("User@123")
                .build();
    }

    /**
     * Tạo một yêu cầu đăng nhập không hợp lệ
     */
    public static LoginRequestDto createInvalidLoginRequest() {
        return LoginRequestDto.builder()
                .email("nonexistent@socius.org")
                .password("WrongPassword")
                .build();
    }

    /**
     * Tạo một yêu cầu thay đổi mật khẩu mẫu
     */
    public static PasswordChangeRequestDto createPasswordChangeRequest() {
        return PasswordChangeRequestDto.builder()
                .currentPassword("User@123")
                .newPassword("NewPass@456")
                .confirmPassword("NewPass@456")
                .build();
    }

    /**
     * Tạo quyền truy cập mẫu cho người dùng admin
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
     * Tạo quyền truy cập mẫu cho người dùng thường
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
     * Tạo một lịch sử đăng nhập mẫu cho người dùng admin
     */
    public static LoginHistoryEntity createAdminLoginHistory(UserEntity adminUser) {
        return LoginHistoryEntity.builder()
                .id(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"))
                .user(adminUser)
                .loginTime(LocalDateTime.now())
                .ipAddress("192.168.1.1")
                .deviceInfo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/96.0.4664.110")
                .build();
    }

    /**
     * Tạo một yêu cầu lịch sử đăng nhập mẫu cho người dùng admin
     */
    public static LoginHistoryRequestDto createAdminLoginHistoryRequest() {
        return LoginHistoryRequestDto.builder()
                .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .ipAddress("192.168.1.1")
                .deviceInfo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/96.0.4664.110")
                .build();
    }

    /**
     * Tạo role response DTO cho người dùng admin
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
     * Tạo role response DTO cho người dùng thường
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

    /**
     * Tạo một UserResponse DTO mẫu cho người dùng admin
     */
    public static UserResponseDto createTestAdminUserResponse() {
        return UserResponseDto.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .firstName("Admin")
                .lastName("User")
                .email("admin@socius.org")
                .build();
    }
}