package org.socius.sociuswebbackend.controllers;

import org.socius.sociuswebbackend.model.dtos.employee.EmployeeCreationRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Tạo tài khoản nhân viên mới với mật khẩu mặc định
     *
     * @param requestDto Thông tin nhân viên cần tạo
     * @return Thông tin nhân viên đã tạo
     */
    @PostMapping("/employees")
    public ResponseEntity<UserResponseDto> createEmployee(@Valid @RequestBody EmployeeCreationRequestDto requestDto) {
        UserResponseDto createdEmployee = adminService.createEmployee(requestDto);
        return ResponseEntity.ok(createdEmployee);
    }
}