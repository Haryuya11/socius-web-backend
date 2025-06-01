package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.services.DepartmentService;
import org.socius.sociuswebbackend.services.PositionService;
import org.socius.sociuswebbackend.services.RoleService;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    final private DepartmentService departmentService;
    final private PositionService positionService;
    final private RoleService roleService;
    final private TeamService teamService;

    // POSITION ENDPOINTS

    /**
     * Lấy danh sách tất cả các vị trí
     *
     * @return Danh sách các vị trí
     */
    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponseDto>> getAllPositions() {
        List<PositionResponseDto> positions = positionService.findAll();
        return ResponseEntity.ok(positions);
    }

    /**
     * Lấy thông tin một vị trí theo ID
     *
     * @param id ID của vị trí cần tìm
     * @return Thông tin vị trí nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionResponseDto> getPositionById(@PathVariable UUID id) {
        PositionResponseDto position = positionService.findById(id);
        return ResponseEntity.ok(position);
    }

    /**
     * Tạo một vị trí mới
     *
     * @param requestDto Thông tin yêu cầu tạo vị trí
     * @return Thông tin vị trí đã được tạo
     */
    @PostMapping("/positions")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<PositionResponseDto> createPosition(@Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto createdPosition = positionService.create(requestDto);
        return ResponseEntity.ok(createdPosition);
    }

    /**
     * Xóa một vị trí theo ID
     *
     * @param positionId ID của vị trí cần xóa
     * @return ResponseEntity với mã trạng thái 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/positions/{positionId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<PositionResponseDto> deletePosition(@PathVariable UUID positionId) {
        positionService.delete(positionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật thông tin một vị trí
     *
     * @param positionId ID của vị trí cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vị trí
     * @return Thông tin vị trí đã được cập nhật
     */
    @PutMapping("/positions/{positionId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<PositionResponseDto> updatePosition(@PathVariable UUID positionId, @Valid @RequestBody PositionRequestDto requestDto) {
        PositionResponseDto updatedPosition = positionService.update(positionId, requestDto);
        return ResponseEntity.ok(updatedPosition);
    }

    // DEPARTMENT ENDPOINTS

    /**
     * Lấy danh sách tất cả các phòng ban
     *
     * @return Danh sách các phòng ban
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.findAll();
        return ResponseEntity.ok(departments);
    }

    /**
     * Lấy thông tin một phòng ban theo ID
     *
     * @param id ID của phòng ban cần tìm
     * @return Thông tin phòng ban nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/departments/{id}")
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
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto createdDepartment = departmentService.create(requestDto);
        return ResponseEntity.ok(createdDepartment);
    }

    /**
     * Xóa một phòng ban theo ID
     *
     * @param departmentId ID của phòng ban cần xóa
     * @return ResponseEntity với mã trạng thái 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> deleteDepartment(@PathVariable UUID departmentId) {
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
    @PutMapping("/departments/{departmentId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(@PathVariable UUID departmentId, @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto updatedDepartment = departmentService.update(departmentId, requestDto);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Thêm một nhân viên vào phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần thêm vào phòng ban
     * @return Thông tin phòng ban đã được cập nhật
     */
    @PostMapping("/departments/{departmentId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> addEmployeeToDepartment(@PathVariable UUID departmentId, @PathVariable UUID employeeId) {
        DepartmentResponseDto updatedDepartment = departmentService.addEmployee(departmentId, employeeId);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Xóa một nhân viên khỏi phòng ban
     *
     * @param departmentId ID của phòng ban
     * @param employeeId   ID của nhân viên cần xóa khỏi phòng ban
     * @return Thông tin phòng ban đã được cập nhật
     */
    @DeleteMapping("/departments/{departmentId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
    public ResponseEntity<DepartmentResponseDto> removeEmployeeFromDepartment(@PathVariable UUID departmentId, @PathVariable UUID employeeId) {
        DepartmentResponseDto updatedDepartment = departmentService.removeEmployee(departmentId, employeeId);
        return ResponseEntity.ok(updatedDepartment);
    }




    // ROLE ENDPOINTS

    /**
     * Lấy danh sách tất cả các vai trò
     *
     * @return Danh sách các vai trò
     */
    @GetMapping("/roles")
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
    @GetMapping("/roles/{id}")
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