package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeTerminationResponseDto;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryUpdateRequestDto;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmploymentDetailController {

    private final EmploymentDetailService employmentDetailService;

    /**
     * Lấy danh sách tất cả nhân viên
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách nhân viên cùng với thông tin phân trang
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllEmployees(pageable));
    }

    /**
     * Lấy danh sách tất cả nhân viên cho trang quản trị
     *
     * @param pageable Thông tin phân trang
     * @return Danh sách nhân viên cùng với thông tin phân trang
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<Map<String, Object>> getAllEmployeesForAdmin(Pageable pageable) {
        return ResponseEntity.ok(employmentDetailService.getAllEmployeesForAdmin(pageable));
    }

    /**
     * Gán role cho nhân viên
     *
     * @param employeeId ID nhân viên
     * @param roleId     ID của role cần gán
     * @return Thông tin nhân viên đã được cập nhật
     */
    @PostMapping("/assign/{employeeId}/role/{roleId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<EmploymentDetailResponseDto> assignRoleToEmployee(
            @PathVariable UUID employeeId,
            @PathVariable UUID roleId
    ) {
        EmploymentDetailResponseDto response = employmentDetailService.assignRoleToEmployee(employeeId, roleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign-multiple/role/{roleId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<List<EmploymentDetailResponseDto>> assignRoleToMultipleEmployees(
            @RequestBody List<UUID> employeeIds,
            @PathVariable UUID roleId
    ) {
        List<EmploymentDetailResponseDto> response = employmentDetailService.assignRoleToMultipleEmployees(employeeIds, roleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa role khỏi nhân viên (set về null)
     *
     * @param employeeId ID của nhân viên
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @DeleteMapping("/remove/{employeeId}/role")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<EmploymentDetailResponseDto> removeRoleFromEmployee(@PathVariable UUID employeeId) {
        employmentDetailService.removeRoleFromEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Xóa role khỏi nhiều nhân viên (set về null)
     *
     * @param employeeIds Danh sách ID của nhân viên
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @DeleteMapping("/remove-multiple")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<EmploymentDetailResponseDto>> removeRoleFromMultipleEmployees(
            @RequestBody @Valid List<UUID> employeeIds) {

        employmentDetailService.removeRoleFromMultipleEmployees(employeeIds);
        return ResponseEntity.noContent().build();
    }

    // ===================== TEAM MANAGEMENT =====================

    /**
     * Thêm một nhân viên vào team
     *
     * @param teamId     ID của team
     * @param employeeId ID của nhân viên cần thêm vào team
     * @return Response thành công
     */
    @PostMapping("/add/{employeeId}/team/{teamId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> addEmployeeToTeam(@PathVariable UUID teamId, @PathVariable UUID employeeId) {
        employmentDetailService.addEmployeeToTeam(teamId, employeeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa nhân viên khỏi team hiện tại
     *
     * @param employeeId ID của nhân viên cần xóa khỏi team
     * @return Response thành công
     */
    @DeleteMapping("/remove/{employeeId}/team")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> removeEmployeeFromTeam(@PathVariable UUID employeeId) {
        employmentDetailService.removeEmployeeFromTeam(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ===================== POSITION MANAGEMENT =====================

    /**
     * Thêm một nhân viên vào position
     *
     * @param positionId ID của position
     * @param employeeId ID của nhân viên cần thêm vào position
     * @return Response thành công
     */
    @PostMapping("/add/{employeeId}/position/{positionId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> addEmployeeToPosition(@PathVariable UUID positionId, @PathVariable UUID employeeId) {
        employmentDetailService.addEmployeeToPosition(positionId, employeeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa nhân viên khỏi position hiện tại
     *
     * @param employeeId ID của nhân viên cần xóa khỏi position
     * @return Response thành công
     */
    @DeleteMapping("/remove/{employeeId}/position")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> removeEmployeeFromPosition(@PathVariable UUID employeeId) {
        employmentDetailService.removeEmployeeFromPosition(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ===================== DEPARTMENT MANAGEMENT =====================

    /**
     * Thêm một nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần thêm vào phòng ban
     * @return Response thành công
     */
    @PostMapping("/add/{employeeId}/department/{departmentId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> addEmployeeToDepartment(@PathVariable UUID departmentId, @PathVariable UUID employeeId) {
        employmentDetailService.addEmployeeToDepartment(departmentId, employeeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa nhân viên khỏi phòng ban hiện tại
     *
     * @param employeeId ID của nhân viên cần xóa khỏi phòng ban
     * @return Response thành công
     */
    @DeleteMapping("/remove/{employeeId}/department")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> removeEmployeeFromDepartment(@PathVariable UUID employeeId) {
        employmentDetailService.removeEmployeeFromDepartment(employeeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật lương của nhân viên
     *
     * @param requestDto Thông tin yêu cầu cập nhật lương
     * @param employeeId ID của nhân viên cần cập nhật lương
     * @return Thông tin chi tiết việc làm sau khi cập nhật lương
     */
    @PostMapping("/update-salary/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> updateEmployeeSalary(
            @RequestBody @Valid SalaryUpdateRequestDto requestDto, @PathVariable UUID employeeId) {

        EmploymentDetailResponseDto updatedEmployee = employmentDetailService.updateEmployeeSalary(requestDto, employeeId);

        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Cập nhật thông tin nhân viên
     *
     * @param employeeId ID của nhân viên cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @PutMapping("/update/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<EmploymentDetailResponseDto> updateEmployee(
            @PathVariable UUID employeeId,
            @RequestBody @Valid EmployeeUpdateRequestDto requestDto) {

        EmploymentDetailResponseDto updatedEmployee = employmentDetailService.updateEmployee(employeeId, requestDto);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PostMapping("/terminate")
    public ResponseEntity<?> terminateEmployees(
            @Valid @RequestBody List<UUID> employeeIds) {
        try {
            List<UUID> successfulIds = new ArrayList<>();
            List<UUID> failedIds = new ArrayList<>();

            for (UUID employeeId : employeeIds) {
                try {
                    employmentDetailService.terminateEmployee(employeeId);
                    successfulIds.add(employeeId);
                } catch (Exception e) {
                    failedIds.add(employeeId);
                }
            }

            EmployeeTerminationResponseDto response = EmployeeTerminationResponseDto.builder()
                    .successfulEmployeeIds(successfulIds)
                    .failedEmployeeIds(failedIds)
                    .terminationDate(LocalDateTime.now())
                    .totalProcessed(employeeIds.size())
                    .successCount(successfulIds.size())
                    .failureCount(failedIds.size())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/terminate/single/{employeeId}")
    public ResponseEntity<Map<String, Object>> terminateEmployee(
            @PathVariable UUID employeeId) {
        try {
            employmentDetailService.terminateEmployee(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nhân viên đã được terminate thành công");
            response.put("employeeId", employeeId);
            response.put("terminationDate", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi terminate nhân viên");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/restore/{employeeId}")
    public ResponseEntity<Map<String, Object>> restoreEmployee(@PathVariable UUID employeeId) {
        try {
            employmentDetailService.restoreEmployee(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nhân viên đã được khôi phục thành công");
            response.put("employeeId", employeeId);
            response.put("restoreDate", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi khôi phục nhân viên");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/terminated")
    public ResponseEntity<Map<String, Object>> getTerminatedEmployees(Pageable pageable) {
        try {
            Map<String, Object> result = employmentDetailService.getTerminatedEmployees(pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}