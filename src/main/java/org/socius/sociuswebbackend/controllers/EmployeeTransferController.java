package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.config.PermissionConstants;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.security.RequirePermission;
import org.socius.sociuswebbackend.services.EmployeeTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employee-transfer")
@RequiredArgsConstructor
public class EmployeeTransferController {
    private final EmployeeTransferService employeeTransferService;

    /**
     * Chuyển phòng ban cho nhân viên
     *
     * @param employeeId      Id của nhân viên cần chuyển
     * @param newDepartmentId Id của phòng ban mới
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @PostMapping("/{employeeId}/department/{newDepartmentId}")
    @RequirePermission(PermissionConstants.EMPLOYEE_TRANSFER_DEPARTMENT)
    public ResponseEntity<EmploymentDetailResponseDto> transferDepartment(
            @PathVariable UUID employeeId,
            @PathVariable UUID newDepartmentId) {
        EmploymentDetailResponseDto result = employeeTransferService
                .transferDepartment(employeeId, newDepartmentId);
        return ResponseEntity.ok(result);
    }

    /**
     * Chuyển team cho nhân viên
     *
     * @param employeeId ID của nhân viên cần chuyển
     * @param newTeamId  ID của team mới
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @PostMapping("/{employeeId}/team/{newTeamId}")
    @RequirePermission(PermissionConstants.EMPLOYEE_TRANSFER_TEAM)
    public ResponseEntity<EmploymentDetailResponseDto> transferTeam(
            @PathVariable UUID employeeId,
            @PathVariable UUID newTeamId) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferTeam(employeeId, newTeamId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{employeeId}/position/{newPositionId}")
    @RequirePermission(PermissionConstants.EMPLOYEE_TRANSFER_POSITION)
    public ResponseEntity<EmploymentDetailResponseDto> transferPosition(
            @PathVariable UUID employeeId,
            @PathVariable UUID newPositionId
    ) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferPosition(employeeId, newPositionId);
        return ResponseEntity.ok(result);
    }

    /**
     * Chuyển role cho nhân viên
     *
     * @param employeeId ID của nhân viên
     * @param newRoleId  ID của role mới
     * @return Thông tin chi tiết việc làm sau khi cập nhật
     */
    @PostMapping("/{employeeId}/role/{newRoleId}")
    @RequirePermission(PermissionConstants.EMPLOYEE_TRANSFER_ROLE)
    public ResponseEntity<EmploymentDetailResponseDto> transferEmployeeRole(
            @PathVariable UUID employeeId,
            @PathVariable UUID newRoleId) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferEmployeeRole(employeeId, newRoleId);
        return ResponseEntity.ok(result);
    }
}
