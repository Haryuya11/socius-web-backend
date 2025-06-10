package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    final private DepartmentService departmentService;

    /**
     * Lấy danh sách tất cả các phòng ban
     *
     * @return Danh sách các phòng ban
     */
    @GetMapping()
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.findAllActiveDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * Lấy thông tin một phòng ban cùng với danh sách thành viên của nó
     *
     * @param departmentId ID của phòng ban cần tìm
     * @param pageable     Thông tin phân trang
     * @return Thông tin phòng ban cùng với danh sách thành viên nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{departmentId}/members")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<Map<String, Object>> getDepartmentWithMember(
            @PathVariable UUID departmentId,
            Pageable pageable
    ) {
        if (departmentId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(departmentService.getDepartmentWithMembers(departmentId, pageable));
    }

    /**
     * Lấy thông tin một phòng ban theo ID
     *
     * @param id ID của phòng ban cần tìm
     * @return Thông tin phòng ban nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable UUID id) {
        DepartmentResponseDto department = departmentService.findById(id);
        return ResponseEntity.ok(department);
    }

    /**
     * Tạo một phòng ban mới
     *
     * @param requestDto Thông tin yêu cầu tạo phòng ban
     * @return Thông tin phòng ban đã được tạo
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto createdDepartment = departmentService.create(requestDto);
        return ResponseEntity.ok(createdDepartment);
    }

    /**
     * Xóa một phòng ban theo ID
     *
     * @param departmentId ID của phòng ban cần xóa
     * @return ResponseEntity với mã trạng thái 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/delete/{departmentId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<?> deleteDepartment(@PathVariable UUID departmentId) {
        departmentService.delete(departmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật thông tin một phòng ban
     *
     * @param departmentId ID của phòng ban cần cập nhật
     * @param requestDto   Thông tin yêu cầu cập nhật phòng ban
     * @return Thông tin phòng ban đã được cập nhật
     */
    @PutMapping("/update/{departmentId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(@PathVariable UUID departmentId, @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto updatedDepartment = departmentService.update(departmentId, requestDto);
        return ResponseEntity.ok(updatedDepartment);
    }
}
