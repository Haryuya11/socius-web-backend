package org.socius.sociuswebbackend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.config.PermissionConstants;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.security.RequirePermission;
import org.socius.sociuswebbackend.services.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    final private UserService userService;

    /**
     * Tìm người dùng theo ID
     *
     * @param userId ID của người dùng cần tìm
     * @return Thông tin người dùng hoặc 404 nếu không tìm thấy
     */
    @GetMapping("/{userId}")
    @RequirePermission({PermissionConstants.USER_READ_ALL, PermissionConstants.USER_READ_OWN})
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        UserResponseDto user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Lấy thông tin người dùng hiện tại từ phiên làm việc
     *
     * @param request Request HTTP hiện tại
     * @return Thông tin người dùng hiện tại hoặc 401 nếu chưa đăng nhập
     */
    @GetMapping("/current-user")
    @RequirePermission({PermissionConstants.USER_READ_ALL, PermissionConstants.USER_READ_OWN})
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        EmploymentDetailResponseDto userDetail = userService.getCurrentUser(request);
        return ResponseEntity.ok(userDetail);
    }

    /**
     * Lấy lịch sử việc làm của người dùng theo ID
     *
     * @param userId ID của người dùng cần lấy lịch sử việc làm
     * @param page   Số trang (mặc định 0)
     * @param size   Kích thước trang (mặc định 10)
     * @return Danh sách lịch sử việc làm cùng với thông tin phân trang
     */
    @GetMapping("/{userId}/employment-history")
    public ResponseEntity<Map<String, Object>> getEmploymentHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = userService.getEmploymentHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy lịch sử lương của người dùng theo ID
     *
     * @param userId ID của người dùng cần lấy lịch sử lương
     * @param page   Số trang (mặc định 0)
     * @param size   Kích thước trang (mặc định 10)
     * @return Danh sách lịch sử lương cùng với thông tin phân trang
     */
    @GetMapping("/{userId}/salary-history")
    @RequirePermission({PermissionConstants.EMPLOYEE_SALARY_VIEW_ALL, PermissionConstants.EMPLOYEE_SALARY_VIEW_OWN})
    public ResponseEntity<Map<String, Object>> getSalaryHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = userService.getSalaryHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin chi tiết về việc làm của người dùng theo ID
     *
     * @param userId ID của người dùng cần lấy thông tin việc làm
     * @return Thông tin chi tiết về việc làm của người dùng
     */
    @GetMapping("/{userId}/employment-detail")
    public ResponseEntity<Map<String, Object>> getEmploymentDetail(
            @PathVariable UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, Object> response = userService.getEmploymentDetailByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách người dùng không thuộc bất kỳ team nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ team nào
     */
    @GetMapping("/not-in-any-team")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> getUsersNotInAnyTeam(HttpServletRequest request) {
        List<UserResponseDto> users = userService.getActiveUsersNotInAnyTeam(request);
        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng nào không thuộc team nào");
        }
    }

    /**
     * Lay danh sách người dùng không thuộc bất kỳ phòng ban nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ phòng ban nào
     */
    @GetMapping("/not-in-any-department")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> getUsersNotInAnyDepartment(HttpServletRequest request) {
        List<UserResponseDto> users = userService.getActiveUsersNotInAnyDepartment(request);
        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng nào không thuộc phòng ban nào");
        }
    }

    /**
     * Lấy danh sách người dùng không thuộc bất kỳ vị trí nào
     *
     * @param request Request HTTP hiện tại
     * @return Danh sách người dùng không thuộc bất kỳ vị trí nào
     */
    @GetMapping("/not-in-any-position")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> getUsersNotInAnyPosition(HttpServletRequest request) {
        List<UserResponseDto> users = userService.getActiveUsersNotInAnyPosition(request);
        if (users != null && !users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng nào không thuộc vị trí nào");
        }
    }

    /**
     * Lấy danh sách task của một người dùng theo ID
     *
     * @param userId ID của người dùng cần lấy danh sách task
     * @param page   Số trang (mặc định là 0)
     * @param size   Số lượng task trên mỗi trang (mặc định là 10)
     * @return Map chứa danh sách task, tổng số task, số trang, và tổng phần tử, hoặc 404 nếu không tìm thấy
     */
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<Map<String, Object>> getTasksByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = userService.getTasksByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật thông tin người dùng
     *
     * @param userId         ID của người dùng cần cập nhật
     * @param userRequestDto DTO chứa thông tin cập nhật người dùng
     * @return Thông tin người dùng đã được cập nhật
     */
    @PutMapping("/{userId}/update")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto updatedUser = userService.updateInfoUser(userId, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }
}