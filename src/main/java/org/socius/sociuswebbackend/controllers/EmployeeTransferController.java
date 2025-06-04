package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.services.EmployeeTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employee-transfer")
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
@RequiredArgsConstructor
public class EmployeeTransferController {
    private final EmployeeTransferService employeeTransferService;

    @PostMapping("/department")
    public ResponseEntity<EmploymentDetailResponseDto> transferDepartment(
            @RequestParam UUID employeeId,
            @RequestParam UUID newDepartmentId) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferDepartment(employeeId, newDepartmentId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/team")
    public ResponseEntity<EmploymentDetailResponseDto> transferTeam(
            @RequestParam UUID employeeId,
            @RequestParam UUID newTeamId) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferTeam(employeeId, newTeamId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/position")
    public ResponseEntity<EmploymentDetailResponseDto> transferPosition(
            @RequestParam UUID employeeId,
            @RequestParam UUID newPositionId) {

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
    @PostMapping("/role")
    public ResponseEntity<EmploymentDetailResponseDto> transferEmployeeRole(
            @RequestParam UUID employeeId,
            @RequestParam UUID newRoleId) {

        EmploymentDetailResponseDto result = employeeTransferService
                .transferEmployeeRole(employeeId, newRoleId);
        return ResponseEntity.ok(result);
    }
}
