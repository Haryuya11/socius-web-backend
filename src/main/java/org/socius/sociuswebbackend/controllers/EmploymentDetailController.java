package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.services.EmploymentDetailService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
}