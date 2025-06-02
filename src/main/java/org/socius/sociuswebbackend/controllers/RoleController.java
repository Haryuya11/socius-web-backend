package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
public class RoleController {

    final private RoleService roleService;

    /**
     * Lấy danh sách tất cả các vai trò
     *
     * @return Danh sách các vai trò
     */
    @GetMapping("/all")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy thông tin một vai trò theo ID
     *
     * @param id ID của vai trò cần tìm
     * @return Thông tin vai trò nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
        RoleResponseDto role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo một vai trò mới
     *
     * @param requestDto Thông tin yêu cầu tạo vai trò
     * @return Thông tin vai trò đã được tạo
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto createdRole = roleService.create(requestDto);
        return ResponseEntity.ok(createdRole);
    }
}
