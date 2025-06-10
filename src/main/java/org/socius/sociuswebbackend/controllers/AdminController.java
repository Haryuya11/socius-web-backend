package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
@RequiredArgsConstructor
public class AdminController {

    final private AdminService adminService;

    /**
     * Tạo tài khoản nhân viên mới với mật khẩu mặc định
     *
     * @param requestDto Thông tin nhân viên cần tạo
     * @return Thông tin nhân viên đã tạo
     */
    @PostMapping("/employee")
    public ResponseEntity<EmploymentDetailResponseDto> createEmployee(@Valid @RequestBody EmployeeCreationRequestDto requestDto) {
        EmploymentDetailResponseDto createdEmployee = adminService.createEmployee(requestDto);
        return ResponseEntity.ok(createdEmployee);
    }
}